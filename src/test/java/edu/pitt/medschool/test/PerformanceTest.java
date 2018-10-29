package edu.pitt.medschool.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;

import edu.pitt.medschool.config.InfluxappConfig;
import okhttp3.OkHttpClient;

public class PerformanceTest {
    static InfluxDB idb = generateIdbClient(true);

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

    public static void main(String[] args) {
        for (int i = 5; i <= 75; i += 10) {
            for (int j = 50; j < 19 * 97; j += 100) {
                colRowTest(j, i);
            }
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
