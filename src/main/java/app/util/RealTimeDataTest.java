/**
 * 
 */
package app.util;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDB.ConsistencyLevel;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.influxdb.dto.Query;

import app.common.InfluxappConfig;

/**
 * @author Isolachine
 *
 */
public class RealTimeDataTest {
    final static Random random = new Random();
    final static InfluxDB influxDB = InfluxDBFactory.connect(InfluxappConfig.IFX_ADDR, InfluxappConfig.IFX_USERNAME, InfluxappConfig.IFX_PASSWD);
    final static String dbName = "alert";
    final static Query query = new Query("CREATE DATABASE alert", "");

    public static double randomDouble() {
        return random.nextDouble();
    }

    public static void insert() {
        double value = randomDouble() * 10;
        Builder p = Point.measurement("realtime").time(System.currentTimeMillis(), TimeUnit.MILLISECONDS).addField("data_value", value);
        Point point = p.build();
        BatchPoints batch = BatchPoints.database(dbName).consistency(ConsistencyLevel.ALL).build();
        batch.point(point);
        influxDB.write(batch);
        System.out.println(LocalDateTime.now() + " Write " + value + " into realtime.");
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        influxDB.query(query);
        final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(RealTimeDataTest::insert, 0, 5, TimeUnit.SECONDS);
    }

}
