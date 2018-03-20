package app.test;

import app.config.DBConfiguration;
import app.config.InfluxappConfig;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Alert msg via email
 * All hard-coded, DEMO ONLY!
 *
 * @author CptTony
 */
public class AlertTest {

    public static final int
            delayMS = 800,
            maxInsert = 300;

    public static void rndInsert(InfluxDB influxDB) {

        Random rnd = new Random();
        String dbName = DBConfiguration.Sys.DBNAME;
        influxDB.createDatabase(dbName);


        int iCount = 0;
        while (iCount < maxInsert) {
            BatchPoints rndP = BatchPoints
                    .database(dbName)
                    .consistency(InfluxDB.ConsistencyLevel.ALL)
                    .build();

            Map<String, String> tags = new HashMap<>();
            tags.put("file_uuid", "fake");
            tags.put("ValueD", Double.toString(rnd.nextDouble() * 100));
            tags.put("ValueI", Integer.toString(rnd.nextInt(10)));

            Map<String, Object> fields = new HashMap<>();
            fields.put("I10_4", rnd.nextDouble() * 20);
            fields.put("I10_1", rnd.nextInt(200));

            Point record = Point.measurement("records_000000001")
                    .tag(tags)
                    .fields(fields) // Notice: fields are not indexable!
                    .build();
            rndP.point(record);
            influxDB.write(rndP);
            System.out.print("Write point #" + iCount);

            try {
                System.out.println(". And sleeping...");
                Thread.sleep(delayMS);
                iCount += 1;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public static void main(String[] args) {
        InfluxDB influxDB = InfluxDBFactory.connect(InfluxappConfig.IFX_ADDR, InfluxappConfig.IFX_USERNAME, InfluxappConfig.IFX_PASSWD);
        rndInsert(influxDB);
    }

}
