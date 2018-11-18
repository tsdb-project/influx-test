package edu.pitt.medschool.service;

import java.io.IOException;
import java.util.ArrayList;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import edu.pitt.medschool.config.DBConfiguration;
import edu.pitt.medschool.config.InfluxappConfig;
import edu.pitt.medschool.framework.util.Util;
import edu.pitt.medschool.model.dao.DownsampleDao;
import edu.pitt.medschool.model.dao.DownsampleGroupDao;
import edu.pitt.medschool.model.dao.ImportedFileDao;
import edu.pitt.medschool.model.dao.PatientDao;
import okhttp3.OkHttpClient;

@Service
public class PerformanceTest {
    static InfluxDB idb = generateIdbClient(true);

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Value("${machine}")
    private String uuid;

    private final static String dbName = DBConfiguration.Data.DBNAME;

    @Autowired
    DownsampleDao downsampleDao;
    @Autowired
    DownsampleGroupDao downsampleGroupDao;
    @Autowired
    ColumnService columnService;
    @Autowired
    PatientDao patientDao;
    @Autowired
    ImportedFileDao importedFileDao;

    private BlockingQueue<String> idQueue = new LinkedBlockingQueue<>();

    public static void columnTest(int limit) {
        StringBuffer coloums = new StringBuffer("select median(avg) from (select (");
        List<String> colList = new ArrayList<>();
        int count = 0;
        for (int j = 123; j <= 141; j++) {
            for (int i = 1; i <= 97; i++) {
                colList.add(String.format("\"I%s_%s\"", j, i));
                count++;
                if (count >= limit) {
                    break;
                }
            }
            if (count >= limit) {
                break;
            }

        }
        coloums.append(String.join(" + ", colList));
        coloums.append(
                ") / " + count + " as avg from \"PUH-2018-026\" where arType = 'ar' limit 3600) group by time(1m) fill(none)");
        long start = System.currentTimeMillis();
        idb.query(new Query(coloums.toString(), "data"));
        long end = System.currentTimeMillis();
        System.out.println(String.format("%s,%s", limit, end - start));
    }

    public static void groupTest(int limit) {
        StringBuffer coloums = new StringBuffer("select ");

        List<String> colList = new ArrayList<>();
        List<String> outterList = new ArrayList<>();
        int count = 0;
        for (int j = 123; j <= 141; j++) {
            for (int i = 1; i <= 97; i++) {
                colList.add(String.format("\"I%s_%s\" as avg%s", j, i, count));
                outterList.add("median(avg" + count + ")");
                count++;
                if (count >= limit) {
                    break;
                }
            }
            if (count >= limit) {
                break;
            }

        }
        coloums.append(String.join(", ", outterList));
        coloums.append(" from (select ");

        coloums.append(String.join(", ", colList));
        coloums.append(" from \"PUH-2018-026\" where arType = 'ar' limit 3600) group by time(1m) fill(none)");
        // System.out.println(coloums.toString());
        long start = System.currentTimeMillis();
        idb.query(new Query(coloums.toString(), "data"));
        long end = System.currentTimeMillis();
        System.out.println(String.format("%s,%s", limit, end - start));
    }

    public static void downsampleTest(int limit) {
        StringBuffer coloums = new StringBuffer("select stddev(avg) from (select (");
        List<String> colList = new ArrayList<>();
        int count = 0;
        for (int i = 1; i <= 97; i++) {
            colList.add(String.format("\"I123_%s\"", i));
            count++;
        }
        coloums.append(String.join(" + ", colList));
        coloums.append(") / " + count);
        String last = String.format(" as avg from \"PUH-2018-026\" where arType = 'ar' limit %s) group by time(%sm) fill(none)",
                3600 * limit, limit);
        coloums.append(last);

        // System.out.println(coloums.toString());

        long start = System.currentTimeMillis();
        idb.query(new Query(coloums.toString(), "data"));
        long end = System.currentTimeMillis();
        System.out.println(String.format("%s,%s", limit, end - start));
    }

    public static void downsampleTest2(int limit) {
        StringBuffer coloums = new StringBuffer("select median(avg) from (select (");
        List<String> colList = new ArrayList<>();
        int count = 0;
        for (int i = 1; i <= 97; i++) {
            colList.add(String.format("\"I123_%s\"", i));
            count++;
        }
        coloums.append(String.join(" + ", colList));
        coloums.append(") / " + count);
        String last = String.format(" as avg from \"PUH-2018-026\" where arType = 'ar' limit %s) group by time(%sm) fill(none)",
                3600 * limit, 1);
        coloums.append(last);

        // System.out.println(coloums.toString());

        long start = System.currentTimeMillis();
        idb.query(new Query(coloums.toString(), "data"));
        long end = System.currentTimeMillis();
        System.out.println(String.format("%s,%s", limit, end - start));
    }

    public static void colRowTest(int colLimit, int rowLimit) {
        StringBuffer coloums = new StringBuffer("select median(avg) from (select (");
        List<String> colList = new ArrayList<>();
        int count = 0;
        for (int j = 123; j <= 141; j++) {
            for (int i = 1; i <= 97; i++) {
                colList.add(String.format("\"I%s_%s\"", j, i));
                count++;
                if (count >= colLimit) {
                    break;
                }
            }
            if (count >= colLimit) {
                break;
            }
        }
        coloums.append(String.join(" + ", colList));
        coloums.append(") / " + count);
        String last = String.format(" as avg from \"PUH-2018-026\" where arType = 'ar' limit %s) group by time(%sm) fill(none)",
                3600 * rowLimit, 1);
        coloums.append(last);

        // System.out.println(coloums.toString());

        long start = System.currentTimeMillis();
        idb.query(new Query(coloums.toString(), "data"));
        long end = System.currentTimeMillis();
        System.out.println(String.format("%s,%s,%s", colLimit, rowLimit, end - start));
    }

    public static void colGroupRowTest(int colLimit, int rowLimit) {
        StringBuffer coloums = new StringBuffer("select ");
        List<String> colList = new ArrayList<>();
        int count = 0;
        for (int j = 123; j <= 141; j++) {
            for (int i = 1; i <= 97; i++) {
                colList.add(String.format("mean(I%s_%s)", j, i));
                count++;
                if (count >= colLimit) {
                    break;
                }
            }
            if (count >= colLimit) {
                break;
            }
        }
        coloums.append(String.join(", ", colList));
        String last = String.format(" from \"PUH-2010-087\" where arType = 'ar' group by time(%sm) fill(none) limit %s", 1,
                60 * rowLimit);
        coloums.append(last);

        System.out.println(coloums.toString());

        long start = System.currentTimeMillis();
        idb.query(new Query(coloums.toString(), "data"));
        long end = System.currentTimeMillis();
        System.out.println(
                String.format("%s,%s,%s,%s,%s", colLimit, colLimit * colLimit, rowLimit, rowLimit * rowLimit, end - start));
    }

    private ExecutorService generateNewThreadPool(int i) {
        return Executors.newFixedThreadPool(i);
    }

    public String multithreadTest(int cores) throws IOException {
//    	idb.query(new Query("drop database haha", "nothing"));
//    	idb.query(new Query("create database haha", "nothing"));
    	
    	
        // Get Patient List by uuid
        List<String> patientIDs = importedFileDao.selectAllImportedPidOnMachine(uuid);
        idQueue = new LinkedBlockingQueue<>(patientIDs);

        List<Col> list = jdbcTemplate.query(
                "SELECT f.SID, f.SID_count FROM feature f WHERE f.electrode LIKE '%Av17%' AND f.type = 'aEEG'",
                new BeanPropertyRowMapper<Col>(Col.class));
        StringBuffer sb = new StringBuffer();
        for (Col col : list) {
            String colName = col.getSID() + "_3";
            sb.append("mean(").append(colName).append(") AS ").append(colName).append(", ");
        }

        String allColumns = sb.substring(0, sb.length() - 2);

        long start = System.currentTimeMillis();

        ExecutorService scheduler = generateNewThreadPool(cores);
        Runnable queryTask = () -> {
            String patientId;
            while ((patientId = idQueue.poll()) != null) {
                try {
                    String template = "SELECT %s FROM \"haha\".\"autogen\".\"%s\" WHERE time >= '%s' and time <= '%s' "
                            + "GROUP BY time(60s, %ss) fill(none)";
                    String firstRecordTimeQuery = "select \"I3_1\" from \"" + patientId + "\" where arType = 'ar' limit 1";
                    String lastRecordTimeQuery = "select \"I3_1\" from \"" + patientId
                            + "\" where arType = 'ar' order by time desc limit 1";
                    QueryResult firstRecordResult = idb.query(new Query(firstRecordTimeQuery, dbName));
                    String firstRecordTime = firstRecordResult.getResults().get(0).getSeries().get(0).getValues().get(0).get(0)
                            .toString();
                    QueryResult lastRecordResult = idb.query(new Query(lastRecordTimeQuery, dbName));
                    String lastRecordTime = lastRecordResult.getResults().get(0).getSeries().get(0).getValues().get(0).get(0)
                            .toString();

                    int offset = Integer.valueOf(firstRecordTime.substring(17, 19));

                    String queryString = String.format(template, allColumns, patientId, firstRecordTime, lastRecordTime,
                            offset);
//                    System.out.println(queryString);

                    Query query = new Query(queryString, "haha");

                    QueryResult result = idb.query(query);
                    System.out.println(patientId + ": " + result.getResults().get(0).getSeries().get(0).getValues().size());
                } catch (Exception e) {
                	e.printStackTrace();
                    idQueue.offer(patientId);
                }
            }
        };

        for (int i = 0; i < cores; ++i) {
            scheduler.submit(queryTask);
        }
        scheduler.shutdown();
        try {
            scheduler.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
            long end = System.currentTimeMillis();
            System.out.println(String.format("%s,%s", cores, end - start));

            return "Success!";
        } catch (InterruptedException e) {
            return "Failed with:" + Util.stackTraceErrorToString(e);
        }
    }

    public static void main(String[] args) throws IOException {
        colGroupRowTest(50, 60);
        // for (int i = 3; i <= 21; i += 3) {
        // for (int j = 25; j < 3 * 97; j += 25) {
        // }
        // }
        for (int i = 1; i < InfluxappConfig.AvailableCores * 0.8; i++) {
        }

    }

    private static InfluxDB generateIdbClient(boolean needGzip) {
        InfluxDB idb = InfluxDBFactory.connect(InfluxappConfig.IFX_ADDR, InfluxappConfig.IFX_USERNAME,
                InfluxappConfig.IFX_PASSWD, new OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS)
                        .readTimeout(90, TimeUnit.MINUTES).writeTimeout(120, TimeUnit.SECONDS));
        if (needGzip) {
            idb.enableGzip();
        } else {
            idb.disableGzip();
        }
        return idb;
    }

}
