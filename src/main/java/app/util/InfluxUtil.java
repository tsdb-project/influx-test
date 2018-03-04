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
     * @param dbname DB Name
     * @return List of names
     */
    public static List<String> getAllTables(String dbname) {
        Query q = new Query("SHOW MEASUREMENTS", dbname);
        QueryResult qr = InfluxappConfig.INFLUX_DB.query(q);
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
     * @param keyName      Key name
     * @param toCheckValue Checked value
     */
    public static boolean hasDuplicateTagKeyValues(String keyName, String toCheckValue, String dbName) {
        Query q = new Query("SHOW TAG VALUES ON \"" + dbName + "\" WITH KEY = \"" + keyName + "\"", dbName);
        Map<String, List<Object>> qr = QueryResultToKV(InfluxappConfig.INFLUX_DB.query(q));
        if (qr.size() == 0) return false;

        return qr.get("value").contains(toCheckValue);
    }

    /**
     * Get number of rows in a table
     *
     * @param tn Table name
     * @return Number of lines
     */
    public static long getDataTableRows(String tn) {
        Query q = new Query("SELECT COUNT(\"Time\") FROM \"" + tn + "\"", DBConfiguration.Data.DBNAME);
        QueryResult qr = InfluxappConfig.INFLUX_DB.query(q);
        if (qr.getResults().get(0).getSeries() == null) return -1;
        return (long) qr.getResults().get(0).getSeries().get(0).getValues().get(0).get(1);
    }


    public static void main(String[] args) {
        List<String> s = getAllTables(DBConfiguration.Data.DBNAME);
        System.out.print(getDataTableRows("data_PUH-2010-093_noar"));
    }

}
