package edu.pitt.medschool.model.dao;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import edu.pitt.medschool.model.DataTimeSpanBean;
import edu.pitt.medschool.model.dto.*;

public class ExportMedicalQueryBuilder {
	
	private static class Template {
        static final String defaultDownsampleColName = "ds_label_";
        static final String defaultAggregationColName = "ag_label_";

        static final String basicAggregationInner = "SELECT %s FROM \"%s\" WHERE %s";
        static final String basicDownsampleOuter = "SELECT %s FROM %s WHERE %s GROUP BY time(%ds,%ds) ORDER BY time ASC";

        static final String aggregationCount = "COUNT(%s) AS C";
        static final String timeCondition = "(time >= '%s' AND time < '%s')";
    }
	
	// Downsample configs
    private int numOfDownsampleGroups;
    private MedicalDownsampleGroup[] medicalDownsampleGroups;
    private boolean isDownSampleFirst;
    private boolean isAr;
    private List<List<String>> columnNames;
    private int exportTotalDuration; // In 's'
    private int exportStartOffsets; // In 's'
    private int downsampleInterval; // In 's'

    // Prebuilt final query string and related
    private String globalTimeLimitWhere = null;
    private ArrayList<String> columnNameAliases;
    private String queryString;

    // Metadata for patients
    private String pid;
    private int numDataSegments;
    private List<DataTimeSpanBean> timeseriesMetadata;

    // Meta that this class generated (That others may use)
    private long downsampleOffset = 0;
    private List<Integer> validTimeSpanIds;
    private Instant firstAvailData = Instant.MAX; // Immutable once set
    private Instant lastAvailData = Instant.MIN; // Immutable once set
    private Instant queryStartTime;
    private Instant queryEndTime;

    // Meta for medication
    private Instant medicalTime;
    private Instant expectStartTime;
    private Instant expectEndTime;
    
    /**
     * Initialize this class (Generate nothing if dts is empty)
     *
     * @param fakeStartTime Determine group by offset
     * @param dts           Data
     * @param v             List of DownsampleGroup
     * @param columns       Columns for every downsample group
     * @param ds            MedicalDownsample itself
     * @param needAr        This job is Ar or NoAr
     */
    public ExportMedicalQueryBuilder(Instant fakeStartTime, List<DataTimeSpanBean> dts, List<MedicalDownsampleGroup> v, List<List<String>> columns,
                                     MedicalDownsample ds, boolean needAr, Medication records) {
    	 if (dts == null || dts.isEmpty()) {
             return;
         }
    	 this.queryString ="";
         this.pid = dts.get(0).getPid();
         this.numDataSegments = dts.size();
         this.timeseriesMetadata = dts;
         this.columnNames = columns;
         this.validTimeSpanIds = new ArrayList<>(this.numDataSegments);
         this.isAr = needAr;
         this.medicalTime = records.getChartDate().toInstant();
         populateDownsampleGroup(v);
         populateDownsampleData(ds);
         findValidFirstLastData();

         // If no available data then stop building
         if (this.validTimeSpanIds.isEmpty()) return;
         if (this.exportTotalDuration > 0) {
             this.queryEndTime = this.firstAvailData.plusSeconds(this.exportTotalDuration);
         }
         this.queryStartTime = this.firstAvailData;
         this.queryEndTime = this.lastAvailData;
         this.expectStartTime = this.medicalTime.minus(ds.getBeforeMedicine(),ChronoUnit.SECONDS);
         this.expectEndTime = this.medicalTime.plusSeconds(ds.getAfterMedicine());

         // If medical date is not in valid time range, stop builder
         if (this.queryStartTime.compareTo(this.medicalTime)<0 && this.queryEndTime.compareTo(this.medicalTime)>0) {
             if(this.queryStartTime.compareTo(this.expectStartTime)<0){
                 this.queryStartTime=this.expectStartTime;
             }
             if(this.queryEndTime.compareTo(this.expectEndTime)>0){
                 this.queryEndTime=this.expectEndTime;
             }
         }else{
             return;
         }
         calcOffsetInSeconds(fakeStartTime);
         this.globalTimeLimitWhere = String.format(Template.timeCondition, this.queryStartTime.toString(), this.queryEndTime.toString());
         buildQuery();
    }
    
    /**
     * Find offset (in seconds) to match the start time
     */
    private void calcOffsetInSeconds(Instant fakeStartTime) {
        this.downsampleOffset = Duration.between(fakeStartTime, queryStartTime).getSeconds();
    }

    private void populateDownsampleGroup(List<MedicalDownsampleGroup> v) {
        this.numOfDownsampleGroups = v.size();
        this.columnNameAliases = new ArrayList<>(this.numOfDownsampleGroups);
        String prefix = isDownSampleFirst ? Template.defaultDownsampleColName : Template.defaultAggregationColName;

        this.medicalDownsampleGroups = v.stream().peek(dvo -> this.columnNameAliases.add(prefix + String.valueOf(dvo.getId())))
                .toArray(MedicalDownsampleGroup[]::new);
    }

    private void populateDownsampleData(MedicalDownsample ds) {
        this.exportStartOffsets = 0;
        this.downsampleInterval = ds.getPeriod();
        this.isDownSampleFirst = ds.getDownsampleFirst();
        this.exportTotalDuration = ds.getAfterMedicine()+ds.getBeforeMedicine();
    }

    // Find first, last data and if ArType matches
    private void findValidFirstLastData() {
        for (int i = 0; i < this.numDataSegments; i++) {
            DataTimeSpanBean d = this.timeseriesMetadata.get(i);
            if (!isDataArTypeGood(d)) {
                continue;
            }
            this.validTimeSpanIds.add(i);
            Instant tmpS = d.getStart(), tmpE = d.getEnd();
            if (tmpS.compareTo(this.firstAvailData) < 0)
                this.firstAvailData = tmpS;
            if (tmpE.compareTo(this.lastAvailData) > 0)
                this.lastAvailData = tmpE;
        }
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

    public Instant getMedicalTime() {
        return medicalTime;
    }

    public Instant getExpectStartTime() {
        return expectStartTime;
    }

    public Instant getExpectEndTime() {
        return expectEndTime;
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
    }

    /**
     * Aggr part for Aggr first
     */
    private String aggregationWhenAggregationFirst(String locator) {
        String[] cols = new String[this.numOfDownsampleGroups];
        for (int i = 0; i < this.numOfDownsampleGroups; i++) {
            MedicalDownsampleGroup dg = this.medicalDownsampleGroups[i];
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
            String template = formDownsampleFunctionTemplate(this.medicalDownsampleGroups[i]);
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
            MedicalDownsampleGroup dg = this.medicalDownsampleGroups[i];

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
    private String populateByAggregationType(List<String> aggregateName, String columnAlias, MedicalDownsampleGroup dg, String delimter) {
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
    private String formDownsampleFunctionTemplate(MedicalDownsampleGroup dg) {
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
        return selectQueryWithAlias(wrapByBracket(names.stream().map(s -> delimter + s + delimter).collect(Collectors.joining("+"))), alias);
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
