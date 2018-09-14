package edu.pitt.medschool.model.dao;

import com.opencsv.CSVWriter;
import edu.pitt.medschool.framework.influxdb.ResultTable;
import edu.pitt.medschool.framework.util.Util;
import edu.pitt.medschool.model.dto.DownsampleGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Managing output for analysis service
 */
public class ExportOutput {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private LocalDateTime outputStartTime;

    private boolean isOutputVertical;
    private int numberOfIntervalBins;
    private int numberOfLabels;
    private int minEveryBinSeconds;
    private int minTotalBinSeconds;

    private int mainHeaderSize;
    private int timeMetaHeaderSize;

    private CSVWriter outputFileWriter;
    private BufferedWriter outputMetaWriter;
    private CSVWriter outputTimeMetaWriter;

    /**
     * Init the output wrapper class for exporting
     *
     * @param rootDir         Root dir for exporting
     * @param columnLabelName Label names for this export
     * @param dg              Downsample Groups
     * @param isOutVertical   V/H output?
     * @param intervals       Number of bins (intervals)
     * @param everyBin        Every bin must have at least some size
     * @param totalBin        Total bin must have at least some size
     */
    public ExportOutput(String rootDir, List<String> columnLabelName, List<DownsampleGroup> dg,
                        boolean isOutVertical, int intervals, int everyBin, int totalBin) throws IOException {
        this.outputStartTime = LocalDateTime.now();
        this.isOutputVertical = isOutVertical;
        this.numberOfIntervalBins = intervals;
        this.numberOfLabels = columnLabelName.size();
        this.minTotalBinSeconds = totalBin;
        this.minEveryBinSeconds = everyBin;

        initWriters(rootDir);
        initCsvHeaders(columnLabelName, dg);
    }

    private void initWriters(String root) throws IOException {
        this.outputFileWriter = new CSVWriter(new BufferedWriter(new FileWriter(root + "/output.csv")));
        this.outputTimeMetaWriter = new CSVWriter(new BufferedWriter(new FileWriter(root + "/output_time_dict.csv")));
        this.outputMetaWriter = new BufferedWriter(new FileWriter(root + "/output_meta.txt"));
    }

    private void initCsvHeaders(List<String> labelNames, List<DownsampleGroup> dg) {
        int numLabel = this.numberOfLabels;
        String[] timemetaHdr = {"PID", "Data begin time", "Data end time",
                "Export start time", "Export end time", "Number of data used", "Comments"};
        String[] mainHeader;
        if (this.isOutputVertical) {
            mainHeader = new String[numLabel + 2];
            mainHeader[0] = "Timestamp";
            mainHeader[1] = "PID";
            for (int i = 0; i < numLabel; i++) {
                mainHeader[i + 2] = labelNames.get(i);
            }
        } else {
            mainHeader = new String[numLabel * this.numberOfIntervalBins + 1];
            mainHeader[0] = "PID";
            for (int j = 0; j < dg.size(); j++) {
                int pfxSize = j * this.numberOfIntervalBins;
                DownsampleGroup g = dg.get(j);
                mainHeader[pfxSize + 1] = g.getLabel() + "_1";
                for (int i = 2; i <= this.numberOfIntervalBins; i++) {
                    mainHeader[pfxSize + i] = String.valueOf(i);
                }
            }
        }
        this.outputTimeMetaWriter.writeNext(timemetaHdr);
        this.outputFileWriter.writeNext(mainHeader);
        this.mainHeaderSize = mainHeader.length;
        this.timeMetaHeaderSize = timemetaHdr.length;
    }

    public void writeMetaFile(String data) {
        try {
            this.outputMetaWriter.write(data);
        } catch (IOException e) {
            logger.error("Message '{}' failed to write as meta text: {}", data, e.getLocalizedMessage());
        }
    }

    public void writeMainData(String patientId, ResultTable r) {
        if (this.isOutputVertical) {
            for (int i = 0; i < r.getRowCount(); i++) {
                List<Object> row = r.getDatalistByRow(i);
                //TODO: Some data is null to mark for N/A
                //TODO: Some to mark as Insuff. Data
                String[] resultDataRow = row.stream().map(Object::toString).toArray(String[]::new);
                String[] mainData = new String[this.mainHeaderSize];
                mainData[0] = patientId;
                System.arraycopy(resultDataRow, 0, mainData, 1, resultDataRow.length);
                this.outputFileWriter.writeNext(mainData);
            }
        } else {
            //TODO: Implement H write out not
        }
    }

    public void writeTimeMeta(String patientId, ExportQueryBuilder eq, ResultTable r) {
        String[] data = {
                patientId, eq.getFirstAvailData().toString(), eq.getLastAvailData().toString(),
                eq.getQueryStartTime().toString(), eq.getQueryEndTime().toString(), "", ""
        };
        //TODO: Fill last 2 columns
        this.outputTimeMetaWriter.writeNext(data);
    }

    public void close() {
        this.closeMetaText();
        this.closeCsv();
    }

    /**
     * Call when this downsample finished
     */
    private void closeMetaText() {
        try {
            this.outputMetaWriter.write(String.format("%nEXPORT ENDED ON '%s'.", LocalDateTime.now().toString()));
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
