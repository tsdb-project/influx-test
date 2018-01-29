/**
 *
 */
package app;

import java.util.List;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;

/**
 * @author Isolachine
 */
public class QueryTest {
    public static void main(String[] args) {
        InfluxDB influxDB = InfluxDBFactory.connect(InfluxConfig.ADDR, InfluxConfig.USERNAME, InfluxConfig.PASSWD);
        String dbName = InfluxConfig.DBNAME;
        influxDB.createDatabase(dbName);
        Query query = new Query("select * from (select count(I10_1) as cocount from records_000000001 where I10_1 > 80 group by time(10s)) where cocount=10", dbName);

        QueryResult result = influxDB.query(query);
        for (List<Object> res : result.getResults().get(0).getSeries().get(0).getValues()) {
            System.out.println(res);
        }

        System.out.println();
        
        Query queryRecords = new Query("select * from (select count(diff) as c from (select * from (SELECT (mean(\"I10_1\") - mean(\"I11_1\"))/mean(I10_1) as diff FROM \"records_000000001\" GROUP BY time(1h)) where diff > 0.03 or diff < -0.03) group by time(5h)) where c = 5", dbName);
        QueryResult records = influxDB.query(queryRecords);
        for (List<Object> res : records.getResults().get(0).getSeries().get(0).getValues()) {
            System.out.println(res);
        }

    }
}
