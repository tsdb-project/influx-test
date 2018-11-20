package edu.pitt.medschool.service;

import com.opencsv.CSVWriter;
import edu.pitt.medschool.config.DBConfiguration;
import edu.pitt.medschool.config.InfluxappConfig;
import edu.pitt.medschool.framework.influxdb.InfluxUtil;
import edu.pitt.medschool.framework.influxdb.ResultTable;
import edu.pitt.medschool.model.DataTimeSpanBean;
import edu.pitt.medschool.model.dao.AnalysisUtil;
import edu.pitt.medschool.model.dao.ImportedFileDao;
import okhttp3.OkHttpClient;
import org.apache.commons.io.FileUtils;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class TrajSqnTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    private final static String dbName = DBConfiguration.Data.DBNAME;

    private final ImportedFileDao importedFileDao;

    private final String interestRawCols = " ((I213_1 + I214_1 + I215_1 + I216_1 + I217_1 + I218_1 + I219_1 + I220_1 + I221_1 + I222_1 + I223_1 + I224_1 + I225_1 + I226_1 + I227_1 + I228_1 + I229_1 + I230_1) / 18) AS aggrm ";

    @Autowired
    public TrajSqnTest(ImportedFileDao importedFileDao) {
        this.importedFileDao = importedFileDao;
    }

    private final int duration = 36 * 3600;

    public void mainProcess(int cores) throws IOException {
        // 60/5*60/15*60/30*60/3600/3600*2/3600*4/3600*8   -- 3600*6/3600*12
        int period = 60;
        List<String> patientIDs = Files.readAllLines(Paths.get("E:\\MyDesktop\\plot\\good.txt"));
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
            writeNewLine(meta, "36hr\tMin: 6hr\tAggr: Mean\tDs: Mean\tperiod: " + period);
            meta.newLine();
            meta.newLine();
        } catch (IOException e) {
            logger.error("Initial failed", e);
            return;
        }
        InfluxDB idb = generateIdbClient(true);

        Instant start = Instant.now();
        for (String pid : patientIDs) {
            scheduler.submit(() -> {
                try {
                    Instant oneS = Instant.now();
                    Instant firstAvailData = Instant.MAX; // Immutable once set
                    Instant lastAvailData = Instant.MIN; // Immutable once set
                    List<DataTimeSpanBean> dtsb = AnalysisUtil.getPatientAllDataSpan(idb, logger, pid);
                    if (dtsb == null) {
                        System.err.println("NULL");
                        return;
                    }
                    for (DataTimeSpanBean d : dtsb) {
                        if (d.getArStat() == DataTimeSpanBean.ArStatus.NoArOnly) continue;
                        Instant tmpS = d.getStart(), tmpE = d.getEnd();
                        if (tmpS.compareTo(firstAvailData) < 0) firstAvailData = tmpS;
                        if (tmpE.compareTo(lastAvailData) > 0) lastAvailData = tmpE;
                    }

                    String intoTmp = "SELECT" + interestRawCols + "INTO \"sqn_tmp\".\"autogen\".\"%s_1s_mean\" " +
                            "FROM \"%s\" WHERE (arType='ar') AND time >= '%s' AND time <= '%s'";
                    String intoQs = String.format(intoTmp, pid, pid, firstAvailData, lastAvailData);

                    ResultTable[] intoRes = InfluxUtil.justQueryData(idb, true, intoQs);
                    Instant oneT = Instant.now();

                    double count = (double) intoRes[0].getDataByColAndRow(1, 0);

                    writeNewLine(meta, String.format("PID <%s>, INTO time: %s, records: %f", pid, Duration.between(oneS, oneT).toString().toLowerCase(), count));
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
            idb.close();
            meta.newLine();
            writeNewLine(meta, "Threads: " + cores);
            writeNewLine(meta, String.format("Start: %s\tEnd: %s\tDur: <%s>", start, end, Duration.between(start, end).toString().toLowerCase()));
            data.close();
            meta.close();
        } catch (Exception e) {
            logger.error("Finish failed", e);
        }
    }

    public static void main(String[] args) throws Exception {
        TrajSqnTest t = new TrajSqnTest(null);
        t.mainProcess(28);
    }

    public static synchronized void writeNewLine(BufferedWriter w, String s) throws IOException {
        w.write(s);
        w.newLine();
    }

    private int calcOffsetInSeconds(Instant fakeStartTime, Instant queryStartTime) {
        LocalDateTime fakeStart = LocalDateTime.ofInstant(fakeStartTime, ZoneOffset.UTC);
        LocalDateTime acutalStart = LocalDateTime.ofInstant(queryStartTime, ZoneOffset.UTC);
        return (acutalStart.getMinute() - fakeStart.getMinute()) * 60 +
                (acutalStart.getSecond() - fakeStart.getSecond());
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
