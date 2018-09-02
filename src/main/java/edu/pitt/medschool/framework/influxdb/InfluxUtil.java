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
     */
    public static List<ResultTable> justQueryData(InfluxDB i, String query) {
        return QueryResultToKV(i.query(new Query(query, "data")));
    }

    /**
     * Query results to a dictionary like struct, in most cases it should only have one series
     */
    public static List<ResultTable> QueryResultToKV(QueryResult results) {
        List<ResultTable> res = new LinkedList<>();

        if (results.hasError()) return res;
        if (results.getResults().isEmpty()) return res;

        // Most queries should have one result, but we have to handle multi-results queries anyway
        for (QueryResult.Result qr : results.getResults()) {
            if (qr.getSeries() == null) continue;
            for (QueryResult.Series sr : qr.getSeries()) {
                if (sr == null) res.add(new ResultTable()); // This result is empty
                else res.add(new ResultTable(sr));
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
        ResultTable[] qr = QueryResultToKV(InfluxappConfig.INFLUX_DB.query(q)).toArray(new ResultTable[0]);
        return qr[0].getRowCount() != 0 && qr[0].getDatalistByColumnName("value").contains(toCheckValue);
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

    /**
     * Manual table join (Left join)
     * Avoid using, due to no index
     *
     * @param l    Table left
     * @param r    Table right
     * @param colL Join on which column (left)
     * @param colR Join on which column (right)
     */
/*
    public static Map<String, List<Object>> ResultTableJoin(Map<String, List<Object>> l, Map<String, List<Object>> r, String colL, String colR) {
        Map<String, List<Object>> result = new TreeMap<>();
        Map<Integer, Integer> matchList = new HashMap<>(l.size());
        // No index here, n^2 comp.
        ArrayList<Object>
                leftVal = new ArrayList<>(l.get(colL)),
                rightVal = new ArrayList<>(r.get(colR));

        for (int i = 0; i < leftVal.size(); i++) {
            int newRightSize = rightVal.size();
            for (int j = 0; j < newRightSize; j++) {
                // objL and objR should be some primitive type (InfluxDB doesn't support much data type)
                if (leftVal.get(i).equals(rightVal.get(j))) {
                    matchList.put(i, j);
                    rightVal.remove(j);
                }
            }
        }

        return result;
    }
*/
    public static void main(String[] args) {
        List<String> s = getAllTables(DBConfiguration.Data.DBNAME);
        System.out.println(s);
    }

}
