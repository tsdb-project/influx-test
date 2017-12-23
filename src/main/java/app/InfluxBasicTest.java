/**
 * 
 */
package app;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDB.ConsistencyLevel;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;

import app.util.Util;

/**
 * @author Isolachine
 *
 */
public class InfluxBasicTest {

    private final static Integer BATCH_MAX = 5000;

    public static void testRun() throws IOException, ParseException {
        InfluxDB influxDB = InfluxDBFactory.connect("http://localhost:8086", "root", "root");
        String dbName = "test";
        influxDB.createDatabase(dbName);
        String filename = "data//1.csv";

        FileReader reader = new FileReader(filename);
        BufferedReader bufferReader = new BufferedReader(reader);

        int lineNumber = 1;
        Map<String, String> file = new HashMap<>();
        String timestamp = "";
        while (bufferReader.ready() && lineNumber <= 6) {
            String line = bufferReader.readLine();
            if (line != null) {
                switch (lineNumber) {
                case 1:
                    file.put(line.split(",")[0], line.split(",")[1]);
                    break;
                case 2:
                    file.put(line.split(",")[0], line.split(",")[1] + line.split(",")[2]);
                    break;
                case 3:
                    file.put(line.split(",")[0], "000000001");
                    break;
                case 4:
                    file.put(line.split(",")[0], String.valueOf(Util.dateTimeFormatToTimestamp(line.split(",")[1], "m/d/yy")));
                    break;
                case 5:
                    timestamp = line.split(",")[1];
                    break;
                case 6:
                    timestamp += " " + line.split(",")[1];
                    break;
                default:
                    break;
                }
            }
            lineNumber++;
        }
        Builder p = Point.measurement("files").time(Util.dateTimeFormatToTimestamp(timestamp, "yyyy.MM.dd HH:mm:ss"), TimeUnit.MILLISECONDS);
        for (String key : file.keySet()) {
            p.addField(key, file.get(key));
        }
        Point point = p.build();

        BatchPoints fileInfo = BatchPoints.database(dbName).consistency(ConsistencyLevel.ALL).build();
        fileInfo.point(point);
        influxDB.write(fileInfo);

        bufferReader.readLine();
        String[] columnNames = bufferReader.readLine().split(",");

        BatchPoints records = BatchPoints.database(dbName).consistency(ConsistencyLevel.ALL).build();
        int batchCount = 0;
        int count = 0;
        while (bufferReader.ready()) {
            String[] values = bufferReader.readLine().split(",");
            Map<String, Object> keyValueMap = new HashMap<>();
            for (int i = 1; i < values.length; i++) {
                keyValueMap.put(columnNames[i], Double.valueOf(values[i]));
            }

            Point record = Point.measurement("records").time(Util.serialTimeToLongDate(values[0]), TimeUnit.MILLISECONDS).fields(keyValueMap).build();
            records.point(record);
            batchCount++;
            count++;
            if (batchCount >= BATCH_MAX) {
                influxDB.write(records);
                records = BatchPoints.database(dbName).consistency(ConsistencyLevel.ALL).build();
                batchCount = 0;
                System.out.println("finished " + count + " records.");
            }
        }

        influxDB.write(records);
        System.out.println("finished " + count + " records.");
        
        reader.close();
    }

    public static void main(String[] args) throws IOException, ParseException {
        long startTime = System.currentTimeMillis();
        testRun();
        long endTime = System.currentTimeMillis();
        System.out.println("running time: " + (endTime - startTime) / 60000.0 + " min");
    }
}
