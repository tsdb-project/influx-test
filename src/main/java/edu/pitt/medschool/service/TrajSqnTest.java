package edu.pitt.medschool.service;

import com.opencsv.CSVWriter;
import edu.pitt.medschool.config.DBConfiguration;
import edu.pitt.medschool.config.InfluxappConfig;
import edu.pitt.medschool.model.dao.*;
import okhttp3.OkHttpClient;
import org.apache.commons.io.FileUtils;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class TrajSqnTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    private final static String dbName = DBConfiguration.Data.DBNAME;
    private final static String intName = "sqn"; // Smart query navigator

    private final DownsampleDao downsampleDao;
    private final DownsampleGroupDao downsampleGroupDao;
    private final ColumnService columnService;
    private final PatientDao patientDao;
    private final ImportedFileDao importedFileDao;

    private final String interestRawCols = " ((I213_1 + I214_1 + I215_1 + I216_1 + I217_1 + I218_1 + I219_1 + I220_1 + I221_1 + I222_1 + I223_1 + I224_1 + I225_1 + I226_1 + I227_1 + I228_1 + I229_1 + I230_1) / 18) AS aggr ";

    @Autowired
    public TrajSqnTest(DownsampleDao downsampleDao, DownsampleGroupDao downsampleGroupDao, ColumnService columnService, PatientDao patientDao, ImportedFileDao importedFileDao) {
        this.downsampleDao = downsampleDao;
        this.downsampleGroupDao = downsampleGroupDao;
        this.columnService = columnService;
        this.patientDao = patientDao;
        this.importedFileDao = importedFileDao;
    }

    private final int duration = 36 * 3600;

    public void mainProcess(int cores) {
        // 60/5*60/15*60/30*60/3600/3600*2/3600*4/3600*8   -- 3600*6/3600*12
        int interval = 60;
        List<String> patientIDs = importedFileDao.selectAllImportedPidOnMachine("realpsc");
        ExecutorService scheduler = Executors.newFixedThreadPool(cores > 0 ? cores : 1);
        String dataPath = InfluxappConfig.OUTPUT_DIRECTORY + "special_trj/A/data_60.csv";
        String metaPath = InfluxappConfig.OUTPUT_DIRECTORY + "special_trj/A/meta_60.txt";
        BufferedWriter meta;
        CSVWriter data;
        try {
            FileUtils.forceMkdirParent(new File(dataPath));
            data = new CSVWriter(new BufferedWriter(new FileWriter(dataPath)));
            meta = new BufferedWriter(new FileWriter(metaPath));
            String[] header;
            //data.writeNext();
            meta.write("36hr\tMin: 6hr\tAggr: Mean\tDs: Mean\tinterval: " + interval);
            meta.newLine();
        } catch (IOException e) {
            logger.error("Initial failed", e);
            return;
        }

        Instant start = Instant.now();
        for (String pid : patientIDs) {
            scheduler.submit(() -> {
                InfluxDB idb = generateIdbClient(true);
                try {
                    String template = "SELECT %s FROM \"haha\".\"autogen\".\"%s\" WHERE time >= '%s' and time <= '%s' "
                            + "GROUP BY time(60s, %ss) fill(none)";
                    String firstRecordTimeQuery = "select \"I3_1\" from \"" + pid + "\" where arType = 'ar' limit 1";
                    String lastRecordTimeQuery = "select \"I3_1\" from \"" + pid
                            + "\" where arType = 'ar' order by time desc limit 1";
                    QueryResult firstRecordResult = idb.query(new Query(firstRecordTimeQuery, dbName));
                    String firstRecordTime = firstRecordResult.getResults().get(0).getSeries().get(0).getValues().get(0).get(0)
                            .toString();
                    QueryResult lastRecordResult = idb.query(new Query(lastRecordTimeQuery, dbName));
                    String lastRecordTime = lastRecordResult.getResults().get(0).getSeries().get(0).getValues().get(0).get(0)
                            .toString();

                    int offset = Integer.valueOf(firstRecordTime.substring(17, 19));

                    String queryString = String.format(template, interestRawCols, pid, firstRecordTime, lastRecordTime,
                            offset);

                    Query query = new Query(queryString, "haha");

                    QueryResult result = idb.query(query);
                    System.out.println(pid + ": " + result.getResults().get(0).getSeries().get(0).getValues().size());
                } catch (Exception e) {
                    logger.error("Process <{}> error", pid);
                    logger.error("Reason", e);
                }
            });
        }
        scheduler.shutdown();

        try {
            scheduler.awaitTermination(48, TimeUnit.HOURS);
            Instant end = Instant.now();
            meta.newLine();
            meta.write("Threads: " + cores);
            meta.newLine();
            meta.write(String.format("Start: %s\tEnd: %s\tDur: <%s>", start, end, Duration.between(start, end)).toLowerCase());
            data.close();
            meta.close();
        } catch (Exception e) {
            logger.error("Finish failed", e);
        }
    }

    public static void main(String[] args) throws Exception {
        TrajSqnTest t = new TrajSqnTest(null, null, null, null, null);

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
