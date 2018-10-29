package edu.pitt.medschool.framework.influxdb;

import edu.pitt.medschool.config.DBConfiguration;
import edu.pitt.medschool.config.InfluxappConfig;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Utilities for InfluxDB Java Client
 *
 * @author tonyz
 */
public class InfluxUtil {

    /**
     * Method for lazy man, just query the 'data' database and get the result
     * Just use fast method for general queries
     */
    public static ResultTable[] justQueryData(InfluxDB i, boolean fastMethod, String query) {
        String dbName = DBConfiguration.Data.DBNAME;
        QueryResult qr = i.query(new Query(query, dbName));
        if (fastMethod) return queryResultToTable(qr).toArray(new ResultTable[0]);
        else return queryResultToKV(qr).toArray(new ResultTable[0]);
    }

    /**
     * Query results to a table, fast conversion but not safe to modify anything
     */
    public static List<FastResultTable> queryResultToTable(QueryResult results) {
        List<FastResultTable> res = new LinkedList<>();

        if (results.hasError()) return res;
        if (results.getResults().isEmpty()) return res;

        // Most queries should have one result,
        for (QueryResult.Result qr : results.getResults()) {
            if (qr.getSeries() == null) continue;
            for (QueryResult.Series sr : qr.getSeries()) {
                res.add(new FastResultTable(sr));
            }
        }

        return res;
    }

    /**
     * Query results to a more user-friendly struct, could be dictionary like
     */
    public static List<DictionaryResultTable> queryResultToKV(QueryResult results) {
        List<DictionaryResultTable> res = new LinkedList<>();

        if (results.hasError()) return res;
        if (results.getResults().isEmpty()) return res;

        // but we have to handle multi-results queries anyway
        for (QueryResult.Result qr : results.getResults()) {
            if (qr.getSeries() == null) continue;
            for (QueryResult.Series sr : qr.getSeries()) {
                res.add(new DictionaryResultTable(sr));
            }
        }

        return res;
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
        List<DictionaryResultTable> qr = queryResultToKV(InfluxappConfig.INFLUX_DB.query(q));
        return qr.get(0).getRowCount() != 0 && qr.get(0).getDatalistByColumnName("value").contains(toCheckValue);
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
        System.out.println(s);
    }

}
