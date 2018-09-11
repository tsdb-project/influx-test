package edu.pitt.medschool.algorithm;

import edu.pitt.medschool.controller.analysis.vo.DownsampleGroupVO;
import edu.pitt.medschool.model.DataTimeSpanBean;
import edu.pitt.medschool.model.dto.DownsampleGroup;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Queries for doing the downsample-aggregation query
 * Refactor from AnalysisService.exportToFile
 */
public class ExportQuery {

    private static class Template {
        static final String defaultDownsampleColName = "ds_label";
        static final String defaultAggregationColName = "ag_label";

        static final String basicSelect = "SELECT %s FROM \"%s\" WHERE %s";
        static final String timeLimitCondition = "(time >= '%s' AND time <= '%s')";
        static final String locatorCondition = "(fileUUID='%s' AND arType='%s')";
        static final String downsampleGroupBy = "GROUP BY time(%ds) fill(none)";
    }

    // Downsample configs
    private DownsampleGroup[] dsGroup;
    private boolean isDownSampleFirst;
    private List<String> columnNames;
    private int startDelta; // In 's'
    private int duration; // In 's'

    // Metadata for patients
    private String pid;
    private int numDataSegments;
    private List<DataTimeSpanBean> timeseriesMetadata;

    // Final query string
    private String[] queriesString;

    /**
     * Assemble the query string from given data.
     *
     * @param d               Data
     * @param v               List of DownsampleGroupVO
     * @param columns         Columns (e.g. I10_4,I11_4)
     * @param downSampleFirst Is Downsample first or aggregation first
     * @param startDelta      Start time (in s)
     * @param duration        Duration (in s)
     */
    public static String[] generate(List<DataTimeSpanBean> d,
                                    List<DownsampleGroupVO> v, List<String> columns,
                                    boolean downSampleFirst,
                                    int startDelta, int duration) throws Exception {
        return new ExportQuery(d, v, columns, downSampleFirst, startDelta, duration).toQureies();
    }

    /**
     * Initialize this class (Expection if dts null or no length)
     */
    private ExportQuery(List<DataTimeSpanBean> dts, List<DownsampleGroupVO> v, List<String> columns,
                        boolean downSampleFirst, int sd, int d) throws IllegalArgumentException {
        initData(dts, v);
        this.isDownSampleFirst = downSampleFirst;
        this.columnNames = columns;
        this.duration = d;
        this.startDelta = sd;

        buildQuery();
    }

    /**
     * Get the final assembled queries
     * Each unique file uuid would have one query
     */
    public String[] toQureies() {
        if (this.queriesString == null) {
            throw new RuntimeException("Failed to build query string");
        } else {
            return this.queriesString;
        }
    }

    private void buildQuery() {
        this.queriesString = new String[this.numDataSegments];
        // A query for each unique file uuid
        for (DataTimeSpanBean d : this.timeseriesMetadata) {
            if (this.isDownSampleFirst) {
                // Downsample then Aggr
                aggrFirst(d);
            } else {
                // Aggr then Downsample
                dsFirst(d);
            }
        }
    }

    private void aggrFirst(DataTimeSpanBean d) {

    }

    /**
     * Aggr first, Aggr part
     */
    private String aggrDs_Aggr() {
        for (DownsampleGroup dg : this.dsGroup) {

        }
        return "";
    }

    /**
     * Aggr first, Ds part
     */
    private String aggrDs_Ds() {
        return "";
    }

    private void dsFirst(DataTimeSpanBean d) {

    }

    /**
     * Ds first, Aggr part
     */
    private String dsAggr_Aggr() {
        return "";
    }

    /**
     * Ds first, Ds part
     */
    private String dsAggr_Ds() {
        return "";
    }

    /**
     * Extract from `DataTimeSpanBean` to set some basic info for this class
     */
    private void initData(List<DataTimeSpanBean> dts, List<DownsampleGroupVO> v) {
        if (dts == null || dts.isEmpty()) throw new IllegalArgumentException("DataTimeSpanBean empty");
        this.pid = dts.get(0).getPid();
        this.numDataSegments = dts.size();
        this.timeseriesMetadata = dts;
        this.dsGroup = v.stream().map(DownsampleGroupVO::getGroup).toArray(DownsampleGroup[]::new);
    }

    /**
     * GROUP BY time(10s) fill(none)
     */
    private String downsampleGroupBy(int sec) {
        return String.format(Template.downsampleGroupBy, sec);
    }

    /**
     * (arType='ar' and fileUUID='xxxx')
     */
    private String whereFileUuidAndarType(String uuid, boolean isAr) {
        return String.format(Template.locatorCondition,
                uuid, isAr ? "ar" : "noar");
    }

    /**
     * Concat the column name list into an add string: ("f1"+"f2")
     * For aggregation
     *
     * @param alias Alias for this list, null for not using
     */
    private String aggregationColumnsSumQuery(String alias) {
        return selectQueryWithAlias(
                wrapByBracket(this.columnNames.stream()
                        .map(s -> "\"" + s + "\"")
                        .collect(Collectors.joining("+"))
                ), alias);
    }

    /**
     * Concat the column name list into an mean string": (("f1"+"f2")/2)
     * For aggregation
     *
     * @param alias Alias for this list, null for not using
     */
    private String aggregationColumnsMeanQuery(String alias) {
        return selectQueryWithAlias(
                String.format("(%s/%d)",
                        aggregationColumnsSumQuery(null),
                        this.columnNames.size()),
                alias);
    }

    /**
     * If already wrapped then do nothing
     * "f1"+"f2" -> ("f1"+"f2")
     */
    private String wrapByBracket(String toWrap) {
        if (toWrap.startsWith("(") && toWrap.endsWith(")"))
            return toWrap;
        return String.format("(%s)", toWrap);
    }

    /**
     * Give alias for select statements
     * (I10_1,I10_2) -> (I10_1,I10_2) AS A
     *
     * @param origin Original select obj
     * @param alias  Alias, null for not using
     */
    private String selectQueryWithAlias(String origin, String alias) {
        if (alias == null) return origin;
        else return String.format("%s AS %s", wrapByBracket(origin), alias);
    }

}
