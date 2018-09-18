package edu.pitt.medschool.model.dao;

import com.opencsv.CSVWriter;
import edu.pitt.medschool.framework.influxdb.ResultTable;
import edu.pitt.medschool.framework.util.Util;
import edu.pitt.medschool.model.DataTimeSpanBean;
import edu.pitt.medschool.model.dto.Downsample;
import edu.pitt.medschool.model.dto.Export;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Managing output for analysis service
 */
public class ExportOutput {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private LocalDateTime outputStartTime;

    // Downsample values
    private Export job;
    private Downsample ds;
    private boolean isOutputVertical;
    private int numberOfLabels;
    private int minBinRow;
    private int minBin;

    private CSVWriter outputFileWriter;
    private BufferedWriter outputMetaWriter;
    private CSVWriter outputTimeMetaWriter;

    // Runtime values
    private int numOfIntervalBins;
    private int mainHeaderSize;
    private boolean initMetaWrote = false;
    private int totalInvalidPatientCount = 0;

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
        // True for using long (vertical) form
        this.isOutputVertical = job.getLayout();
        this.job = job;

        initWriters(rootDir);
        initCsvHeaders(columnLabelName);
    }

    private void initWriters(String root) throws IOException {
        this.outputFileWriter = new CSVWriter(new BufferedWriter(new FileWriter(root + "/output_vert.csv")));
        this.outputTimeMetaWriter = new CSVWriter(new BufferedWriter(new FileWriter(root + "/output_time_dict.csv")));
        this.outputMetaWriter = new BufferedWriter(new FileWriter(root + "/output_meta.txt"));
    }

    /**
     * Initialize the CSV headers.
     * If using wide format, set numOfIntervalBins as well.
     */
    private void initCsvHeaders(List<String> labelNames) {
        int numLabel = this.numberOfLabels;
        String[] mainHeader;
        if (this.job.getLayout()) {
            mainHeader = new String[numLabel + 2];
            mainHeader[0] = "PID";
            //TODO: Remove timestamp?
            mainHeader[1] = "Timestamp";
            for (int i = 0; i < numLabel; i++) {
                mainHeader[i + 2] = labelNames.get(i);
            }
        } else {
            this.numOfIntervalBins = (int) (Math.ceil(this.ds.getDuration() * 1.0 / this.ds.getPeriod()));
            mainHeader = new String[numLabel * this.numOfIntervalBins + 1];
            mainHeader[0] = "PID";
            for (int j = 0; j < numLabel; j++) {
                int prefixSize = j * this.numOfIntervalBins;
                // Set the first one
                mainHeader[prefixSize + 1] = labelNames.get(j) + "_1";
                for (int k = 2; k <= this.numOfIntervalBins; k++) {
                    mainHeader[prefixSize + k] = String.valueOf(k);
                }
            }
        }
        this.outputFileWriter.writeNext(mainHeader);
        this.mainHeaderSize = mainHeader.length;
        String[] timemetaHdr = {"PID", "Export start time", "Export end time", "Number of data segments",
                "Duration in seconds", "Number of data used", "Occurance insufficient data", "Should considered as good"};
        this.outputTimeMetaWriter.writeNext(timemetaHdr);
    }

    public void writeInitialMetaText(int numPatientsInTsdb, int numQueueSize, int threads) {
        if (this.initMetaWrote)
            return;
        this.writeMetaFile(String.format("EXPORT <%s> - #%d, built on: %s%nJob started on: %s%n%n",
                this.ds.getAlias(), this.ds.getId(), this.job.getUpdateTime(), this.outputStartTime.toString()));
        this.writeMetaFile(String.format("# of cores utilized: %d%n", threads));
        this.writeMetaFile(String.format("# of patients in database: %d%n", numPatientsInTsdb));
        this.writeMetaFile(String.format("# of patients in queue initially: %d%n", numQueueSize));
        this.writeMetaFile(String.format("Dataset AR status is: %s%n%n%n", this.job.getAr() ? "AR" : "NoAR"));
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
        this.writeMainData(pid, r, eq, validId.size(), AnalysisUtil.dataValidTotalSpan(validId, dtsb));
    }

    private void writeMainData(String patientId, ResultTable r, ExportQueryBuilder eq, int numSegments, long totalDataSeconds) {
        int dataRows = r.getRowCount();
        long thisPatientTotalCount = 0, thisPatientTotalInsufficientCount = 0;
        String[] mainData = new String[this.mainHeaderSize];
        mainData[0] = patientId;
        if (this.isOutputVertical) {
            for (int i = 0; i < dataRows; i++) {
                List<Object> row = r.getDatalistByRow(i);
                int resultSize = row.size(), count = (int) (double) row.get(resultSize - 1);
                mainData[1] = row.get(0).toString(); // timestamp column
                boolean thisRowInsuffData = count < this.minBinRow; // Based on the fact that single data per second
                if (thisRowInsuffData) {
                    thisPatientTotalInsufficientCount += 1;
                }
                for (int j = 0; j < this.numberOfLabels; j++) {
                    Object data = row.get(j + 1);
                    if (data == null) {
                        mainData[j + 2] = "N/A";
                    } else if (thisRowInsuffData) {
                        mainData[j + 2] = "Insuff. Data";
                    } else {
                        mainData[j + 2] = data.toString();
                    }
                }
                thisPatientTotalCount += count;
                this.outputFileWriter.writeNext(mainData);
            }
        } else {
            int intervals = this.numOfIntervalBins, nLabels = this.numberOfLabels;
            // Reference: c64e604145 from line 207-226
            for (int i = 0; i < intervals; i++) {
                if (dataRows > i) {
                    List<Object> row = r.getDatalistByRow(i);
                    int resultSize = row.size(), count = (int) (double) row.get(resultSize - 1);
                    boolean thisRowInsuffData = count < this.minBinRow; // Based on the fact that single data per second
                    if (thisRowInsuffData) {
                        thisPatientTotalInsufficientCount += 1;
                    }
                    for (int j = 1; j <= nLabels; j++) {
                        Object data = row.get(j);
                        if (thisRowInsuffData) {
                            mainData[1 + (j - 1) * intervals + i] = "Insuff. Data";
                        } else if (data == null) {
                            mainData[1 + (j - 1) * intervals + i] = "N/A";
                        } else {
                            mainData[1 + (j - 1) * intervals + i] = data.toString();
                        }
                    }
                    thisPatientTotalCount += count;
                } else {
                    for (int j = 1; j <= nLabels; j++) {
                        mainData[1 + (j - 1) * intervals + i] = "";
                    }
                }
            }
            // Only one line for a patient in this mode
            this.outputFileWriter.writeNext(mainData);
        }
        boolean tooFewData = false;
        if (dataRows - thisPatientTotalInsufficientCount < this.minBin) {
            this.totalInvalidPatientCount += 1;
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
        this.outputTimeMetaWriter.writeNext(data);
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
            this.outputMetaWriter.write(String.format("%n%n# of patients with insufficient data: %d%n", this.totalInvalidPatientCount));
            this.outputMetaWriter.write(String.format("# of valid patients: %d%nJob ended on %s", validNum, LocalDateTime.now().toString()));
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
            this.outputFileWriter.close();
            this.outputTimeMetaWriter.close();
        } catch (IOException e) {
            logger.error("Writers fail to close: {}", Util.stackTraceErrorToString(e));
        }
    }

}
