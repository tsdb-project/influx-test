package app.util;

import app.common.DBConfiguration;
import app.common.InfluxappConfig;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utilities for InfluxDB Java Client
 */
public class InfluxUtil {

    /**
     * Reshape InfluxDB Java Client result to a "Dictionary"
     *
     * @param results Influx Query result
     * @return KV-Map (Size = 0 if no result)
     */
    public static Map<String, List<Object>> QueryResultToKV(QueryResult results) {
        if (results.hasError()) throw new RuntimeException(results.getError());

        List<QueryResult.Series> resSer = results.getResults().get(0).getSeries();
        // No results
        if (resSer == null) return new HashMap<>(0);

        List<String> columnsData = resSer.get(0).getColumns();

        int a = resSer.size(), b = resSer.get(0).getValues().size();
        int cols = columnsData.size(),
                rows = a > b ? a : b;

        Map<String, List<Object>> finalKV = new HashMap<>((int) (rows / 0.75));

        for (int i = 0; i < cols; ++i) {
            String colName = columnsData.get(i);
            List<Object> dataList = new ArrayList<>(rows);
            for (int j = 0; j < rows; j++) {
                if (a > b) {
                    dataList.add(resSer.get(j).getValues().get(0).get(i));
                } else {
                    dataList.add(resSer.get(0).getValues().get(j).get(i));
                }
            }
            finalKV.put(colName, dataList);
        }

        return finalKV;
    }

    /**
     * Get all tables in database
     *
     * @param idb InfluxDB Connection
     * @return List of names
     */
    public static List<String> getAllTables(InfluxDB idb) {
        Query q = new Query("SHOW MEASUREMENTS", DBConfiguration.Data.DBNAME);
        QueryResult qr = idb.query(q);
        List<QueryResult.Series> s = qr.getResults().get(0).getSeries();
        if (s == null) return new ArrayList<>(0);
        List<List<Object>> os = s.get(0).getValues();

        List<String> lists = new ArrayList<>(os.size());
        for (List<Object> oss : os) {
            lists.add(String.valueOf(oss.get(0)));
        }
        return lists;
    }

    /**
     * Duplicate tag key values?
     *
     * @param idb          InfluxDB Connection
     * @param keyName      Key name
     * @param toCheckValue Checked value
     */
    public static boolean hasDuplicateTagKeyValues(InfluxDB idb, String keyName, String toCheckValue, String dbName) {
        Query q = new Query("SHOW TAG VALUES ON \"" + dbName + "\" WITH KEY = \"" + keyName + "\"", dbName);
        Map<String, List<Object>> qr = QueryResultToKV(idb.query(q));
        if (qr.size() == 0) return false;

        return qr.get("value").contains(toCheckValue);
    }

    /**
     * Get number of rows in a table
     *
     * @param idb InfluxDB Connection
     * @param tn  Table name
     * @return Number of lines
     */
    public static long getDataTableRows(InfluxDB idb, String tn) {
        Query q = new Query("SELECT COUNT(\"Time\") FROM \"" + tn + "\"", DBConfiguration.Data.DBNAME);
        QueryResult qr = idb.query(q);
        if (qr.getResults().get(0).getSeries() == null) return -1;
        return (long) qr.getResults().get(0).getSeries().get(0).getValues().get(0).get(1);
    }

    private static void test_map(InfluxDB idb) {
        Query q = new Query("SELECT * FROM Files", DBConfiguration.Data.DBNAME);
        QueryResult qr = idb.query(q);
        Map<String, List<Object>> s = QueryResultToKV(qr);
        List<Object> ss = s.get("name");
        for (Object sss : ss) {
            System.out.println(sss);
        }
    }

    public static void main(String[] args) {
        InfluxDB influxDB = InfluxDBFactory.connect(InfluxappConfig.IFX_ADDR, InfluxappConfig.IFX_USERNAME, InfluxappConfig.IFX_PASSWD);
        test_map(influxDB);
        getAllTables(influxDB);
        System.out.print(getDataTableRows(influxDB, "data_PUH-2010-093_noar"));
    }

}
