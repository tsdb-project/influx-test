package edu.pitt.medschool.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.dto.QueryResult.Result;
import org.influxdb.dto.QueryResult.Series;

import edu.pitt.medschool.config.InfluxappConfig;
import edu.pitt.medschool.framework.util.TimeUtil;
import okhttp3.OkHttpClient;

public class IntegrityThreeTest {
    static InfluxDB idb = generateIdbClient(true);
    static File file = new File("/tsdb/dbtime.csv");

    public static void checkPatientFiles(String pid) {
        boolean longQuery = false;
        String queryString = String.format("SELECT \"I123_1\" FROM \"%s\" GROUP BY * LIMIT 1;", pid, pid);
        if (longQuery) {
            queryString += String.format("SELECT \"I123_1\" FROM \"%s\" GROUP BY * ORDER BY time DESC LIMIT 1;", pid);
        }

        Query query = new Query(queryString, "data");
        QueryResult result = idb.query(query);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        formatter.setTimeZone(TimeUtil.nycTimeZone);

        if (result.getResults().get(0).getSeries() != null) {
            for (Result e : result.getResults()) {
                for (Series s : e.getSeries()) {
                    for (List<Object> o : s.getValues()) {

                        Instant header = Instant.parse(o.get(0).toString());

                        String headerTime = formatter.format(Date.from(header));

                        System.out.println(headerTime + "," + s.getTags().get("fileName"));
                    }
                }
            }

        } else {
            System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%");
            System.out.println("nothing");
            System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%");
        }

    }

    public static void main(String[] args) throws IOException {

        FileWriter writer = new FileWriter(file);
        writer.write("first,name\n");

        Query pidListQuery = new Query("Show measurements", "data");
        QueryResult resultL = idb.query(pidListQuery);
        for (List<Object> PID : resultL.getResults().get(0).getSeries().get(0).getValues()) {
            String pid = PID.get(0).toString();

            boolean longQuery = false;
            String queryString = String.format("SELECT \"I123_1\" FROM \"%s\" GROUP BY * LIMIT 1;", pid, pid);
            if (longQuery) {
                queryString += String.format("SELECT \"I123_1\" FROM \"%s\" GROUP BY * ORDER BY time DESC LIMIT 1;", pid);
            }

            Query query = new Query(queryString, "data");
            QueryResult result = idb.query(query);

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
            formatter.setTimeZone(TimeUtil.nycTimeZone);

            if (result.getResults().get(0).getSeries() != null) {
                System.out.println(pid + ": finished");
                for (Result e : result.getResults()) {
                    for (Series s : e.getSeries()) {
                        for (List<Object> o : s.getValues()) {

                            Instant header = Instant.parse(o.get(0).toString());

                            String headerTime = formatter.format(Date.from(header));
                            writer.write(headerTime + "," + s.getTags().get("fileName") + "\n");
                        }
                    }
                }

            } else {
                System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%");
                System.out.println(pid + ": nothing");
                System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%");
            }

        }
        writer.close();
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
