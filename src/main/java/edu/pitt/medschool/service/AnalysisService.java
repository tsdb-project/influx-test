package edu.pitt.medschool.service;

import com.opencsv.CSVWriter;
import edu.pitt.medschool.config.DBConfiguration;
import edu.pitt.medschool.config.InfluxappConfig;
import edu.pitt.medschool.controller.analysis.vo.DownsampleGroupVO;
import edu.pitt.medschool.framework.util.Util;
import edu.pitt.medschool.model.dao.*;
import edu.pitt.medschool.model.dto.Downsample;
import edu.pitt.medschool.model.dto.DownsampleGroup;
import edu.pitt.medschool.model.dto.DownsampleGroupColumn;
import okhttp3.OkHttpClient;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Export functions
 */
@Service
public class AnalysisService {

    @Value("${machine}")
    private String uuid;

    @Value("${load}")
    private double loadFactor;

    private final static String dbName = DBConfiguration.Data.DBNAME;
    private final static String DIRECTORY = "/tsdb/output/";
    @Autowired
    DownsampleDao downsampleDao;
    @Autowired
    DownsampleGroupDao downsampleGroupDao;
    @Autowired
    DownsampleGroupColumnDao downsampleGroupColumnDao;
    @Autowired
    DownsampleMetaDao downsampleMetaDao;
    @Autowired
    DownsampleGroupAggrDao downsampleGroupAggrDao;

    /*
     * Be able to restrict the epochs for which data are exported (e.g. specify to export up to the first 36 hours of available data, but truncate
     * data thereafter). Be able to specify which columns are exported (e.g. I10_*, I10_2 only, all data, etc) Be able to export down sampled data
     * (e.g. hourly mean, median, variance, etc)
     */
    @Autowired
    PatientDao patientDao;
    @Autowired
    ImportedFileDao importedFileDao;

    private InfluxDB influxDB = InfluxDBFactory.connect(InfluxappConfig.IFX_ADDR, InfluxappConfig.IFX_USERNAME, InfluxappConfig.IFX_PASSWD,
            new OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS).readTimeout(300, TimeUnit.SECONDS).writeTimeout(120, TimeUnit.SECONDS));
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private BlockingQueue<String> idQueue = new LinkedBlockingQueue<>();

    public static void main(String[] args) {

    }

    /**
     * Export (downsample) a single query to files (Could be called mutiple times)
     */
    public void exportToFile(Integer queryId, Boolean testRun) throws IOException {
        Downsample exportQuery = downsampleDao.selectByPrimaryKey(queryId);

        // Create Folder
        File outputDir = generateOutputDir(exportQuery.getAlias());
        if (outputDir == null) return;

        // Get Patient List by 'test' or uuid
        List<String> patientIDs = importedFileDao.selectAllImportedPidOnMachine(testRun ? "jetest" : uuid);
        idQueue = new LinkedBlockingQueue<>(patientIDs);

        // List<String> patientIDs = patientDao.selectIdAll();
        // patientIDs.clear();
        // patientIDs.add("PUH-2010-014");
        // patientIDs.add("PUH-2010-064");
        // patientIDs.add("PUH-2010-068");
        // idQueue = new LinkedBlockingQueue<>(patientIDs);

        // Construct & Write CSV Column Header
        CSVWriter writer = new CSVWriter(new FileWriter(outputDir.getAbsolutePath() + "/output.csv"));
        List<String> colsList = new ArrayList<>();
        colsList.add("ID");
        List<DownsampleGroupVO> groups = downsampleGroupDao.selectAllDownsampleGroupVO(queryId);
        int intervals = Double.valueOf(Math.ceil(exportQuery.getDuration() * 1.0 / exportQuery.getPeriod())).intValue();
        for (DownsampleGroupVO group : groups) {
            String groupName = group.getGroup().getLabel();
            for (int i = 1; i <= intervals; i++) {
                if (i == 1) {
                    colsList.add(groupName + ' ' + i);
                } else {
                    colsList.add("" + i);
                }
            }
        }
        String[] cols = colsList.toArray(new String[0]);
        writer.writeNext(cols);
        writer.flush();

        // Query Task
        int paraCount = determineParaNumber();
        ExecutorService scheduler = generateNewThreadPool(paraCount);
        Runnable queryTask = () -> {
            String patientId;
            while ((patientId = idQueue.poll()) != null) {
                try {
                    String monitorTimeQuery = "select count(\"I3_1\") from \"" + patientId + "\" where arType = 'ar'";

                    QueryResult timeRes = influxDB.query(new Query(monitorTimeQuery, dbName));

                    String monitorTime = "0";
                    if (timeRes.getResults().get(0).getSeries() != null) {
                        monitorTime = timeRes.getResults().get(0).getSeries().get(0).getValues().get(0).get(1).toString();
                    }

                    if (Double.valueOf(monitorTime).intValue() >= 6 * 0) {

                        CSVWriter writerSeparate = new CSVWriter(new FileWriter(outputDir.getAbsolutePath() + "/" + patientId + ".csv"));
                        writerSeparate.writeNext(cols);

                        StringBuffer subQuery = new StringBuffer();
                        subQuery.append("SELECT ");

                        for (DownsampleGroupVO group : groups) {
                            List<String> columnAggregation = Arrays.asList(group.getGroup().getColumns().split(", "));
                            String columnAggregationComplete = columnAggregation.stream().map(s -> "\"" + s + "\"")
                                    .collect(Collectors.joining(" + "));
                            columnAggregationComplete = "(" + columnAggregationComplete + ")";
                            switch (group.getGroup().getAggregation()) {
                                case "mean":
                                    columnAggregationComplete += " / " + columnAggregation.size();
                                    break;
                                default:
                                    break;
                            }
                            columnAggregationComplete += " AS label" + group.getGroup().getId();
                            subQuery.append(columnAggregationComplete).append(", ");
                        }
                        subQuery = new StringBuffer(subQuery.substring(0, subQuery.length() - 2));
                        subQuery.append(" FROM \"" + patientId + "\"");
                        subQuery.append(" WHERE arType = 'ar'");
                        subQuery.append(" LIMIT ").append(exportQuery.getDuration());

                        // TODO number of aggregation groups needs to be dynamically coded

                        String template = "select median(label45) as LABEL45, mean(label46) as LABEL46, count(label45) as COUNT from ("
                                + subQuery.toString() + ") where time >= '%s' and time < '%s' + " + exportQuery.getDuration() + "s "
                                + "group by time(%ss, %ss)";

                        String firstRecordTimeQuery = "select \"I3_1\" from \"" + patientId + "\" where arType = 'ar' limit 1";
                        QueryResult recordResult = influxDB.query(new Query(firstRecordTimeQuery, dbName));
                        String firstRecordTime = recordResult.getResults().get(0).getSeries().get(0).getValues().get(0).get(0).toString();

                        int offset = Integer.valueOf(firstRecordTime.substring(14, 16)) * 60 + Integer.valueOf(firstRecordTime.substring(17, 19));

                        String queryString = String.format(template, firstRecordTime, firstRecordTime, exportQuery.getPeriod(), offset);
                        logger.debug(patientId + " :\n" + queryString);

                        Query query = new Query(queryString, dbName);
                        QueryResult result = influxDB.query(query);

                        logger.debug(patientId + " :\n" + result.toString());

                        String[] row = new String[1 + intervals * groups.size()];
                        row[0] = patientId;

                        List<List<Object>> res = result.getResults().get(0).getSeries().get(0).getValues();
                        logger.debug(patientId + " : " + String.valueOf(res.size()));

                        for (int i = 0; i < intervals; i++) {
                            if (res.size() > i) {
                                List<Object> vals = res.get(i);
                                int resultSize = vals.size();
                                for (int j = 1; j <= groups.size(); j++) {
                                    if (Double.valueOf(vals.get(resultSize - 1).toString()).intValue() < 600
                                            && Double.valueOf(vals.get(resultSize - 1).toString()).intValue() > 0) {
                                        row[1 + (j - 1) * intervals + i] = "Insuff. Data";
                                    } else if (vals.get(j) == null) {
                                        row[1 + (j - 1) * intervals + i] = "N/A";
                                    } else {
                                        row[1 + (j - 1) * intervals + i] = vals.get(j).toString();
                                    }
                                }
                            } else {
                                for (int j = 1; j <= groups.size(); j++) {
                                    row[1 + (j - 1) * intervals + i] = "";
                                }
                            }
                        }
                        writer.writeNext(row);
                        writerSeparate.writeNext(row);
                        writerSeparate.close();
                    } else {
                        logger.debug(patientId + " : Not enough data, only " + Double.valueOf(monitorTime).intValue() + " seconds");
                    }
                } catch (Exception e) {
                    logger.error(patientId + " : " + Util.stackTraceErrorToString(e));
                    idQueue.offer(patientId);
                }
            }
        };

        for (int i = 0; i < paraCount; ++i) {
            scheduler.submit(queryTask);
        }
        scheduler.shutdown();
        try {
            scheduler.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
            writer.close();
        } catch (InterruptedException e) {
            logger.error(Util.stackTraceErrorToString(e));
            writer.close();
        }
    }

    /**
     * Traditional use case 1, don't improve unless JE stats
     */
    public void useCaseTwo() throws IOException {
        File dir = generateOutputDir("UC2");
        if (dir == null) return;

        // Get Patient List by uuid
        List<String> patientIDs = importedFileDao.selectAllImportedPidOnMachine(uuid);
        idQueue = new LinkedBlockingQueue<>(patientIDs);

        CSVWriter writer = new CSVWriter(new FileWriter(dir.getAbsolutePath() + "/output.csv"));
        String[] cols = new String[]{"ID", "aEEG1", "aEEG2", "aEEG3", "aEEG4", "aEEG5", "aEEG6", "aEEG7", "aEEG8", "aEEG9", "aEEG10", "aEEG11",
                "aEEG12", "aEEG13", "aEEG14", "aEEG15", "aEEG16", "aEEG17", "aEEG18", "aEEG19", "aEEG20", "aEEG21", "aEEG22", "aEEG23", "aEEG24",
                "aEEG25", "aEEG26", "aEEG27", "aEEG28", "aEEG29", "aEEG30", "aEEG31", "aEEG32", "aEEG33", "aEEG34", "aEEG35", "aEEG36", "aEEG37",
                "aEEG38", "aEEG39", "aEEG40", "aEEG41", "aEEG42", "aEEG43", "aEEG44", "aEEG45", "aEEG46", "aEEG47", "aEEG48"};
        writer.writeNext(cols);

        int paraCount = determineParaNumber();
        ExecutorService scheduler = generateNewThreadPool(paraCount);
        Runnable queryTask = () -> {
            String patientId;
            while ((patientId = idQueue.poll()) != null) {
                try {
                    String monitorTimeQuery = "select count(\"I3_1\") from \"" + patientId + "\" where arType = 'ar'";

                    QueryResult timeRes = influxDB.query(new Query(monitorTimeQuery, dbName));

                    String monitorTime = "0";
                    if (timeRes.getResults().get(0).getSeries() != null) {
                        monitorTime = timeRes.getResults().get(0).getSeries().get(0).getValues().get(0).get(1).toString();
                    }
                    if (Double.valueOf(monitorTime).intValue() >= 6 * 3600) {

                        CSVWriter writerSeparate = new CSVWriter(new FileWriter(dir.getAbsolutePath() + "/" + patientId + ".csv"));
                        writerSeparate.writeNext(cols);

                        String template = "select median(avg) as MEDIAN, count(avg) as COUNT from "
                                + "(select (\"I64_1\" + \"I64_2\" + \"I64_3\" + \"I64_4\" + \"I64_5\" + \"I65_1\" + \"I65_2\" + \"I65_3\" + "
                                + "\"I65_4\" + \"I65_5\" + \"I66_1\" + \"I66_2\" + \"I66_3\" + \"I66_4\" + \"I66_5\" + \"I67_1\" + \"I67_2\" + "
                                + "\"I67_3\" + \"I67_4\" + \"I67_5\" + \"I68_1\" + \"I68_2\" + \"I68_3\" + \"I68_4\" + \"I68_5\" + \"I69_1\" + "
                                + "\"I69_2\" + \"I69_3\" + \"I69_4\" + \"I69_5\" + \"I70_1\" + \"I70_2\" + \"I70_3\" + \"I70_4\" + \"I70_5\" + "
                                + "\"I71_1\" + \"I71_2\" + \"I71_3\" + \"I71_4\" + \"I71_5\" + \"I72_1\" + \"I72_2\" + \"I72_3\" + \"I72_4\" + "
                                + "\"I72_5\" + \"I73_1\" + \"I73_2\" + \"I73_3\" + \"I73_4\" + \"I73_5\" + \"I74_1\" + \"I74_2\" + \"I74_3\" + "
                                + "\"I74_4\" + \"I74_5\" + \"I75_1\" + \"I75_2\" + \"I75_3\" + \"I75_4\" + \"I75_5\" + \"I76_1\" + \"I76_2\" + "
                                + "\"I76_3\" + \"I76_4\" + \"I76_5\" + \"I77_1\" + \"I77_2\" + \"I77_3\" + \"I77_4\" + \"I77_5\" + \"I78_1\" + "
                                + "\"I78_2\" + \"I78_3\" + \"I78_4\" + \"I78_5\" + \"I79_1\" + \"I79_2\" + \"I79_3\" + \"I79_4\" + \"I79_5\" + "
                                + "\"I80_1\" + \"I80_2\" + \"I80_3\" + \"I80_4\" + \"I80_5\" + \"I81_1\" + \"I81_2\" + \"I81_3\" + \"I81_4\" + "
                                + "\"I81_5\") / 90 as avg from \"%s\" where arType = 'ar' LIMIT 172800) where time >= '%s' and time < '%s' + 48h "
                                + "and avg > 2 group by time(1h, %ss)";

                        String firstRecordTimeQuery = "select \"I3_1\" from \"" + patientId + "\" where arType = 'ar' limit 1";
                        QueryResult recordResult = influxDB.query(new Query(firstRecordTimeQuery, dbName));
                        String firstRecordTime = recordResult.getResults().get(0).getSeries().get(0).getValues().get(0).get(0).toString();

                        int offset = Integer.valueOf(firstRecordTime.substring(14, 16)) * 60 + Integer.valueOf(firstRecordTime.substring(17, 19));

                        String queryString = String.format(template, patientId, firstRecordTime, firstRecordTime, offset);
                        logger.debug(patientId + " :\n" + queryString);

                        Query query = new Query(queryString, dbName);
                        QueryResult result = influxDB.query(query);

                        logger.debug(patientId + " :\n" + result.toString());

                        String[] row = new String[1 + 48];
                        row[0] = patientId;

                        List<List<Object>> res = result.getResults().get(0).getSeries().get(0).getValues();
                        logger.debug(patientId + " : " + String.valueOf(res.size()));

                        for (int i = 0; i < 48; i++) {
                            if (res.size() > i) {
                                List<Object> vals = res.get(i);
                                System.out.println(vals.toString());
                                if (Double.valueOf(vals.get(2).toString()).intValue() < 60 && Double.valueOf(vals.get(2).toString()).intValue() > 0) {
                                    row[1 + i] = "Insuff. Data";
                                } else if (vals.get(1) == null) {
                                    row[1 + i] = "N/A";
                                } else {
                                    row[1 + i] = vals.get(1).toString();
                                }
                            } else {
                                row[1 + i] = "";
                            }
                        }
                        writer.writeNext(row);
                        writerSeparate.writeNext(row);
                        writerSeparate.close();
                    } else {
                        logger.debug(patientId + " : Not enough data, only " + Double.valueOf(monitorTime).intValue() + " seconds");
                    }
                } catch (Exception e) {
                    logger.error(patientId + " : " + Util.stackTraceErrorToString(e));
                    idQueue.offer(patientId);
                }
            }
        };

        for (int i = 0; i < paraCount; ++i) {
            scheduler.submit(queryTask);
        }
        scheduler.shutdown();
        try {
            scheduler.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
            writer.close();
        } catch (InterruptedException e) {
            logger.error(Util.stackTraceErrorToString(e));
            writer.close();
        }
    }

    /**
     * Traditional use case 2, don't improve unless JE stats
     */
    public void useCaseOne() throws IOException {
        File dir = generateOutputDir("UC1");
        if (dir == null) return;

        // Get Patient List by uuid
        List<String> patientIDs = importedFileDao.selectAllImportedPidOnMachine(uuid);
        idQueue = new LinkedBlockingQueue<>(patientIDs);

        System.out.println(dir.getAbsolutePath() + '/');
        CSVWriter writer = new CSVWriter(new FileWriter(dir.getAbsolutePath() + "/output.csv"));
        String[] cols = new String[]{"ID", "SR1", "SR2", "SR3", "SR4", "SR5", "SR6", "SR7", "SR8", "SR9", "SR10", "SR11", "SR12", "SR13", "SR14",
                "SR15", "SR16", "SR17", "SR18", "SR19", "SR20", "SR21", "SR22", "SR23", "SR24", "SR25", "SR26", "SR27", "SR28", "SR29", "SR30",
                "SR31", "SR32", "SR33", "SR34", "SR35", "SR36", "SR37", "SR38", "SR39", "SR40", "SR41", "SR42", "SR43", "SR44", "SR45", "SR46",
                "SR47", "SR48", "aEEG1", "aEEG2", "aEEG3", "aEEG4", "aEEG5", "aEEG6", "aEEG7", "aEEG8", "aEEG9", "aEEG10", "aEEG11", "aEEG12",
                "aEEG13", "aEEG14", "aEEG15", "aEEG16", "aEEG17", "aEEG18", "aEEG19", "aEEG20", "aEEG21", "aEEG22", "aEEG23", "aEEG24", "aEEG25",
                "aEEG26", "aEEG27", "aEEG28", "aEEG29", "aEEG30", "aEEG31", "aEEG32", "aEEG33", "aEEG34", "aEEG35", "aEEG36", "aEEG37", "aEEG38",
                "aEEG39", "aEEG40", "aEEG41", "aEEG42", "aEEG43", "aEEG44", "aEEG45", "aEEG46", "aEEG47", "aEEG48", "SZProb1", "SZProb2", "SZProb3",
                "SZProb4", "SZProb5", "SZProb6", "SZProb7", "SZProb8", "SZProb9", "SZProb10", "SZProb11", "SZProb12", "SZProb13", "SZProb14",
                "SZProb15", "SZProb16", "SZProb17", "SZProb18", "SZProb19", "SZProb20", "SZProb21", "SZProb22", "SZProb23", "SZProb24", "SZProb25",
                "SZProb26", "SZProb27", "SZProb28", "SZProb29", "SZProb30", "SZProb31", "SZProb32", "SZProb33", "SZProb34", "SZProb35", "SZProb36",
                "SZProb37", "SZProb38", "SZProb39", "SZProb40", "SZProb41", "SZProb42", "SZProb43", "SZProb44", "SZProb45", "SZProb46", "SZProb47",
                "SZProb48"};
        writer.writeNext(cols);

        int paraCount = determineParaNumber();
        ExecutorService scheduler = generateNewThreadPool(paraCount);
        Runnable queryTask = () -> {
            String patientId;
            while ((patientId = idQueue.poll()) != null) {
                try {
                    String monitorTimeQuery = "select count(\"I3_1\") from \"" + patientId + "\" where arType = 'ar'";

                    QueryResult timeRes = influxDB.query(new Query(monitorTimeQuery, dbName));

                    String monitorTime = "0";
                    if (timeRes.getResults().get(0).getSeries() != null) {
                        monitorTime = timeRes.getResults().get(0).getSeries().get(0).getValues().get(0).get(1).toString();
                    }
                    if (Double.valueOf(monitorTime).intValue() >= 6 * 3600) {

                        CSVWriter writerSeparate = new CSVWriter(new FileWriter(dir.getAbsolutePath() + "/" + patientId + ".csv"));
                        writerSeparate.writeNext(cols);

                        String template = "select median(SR) as SR, median(aEEG) as aEEG, median(SZProb) as SZProb, count(SZProb) as count from "
                                + "(select (\"I213_1\" + \"I214_1\" + \"I215_1\" + \"I216_1\" + \"I217_1\" + \"I218_1\" + \"I219_1\" + "
                                + "\"I220_1\" + \"I221_1\" + \"I222_1\" + \"I223_1\" + \"I224_1\" + \"I225_1\" + \"I226_1\" + \"I227_1\" + "
                                + "\"I228_1\" + \"I229_1\" + \"I230_1\") / 18 as SR, (\"I64_1\" + \"I64_2\" + \"I64_3\" + \"I64_4\" + \"I64_5\" + "
                                + "\"I65_1\" + \"I65_2\" + \"I65_3\" + \"I65_4\" + \"I65_5\" + \"I66_1\" + \"I66_2\" + \"I66_3\" + \"I66_4\" + "
                                + "\"I66_5\" + \"I67_1\" + \"I67_2\" + \"I67_3\" + \"I67_4\" + \"I67_5\" + \"I68_1\" + \"I68_2\" + \"I68_3\" + "
                                + "\"I68_4\" + \"I68_5\" + \"I69_1\" + \"I69_2\" + \"I69_3\" + \"I69_4\" + \"I69_5\" + \"I70_1\" + \"I70_2\" + "
                                + "\"I70_3\" + \"I70_4\" + \"I70_5\" + \"I71_1\" + \"I71_2\" + \"I71_3\" + \"I71_4\" + \"I71_5\" + \"I72_1\" + "
                                + "\"I72_2\" + \"I72_3\" + \"I72_4\" + \"I72_5\" + \"I73_1\" + \"I73_2\" + \"I73_3\" + \"I73_4\" + \"I73_5\" + "
                                + "\"I74_1\" + \"I74_2\" + \"I74_3\" + \"I74_4\" + \"I74_5\" + \"I75_1\" + \"I75_2\" + \"I75_3\" + \"I75_4\" + "
                                + "\"I75_5\" + \"I76_1\" + \"I76_2\" + \"I76_3\" + \"I76_4\" + \"I76_5\" + \"I77_1\" + \"I77_2\" + \"I77_3\" + "
                                + "\"I77_4\" + \"I77_5\" + \"I78_1\" + \"I78_2\" + \"I78_3\" + \"I78_4\" + \"I78_5\" + \"I79_1\" + \"I79_2\" + "
                                + "\"I79_3\" + \"I79_4\" + \"I79_5\" + \"I80_1\" + \"I80_2\" + \"I80_3\" + \"I80_4\" + \"I80_5\" + \"I81_1\" + "
                                + "\"I81_2\" + \"I81_3\" + \"I81_4\" + \"I81_5\") / 90 as aEEG, \"I3_1\" as SZProb from \"%s\" where arType = 'ar' LIMIT 172800) "
                                + "where time >= '%s' and time < '%s' + 48h group by time(1h, %ss)";

                        String firstRecordTimeQuery = "select \"I3_1\" from \"" + patientId + "\" where arType = 'ar' limit 1";
                        QueryResult recordResult = influxDB.query(new Query(firstRecordTimeQuery, dbName));
                        String firstRecordTime = recordResult.getResults().get(0).getSeries().get(0).getValues().get(0).get(0).toString();

                        int offset = Integer.valueOf(firstRecordTime.substring(14, 16)) * 60 + Integer.valueOf(firstRecordTime.substring(17, 19));

                        String queryString = String.format(template, patientId, firstRecordTime, firstRecordTime, offset);
                        logger.debug(patientId + " :\n" + queryString);

                        Query query = new Query(queryString, dbName);
                        QueryResult result = influxDB.query(query);

                        logger.debug(patientId + " :\n" + result.toString());

                        String[] row = new String[1 + 48 * 3];
                        row[0] = patientId;

                        List<List<Object>> res = result.getResults().get(0).getSeries().get(0).getValues();
                        logger.debug(patientId + " : " + String.valueOf(res.size()));

                        for (int i = 0; i < 48; i++) {
                            if (res.size() > i) {
                                List<Object> vals = res.get(i);
                                for (int j = 1; j <= 3; j++) {
                                    if (Double.valueOf(vals.get(4).toString()).intValue() < 600
                                            && Double.valueOf(vals.get(4).toString()).intValue() > 0) {
                                        row[1 + (j - 1) * 48 + i] = "Insuff. Data";
                                    } else if (vals.get(j) == null) {
                                        row[1 + (j - 1) * 48 + i] = "N/A";
                                    } else {
                                        row[1 + (j - 1) * 48 + i] = vals.get(j).toString();
                                    }
                                }
                            } else {
                                for (int j = 1; j <= 3; j++) {
                                    row[1 + (j - 1) * 48 + i] = "";
                                }
                            }
                        }
                        writer.writeNext(row);
                        writerSeparate.writeNext(row);
                        writerSeparate.close();
                    } else {
                        logger.debug(patientId + " : Not enough data, only " + Double.valueOf(monitorTime).intValue() + " seconds");
                    }
                } catch (Exception e) {
                    logger.error(patientId + " : " + Util.stackTraceErrorToString(e));
                    idQueue.offer(patientId);
                }
            }
        };

        for (int i = 0; i < paraCount; ++i)
            scheduler.submit(queryTask);
        scheduler.shutdown();
        try {
            scheduler.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error(Util.stackTraceErrorToString(e));
        } finally {
            writer.close();
        }
    }

    public void exportFromPatientsWithDownsampling(List<String> patients, String column, String method, String interval, String time)
            throws IOException {
        File dir = generateOutputDir("");
        if (dir == null) return;

        for (String patientId : patients) {
            String queryString = "SELECT " + method + "(\"" + column + "\")" + " FROM \"" + patientId + "\" ";
            boolean timeGiven = (interval == null || interval.isEmpty());
            // if (timeGiven) {
            String lastRecordTimeQuery = "select \"I1_1\" from \"" + patientId + "\" order by desc limit 1";
            QueryResult recordResult = influxDB.query(new Query(lastRecordTimeQuery, dbName));
            String lastRecordTime = recordResult.getResults().get(0).getSeries().get(0).getValues().get(0).get(0).toString();
            queryString += "WHERE time <= '" + lastRecordTime + "' ";
            // }
            queryString += "GROUP BY time(" + interval + "s) ";
            if (!timeGiven) {
                queryString += "LIMIT " + (Integer.valueOf(time) * 3600 / Integer.valueOf(interval));
            }
            System.out.println(queryString);
            Query query = new Query(queryString, dbName);
            QueryResult result = influxDB.query(query);
            System.out.println(result);

            System.out.println(dir.getAbsolutePath() + '/');
            CSVWriter writer = new CSVWriter(new FileWriter(dir.getAbsolutePath() + '/' + patientId + ".csv"));
            Object[] columns = result.getResults().get(0).getSeries().get(0).getColumns().toArray();
            String[] entries = Arrays.asList(columns).toArray(new String[0]);
            writer.writeNext(entries);

            List<List<Object>> res = result.getResults().get(0).getSeries().get(0).getValues();
            for (List<Object> values : res) {
                String[] vals = new String[values.size()];
                for (int i = 0; i < values.size(); i++) {
                    if (values.get(i) == null) {
                        vals[i] = "N/A";
                    } else {
                        vals[i] = values.get(i).toString();
                    }
                }
                writer.writeNext(vals);
            }
            writer.close();
        }
    }

    public int insert(Downsample downsample) throws Exception {
        return downsampleDao.insert(downsample);
    }

    public List<Downsample> selectAll() {
        return downsampleDao.selectAll();
    }

    public Downsample selectByPrimaryKey(int id) {
        return downsampleDao.selectByPrimaryKey(id);
    }

    public int insertAggregationGroup(Downsample query, DownsampleGroup group, List<DownsampleGroupColumn> columns) throws Exception {
        // TODO: Implementation of method
        for (DownsampleGroupColumn dgc : columns) {
            dgc.getClass();
        }
        return 0;
    }

    public int insertMetaFilter(Downsample query, String key, String value) throws Exception {
        // TODO: Implementation of method
        return 0;
    }

    public int updateByPrimaryKey(Downsample downsample) {
        return downsampleDao.updateByPrimaryKey(downsample);
    }

    public int deleteByPrimaryKey(int id) {
        return downsampleDao.deleteByPrimaryKey(id);
    }

    /**
     * @param queryId
     * @return
     */
    public List<DownsampleGroupVO> selectAllAggregationGroupByQueryId(Integer queryId) {
        List<DownsampleGroupVO> groups = downsampleGroupDao.selectAllDownsampleGroupVO(queryId);
        return groups;
    }

    /**
     * @param group
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean insertAggregationGroup(DownsampleGroupVO group) {
        try {
            group.getGroup().setColumns(group.getColumns());
            downsampleGroupDao.insert(group.getGroup());
            // int queryGroupId = group.getGroup().getId();
            // for (String columnName : group.getColumns().split(", ")) {
            // DownsampleGroupColumn column = new DownsampleGroupColumn();
            // column.setQueryGroupId(queryGroupId);
            // column.setColumnName(columnName);
            // downsampleGroupColumnDao.insert(column);
            // }
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
        return true;
    }

    /**
     * @param group
     * @return
     */
    public int updateAggregationGroup(DownsampleGroupVO group) {
        return downsampleGroupDao.updateByPrimaryKeyWithBLOBs(group.getGroup());
    }

    //TODO: Add some comments about this function?
    /**
     * @param pids
     * @param downsample
     * @param downsampleGroups
     * @throws IOException
     */
    public void exportFromPatientsWithDownsamplingGroups(List<String> pids, Downsample downsample, List<DownsampleGroupVO> downsampleGroups)
            throws IOException {
        File dir = generateOutputDir("");
        if (dir == null) return;

        String fields = "mean(\"I1_1\")";
        List<String> fieldList = new ArrayList<>();
        for (DownsampleGroupVO downsampleGroupVO : downsampleGroups) {
            String field = "";// downsampleGroupVO.getGroup().getAggregation();
            for (String column : downsampleGroupVO.getColumns().split(", ")) {
                field += "mean" + "(\"" + column + "\")" + "+";
            }
            field = field.substring(0, field.length() - 1) + " as \"" + downsampleGroupVO.getGroup().getAggregation() + "("
                    + String.join(", ", downsampleGroupVO.getColumns()) + ")\"";
            fieldList.add(field);
        }
        fields = String.join(", ", fieldList);

        for (String patientId : pids) {
            String queryString = "SELECT " + fields + " FROM \"" + patientId + "\" ";
            boolean timeGiven = (downsample.getPeriod() == null || downsample.getPeriod() == 0);
            // if (timeGiven) {
            String lastRecordTimeQuery = "select \"I1_1\" from \"" + patientId + "\" order by desc limit 1";
            QueryResult recordResult = influxDB.query(new Query(lastRecordTimeQuery, dbName));
            String lastRecordTime = recordResult.getResults().get(0).getSeries().get(0).getValues().get(0).get(0).toString();
            queryString += "WHERE time <= '" + lastRecordTime + "' ";
            // }
            queryString += "GROUP BY time(" + downsample.getPeriod() + "s) ";
            if (!timeGiven) {
                queryString += "LIMIT " + (Integer.valueOf(downsample.getDuration()) * 3600 / Integer.valueOf(downsample.getPeriod()));
            }
            System.out.println(queryString);
            Query query = new Query(queryString, dbName);
            QueryResult result = influxDB.query(query);
            System.out.println(result);

            System.out.println(dir.getAbsolutePath() + '/');
            CSVWriter writer = new CSVWriter(new FileWriter(dir.getAbsolutePath() + '/' + patientId + ".csv"));
            Object[] columns = result.getResults().get(0).getSeries().get(0).getColumns().toArray();
            String[] entries = Arrays.asList(columns).toArray(new String[columns.length]);
            writer.writeNext(entries);

            List<List<Object>> res = result.getResults().get(0).getSeries().get(0).getValues();
            for (List<Object> values : res) {
                String[] vals = new String[values.size()];
                for (int i = 0; i < values.size(); i++) {
                    if (values.get(i) == null) {
                        vals[i] = "N/A";
                    } else {
                        vals[i] = values.get(i).toString();
                    }
                }
                writer.writeNext(vals);
            }
            writer.close();
            logger.debug("EXPORT JOB FINISHED");
        }

    }

    public DownsampleGroupVO selectAggregationGroupByGroupId(Integer groupId) {
        return downsampleGroupDao.selectDownsampleGroupVO(groupId);
    }

    public int deleteGroupByPrimaryKey(Integer groupId) {
        return downsampleGroupDao.deleteByPrimaryKey(groupId);
    }

    /**
     * Generate an object for output directory class
     */
    private File generateOutputDir(String purpose) {
        String rfc3339 = "_(" + LocalDateTime.now().toString() + ")";
        // Workaround for Windows name restriction
        String path = DIRECTORY + purpose + rfc3339.replace(':', '.');
        File outputDir = new File(path);
        boolean dirCreationSuccess = true;

        if (!outputDir.exists()) {
            String err = "Failed to create 'Results' dir. ";
            try {
                if (!outputDir.mkdirs()) {
                    dirCreationSuccess = false;
                }
            } catch (SecurityException se) {
                err += se.getLocalizedMessage();
                dirCreationSuccess = false;
            }
            // Use a flag for flexible work flow
            if (!dirCreationSuccess) {
                logger.error(err);
                return null;
            }
            return outputDir;
        }
        // If the directory already exists (bad for us), we should consider this operation failed
        return null;
    }

    /**
     * How parallel should we go?
     */
    private int determineParaNumber() {
        int paraCount = (int) Math.round(loadFactor * InfluxappConfig.AvailableCores);
        return paraCount > 0 ? paraCount : 1;
    }

    /**
     * Generate a new threadpool (when new request comes)
     *
     * @param i Number of threads
     */
    private ExecutorService generateNewThreadPool(int i) {
        return Executors.newFixedThreadPool(i);
    }

}
