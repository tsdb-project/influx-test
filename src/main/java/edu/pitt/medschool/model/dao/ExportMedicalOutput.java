package edu.pitt.medschool.model.dao;

import com.opencsv.CSVWriter;
import edu.pitt.medschool.framework.influxdb.ResultTable;
import edu.pitt.medschool.framework.util.TimeUtil;
import edu.pitt.medschool.framework.util.Util;
import edu.pitt.medschool.model.DataTimeSpanBean;
import edu.pitt.medschool.model.dto.Export;
import edu.pitt.medschool.model.dto.MedicalDownsample;
import edu.pitt.medschool.model.dto.Medication;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ExportMedicalOutput {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private LocalDateTime outputStartTime;

    // Downsample values
    private final Export job;
    private final MedicalDownsample mds;
    private final int numberOfLabels;
    private final int minSecondsForBinPerRow;
    private final int minTotalBinForOne;
    private final boolean shouldOutputWide;

    private final String spiltLongRootPath;
    private final String spiltWideRootPath;
    private final String flatRootPath;
    private final CSVWriter outputFileLongWriter;
    private final CSVWriter outputFileWideWriter;
    private final BufferedWriter outputMetaWriter;
    private final CSVWriter outputFileMetaWriter;
    private final AtomicInteger totalInvalidPatientCount = new AtomicInteger(0);

    // Runtime values
    private int numOfIntervalBins;
    private int mainHeaderLongSize;
    private int mainHeaderWideSize;
    private boolean initMetaWrote = false;
    private boolean isClosed = false;

    /**
     * Init the output wrapper class for exporting
     * To be noticed, long form is always available, while wide form is only available when duration is set
     *
     * @param rootDir         Root dir for exporting
     * @param columnLabelName Label names for this export
     * @param mds              MedicalDownsample data
     * @param job             An export job define by user
     */
    public ExportMedicalOutput(String rootDir, List<String> columnLabelName, MedicalDownsample mds, Export job) throws IOException {
        this.outputStartTime = LocalDateTime.now();
        this.numberOfLabels = columnLabelName.size();
        this.minSecondsForBinPerRow = mds.getMinBinRow();
        this.minTotalBinForOne = mds.getMinBin();
        this.mds = mds;
        this.job = job;
        int jid = this.job.getId();
        // no wide output for medical query
        this.shouldOutputWide = false;

        this.flatRootPath = String.format("%s/flat/", rootDir); //TODO: This needs discussion
        this.spiltLongRootPath = String.format("%s_split/long/", rootDir);
        FileUtils.forceMkdir(new File(this.spiltLongRootPath));
        if (this.shouldOutputWide) {
            this.spiltWideRootPath = String.format("%s_split/wide/", rootDir);
            FileUtils.forceMkdir(new File(this.spiltWideRootPath));
        } else {
            this.spiltWideRootPath = "";
        }

        // Init writer specific variable
        this.outputFileLongWriter = new CSVWriter(new BufferedWriter(new FileWriter(String.format("%s/output_all_long_%d.csv", rootDir, jid))));
        if (this.shouldOutputWide) {
            this.outputFileWideWriter = new CSVWriter(new BufferedWriter(new FileWriter(String.format("%s/output_all_wide_%d.csv", rootDir, jid))));
        } else {
            this.outputFileWideWriter = null;
        }
        this.outputFileMetaWriter = new CSVWriter(new BufferedWriter(new FileWriter(String.format("%s/output_all_meta_%d.csv", rootDir, jid))));
        this.outputMetaWriter = new BufferedWriter(new FileWriter(String.format("%s/job_meta_%d.txt", rootDir, jid)));

        initCsvHeaders(columnLabelName);
    }

    /**
     * Initialize the CSV headers.
     * If using wide format, set numOfIntervalBins as well.
     */
    private void initCsvHeaders(List<String> labelNames) {
        int numLabel = this.numberOfLabels;

        // Long form header
        String[] mainHeaderLong = new String[numLabel + 7];
        mainHeaderLong[0] = "PID";
        mainHeaderLong[2] = "Timebins";
        mainHeaderLong[1] = "Timestamp";
        mainHeaderLong[numLabel+3] = "Drug_Name";
        mainHeaderLong[numLabel+4] = "Chart_Date";
        mainHeaderLong[numLabel+5] = "Dose";
        mainHeaderLong[numLabel+6] = "Dose_Unit";
        for (int i = 0; i < numLabel; i++) {
            mainHeaderLong[i + 3] = labelNames.get(i);
        }
        this.mainHeaderLongSize = mainHeaderLong.length;
        this.outputFileLongWriter.writeNext(mainHeaderLong);

        // Wide form header
        if (this.shouldOutputWide) {
            this.numOfIntervalBins = (int) (Math.ceil((this.mds.getAfterMedicine()+this.mds.getBeforeMedicine()) * 1.0 / this.mds.getPeriod()));
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
                this.mds.getAlias(), this.mds.getId(), this.job.getUpdateTime(), TimeUtil.formatLocalDateTime(this.outputStartTime, "")));
        this.writeMetaFile(String.format("# of cores utilized: %d%n", threads));
        this.writeMetaFile(String.format("# of patients in database: %d%n", numPatientsInTsdb));
        this.writeMetaFile(String.format("# of patients in queue initially: %d%n", numQueueSize));
        if (!this.shouldOutputWide)
            this.writeMetaFile(String.format("Note: Can't output wide form if duration is NOT set.%n"));
        this.writeMetaFile(String.format("Which database: %s%n", this.job.getDbType()));
        this.writeMetaFile(String.format("Job AR status is: %s%n%n%n", this.job.getAr() ? "AR" : "NoAR"));
        this.initMetaWrote = true;
    }

    public synchronized void writeMetaFile(String data) {
        try {
            this.outputMetaWriter.write(data);
        } catch (IOException e) {
            logger.error("Message '{}' failed to write as meta text: {}", data, e.getLocalizedMessage());
        }
    }

    /**
     * Call this when finished query on one patient
     */
    public void writeForOnePatient(String pid, ResultTable r, ExportMedicalQueryBuilder eq, List<DataTimeSpanBean> dtsb, Medication record) {
        List<Integer> validId = eq.getGoodDataTimeId();
        this.writeMainData(pid, r, eq, validId.size(), AnalysisUtil.dataValidTotalSpan(validId, dtsb) / 1000, record);
    }

    /**
     * Write the `main` csv data file (Hoz and Vert in one function)
     * TO ANYONE WHO TOOK MY PLACE IN THE FUTURE THAT NEED TO MAINTAIN THIS PART:
     * I know this function seems to be complicated, but it's okay if you understand the structure
     *
     * @param patientId        PID
     * @param r                Result table queried
     * @param eq               ExportQueryBuilder
     * @param numSegments      How many files used for this patient
     * @param totalDataSeconds Total data used for this patient in seconds
     * @param record The medical record of this aggregate result
     */
    private void writeMainData(String patientId, ResultTable r, ExportMedicalQueryBuilder eq, int numSegments, long totalDataSeconds, Medication record) {
        int dataRows = r.getRowCount();
        long thisPatientTotalCount = 0, thisPatientTotalInsufficientCount = 0;

        // A test on algorithm
        if (dataRows != this.numOfIntervalBins) {
            logger.error("Test: dataRows!=numOfIntervalBins. Actual: <{}> vs <{}>", dataRows, numOfIntervalBins);
        }

        // Output long form
        String[] mainDataLong = new String[this.mainHeaderLongSize+4];
        mainDataLong[0] = patientId;
        for (int i = 0; i < dataRows; i++) {
            List<Object> row = r.getDatalistByRow(i);
            int resultSize = row.size(), count = (int) (double) row.get(resultSize - 1);
            mainDataLong[2] = String.valueOf(i + 1);
            mainDataLong[1] = String.valueOf(row.get(0));
            boolean thisRowInsuffData = count < this.minSecondsForBinPerRow; // Based on the fact that single data per second
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
            mainDataLong[this.numberOfLabels+3] = record.getDrugName();
            mainDataLong[this.numberOfLabels+4] = record.getChartDate().toString();
            mainDataLong[this.numberOfLabels+5] = record.getDose().toString();
            mainDataLong[this.numberOfLabels+6] = record.getDoseUnit();
            thisPatientTotalCount += count;
            writeOneSpiltFile(patientId, mainDataLong, false);
            synchronized (this) {
                this.outputFileLongWriter.writeNext(mainDataLong);
            }
        }

        // Determine if data is enough or not
        boolean enoughData = true;
        //TODO: Check the following logic really works as intended?
        if (dataRows - thisPatientTotalInsufficientCount < this.minTotalBinForOne) {
            this.totalInvalidPatientCount.incrementAndGet();
            enoughData = false;
            this.writeMetaFile(String.format("  PID '%s' overall data insufficient.%n", patientId));
        }

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
                    boolean thisRowInsuffData = count < this.minSecondsForBinPerRow; // Based on the fact that single data per second
                    for (int j = 1; j <= nLabels; j++) {
                        Object data = row.get(j);
                        if (data == null) {
                            mainDataWide[2 + (j - 1) * intervals + i] = "N/A";
                        } else if (thisRowInsuffData) {
                            mainDataWide[2 + (j - 1) * intervals + i] = "Insuff. Data";
                        } else {
                            mainDataWide[2 + (j - 1) * intervals + i] = data.toString();
                        }
                    }
                } else {
                    // This should probably not happen
                    for (int j = 1; j <= nLabels; j++) {
                        mainDataWide[2 + (j - 1) * intervals + i] = "";
                    }
                }
            }
            // Always output wide in a separate file for debugging purpose
            writeOneSpiltFile(patientId, mainDataWide, true);
            // Output wide form to end-user only if data is enough
            if (enoughData) {
                synchronized (this) {
                    // Only one line for a patient in this mode
                    this.outputFileWideWriter.writeNext(mainDataWide);
                }
            }
        }

        this.writeTimeMeta(patientId, eq, thisPatientTotalCount, thisPatientTotalInsufficientCount, numSegments, totalDataSeconds, enoughData);
    }

    /**
     * Write a single spilt file for a patient
     *
     * @param pid  PID
     * @param data Data to write
     */
    private void writeOneSpiltFile(String pid, String[] data, boolean isWide) {
        String path = (isWide ? this.spiltWideRootPath : this.spiltLongRootPath) + pid + ".txt";
        if (isWide) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(path, false))) {
                for (int i = 0; i < data.length - 1; i++) {
                    bw.write(data[i]);
                    bw.write("\t");
                }
                bw.write(data[data.length - 1]);
                bw.newLine();
            } catch (IOException e) {
                logger.error("Write spilt wide file failed", e);
            }
        } else {
            String dataToWrite = String.join("\t", data);
            // Long format should be append, therefore requires a thread lock
            synchronized (this) {
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(path, true))) {
                    bw.write(dataToWrite);
                    bw.newLine();
                } catch (IOException e) {
                    logger.error("Write spilt long file failed", e);
                }
            }
        }
    }

    /**
     * Write meta file according to data file
     *
     * @param patientId        PID
     * @param eq               Meta data that needed from ExportQueryBuilder
     * @param dataCount        Total data for this patient
     * @param insuffCount      Total "bad" data for this patient
     * @param numSegments      How many files used for this patient
     * @param totalDataSeconds Total data used for this patient in seconds
     * @param enoughData       Does this patient have enough data
     */
    private void writeTimeMeta(String patientId, ExportMedicalQueryBuilder eq, long dataCount, long insuffCount, int numSegments, long totalDataSeconds, boolean enoughData) {
        Instant start = eq.getQueryStartTime(), end = eq.getQueryEndTime();
        String[] data = {patientId, start.toString(), end.toString(), String.valueOf(numSegments), String.valueOf(totalDataSeconds),
                String.valueOf(dataCount), String.valueOf(insuffCount), enoughData ? "Yes" : "No"};
        synchronized (this) {
            this.outputFileMetaWriter.writeNext(data);
        }

    }

    /**
     * Close export system and write out final meta
     *
     * @param validNum # of valid patients
     */
    public void close(int validNum) {
        if (this.isClosed)
            return;
        this.closeCsv();
        this.closeMetaText(validNum);
        this.isClosed = true;
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
