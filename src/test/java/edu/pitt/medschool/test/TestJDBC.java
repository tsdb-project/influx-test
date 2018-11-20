package edu.pitt.medschool.test;

import edu.pitt.medschool.config.InfluxappConfig;
import edu.pitt.medschool.model.DataTimeSpanBean;
import edu.pitt.medschool.model.dao.AnalysisUtil;
import edu.pitt.medschool.model.dao.ImportedFileDao;
import edu.pitt.medschool.model.dao.PatientDao;
import okhttp3.OkHttpClient;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Test JDBC related works
 */
@SpringBootApplication(scanBasePackages = {"edu.pitt.medschool"})
public class TestJDBC implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(TestJDBC.class);

    public static void main(String[] args) {
        SpringApplication.run(TestJDBC.class, args);
    }

    @Value("${machine}")
    private String uuid;

    @Autowired
    PatientDao pd;

    @Autowired
    ImportedFileDao ifd;

    @Override
    public void run(String... args) throws Exception {
        List<String> patientIDs = ifd.selectAllImportedPidOnMachine("realpsc");
        BufferedWriter
                b1 = Files.newBufferedWriter(Paths.get("bad.txt")),
                b2 = Files.newBufferedWriter(Paths.get("good.txt"));

        ExecutorService s = Executors.newFixedThreadPool(28);
        InfluxDB idb = generateIdbClient(true);

        Instant st = Instant.now();
        for (String pid : patientIDs) {
            s.submit(() -> {
                try {
                    List<DataTimeSpanBean> dtsb = AnalysisUtil.getPatientAllDataSpan(idb, logger, pid);
                    if (dtsb == null) throw new IllegalArgumentException(pid + " data not good");

                    long totalLen = 0;
                    for (DataTimeSpanBean d : dtsb) {
                        // Only Need AR
                        if (d.getArStat() == DataTimeSpanBean.ArStatus.NoArOnly) continue;
                        totalLen += d.getEffectiveDataCount();
                    }
                    // At least 6 hr of data (1s one dp)
                    if (totalLen < 6 * 3600) {
                        writeNewLine(b1, pid);
                    } else {
                        writeNewLine(b2, pid);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        s.shutdown();
        s.awaitTermination(20, TimeUnit.HOURS);
        System.err.println(Duration.between(st, Instant.now()).toString().toLowerCase());
        b1.close();
        b2.close();
        idb.close();
        System.exit(0);
    }

    public static synchronized void writeNewLine(BufferedWriter w, String s) throws IOException {
        w.write(s);
        w.newLine();
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
