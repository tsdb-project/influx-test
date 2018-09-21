package edu.pitt.medschool.model.dao;

import com.opencsv.CSVWriter;
import edu.pitt.medschool.framework.influxdb.ResultTable;
import edu.pitt.medschool.framework.util.TimeUtil;
import edu.pitt.medschool.framework.util.Util;
import edu.pitt.medschool.model.DataTimeSpanBean;
import edu.pitt.medschool.model.dto.Downsample;
import edu.pitt.medschool.model.dto.Export;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Managing output for analysis service
 */
public class ExportOutput {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private LocalDateTime outputStartTime;

    // Downsample values
    private Export job;
    private Downsample ds;
    private int numberOfLabels;
    private int minBinRow;
    private int minBin;
    private boolean shouldOutputWide;

    private CSVWriter outputFileLongWriter;
    private CSVWriter outputFileWideWriter;
    private BufferedWriter outputMetaWriter;
    private CSVWriter outputFileMetaWriter;

    // Runtime values
    private int numOfIntervalBins;
    private int mainHeaderLongSize;
    private int mainHeaderWideSize;
    private boolean initMetaWrote = false;
    private AtomicInteger totalInvalidPatientCount = new AtomicInteger(0);

    /**
     * Init the output wrapper class for exporting
     *
     * @param rootDir         Root dir for exporting
     * @param columnLabelName Label names for this export
     * @param ds              Downsample data
     * @param job             An export job define by user
     */
    public ExportOutput(String rootDir, List<String> columnLabelName, Downsample ds, Export job) throws IOException {
        this.outputStartTime = LocalDateTime.now();
        this.numberOfLabels = columnLabelName.size();
        this.minBinRow = ds.getMinBinRow();
        this.minBin = ds.getMinBin();
        this.ds = ds;
        this.job = job;
        // Wide output won't work if duration is not set
        this.shouldOutputWide = ds.getDuration() != null && ds.getDuration() > 0;

        initWriters(rootDir);
        initCsvHeaders(columnLabelName);
    }

    private void initWriters(String root) throws IOException {
        this.outputFileLongWriter = new CSVWriter(new BufferedWriter(new FileWriter(root + "/output_all_long.csv")));
        if (this.shouldOutputWide)
            this.outputFileWideWriter = new CSVWriter(new BufferedWriter(new FileWriter(root + "/output_all_wide.csv")));
        this.outputFileMetaWriter = new CSVWriter(new BufferedWriter(new FileWriter(root + "/output_all_meta.csv")));
        this.outputMetaWriter = new BufferedWriter(new FileWriter(root + "/output_meta.txt"));
    }

    /**
     * Initialize the CSV headers.
     * If using wide format, set numOfIntervalBins as well.
     */
    private void initCsvHeaders(List<String> labelNames) {
        int numLabel = this.numberOfLabels;

        // Long form header
        String[] mainHeaderLong = new String[numLabel + 3];
        mainHeaderLong[0] = "PID";
        mainHeaderLong[2] = "Timebins";
        mainHeaderLong[1] = "Timestamp";
        for (int i = 0; i < numLabel; i++) {
            mainHeaderLong[i + 3] = labelNames.get(i);
        }
        this.mainHeaderLongSize = mainHeaderLong.length;
        this.outputFileLongWriter.writeNext(mainHeaderLong);

        // Wide form header
        if (this.shouldOutputWide) {
            this.numOfIntervalBins = (int) (Math.ceil(this.ds.getDuration() * 1.0 / this.ds.getPeriod()));
            String[] mainHeaderWide = new String[numLabel * this.numOfIntervalBins + 2];
            mainHeaderWide[0] = "PID";
            mainHeaderWide[1] = "Start Timestamp";
            for (int j = 0; j < numLabel; j++) {
                int prefixSize = j * this.numOfIntervalBins;
                String labelJ = labelNames.get(j);
                for (int k = 1; k <= this.numOfIntervalBins; k++) {
                    String hdr = labelJ + "-" + k;
                    mainHeaderWide[prefixSize + k + 1] = hdr.replace(' ', '_');
                }
            }
            this.mainHeaderWideSize = mainHeaderWide.length;
            this.outputFileWideWriter.writeNext(mainHeaderWide);
        }

        String[] timemetaHdr = {"PID", "Export start time", "Export end time", "Number of data segments",
                "Duration in seconds", "Number of data used", "Occurance insufficient data", "Should considered as good"};
        this.outputFileMetaWriter.writeNext(timemetaHdr);
    }

    public void writeInitialMetaText(int numPatientsInTsdb, int numQueueSize, int threads) {
        if (this.initMetaWrote)
            return;
        this.writeMetaFile(String.format("EXPORT <%s> - #%d, built on: %s%nJob started on: %s%n%n",
                this.ds.getAlias(), this.ds.getId(), this.job.getUpdateTime(), TimeUtil.formatLocalDateTime(this.outputStartTime, "")));
        this.writeMetaFile(String.format("# of cores utilized: %d%n", threads));
        this.writeMetaFile(String.format("# of patients in database: %d%n", numPatientsInTsdb));
        this.writeMetaFile(String.format("# of patients in queue initially: %d%n", numQueueSize));
        this.writeMetaFile(String.format("Dataset AR status is: %s%n", this.job.getAr() ? "AR" : "NoAR"));
        if (!this.shouldOutputWide)
            this.writeMetaFile(String.format("Note: Can't output wide form if duration is NOT set.%n%n%n"));
        this.initMetaWrote = true;
    }

    public void writeMetaFile(String data) {
        try {
            this.outputMetaWriter.write(data);
        } catch (IOException e) {
            logger.error("Message '{}' failed to write as meta text: {}", data, e.getLocalizedMessage());
        }
    }

    /**
     * Call this when finished query on one patient
     */
    public void writeForOnePatient(String pid, ResultTable r, ExportQueryBuilder eq, List<DataTimeSpanBean> dtsb) {
        List<Integer> validId = eq.getGoodDataTimeId();
        this.writeMainData(pid, r, eq, validId.size(), AnalysisUtil.dataValidTotalSpan(validId, dtsb) / 1000);
    }

    private void writeMainData(String patientId, ResultTable r, ExportQueryBuilder eq, int numSegments, long totalDataSeconds) {
        int dataRows = r.getRowCount();
        long thisPatientTotalCount = 0, thisPatientTotalInsufficientCount = 0;

        // Output long form
        String[] mainDataLong = new String[this.mainHeaderLongSize];
        mainDataLong[0] = patientId;
        for (int i = 0; i < dataRows; i++) {
            List<Object> row = r.getDatalistByRow(i);
            int resultSize = row.size(), count = (int) (double) row.get(resultSize - 1);
            mainDataLong[2] = String.valueOf(i + 1);
            mainDataLong[1] = String.valueOf(row.get(0));
            boolean thisRowInsuffData = count < this.minBinRow; // Based on the fact that single data per second
            if (thisRowInsuffData) {
                thisPatientTotalInsufficientCount += 1;
            }
            for (int j = 0; j < this.numberOfLabels; j++) {
                Object data = row.get(j + 1);
                if (data == null) {
                    mainDataLong[j + 3] = "N/A";
                } else if (thisRowInsuffData) {
                    mainDataLong[j + 3] = "Insuff. Data";
                } else {
                    mainDataLong[j + 3] = data.toString();
                }
            }
            thisPatientTotalCount += count;
            this.outputFileWideWriter.writeNext(mainDataLong);
        }

        // Output wide form
        if (this.shouldOutputWide) {
            String[] mainDataWide = new String[this.mainHeaderWideSize];
            mainDataWide[0] = patientId;
            mainDataWide[1] = String.valueOf(eq.getQueryStartTime());
            int intervals = this.numOfIntervalBins, nLabels = this.numberOfLabels;
            // Reference: c64e604145 from line 207-226
            for (int i = 0; i < intervals; i++) {
                if (dataRows > i) {
                    List<Object> row = r.getDatalistByRow(i);
                    int resultSize = row.size(), count = (int) (double) row.get(resultSize - 1);
                    boolean thisRowInsuffData = count < this.minBinRow; // Based on the fact that single data per second
                    for (int j = 1; j <= nLabels; j++) {
                        Object data = row.get(j);
                        if (thisRowInsuffData) {
                            mainDataWide[2 + (j - 1) * intervals + i] = "Insuff. Data";
                        } else if (data == null) {
                            mainDataWide[2 + (j - 1) * intervals + i] = "N/A";
                        } else {
                            mainDataWide[2 + (j - 1) * intervals + i] = data.toString();
                        }
                    }
                } else {
                    for (int j = 1; j <= nLabels; j++) {
                        mainDataWide[2 + (j - 1) * intervals + i] = "";
                    }
                }
            }
            // Only one line for a patient in this mode
            this.outputFileWideWriter.writeNext(mainDataWide);
        }

        // Determine if data is enough or not
        boolean tooFewData = false;
        if (dataRows - thisPatientTotalInsufficientCount < this.minBin) {
            this.totalInvalidPatientCount.incrementAndGet();
            tooFewData = true;
            this.writeMetaFile(String.format("  PID '%s' overall data insufficient.%n", patientId));
        }
        this.writeTimeMeta(patientId, eq, thisPatientTotalCount, thisPatientTotalInsufficientCount, numSegments, totalDataSeconds, !tooFewData);
    }

    private void writeTimeMeta(String patientId, ExportQueryBuilder eq, long dataCount, long insuffCount,
                               int numSegments, long totalDataSeconds, boolean enoughData) {
        Instant start = eq.getQueryStartTime(), end = eq.getQueryEndTime();
        String[] data = {patientId, start.toString(), end.toString(), String.valueOf(numSegments), String.valueOf(totalDataSeconds),
                String.valueOf(dataCount), String.valueOf(insuffCount), enoughData ? "Yes" : "No"};
        this.outputFileMetaWriter.writeNext(data);
    }

    /**
     * Close export system and write out final meta
     *
     * @param validNum # of valid patients
     */
    public void close(int validNum) {
        this.closeCsv();
        this.closeMetaText(validNum);
    }

    /**
     * Call when this downsample finished
     */
    private void closeMetaText(int validNum) {
        try {
            LocalDateTime now = LocalDateTime.now();
            this.writeMetaFile(String.format("%n# of patients with insufficient data: %d%n", this.totalInvalidPatientCount.get()));
            this.writeMetaFile(String.format("# of valid patients: %d%n", validNum));
            this.writeMetaFile(String.format("Job ended on: %s%nTotal running time for this job: %s%n",
                    TimeUtil.formatLocalDateTime(now, ""), Duration.between(this.outputStartTime, now).toString().replace("PT", "")));
            this.outputMetaWriter.flush();
            this.outputMetaWriter.close();
        } catch (IOException e) {
            logger.error("Meta text fail to close: {}", Util.stackTraceErrorToString(e));
        }
    }

    /**
     * Call when this downsample finished
     */
    private void closeCsv() {
        try {
            this.outputFileLongWriter.close();
            if (this.shouldOutputWide)
                this.outputFileWideWriter.close();
            this.outputFileMetaWriter.close();
        } catch (IOException e) {
            logger.error("Writers fail to close: {}", Util.stackTraceErrorToString(e));
        }
    }

}
