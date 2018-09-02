package edu.pitt.medschool.test;

import edu.pitt.medschool.config.InfluxappConfig;
import edu.pitt.medschool.framework.influxdb.ResultTable;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;

import static edu.pitt.medschool.framework.influxdb.InfluxUtil.QueryResultToKV;

/**
 * Test for InfluxDB Util
 */
public class InfluxUtilTest {

    private static void doQuery(String d) {
        Query q = new Query(d, "data");
        QueryResult qr = InfluxappConfig.INFLUX_DB.query(q);
        ResultTable[] data = QueryResultToKV(qr).toArray(new ResultTable[0]);
        System.err.println(data.length);
    }

    /**
     * Must MANUALLY CHECK THIS FIVE QUERY, ADJUST IF NECESSARY
     */
    public static void main(String... args) {
        // No results
        doQuery("select Time from \"PUH-11\" LIMIT 1");
        // Has 2 series
        doQuery("show tag values with key in (arType, fileUUID)");
        // Normal query
        doQuery("select Time from \"PUH-2010-141\" WHERE fileUUID = '9571acfa-7dd4-47d8-b19a-ac656d828b90' ORDER BY time DESC LIMIT 1");
        // Skewed values & keys
        doQuery("SELECT mean(label47) as label047 FROM (SELECT (\"I6_1\" + \"I6_3\") / 2 AS label47 FROM \"PUH-2010-014\" WHERE arType = 'ar' LIMIT 7200) where time >= '2010-10-12T15:05:34Z' and time < '2010-10-12T15:05:34Z' + 7200s group by time(300s, 334s)");
        // Two resutls (2 series + normal)
        doQuery("show tag values with key in (arType, fileUUID); select time,Time,I10_4 from \"PUH-2010-141\" WHERE fileUUID = '9571acfa-7dd4-47d8-b19a-ac656d828b90' ORDER BY time ASC LIMIT 1;");
        // Two results (2 series + empty)
        doQuery("show tag values with key in (arType, fileUUID); select Time from \"PUH-11\" LIMIT 1;");
        // Need a test for data when some fields is null
        doQuery("s");
    }

}
