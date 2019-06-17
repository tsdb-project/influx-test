package edu.pitt.medschool.framework.influxdb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.pitt.medschool.config.DBConfiguration;
import edu.pitt.medschool.config.InfluxappConfig;
import okhttp3.OkHttpClient;

/**
 * Utilities for InfluxDB Java Client
 *
 * @author tonyz
 */
public class InfluxUtil {

    private static Logger logger = LoggerFactory.getLogger(InfluxUtil.class);

    /**
     * Method for lazy man, just query the 'data' database and get the result Just use fast method for general queries
     */
    public static ResultTable[] justQueryData(InfluxDB i, boolean fastMethod, String query) {
        String dbName = DBConfiguration.Data.DBNAME;
        QueryResult qr = i.query(new Query(query, dbName));
        if (fastMethod)
            return queryResultToTable(qr).toArray(new ResultTable[0]);
        else
            return queryResultToKV(qr).toArray(new ResultTable[0]);
    }

    /**
     * Query results to a table, fast conversion but not safe to modify anything
     */
    public static List<FastResultTable> queryResultToTable(QueryResult results) {
        List<FastResultTable> res = new LinkedList<>();

        if (results.hasError())
            return res;
        if (results.getResults().isEmpty())
            return res;

        // Most queries should have one result,
        for (QueryResult.Result qr : results.getResults()) {
            if (qr.getSeries() == null)
                continue;
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

        if (results.hasError())
            return res;
        if (results.getResults().isEmpty())
            return res;

        // but we have to handle multi-results queries anyway
        for (QueryResult.Result qr : results.getResults()) {
            if (qr.getSeries() == null)
                continue;
            for (QueryResult.Series sr : qr.getSeries()) {
                res.add(new DictionaryResultTable(sr));
            }
        }

        return res;
    }

    public static boolean deleteDataByTagValues(String patient, Map<String, String> tags) throws Exception {
        String dbName = DBConfiguration.Data.DBNAME;
        String queryTemplate = "DELETE FROM \"%s\" %s";
        StringBuffer whereClause = new StringBuffer("WHERE ");

        boolean usingWhereClause = false;
        for (String tag : tags.keySet()) {
            whereClause.append("\"").append(tag).append("\"");
            whereClause.append(" = ").append("'").append(tags.get(tag)).append("' ");
            usingWhereClause = true;
        }
        String query = String.format(queryTemplate, patient, usingWhereClause ? whereClause : whereClause);
        logger.debug(query);
        InfluxDB influxDB = generateIdbClient(false);
        try {
            QueryResult result = influxDB.query(new Query(query, dbName));
            logger.debug(result.toString());
        } catch (Exception e) {
            logger.error("FAILED TO DELETE FROM INFLUX!");
            logger.error(e.toString());
            throw e;
        }
        return true;
    }

    /**
     * Get all tables in database
     *
     * @param dbname DB Name
     * @return List of names
     */
    public static List<String> getAllTables(String dbname) {
        Query q = new Query("SHOW MEASUREMENTS", dbname);
        InfluxDB i = generateIdbClient(true);
        QueryResult qr = i.query(q);
        List<QueryResult.Series> s = qr.getResults().get(0).getSeries();
        i.close();
        if (s == null)
            return new ArrayList<>(0);

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
        InfluxDB i = generateIdbClient(true);
        List<DictionaryResultTable> qr = queryResultToKV(i.query(q));
        i.close();
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
        InfluxDB i = generateIdbClient(true);
        QueryResult qr = i.query(q);
        i.close();
        if (qr.getResults().get(0).getSeries() == null)
            return -1;
        return (long) qr.getResults().get(0).getSeries().get(0).getValues().get(0).get(1);
    }

    public static long[] getTimeRangeandRows(String tn, String filename){
        Query q1 = new Query("SELECT COUNT(\"Time\") FROM \"" + tn + "\" WHERE fileName='"+filename+"'", DBConfiguration.Data.DBNAME);
        Query q2 = new Query("SELECT FIRST(\"Time\") FROM \""+tn+"\"WHERE fileName='"+filename+"'",DBConfiguration.Data.DBNAME);
        Query q3 = new Query("SELECT LAST(\"Time\") FROM \""+tn+"\"WHERE fileName='"+filename+"'",DBConfiguration.Data.DBNAME);
        InfluxDB i = generateIdbClient(true);
        QueryResult qr1 = i.query(q1);
        QueryResult qr2 = i.query(q2);
        QueryResult qr3 = i.query(q3);
        long[] result = new long[3];
        result[0] = (long)qr1.getResults().get(0).getSeries().get(0).getValues().get(0).get(1);
        result[1] = (long)qr2.getResults().get(0).getSeries().get(0).getValues().get(0).get(0);
        result[2] = (long)qr3.getResults().get(0).getSeries().get(0).getValues().get(0).get(0);
        i.close();
        if(qr1.getResults().get(0).getSeries() == null||qr2.getResults().get(0).getSeries()==null||qr3.getResults().get(0).getSeries()==null){
            logger.info("cannot find time in %s",filename);
        }
        return result;
    }

    /**
     * Generate one IdbClient for one thread when doing exports
     *
     * @param needGzip Unless Idb not running with Brainflux, you should disable GZip
     */
    public static InfluxDB generateIdbClient(boolean needGzip) {
        InfluxDB idb = InfluxDBFactory.connect(InfluxappConfig.IFX_ADDR, InfluxappConfig.IFX_USERNAME,
                InfluxappConfig.IFX_PASSWD, new OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS)
                        .readTimeout(1, TimeUnit.HOURS).writeTimeout(1, TimeUnit.HOURS));
        if (needGzip) {
            idb.enableGzip();
        } else {
            idb.disableGzip();
        }
        return idb;
    }

    public static void main(String[] args) {
        try {
            deleteDataByTagValues("PUH", new HashMap<>());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
