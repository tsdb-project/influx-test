package edu.pitt.medschool.model.dao;

import com.opencsv.CSVWriter;
import edu.pitt.medschool.framework.influxdb.ResultTable;
import edu.pitt.medschool.framework.util.Util;
import edu.pitt.medschool.model.dto.Downsample;
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
    private Downsample ds;
    private int dsInterval;
    private int numberOfLabels;
    private int minBinRow;
    private int minBin;

    private CSVWriter outputFileWriter;
    private BufferedWriter outputMetaWriter;
    private CSVWriter outputTimeMetaWriter;

    // Runtime values
    private int maxNumOfBins = Integer.MIN_VALUE;
    private int mainHeaderSize;
    private int timeMetaHeaderSize;
    private boolean initMetaWrote = false;
    private int numTotalInsuffBin = 0;

    /**
     * Init the output wrapper class for exporting
     *
     * @param rootDir         Root dir for exporting
     * @param columnLabelName Label names for this export
     * @param ds              Downsample data
     */
    public ExportOutput(String rootDir, List<String> columnLabelName, Downsample ds) throws IOException {
        this.outputStartTime = LocalDateTime.now();
        this.numberOfLabels = columnLabelName.size();
        this.minBinRow = ds.getMinBinRow();
        this.minBin = ds.getMinBin();
        this.ds = ds;
        this.dsInterval = ds.getPeriod();

        initWriters(rootDir);
        initCsvHeaders(columnLabelName);
    }

    private void initWriters(String root) throws IOException {
        this.outputFileWriter = new CSVWriter(new BufferedWriter(new FileWriter(root + "/output_vert.csv")));
        this.outputTimeMetaWriter = new CSVWriter(new BufferedWriter(new FileWriter(root + "/output_time_dict.csv")));
        this.outputMetaWriter = new BufferedWriter(new FileWriter(root + "/output_meta.txt"));
    }

    private void initCsvHeaders(List<String> labelNames) {
        int numLabel = this.numberOfLabels;
        String[] mainHeader = new String[numLabel + 2];
        mainHeader[0] = "PID";
        mainHeader[1] = "Timestamp";
        for (int i = 0; i < numLabel; i++) {
            mainHeader[i + 2] = labelNames.get(i);
        }
        this.outputFileWriter.writeNext(mainHeader);
        this.mainHeaderSize = mainHeader.length;
        // TODO: Discuss what do we need.
        String[] timemetaHdr = { "PID", "Export start time", "Export end time", "Duration in seconds", "Number of data used",
                "Occurance insufficient data", "Should considered as good" };
        this.timeMetaHeaderSize = timemetaHdr.length;
        this.outputTimeMetaWriter.writeNext(timemetaHdr);
    }

    public void writeInitialMetaText(int numPatientsInTsdb, int numQueueSize, boolean isAr, int threads) {
        if (this.initMetaWrote)
            return;

        this.writeMetaFile(
                String.format("EXPORT '%s' - #%d:%nStarted on: '%s'%n%n", this.ds.getAlias(), this.ds.getId(), this.outputStartTime.toString()));
        this.writeMetaFile(String.format("# of threads: %d%n", threads));
        this.writeMetaFile(String.format("# of patients in database: %d%n", numPatientsInTsdb));
        this.writeMetaFile(String.format("# of patients initially: %d%n", numQueueSize));
        this.writeMetaFile(String.format("AR status is: %s%n%n%n", isAr ? "AR" : "NoAR"));
        this.initMetaWrote = true;
    }

    public void writeMetaFile(String data) {
        try {
            this.outputMetaWriter.write(data);
        } catch (IOException e) {
            logger.error("Message '{}' failed to write as meta text: {}", data, e.getLocalizedMessage());
        }
    }

    public void writeMain(String pid, ResultTable r, ExportQueryBuilder eq) {
        this.writeMainData(pid, r, eq);
    }

    private void writeMainData(String patientId, ResultTable r, ExportQueryBuilder eq) {
        int dataRows = r.getRowCount();
        if (dataRows > this.maxNumOfBins) {
            this.maxNumOfBins = dataRows;
        }
        int thisPatientTotalCount = 0, thisPatientTotalInsufficientCount = 0;
        for (int i = 0; i < dataRows; i++) {
            List<Object> row = r.getDatalistByRow(i);
            int rowSize = row.size();

            String[] mainData = new String[this.mainHeaderSize];
            mainData[0] = patientId;
            mainData[1] = row.get(0).toString();
            int count = (int) (double) row.get(rowSize - 1);

            // One data per second, however should NOT rely on that
            boolean thisRowInsuffData = count < this.minBinRow;
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
        boolean tooFewData = false;
        if (thisPatientTotalInsufficientCount > this.minBin) {
            this.numTotalInsuffBin += 1;
            tooFewData = true;
            this.writeMetaFile(String.format("  PID '%s' overall data insufficient.%n", patientId));
        }
        this.writeTimeMeta(patientId, eq, thisPatientTotalCount, thisPatientTotalInsufficientCount, tooFewData);
    }

    private void writeTimeMeta(String patientId, ExportQueryBuilder eq, int dataCount, int insuffCount, boolean tooFewData) {
        Instant start = eq.getQueryStartTime(), end = eq.getQueryEndTime();
        String[] data = { patientId, start.toString(), end.toString(), String.valueOf((end.toEpochMilli() - start.toEpochMilli()) / 1000),
                String.valueOf(dataCount), String.valueOf(insuffCount), tooFewData ? "No" : "Yes" };
        this.outputTimeMetaWriter.writeNext(data);
    }

    public void close(int validNum) {
        this.closeMetaText(validNum);
        this.closeCsv();
    }

    /**
     * Call when this downsample finished
     */
    private void closeMetaText(int validNum) {
        try {
            this.outputMetaWriter.write(String.format("%n%n# of insufficient data patients:%d%n", this.numTotalInsuffBin));
            this.outputMetaWriter.write(String.format("# of valid patients: %d%nENDED ON '%s'", validNum, LocalDateTime.now().toString()));
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

    /**
     * Transpose vert table to get a horz one
     */
    private void transposeVertCsv() {
        // TODO: Finish this.
    }

}
