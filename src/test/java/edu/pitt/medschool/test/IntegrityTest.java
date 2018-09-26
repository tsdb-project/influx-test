/**
 *
 */
package edu.pitt.medschool.test;

import edu.pitt.medschool.config.InfluxappConfig;
import edu.pitt.medschool.framework.influxdb.InfluxUtil;
import okhttp3.OkHttpClient;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.dto.QueryResult.Series;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Isolachine
 */
public class IntegrityTest {

    static void lineCount() {
        //TODO: Proper handle
        InfluxDB idb = InfluxDBFactory.connect(InfluxappConfig.IFX_ADDR_LOCAL, InfluxappConfig.IFX_USERNAME, InfluxappConfig.IFX_PASSWD,
                new OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS).readTimeout(120, TimeUnit.SECONDS).writeTimeout(120, TimeUnit.SECONDS));
        Query query = new Query("show measurements", "data");
        QueryResult result = idb.query(query);

        for (List<Object> id : result.getResults().get(0).getSeries().get(0).getValues()) {
            String iid = (String) id.get(0);
            if (iid.startsWith("PUH-2015") || iid.startsWith("PUH-2016") || iid.startsWith("PUH-2017") || iid.startsWith("PUH-2018")) {
                Query numq = new Query("select count(\"I1_1\") from \"" + iid + "\"", "data");
                Double count = (Double) idb.query(numq).getResults().get(0).getSeries().get(0).getValues().get(0).get(1);
                System.out.println(iid + " : " + count.intValue());
            }
        }
    }

    static void startAndEnd() {
        //TODO: Proper handle
        InfluxDB idb = InfluxDBFactory.connect(InfluxappConfig.IFX_ADDR_LOCAL, InfluxappConfig.IFX_USERNAME, InfluxappConfig.IFX_PASSWD,
                new OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS).readTimeout(120, TimeUnit.SECONDS).writeTimeout(120, TimeUnit.SECONDS));
        for (int i = 0; i < 9; i++) {
            Query start = new Query("select \"I3_1\" from /PUH-201" + i + "/ limit 1 tz('America/New_York')", "data");
            Query end = new Query("select \"I3_1\" from /PUH-201" + i + "/ order by time desc limit 1 tz('America/New_York')", "data");
            System.out.println(start.getCommand());
            System.out.println(end.getCommand());
            QueryResult resultStart = idb.query(start);
            QueryResult resultEnd = idb.query(end);

            Map<String, String[]> map = new HashMap<>();

            for (Series series : resultStart.getResults().get(0).getSeries()) {
                String pid = series.getName();
                String[] times = new String[2];
                times[0] = series.getValues().get(0).get(0).toString();
                map.put(pid, times);
            }

            for (Series series : resultEnd.getResults().get(0).getSeries()) {
                String pid = series.getName();
                String[] times = map.get(pid);
                times[1] = series.getValues().get(0).get(0).toString();
                map.put(pid, times);
            }

            for (String key : map.keySet()) {
                System.out.println(key + ',' + map.get(key)[0] + ',' + map.get(key)[1]);
            }

        }
    }

    static void problemFour() throws IOException {
        FileWriter fw = null;
        try {
            fw = new FileWriter("/tsdb/gap.csv");
            fw.write("PID,time,record count\n");
            //TODO: Proper handle
            InfluxDB influxDB = InfluxDBFactory.connect(InfluxappConfig.IFX_ADDR_LOCAL, InfluxappConfig.IFX_USERNAME, InfluxappConfig.IFX_PASSWD,
                    new OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS).readTimeout(120, TimeUnit.SECONDS).writeTimeout(120, TimeUnit.SECONDS));
            List<String> patientIds = InfluxUtil.getAllTables(influxDB, "data");
            for (String patientId : patientIds) {
                Query start = new Query("select \"I3_1\" from \"" + patientId + "\" limit 1", "data");
                Query end = new Query("select \"I3_1\" from \"" + patientId + "\" order by time desc limit 1", "data");

                String startTime = influxDB.query(start).getResults().get(0).getSeries().get(0).getValues().get(0).get(0).toString();
                String endTime = influxDB.query(end).getResults().get(0).getSeries().get(0).getValues().get(0).get(0).toString();

                int offset = Integer.valueOf(startTime.substring(11, 13)) * 3600 + Integer.valueOf(startTime.substring(14, 16)) * 60 + Integer.valueOf(startTime.substring(17, 19));

                String template = "select count(\"I3_1\") from \"%s\" where time <= '%s' and time >= '%s' group by time(1d,%ss)";

                String queryString = String.format(template, patientId, endTime, startTime, offset);

                Query query = new Query(queryString, "data");
                QueryResult result = influxDB.query(query);

                List<List<Object>> res = result.getResults().get(0).getSeries().get(0).getValues();

                boolean gap = false;
                for (List<Object> list : res) {
                    if (Double.valueOf(list.get(1).toString()).intValue() == 0) {
                        gap = true;
                    }
                }
                if (gap) {
                    System.out.print(patientId);
                    fw.write(patientId);
                    for (List<Object> list : res) {
                        System.out.println(',' + list.get(0).toString() + ',' + Double.valueOf(list.get(1).toString()).intValue());
                        fw.write(',' + list.get(0).toString() + ',' + Double.valueOf(list.get(1).toString()).intValue() + "\n");
                    }
                    System.out.println("");
                    fw.write("\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fw.close();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }

    }

    public static void main(String[] args) throws IOException {
        problemFour();
    }

}
