/**
 * 
 */
package app;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.dto.QueryResult.Result;

/**
 * @author Isolachine
 *
 */
public class QueryTest {
    public static void main(String[] args) {
        InfluxDB influxDB = InfluxDBFactory.connect("http://localhost:8086", "root", "root");
        String dbName = "test";
        Query query = new Query("SELECT * FROM files", dbName);
        QueryResult result = influxDB.query(query);
        for (Result res : result.getResults()) {
            System.out.println(res);
        }
        Query queryRecords = new Query("SELECT ClockDateTime, I10_2 FROM records LIMIT 100", dbName);
        QueryResult records = influxDB.query(queryRecords);
        for (Result res : records.getResults()) {
            System.out.println(res);
        }
        
        
        
    }
}
