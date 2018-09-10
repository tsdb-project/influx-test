package edu.pitt.medschool.algorithm;

import edu.pitt.medschool.model.DataTimeSpanBean;
import edu.pitt.medschool.model.dto.DownsampleGroup;

import java.util.List;
import java.util.stream.Collectors;

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
    private List<String> columnNames;

    // Metadata for patients
    private String pid;
    private int numDataSegments;
    private List<DataTimeSpanBean> timeseriesMetadata;

    // Final query string
    private boolean isQueryBuilt = false;
    private String queryString;

    /**
     * Initialize this class (Expection if dts null or no length)
     *
     * @param dts             Data
     * @param v               DownsampleGroupVO
     * @param columns         Columns (e.g. I10_4,I11_4)
     * @param downSampleFirst Is Downsample first or aggregation first
     */
    private ExportQuery(
            List<DataTimeSpanBean> dts,
            DownsampleGroup v,
            List<String> columns,
            boolean downSampleFirst) throws IllegalArgumentException {
        initData(dts);
        this.dsgVo = v;
        this.isDownSampleFirst = downSampleFirst;
        this.columnNames = columns;

        buildQuery();
    }

    /**
     * Assemble the query string from given data.
     *
     * @param d               Data
     * @param v               DownsampleGroupVO
     * @param columns         Columns (e.g. I10_4,I11_4)
     * @param downSampleFirst Is Downsample first or aggregation first
     */
    public static String generate(List<DataTimeSpanBean> d,
                                  DownsampleGroup v, List<String> columns,
                                  boolean downSampleFirst) throws Exception {
        return new ExportQuery(d, v, columns, downSampleFirst).toString();
    }

    /**
     * Get the final assembled queries
     */
    @Override
    public String toString() {
        if (!this.isQueryBuilt) {
            throw new RuntimeException("Failed to build query string");
        } else {
            return this.queryString;
        }
    }

    private void buildQuery() {
        StringBuilder sb = new StringBuilder("SELECT ");

        this.queryString = sb.toString();
        this.isQueryBuilt = true;
    }

    /**
     * Extract from `DataTimeSpanBean` to set some basic info for this class
     */
    private void initData(List<DataTimeSpanBean> dts) {
        if (dts == null || dts.isEmpty()) throw new IllegalArgumentException("DataTimeSpanBean empty");
        this.pid = dts.get(0).getPid();
        this.numDataSegments = dts.size();
        this.timeseriesMetadata = dts;
    }

    /**
     * Concat the column name list into an add string: ("f1"+"f2")
     * For aggregation
     *
     * @param alias Alias for this list, null for not using
     */
    private String formColumnNamesToAddQuery(String alias) {
        return selectQueryWithAlias(String.format("(%s)",
                this.columnNames.stream()
                        .map(s -> "\"" + s + "\"")
                        .collect(Collectors.joining("+"))),
                alias);
    }

    /**
     * Concat the column name list into an mean string": (("f1"+"f2")/2)
     * For aggregation
     *
     * @param alias Alias for this list, null for not using
     */
    private String formColumnNamesToMeanQuery(String alias) {
        return selectQueryWithAlias(
                String.format("((%s)/%d)",
                        formColumnNamesToAddQuery(null),
                        this.columnNames.size()),
                alias);
    }

    /**
     * Give alias for select statements
     * (COUNT(I10_1,I10_2) -> COUNT(I10_1,I10_2) AS A)
     *
     * @param origin Original select obj
     * @param alias  Alias, null for not using
     */
    private String selectQueryWithAlias(String origin, String alias) {
        if (alias == null) return origin;
        else return String.format("%s AS %s", origin, alias);
    }

}
