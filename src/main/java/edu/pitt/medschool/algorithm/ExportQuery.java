package edu.pitt.medschool.algorithm;

import edu.pitt.medschool.model.DataTimeSpanBean;
import edu.pitt.medschool.model.dto.DownsampleGroup;

import java.util.List;

/**
 * Queries for doing the downsample-aggregation query
 * Refactor from AnalysisService.exportToFile
 */
public class ExportQuery {

    private static class Config {
        static final String defaultDownsampleColName = "ds_label";
        static final String defaultAggregationColName = "ag_label";
    }

    // Downsample configs
    private DownsampleGroup dsgVo;
    private boolean isDownSampleFirst;

    // Metadata for patients
    private String pid;
    private int numDataSegments;
    private List<DataTimeSpanBean> timeseriesMetadata;

    /**
     * Initialize this class (Expection if dts null or no length)
     *
     * @param dts             Data
     * @param v               DownsampleGroupVO
     * @param downSampleFirst Is Downsample first or aggregation first
     */
    public ExportQuery(List<DataTimeSpanBean> dts, DownsampleGroup v, boolean downSampleFirst) {
        initData(dts);
        this.dsgVo = v;
        this.isDownSampleFirst = downSampleFirst;
    }

    /**
     * Extract from `DataTimeSpanBean` to set some info for this class
     */
    private void initData(List<DataTimeSpanBean> dts) {
        if (dts == null || dts.isEmpty()) throw new IllegalArgumentException("DataTimeSpanBean empty");
        this.pid = dts.get(0).getPid();
        this.numDataSegments = dts.size();
        this.timeseriesMetadata = dts;
    }

    /**
     * Get the final assembled queries
     */
    @Override
    public String toString() {
        return super.toString();
    }
}
