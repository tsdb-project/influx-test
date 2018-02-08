/**
 * 
 */
package app.util;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import app.InfluxappConfig;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.InfluxDB.ConsistencyLevel;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;

/**
 * @author Isolachine
 *
 */
public class RealTimeDataTest {
    static Random random = new Random();

    public static double randomDouble() {
        return random.nextDouble();
    }

    public static void insert() {
        InfluxDB influxDB = InfluxDBFactory.connect(InfluxappConfig.IFX_ADDR, InfluxappConfig.IFX_USERNAME, InfluxappConfig.IFX_PASSWD);
        String dbName = "test";
        influxDB.createDatabase(dbName);

        long lastSec = 0;
        while (true) {
            long sec = System.currentTimeMillis() / 1000;
            if (sec != lastSec) {
                Builder p = Point.measurement("realtime").time(System.currentTimeMillis(), TimeUnit.MILLISECONDS).addField("data_value", randomDouble());
                Point point = p.build();
                BatchPoints batch = BatchPoints.database(dbName).consistency(ConsistencyLevel.ALL).build();
                batch.point(point);
                influxDB.write(batch);
                lastSec = sec;
            }
        }

    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        insert();
    }

}
