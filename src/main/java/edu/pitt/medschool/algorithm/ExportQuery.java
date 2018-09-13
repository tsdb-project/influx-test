package edu.pitt.medschool.algorithm;

import edu.pitt.medschool.controller.analysis.vo.DownsampleGroupVO;
import edu.pitt.medschool.model.DataTimeSpanBean;
import edu.pitt.medschool.model.dto.Downsample;
import edu.pitt.medschool.model.dto.DownsampleGroup;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Queries for doing the downsample-aggregation query
 * Refactor from AnalysisService.exportToFile
 */
public class ExportQuery {

    // Downsample configs
    private int numOfDownsampleGroups;
    private DownsampleGroup[] downsampleGroups;
    private int totalDuration; // In 's'
    private boolean isDownSampleFirst;
    private boolean needAr;
    private List<List<String>> columnNames;
    private int startDelta; // In 's'
    private int downsampleInterval; // In 's'
    // Prebuilt final query string and related
    private String globalTimeLimitWhere = null;

    // Metadata for patients
    private String pid;
    private int numDataSegments;
    private List<DataTimeSpanBean> timeseriesMetadata;
    private ArrayList<String> columnNameAliases;
    private String queryString = "";
    // Meta that this class generated (That others may use)
    private List<Integer> validTimeSpanIds;
    private Instant firstAvailData = Instant.MAX;
    private Instant lastAvailData = Instant.MIN;
    private Instant queryStartTime = null;
    private Instant queryEndTime = null;
    /**
     * Initialize this class (Generate nothing if dts is empty)
     *
     * @param dts     Data
     * @param vo      List of DownsampleGroupVO
     * @param columns Columns for every downsample group
     * @param ds      Downsample itself
     */
    public ExportQuery(List<DataTimeSpanBean> dts, List<DownsampleGroupVO> vo, List<List<String>> columns, Downsample ds) {
        if (dts == null || dts.isEmpty()) {
            return;
        }
        this.pid = dts.get(0).getPid();
        this.numDataSegments = dts.size();
        this.timeseriesMetadata = dts;
        this.columnNames = columns;
        this.validTimeSpanIds = new ArrayList<>(this.numDataSegments);

        populateDownsampleGroup(vo);
        populateDownsampleData(ds);
        findFirstLastMatchData();

        this.globalTimeLimitWhere = String.format(Template.timeCondition,
                this.queryStartTime.toString(), this.queryEndTime.toString());
        buildQuery();
    }

    private void populateDownsampleGroup(List<DownsampleGroupVO> v) {
        this.numOfDownsampleGroups = v.size();
        this.columnNameAliases = new ArrayList<>(this.numOfDownsampleGroups);
        String prefix = isDownSampleFirst ? Template.defaultDownsampleColName : Template.defaultAggregationColName;

        this.downsampleGroups = v.stream().map(dvo -> {
            DownsampleGroup dg = dvo.getGroup();
            this.columnNameAliases.add(prefix + String.valueOf(dg.getId()));
            return dg;
        }).toArray(DownsampleGroup[]::new);
    }

    private void populateDownsampleData(Downsample ds) {
        this.startDelta = ds.getOrigin();
        this.downsampleInterval = ds.getPeriod();
        this.needAr = ds.getNeedar();
        this.isDownSampleFirst = ds.getIsDownsampleFirst();
        this.totalDuration = ds.getDuration();
    }

    // Find first, last data and if ArType matches
    private void findFirstLastMatchData() {
        for (int i = 0; i < this.numDataSegments; i++) {
            DataTimeSpanBean d = this.timeseriesMetadata.get(i);
            if (!isDataArTypeGood(d)) {
                continue;
            }
            this.validTimeSpanIds.add(i);
            Instant tmpS = d.getStart(), tmpE = d.getEnd();
            if (tmpS.compareTo(this.firstAvailData) < 0)
                this.queryStartTime = this.firstAvailData = tmpS;
            if (tmpE.compareTo(this.lastAvailData) > 0)
                this.queryEndTime = this.lastAvailData = tmpE;
        }

        if (this.totalDuration > 0) {
            this.queryEndTime = this.firstAvailData.plusSeconds(this.totalDuration);
        }
        if (this.startDelta > 0) {
            this.queryStartTime = this.firstAvailData.plusSeconds(this.startDelta);
        }
    }

    public String toQuery() {
        return this.queryString;
    }

    public List<Integer> getGoodDataTimeId() {
        return this.validTimeSpanIds;
    }

    // Lookup all in one query, DO NOT lookup by files
    private void buildQuery() {
        String whereClause = this.artypeWhereClause(this.needAr) + " AND " + this.globalTimeLimitWhere;

        if (!this.isDownSampleFirst) {
            String aggrQ = aggregationWhenAggregationFirst(whereClause);
            this.queryString = downsampleWhenAggregationFirst(aggrQ);
        } else {
            // Aggr then Downsample
            throw new RuntimeException("Not implemented");
        }
    }

    /**
     * Aggr part for Aggr first
     */
    private String aggregationWhenAggregationFirst(String locator) {
        String[] cols = new String[this.numOfDownsampleGroups];
        for (int i = 0; i < this.numOfDownsampleGroups; i++) {
            DownsampleGroup dg = this.downsampleGroups[i];
            switch (dg.getAggregation().toLowerCase()) {
                case "mean":
                    cols[i] = columnsMeanQuery(this.columnNames.get(i), this.columnNameAliases.get(i));
                    break;
                case "sum":
                    cols[i] = columnsSumQuery(this.columnNames.get(i), this.columnNameAliases.get(i));
                    break;
                default:
                    throw new RuntimeException("Unsupported aggregation type: " + dg.getAggregation());
            }
        }
        return String.format(Template.basicAggregationInner,
                String.join(", ", cols), this.pid, locator);
    }

    /**
     * Aggr first, Ds part
     */
    private String downsampleWhenAggregationFirst(String aggrQuery) {
        StringBuilder cols = new StringBuilder();
        for (int i = 0; i < this.numOfDownsampleGroups; i++) {
            cols.append(formDownsampleFunction(this.downsampleGroups[i], this.columnNameAliases.get(i)));
            cols.append(", ");
        }
        // A count column
        cols.append(String.format(Template.aggregationCount, this.columnNameAliases.get(0)));

        return String.format(Template.basicDownsampleOuter, cols.toString(), wrapByBracket(aggrQuery),
                "time <= " + this.queryEndTime.toString(), this.downsampleInterval);
    }

    /**
     * Ds first, Ds part
     */
    private String downsampleWhenDownsampleFirst(String locator) {
        return "";
    }

    /**
     * Ds first, Aggr part
     */
    private String aggregationWhenDownsampleFirst() {

        return "";
    }

    /**
     * MEAN(some_column)
     */
    private String formDownsampleFunction(DownsampleGroup dg, String colAlias) {
        String oper;
        switch (dg.getDownsample().toLowerCase()) {
            case "mean":
                oper = String.format("MEAN(%s)", colAlias);
                break;
            case "median":
                oper = String.format("MEDIAN(%s)", colAlias);
                break;
            case "sum":
                oper = String.format("SUM(%s)", colAlias);
                break;
            case "stddev":
                oper = String.format("STDDEV(%s)", colAlias);
                break;
            case "min":
                oper = String.format("MIN(%s)", colAlias);
                break;
            case "max":
                oper = String.format("MAX(%s)", colAlias);
                break;
            case "25":
                oper = String.format("PERCENTILE(%s,25)", colAlias);
                break;
            case "75":
                oper = String.format("PERCENTILE(%s,75)", colAlias);
                break;
            default:
                throw new RuntimeException("Unsupported downsample type: " + dg.getDownsample());
        }
        return oper;
    }

    /**
     * (arType='ar')
     */
    private String artypeWhereClause(boolean isAr) {
        return String.format("(arType='%s')", isAr ? "ar" : "noar");
    }

    /**
     * Concat the column name list into an add string: ("f1"+"f2")
     * For aggregation
     *
     * @param alias Alias for this list, null for not using
     */
    private String columnsSumQuery(List<String> names, String alias) {
        return selectQueryWithAlias(
                wrapByBracket(names.stream()
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
    private String columnsMeanQuery(List<String> names, String alias) {
        String cols = String.format("(%s/%d)", columnsSumQuery(names, null), names.size());
        return selectQueryWithAlias(cols, alias);
    }

    private static class Template {
        static final String defaultDownsampleColName = "ds_label_";
        static final String defaultAggregationColName = "ag_label_";

        static final String aggregationCount = "COUNT(%s) AS C";
        static final String basicAggregationInner = "SELECT %s FROM \"%s\" WHERE %s";
        static final String basicDownsampleOuter = "SELECT %s FROM %s WHERE %s GROUP BY time(%ds)";
        static final String timeCondition = "(time >= %s AND time <= %s)";
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

    /**
     * This DataTimeSpanBean of our interest?
     */
    private boolean isDataArTypeGood(DataTimeSpanBean d) {
        DataTimeSpanBean.ArStatus as = d.getArStat();
        if (needAr) {
            // Need Ar but this UUID only has NoAr
            return !as.equals(DataTimeSpanBean.ArStatus.NoArOnly);
        } else {
            // Need NoAr but this UUID only has Ar
            return !as.equals(DataTimeSpanBean.ArStatus.ArOnly);
        }
    }

}
