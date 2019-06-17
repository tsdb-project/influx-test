package edu.pitt.medschool.model.dao;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.pitt.medschool.model.DataTimeSpanBean;
import edu.pitt.medschool.model.dto.Downsample;
import edu.pitt.medschool.model.dto.DownsampleGroup;

/**
 * Queries for doing the downsample-aggregation query Refactor from AnalysisService.exportToFile
 */
public class ExportQueryBuilder {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    static final int TRIMMING_OFFSET = 30;

    private static class Template {
        static final String defaultDownsampleColName = "ds_label_";
        static final String defaultAggregationColName = "ag_label_";

        static final String basicAggregationInner = "SELECT %s FROM \"%s\" WHERE %s";
        static final String basicDownsampleOuter = "SELECT %s FROM %s WHERE %s GROUP BY time(%ds,%ds) ORDER BY time ASC";

        static final String aggregationCount = "COUNT(%s) AS C";
        static final String timeCondition = "(time >= '%s' AND time < '%s')";
        static final String timeZone = "tz('America/New_York')";
    }

    // Downsample configs
    private int numOfDownsampleGroups;
    private DownsampleGroup[] downsampleGroups;
    private boolean isDownSampleFirst;
    private boolean isAr;
    private List<List<String>> columnNames;
    private int exportTotalDuration; // In 's'
    private int exportStartOffset; // In 's'
    private int downsampleInterval; // In 's'

    // Prebuilt final query string and related
    private String globalTimeLimitWhere = null;
    private ArrayList<String> columnNameAliases;
    private String queryString = "";

    // Metadata for patients
    private String pid;
    private int numDataSegments;
    private List<DataTimeSpanBean> timeseriesMetadata;

    // Meta that this class generated (That others may use)
    private long downsampleOffset = 0;
    private List<Integer> validTimeSpanIds;
    private Instant firstAvailData = Instant.MAX; // Immutable once set
    private Instant lastAvailData = Instant.MIN; // Immutable once set
    private Instant queryStartTime = null;
    private Instant queryEndTime = null;

    /**
     * Initialize this class (Generate nothing if dts is empty)
     *
     * @param fakeStartTime Determine group by offset
     * @param dts           Data
     * @param v             List of DownsampleGroup
     * @param columns       Columns for every downsample group
     * @param ds            Downsample itself
     * @param needAr        This job is Ar or NoAr
     */
    public ExportQueryBuilder(Instant startTime, Instant fakeStartTime, List<DataTimeSpanBean> dts, List<DownsampleGroup> v,
            List<List<String>> columns, Downsample ds, boolean needAr) {
        // no available data
        if (dts == null || dts.isEmpty()) {
            return;
        }
        this.pid = dts.get(0).getPid();
        this.numDataSegments = dts.size();
        this.timeseriesMetadata = dts;
        this.columnNames = columns;
        this.validTimeSpanIds = new ArrayList<>(this.numDataSegments);
        this.isAr = needAr;

        populateDownsampleGroup(v);
        populateDownsampleData(ds);
        findValidFirstLastData(startTime);

        // If no available data then stop building
        if (this.validTimeSpanIds.isEmpty()) {
            logger.info("no valid data");
            return;
        }
        if (this.exportTotalDuration > 0) {
            this.queryEndTime = this.firstAvailData.plusSeconds(this.exportTotalDuration);
        }
        if (this.exportStartOffset > 0) {
            this.queryStartTime = this.queryStartTime.plusSeconds(this.exportStartOffset);
            this.queryEndTime = this.lastAvailData.plusSeconds(this.exportStartOffset);
        }
        calcOffsetInSeconds(fakeStartTime);
        this.globalTimeLimitWhere = String.format(Template.timeCondition, this.queryStartTime.toString(),
                this.queryEndTime.toString());
        buildQuery();
    }

    /**
     * Find offset (in seconds) to match the start time
     */
    private void calcOffsetInSeconds(Instant fakeStartTime) {
        this.downsampleOffset = Duration.between(fakeStartTime, queryStartTime).getSeconds();
    }

    private void populateDownsampleGroup(List<DownsampleGroup> v) {
        this.numOfDownsampleGroups = v.size();
        this.columnNameAliases = new ArrayList<>(this.numOfDownsampleGroups);
        String prefix = isDownSampleFirst ? Template.defaultDownsampleColName : Template.defaultAggregationColName;

        this.downsampleGroups = v.stream().peek(dvo -> this.columnNameAliases.add(prefix + String.valueOf(dvo.getId())))
                .toArray(DownsampleGroup[]::new);
    }

    private void populateDownsampleData(Downsample ds) {
        this.exportStartOffset = ds.getOrigin();
        this.downsampleInterval = ds.getPeriod();
        this.isDownSampleFirst = ds.getDownsampleFirst();
        this.exportTotalDuration = ds.getDuration();
    }

    // Find first, last data and if ArType matches
    private void findValidFirstLastData(Instant starttime) {
        for (int i = 0; i < this.numDataSegments; i++) {
            DataTimeSpanBean d = this.timeseriesMetadata.get(i);
            if (!isDataArTypeGood(d)) {
                continue;
            }
            this.validTimeSpanIds.add(i);
            Instant tmpS = d.getStart(), tmpE = d.getEnd();
            // take the first(smallest) time of strat as firstAvailData, the last (largest) time of end as lastAvailData
            if (tmpS.compareTo(this.firstAvailData) < 0)
                this.queryStartTime = this.firstAvailData = tmpS;
            if (tmpE.compareTo(this.lastAvailData) > 0)
                this.queryEndTime = this.lastAvailData = tmpE;
        }
        this.queryStartTime = starttime;
    }

    public String getQueryString() {
        return this.queryString;
    }

    public List<Integer> getGoodDataTimeId() {
        return this.validTimeSpanIds;
    }

    public Instant getFirstAvailData() {
        return firstAvailData;
    }

    public Instant getLastAvailData() {
        return lastAvailData;
    }

    public Instant getQueryStartTime() {
        return queryStartTime;
    }

    public Instant getQueryEndTime() {
        return queryEndTime;
    }

    // Lookup all in one query, DO NOT lookup by files
    private void buildQuery() {
        String whereClause = this.artypeWhereClause(this.isAr) + " AND " + this.globalTimeLimitWhere;

        if (!this.isDownSampleFirst) {
            String aggrQ = aggregationWhenAggregationFirst(whereClause);
            this.queryString = downsampleWhenAggregationFirst(aggrQ);
        } else {
            this.queryString = whenDownsampleFirst(whereClause);
        }
        // add time zone, change the output tz to ET
        this.queryString = addTimeZone(this.queryString);
    }




    /**
     * Aggr part for Aggr first
     */
    private String aggregationWhenAggregationFirst(String locator) {
        String[] cols = new String[this.numOfDownsampleGroups];
        for (int i = 0; i < this.numOfDownsampleGroups; i++) {
            DownsampleGroup dg = this.downsampleGroups[i];
            cols[i] = populateByAggregationType(this.columnNames.get(i), this.columnNameAliases.get(i), dg, "\"");
        }
        return String.format(Template.basicAggregationInner, String.join(", ", cols), this.pid, locator);
    }

    /**
     * Aggr first, Ds part
     */
    private String downsampleWhenAggregationFirst(String aggrQuery) {
        StringBuilder cols = new StringBuilder();
        for (int i = 0; i < this.numOfDownsampleGroups; i++) {
            String template = formDownsampleFunctionTemplate(this.downsampleGroups[i]);
            cols.append(String.format(template, this.columnNameAliases.get(i)));
            cols.append(", ");
        }
        // A count column
        cols.append(String.format(Template.aggregationCount, this.columnNameAliases.get(0)));
        String timeBoud = String.format(Template.timeCondition, this.queryStartTime.toString(), this.queryEndTime.toString());

        return String.format(Template.basicDownsampleOuter, cols.toString(), wrapByBracket(aggrQuery), timeBoud,
                this.downsampleInterval, this.downsampleOffset);
    }

    /**
     * Ds first, ds and aggr part
     */
    private String whenDownsampleFirst(String locator) {
        String[] cols = new String[this.numOfDownsampleGroups + 1];
        for (int i = 0; i < this.numOfDownsampleGroups; i++) {
            DownsampleGroup dg = this.downsampleGroups[i];

            // Generate downsample InfluxQLs
            String downsampleTemplate = formDownsampleFunctionTemplate(dg);
            List<String> downsampleOperators = this.columnNames.get(i).stream().map(s -> String.format(downsampleTemplate, s))
                    .collect(Collectors.toList());

            // Concat downsample to form aggregation
            String concated = populateByAggregationType(downsampleOperators, null, dg, "");
            cols[i] = selectQueryWithAlias(concated, this.columnNameAliases.get(i));
        }
        cols[cols.length - 1] = String.format(Template.aggregationCount, "Time");

        return String.format(Template.basicDownsampleOuter, String.join(", ", cols), "\"" + pid + "\"", locator,
                this.downsampleInterval, this.downsampleOffset);
    }

    /**
     * Create a new String to fulfill the aggregation InfluxQL
     *
     * @param aggregateName Name of column to aggregate
     * @param columnAlias   Alias for aggregation function
     * @param dg            Downsample group data
     * @param delimter      Delimter for wrapping every column name
     */
    private String populateByAggregationType(List<String> aggregateName, String columnAlias, DownsampleGroup dg,
            String delimter) {
        switch (dg.getAggregation().toLowerCase()) {
            case "mean":
                return columnsMeanQuery(aggregateName, columnAlias, delimter);
            case "sum":
                return columnsSumQuery(aggregateName, columnAlias, delimter);
            default:
                throw new RuntimeException("Unsupported aggregation type: " + dg.getAggregation());
        }
    }

    /**
     * MEAN(%s)
     */
    private String formDownsampleFunctionTemplate(DownsampleGroup dg) {
        switch (dg.getDownsample().toLowerCase()) {
            case "mean":
                return "MEAN(%s)";
            case "median":
                return "MEDIAN(%s)";
            case "sum":
                return "SUM(%s)";
            case "stddev":
                return "STDDEV(%s)";
            case "min":
                return "MIN(%s)";
            case "max":
                return "MAX(%s)";
            case "25":
                return "PERCENTILE(%s,25)";
            case "75":
                return "PERCENTILE(%s,75)";
            default:
                throw new RuntimeException("Unsupported downsample type: " + dg.getDownsample());
        }
    }

    /**
     * Concat the column name list into an add string: ("f1"+"f2") " Could be set as `delimters`
     *
     * @param alias Alias for this list, null for not using
     */
    private String columnsSumQuery(List<String> names, String alias, String delimter) {
        return selectQueryWithAlias(
                wrapByBracket(names.stream().map(s -> delimter + s + delimter).collect(Collectors.joining("+"))), alias);
    }

    /**
     * (arType='ar')
     */
    private String artypeWhereClause(boolean isAr) {
        return String.format("(arType='%s')", isAr ? "ar" : "noar");
    }

    /**
     * Concat the column name list into an mean string": (("f1"+"f2")/2) " Could be set as `delimters`
     *
     * @param alias Alias for this list, null for not using
     */
    private String columnsMeanQuery(List<String> names, String alias, String delimter) {
        String cols = String.format("(%s/%d)", columnsSumQuery(names, null, delimter), names.size());
        return selectQueryWithAlias(cols, alias);
    }

    /**
     * If already wrapped then do nothing "f1"+"f2" -> ("f1"+"f2")
     */
    private String wrapByBracket(String toWrap) {
        if (toWrap.startsWith("(") && toWrap.endsWith(")"))
            return toWrap;
        return String.format("(%s)", toWrap);
    }

    /**
     * Give alias for select statements (I10_1,I10_2) -> (I10_1,I10_2) AS A
     *
     * @param origin Original select obj
     * @param alias  Alias, null for not using
     */
    private String selectQueryWithAlias(String origin, String alias) {
        if (alias == null)
            return origin;
        else
            return String.format("%s AS %s", wrapByBracket(origin), alias);
    }

    private String addTimeZone(String queryString){
        return queryString+Template.timeZone;
    }

    /**
     * This DataTimeSpanBean of our interest?
     */
    private boolean isDataArTypeGood(DataTimeSpanBean d) {
        DataTimeSpanBean.ArStatus as = d.getArStat();
        if (this.isAr) {
            // Need Ar but this UUID only has NoAr
            return !as.equals(DataTimeSpanBean.ArStatus.NoArOnly);
        } else {
            // Need NoAr but this UUID only has Ar
            return !as.equals(DataTimeSpanBean.ArStatus.ArOnly);
        }
    }

}
