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
 */
public class QueryTest {
    public static void main(String[] args) {
        InfluxDB influxDB = InfluxDBFactory.connect(InfluxConfig.ADDR, InfluxConfig.USERNAME, InfluxConfig.PASSWD);
        String dbName = InfluxConfig.DBNAME;
        influxDB.createDatabase(dbName);
//        String rpName = "aRetentionPolicy";
//        influxDB.createRetentionPolicy(rpName, dbName, "30d", "30m", 2, true);
        Query query = new Query("SELECT * FROM records", dbName);
        QueryResult result = influxDB.query(query);
        for (Result res : result.getResults()) {
            System.out.println(res);
        }

        /*
        BatchPoints batchPoints = BatchPoints.database(dbName).tag("async", "true").retentionPolicy(rpName).consistency(ConsistencyLevel.ALL).build();
        Builder point3 = Point.measurement("cpu").time(System.currentTimeMillis(), TimeUnit.MILLISECONDS).addField("idle", 60L).addField("user", 3L).addField("system", 1L);
        Point point1 = Point.measurement("cpu").time(System.currentTimeMillis() - 5, TimeUnit.MILLISECONDS).addField("idle", 90L).addField("user", 9L).addField("system", 1L).build();
        Point point2 = Point.measurement("disk").time(System.currentTimeMillis(), TimeUnit.MILLISECONDS).addField("used", 80L).addField("free", 1L).build();
        batchPoints.point(point3);
        batchPoints.point(point1);
        batchPoints.point(point2);
        influxDB.write(batchPoints);
        Query query = new Query("SELECT idle FROM cpu", dbName);
        QueryResult result = influxDB.query(query);
        for (List<Object> res : result.getResults().get(0).getSeries().get(0).getValues()) {
            System.out.println(res);
        }
        influxDB.dropRetentionPolicy(rpName, dbName);
        influxDB.deleteDatabase(dbName);
        */

    }
}
