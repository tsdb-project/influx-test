package edu.pitt.medschool.model;

import static edu.pitt.medschool.framework.influxdb.InfluxUtil.queryResultToKV;
import static edu.pitt.medschool.framework.influxdb.InfluxUtil.queryResultToTable;

import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.junit.Ignore;
import org.junit.Test;

import edu.pitt.medschool.framework.influxdb.InfluxUtil;
import edu.pitt.medschool.framework.influxdb.ResultTable;

/**
 * Test for InfluxDB Util
 */
public class InfluxUtilTest {

    private void doQuery(String d, String db) {
        Query q = new Query(d, db);
        QueryResult qr = InfluxUtil.generateIdbClient(true).query(q);
        ResultTable[] data;
        data = queryResultToKV(qr).toArray(new ResultTable[0]);
        data = queryResultToTable(qr).toArray(new ResultTable[0]);
        System.err.println(data.length);
    }

    /**
     * Must MANUALLY CHECK THIS FIVE QUERY, ADJUST IF NECESSARY
     */
    @Test
    @Ignore
    public void main() {
        // No results
        doQuery("select Time from \"PUH-11\" LIMIT 1", "data");
        // Has 2 series
        doQuery("show tag values with key in (arType, fileUUID)", "data");
        // Normal query
        doQuery("select Time from \"PUH-2010-141\" WHERE fileUUID = '9571acfa-7dd4-47d8-b19a-ac656d828b90' ORDER BY time DESC LIMIT 1",
                "data");
        // Skewed values & keys
        doQuery("SELECT mean(label47) as label047 FROM (SELECT (\"I6_1\" + \"I6_3\") / 2 AS label47 FROM \"PUH-2010-014\" WHERE arType = 'ar' LIMIT 7200) where time >= '2010-10-12T15:05:34Z' and time < '2010-10-12T15:05:34Z' + 7200s group by time(300s, 334s)",
                "data");
        // Two resutls (2 series + normal)
        doQuery("show tag values with key in (arType, fileUUID); select time,Time,I10_4 from \"PUH-2010-141\" WHERE fileUUID = '9571acfa-7dd4-47d8-b19a-ac656d828b90' ORDER BY time ASC LIMIT 1;",
                "data");
        // Two results (2 series + empty)
        doQuery("show tag values with key in (arType, fileUUID); select Time from \"PUH-11\" LIMIT 1;", "data");
        // Some fields is null (https://github.com/influxdata/influxdb/issues/8867)
        doQuery("select \"f1\", \"f2\", \"f3\", 1.5*f1+2*f2-f3+100 as tot from \"m1\" where time < 1504609384000000000 and time > 1504609184000000000 fill(null)",
                "test_fill");
    }

}
