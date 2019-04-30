package edu.pitt.medschool.test;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.dto.QueryResult.Result;
import org.influxdb.dto.QueryResult.Series;

import edu.pitt.medschool.config.InfluxappConfig;
import okhttp3.OkHttpClient;

public class DataDeletionTest {
    static InfluxDB idb = generateIdbClient(true);
    static String[] completeDelete = new String[] { "PUH-2012-052", "PUH-2010-014", "PUH-2010-014", "PUH-2010-117",
            "PUH-2010-129", "PUH-2010-155", "PUH-2010-155", "PUH-2010-172", "PUH-2011-119", "PUH-2011-205", "PUH-2012-005",
            "PUH-2012-005", "PUH-2012-082", "PUH-2012-082", "PUH-2012-157", "PUH-2012-211", "PUH-2013-116", "PUH-2013-198",
            "PUH-2014-038", "PUH-2014-271", "PUH-2014-271", "PUH-2015-208", "PUH-2015-278", "PUH-2016-147", "PUH-2017-071",
            "PUH-2017-199", "PUH-2017-199", "PUH-2018-008" };
    // static String[] completeDelete2 = new String[] { "PUH-2015-198" };
    static String[] completeDelete2 = new String[] { "PUH-2017-123" };

    // private static final Logger logger = LoggerFactory.getLogger(DataDeletionTest.class);

    public static void checkPatientFiles(String pid) {
        boolean longQuery = true;

        String queryString = String.format("SELECT \"I123_1\" FROM \"%s\" GROUP BY * LIMIT 1;", pid, pid);
        if (longQuery) {
            queryString += String.format("SELECT \"I123_1\" FROM \"%s\" GROUP BY * ORDER BY time DESC LIMIT 1;", pid);
        }

        System.out.println("============================");
        System.out.println(queryString);
        System.out.println("============================");

        Query query = new Query(queryString, "data");
        QueryResult result = idb.query(query);

        if (result.getResults().get(0).getSeries() != null) {
            for (Result e : result.getResults()) {
                for (Series s : e.getSeries()) {
                    for (List<Object> o : s.getValues()) {
                        System.out
                                .println(o.get(0) + " : " + s.getTags().get("fileUUID") + " : " + s.getTags().get("fileName"));
                    }
                }
            }
            System.out.println();
            for (Result e : result.getResults()) {
                for (Series s : e.getSeries()) {
                    System.out.println(String.format("DROP SERIES FROM \"%s\" WHERE \"fileUUID\" = '%s'", pid,
                            s.getTags().get("fileUUID")));
                }
            }

        } else {
            System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%");
            System.out.println("nothing");
            System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%");
        }

    }

    public static void main(String[] args) {
        for (int i = 0; i < completeDelete2.length; i++) {
            String string = completeDelete2[i];
            checkPatientFiles(string);
        }
        // AnalysisUtil.getPatientAllDataSpan(idb, logger, "PUH-2014-096");
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
