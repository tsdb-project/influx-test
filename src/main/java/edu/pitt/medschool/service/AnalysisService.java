package edu.pitt.medschool.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opencsv.CSVWriter;

import edu.pitt.medschool.config.DBConfiguration;
import edu.pitt.medschool.config.InfluxappConfig;
import edu.pitt.medschool.controller.analysis.vo.DownsampleGroupVO;
import edu.pitt.medschool.framework.util.Util;
import edu.pitt.medschool.model.dao.DownsampleDao;
import edu.pitt.medschool.model.dao.DownsampleGroupAggrDao;
import edu.pitt.medschool.model.dao.DownsampleGroupColumnDao;
import edu.pitt.medschool.model.dao.DownsampleGroupDao;
import edu.pitt.medschool.model.dao.DownsampleMetaDao;
import edu.pitt.medschool.model.dao.ImportedFileDao;
import edu.pitt.medschool.model.dao.PatientDao;
import edu.pitt.medschool.model.dto.Downsample;
import edu.pitt.medschool.model.dto.DownsampleGroupColumn;
import okhttp3.OkHttpClient;

/**
 * Export functions
 */
@Service
public class AnalysisService {

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

    @Autowired
    PatientDao patientDao;
    @Autowired
    ImportedFileDao importedFileDao;

    /*
     * Be able to restrict the epochs for which data are exported (e.g. specify to export up to the first 36 hours of available data, but truncate data thereafter). Be able to specify which columns are exported (e.g.
     * I10_*, I10_2 only, all data, etc) Be able to export down sampled data (e.g. hourly mean, median, variance, etc)
     */

    private InfluxDB influxDB = InfluxDBFactory.connect(InfluxappConfig.IFX_ADDR, InfluxappConfig.IFX_USERNAME, InfluxappConfig.IFX_PASSWD,
            new OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS).readTimeout(300, TimeUnit.SECONDS).writeTimeout(120, TimeUnit.SECONDS));

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private final static String dbName = DBConfiguration.Data.DBNAME;
    private final static String DIRECTORY = "/tsdb/output/";

    private ExecutorService scheduler = Executors.newFixedThreadPool(8);

    private BlockingQueue<String> idQueue = new LinkedBlockingQueue<>();

    public void useCaseOne() throws IOException {
        File dir = new File(DIRECTORY + LocalDateTime.now().toString());
        if (!dir.exists()) {
            try {
                dir.mkdirs();
            } catch (SecurityException se) {
                System.out.println("Failed to create dir \"/results\"");
            }
        }

        List<String> patientIDs = importedFileDao.selectAllImportedPidPSC();

        // List<String> patientIDs = patientDao.selectIdAll();
        // patientIDs.retainAll(importedFileDao.getAllImportedPid("quz3"));
        idQueue = new LinkedBlockingQueue<>(patientIDs);

        System.out.println(dir.getAbsolutePath() + '/');
        CSVWriter writer = new CSVWriter(new FileWriter(dir.getAbsolutePath() + "/output.csv"));
        String[] cols = new String[] { "ID", "SR1", "SR2", "SR3", "SR4", "SR5", "SR6", "SR7", "SR8", "SR9", "SR10", "SR11", "SR12", "SR13", "SR14", "SR15", "SR16", "SR17", "SR18", "SR19", "SR20",
                "SR21", "SR22", "SR23", "SR24", "SR25", "SR26", "SR27", "SR28", "SR29", "SR30", "SR31", "SR32", "SR33", "SR34", "SR35", "SR36", "SR37", "SR38", "SR39", "SR40", "SR41", "SR42", "SR43",
                "SR44", "SR45", "SR46", "SR47", "SR48", "aEEG1", "aEEG2", "aEEG3", "aEEG4", "aEEG5", "aEEG6", "aEEG7", "aEEG8", "aEEG9", "aEEG10", "aEEG11", "aEEG12", "aEEG13", "aEEG14", "aEEG15",
                "aEEG16", "aEEG17", "aEEG18", "aEEG19", "aEEG20", "aEEG21", "aEEG22", "aEEG23", "aEEG24", "aEEG25", "aEEG26", "aEEG27", "aEEG28", "aEEG29", "aEEG30", "aEEG31", "aEEG32", "aEEG33",
                "aEEG34", "aEEG35", "aEEG36", "aEEG37", "aEEG38", "aEEG39", "aEEG40", "aEEG41", "aEEG42", "aEEG43", "aEEG44", "aEEG45", "aEEG46", "aEEG47", "aEEG48", "SZProb1", "SZProb2", "SZProb3",
                "SZProb4", "SZProb5", "SZProb6", "SZProb7", "SZProb8", "SZProb9", "SZProb10", "SZProb11", "SZProb12", "SZProb13", "SZProb14", "SZProb15", "SZProb16", "SZProb17", "SZProb18",
                "SZProb19", "SZProb20", "SZProb21", "SZProb22", "SZProb23", "SZProb24", "SZProb25", "SZProb26", "SZProb27", "SZProb28", "SZProb29", "SZProb30", "SZProb31", "SZProb32", "SZProb33",
                "SZProb34", "SZProb35", "SZProb36", "SZProb37", "SZProb38", "SZProb39", "SZProb40", "SZProb41", "SZProb42", "SZProb43", "SZProb44", "SZProb45", "SZProb46", "SZProb47", "SZProb48" };
        writer.writeNext(cols);
        Runnable queryTask = () -> {
            String patientId;
            while ((patientId = idQueue.poll()) != null) {
                try {
                    String monitorTimeQuery = "select count(\"I1_1\") from \"" + patientId + "\" where arType = 'ar'";

                    QueryResult timeRes = influxDB.query(new Query(monitorTimeQuery, dbName));
                    String monitorTime = timeRes.getResults().get(0).getSeries().get(0).getValues().get(0).get(1).toString();
                    if (Double.valueOf(monitorTime) > 6 * 3600) {

                        CSVWriter writerSeparate = new CSVWriter(new FileWriter(dir.getAbsolutePath() + "/" + patientId + ".csv"));
                        writerSeparate.writeNext(cols);

                        String template = "select median(SR) as SR, median(aEEG) as aEEG, median(SZProb) as SZProb from (select (\"I194_1\" + \"I195_1\" + \"I196_1\" + \"I197_1\" + \"I198_1\" + \"I199_1\" + \"I200_1\" + \"I201_1\" + \"I202_1\" + \"I203_1\" + \"I204_1\" + \"I205_1\" + \"I206_1\" + \"I207_1\" + \"I208_1\" + \"I209_1\" + \"I210_1\" + \"I211_1\" + \"I212_1\" + \"I213_1\" + \"I214_1\" + \"I215_1\" + \"I216_1\" + \"I217_1\" + \"I218_1\" + \"I219_1\" + \"I220_1\" + \"I221_1\" + \"I222_1\" + \"I223_1\" + \"I224_1\" + \"I225_1\" + \"I226_1\" + \"I227_1\" + \"I228_1\" + \"I229_1\" + \"I230_1\") / 37 as SR, (\"I43_1\" + \"I43_2\" + \"I43_3\" + \"I43_4\" + \"I43_5\" + \"I44_1\" + \"I44_2\" + \"I44_3\" + \"I44_4\" + \"I44_5\" + \"I45_1\" + \"I45_2\" + \"I45_3\" + \"I45_4\" + \"I45_5\" + \"I46_1\" + \"I46_2\" + \"I46_3\" + \"I46_4\" + \"I46_5\" + \"I47_1\" + \"I47_2\" + \"I47_3\" + \"I47_4\" + \"I47_5\" + \"I48_1\" + \"I48_2\" + \"I48_3\" + \"I48_4\" + \"I48_5\" + \"I49_1\" + \"I49_2\" + \"I49_3\" + \"I49_4\" + \"I49_5\" + \"I50_1\" + \"I50_2\" + \"I50_3\" + \"I50_4\" + \"I50_5\" + \"I51_1\" + \"I51_2\" + \"I51_3\" + \"I51_4\" + \"I51_5\" + \"I52_1\" + \"I52_2\" + \"I52_3\" + \"I52_4\" + \"I52_5\" + \"I53_1\" + \"I53_2\" + \"I53_3\" + \"I53_4\" + \"I53_5\" + \"I54_1\" + \"I54_2\" + \"I54_3\" + \"I54_4\" + \"I54_5\" + \"I55_1\" + \"I55_2\" + \"I55_3\" + \"I55_4\" + \"I55_5\" + \"I56_1\" + \"I56_2\" + \"I56_3\" + \"I56_4\" + \"I56_5\" + \"I57_1\" + \"I57_2\" + \"I57_3\" + \"I57_4\" + \"I57_5\" + \"I58_1\" + \"I58_2\" + \"I58_3\" + \"I58_4\" + \"I58_5\" + \"I59_1\" + \"I59_2\" + \"I59_3\" + \"I59_4\" + \"I59_5\" + \"I60_1\" + \"I60_2\" + \"I60_3\" + \"I60_4\" + \"I60_5\" + \"I61_1\" + \"I61_2\" + \"I61_3\" + \"I61_4\" + \"I61_5\" + \"I62_1\" + \"I62_2\" + \"I62_3\" + \"I62_4\" + \"I62_5\" + \"I63_1\" + \"I63_2\" + \"I63_3\" + \"I63_4\" + \"I63_5\" + \"I64_1\" + \"I64_2\" + \"I64_3\" + \"I64_4\" + \"I64_5\" + \"I65_1\" + \"I65_2\" + \"I65_3\" + \"I65_4\" + \"I65_5\" + \"I66_1\" + \"I66_2\" + \"I66_3\" + \"I66_4\" + \"I66_5\" + \"I67_1\" + \"I67_2\" + \"I67_3\" + \"I67_4\" + \"I67_5\" + \"I68_1\" + \"I68_2\" + \"I68_3\" + \"I68_4\" + \"I68_5\" + \"I69_1\" + \"I69_2\" + \"I69_3\" + \"I69_4\" + \"I69_5\" + \"I70_1\" + \"I70_2\" + \"I70_3\" + \"I70_4\" + \"I70_5\" + \"I71_1\" + \"I71_2\" + \"I71_3\" + \"I71_4\" + \"I71_5\" + \"I72_1\" + \"I72_2\" + \"I72_3\" + \"I72_4\" + \"I72_5\" + \"I73_1\" + \"I73_2\" + \"I73_3\" + \"I73_4\" + \"I73_5\" + \"I74_1\" + \"I74_2\" + \"I74_3\" + \"I74_4\" + \"I74_5\" + \"I75_1\" + \"I75_2\" + \"I75_3\" + \"I75_4\" + \"I75_5\" + \"I76_1\" + \"I76_2\" + \"I76_3\" + \"I76_4\" + \"I76_5\" + \"I77_1\" + \"I77_2\" + \"I77_3\" + \"I77_4\" + \"I77_5\" + \"I78_1\" + \"I78_2\" + \"I78_3\" + \"I78_4\" + \"I78_5\" + \"I79_1\" + \"I79_2\" + \"I79_3\" + \"I79_4\" + \"I79_5\" + \"I80_1\" + \"I80_2\" + \"I80_3\" + \"I80_4\" + \"I80_5\" + \"I81_1\" + \"I81_2\" + \"I81_3\" + \"I81_4\" + \"I81_5\") / 195 as aEEG, \"I3_1\" as SZProb from \"%s\" where arType = 'ar' LIMIT 172800) group by time(1h, %ss) fill(none)";

                        String firstRecordTimeQuery = "select \"I1_1\" from \"" + patientId + "\" limit 1";
                        QueryResult recordResult = influxDB.query(new Query(firstRecordTimeQuery, dbName));
                        String firstRecordTime = recordResult.getResults().get(0).getSeries().get(0).getValues().get(0).get(0).toString();

                        int offset = Integer.valueOf(firstRecordTime.substring(14, 16)) * 60 + Integer.valueOf(firstRecordTime.substring(17, 19));

                        String queryString = String.format(template, patientId, offset);
                        logger.debug(patientId);
                        logger.debug(queryString);

                        Query query = new Query(queryString, dbName);
                        QueryResult result = influxDB.query(query);

                        logger.debug(result.toString());

                        String[] row = new String[1 + 48 * 3];
                        row[0] = patientId;

                        List<List<Object>> res = result.getResults().get(0).getSeries().get(0).getValues();
                        logger.debug(String.valueOf(res.size()));

                        for (int i = 0; i < 48; i++) {
                            if (res.size() > i) {
                                List<Object> vals = res.get(i);
                                for (int j = 1; j <= 3; j++) {
                                    if (vals.get(j) == null) {
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
                        logger.debug(patientId + " : Not enough data, only " + monitorTime + " seconds");
                    }
                } catch (Exception e) {
                    logger.error(patientId);
                    logger.error(Util.stackTraceErrorToString(e));
                }
            }
        };

        for (int i = 0; i < 8; ++i)
            scheduler.submit(queryTask);
        scheduler.shutdown();
        try {
            scheduler.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
            writer.close();
        } catch (InterruptedException e) {
            logger.error(Util.stackTraceErrorToString(e));
            writer.close();
        }
    }

    public void exportFromPatientsWithDownsampling(List<String> patients, String column, String method, String interval, String time) throws IOException {
        File dir = new File(DIRECTORY + LocalDateTime.now().toString());
        if (!dir.exists()) {
            try {
                dir.mkdirs();
            } catch (SecurityException se) {
                System.out.println("Failed to create dir \"/results\"");
            }
        }

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

    // public int insertAggregationGroup(Downsample query, DownsampleGroup group, List<DownsampleGroupColumn> columns) throws Exception {
    // // TODO: Implementation of method
    // for (DownsampleGroupColumn dgc : columns) {
    //
    // }
    // return 0;
    // }

    public int insertMetaFilter(Downsample query, String key, String value) throws Exception {
        // TODO: Implementation of method
        return 0;
    }

    public static void main(String[] args) {

    }

    public int updateByPrimaryKey(Downsample downsample) {
        return downsampleDao.updateByPrimaryKey(downsample);
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
            downsampleGroupDao.insert(group.getGroup());
            int queryGroupId = group.getGroup().getId();
            for (String columnName : group.getColumns()) {
                DownsampleGroupColumn column = new DownsampleGroupColumn();
                column.setQueryGroupId(queryGroupId);
                column.setColumnName(columnName);
                downsampleGroupColumnDao.insert(column);
            }
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
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * @param pids
     * @param downsample
     * @param downsampleGroups
     * @throws IOException
     */
    public void exportFromPatientsWithDownsamplingGroups(List<String> pids, Downsample downsample, List<DownsampleGroupVO> downsampleGroups) throws IOException {
        File dir = new File(DIRECTORY + LocalDateTime.now().toString());
        if (!dir.exists()) {
            try {
                dir.mkdirs();
            } catch (SecurityException se) {
                System.out.println("Failed to create dir \"/results\"");
            }
        }

        String fields = "mean(\"I1_1\")";
        List<String> fieldList = new ArrayList<>();
        for (DownsampleGroupVO downsampleGroupVO : downsampleGroups) {
            String field = "";// downsampleGroupVO.getGroup().getAggregation();
            for (String column : downsampleGroupVO.getColumns()) {
                field += "mean" + "(\"" + column + "\")" + "+";
            }
            field = field.substring(0, field.length() - 1) + " as \"" + downsampleGroupVO.getGroup().getAggregation() + "(" + String.join(", ", downsampleGroupVO.getColumns()) + ")\"";
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
        }

    }

}
