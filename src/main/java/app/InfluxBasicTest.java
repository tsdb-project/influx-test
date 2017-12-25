/**
 *
 */
package app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDB.ConsistencyLevel;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;

import app.util.Util;

/**
 * Test for importing CSV data into InfluxDB
 *
 * @author Isolachine
 */
public class InfluxBasicTest {

    private final static Integer BULK_INSERT_MAX = 5000;

    public static void testSingleFileImport(String file_path) throws IOException, ParseException {
        InfluxDB influxDB = InfluxDBFactory.connect(InfluxConfig.ADDR, InfluxConfig.USERNAME, InfluxConfig.PASSWD);
        String dbName = InfluxConfig.DBNAME;
        influxDB.createDatabase(dbName);

        File file_info = new File(file_path);
        String uuid = "unknown";
        // Extract UUID from file name
        Matcher matcher = Pattern.compile("(_)(.+)(.csv)").matcher(file_info.getName());
        if (matcher.find()) uuid = matcher.group(2);

        FileReader reader = new FileReader(file_info);
        BufferedReader bufferReader = new BufferedReader(reader, 4096000);

        int lineNumber = 1;
        Map<String, String> file = new HashMap<>();
        String timestamp = "";
        while (bufferReader.ready() && (lineNumber <= 6)) {
            String line = bufferReader.readLine();
            if (line != null) {
                switch (lineNumber) {
                    case 1:
                        // File name
                        // TODO: FN for each patient
                        file.put(line.split(",")[0], line.split(",")[1]);
                        break;
                    case 2:
                        // Patient Name
                        // TODO: LN for each patient
                        file.put(line.split(",")[0], line.split(",")[1] + line.split(",")[2]);
                        break;
                    case 3:
                        // Patient ID, hided in the CSV
                        // TODO: UUID for each patient
                        file.put(line.split(",")[0], "000000001");
                        break;
                    case 4:
                        // Patient DoB
                        file.put(line.split(",")[0],
                                String.valueOf(Util.dateTimeFormatToTimestamp(line.split(",")[1], "m/d/yy")));
                        break;
                    case 5:
                        // Current Date
                        timestamp = line.split(",")[1];
                        break;
                    case 6:
                        // Current Time
                        timestamp += " " + line.split(",")[1];
                        break;
                    default:
                        break;
                }
            }
            lineNumber++;
        }

        // File metadata table
        Builder p = Point.measurement("files")
                .time(Util.dateTimeFormatToTimestamp(timestamp, "yyyy.MM.dd HH:mm:ss"), TimeUnit.MILLISECONDS);
        for (String key : file.keySet()) {
            // TODO: field is not indexable
            p.addField(key, file.get(key));
        }
        Point point = p.build();
        BatchPoints fileInfo = BatchPoints.database(dbName).consistency(ConsistencyLevel.ALL).build();
        fileInfo.point(point);
        influxDB.write(fileInfo);

        // Skip the 7th line
        bufferReader.readLine();
        // Read the column name
        String[] columnNames = bufferReader.readLine().split(",");

        // File records table
        BatchPoints records = BatchPoints.database(dbName).consistency(ConsistencyLevel.ALL).build();
        int batchCount = 0, totalCount = 0;
        while (bufferReader.ready()) {
            String[] values = bufferReader.readLine().split(",");
            Map<String, Object> keyValueMap = new HashMap<>();
            for (int i = 1; i < values.length; i++) {
                keyValueMap.put(columnNames[i], Double.valueOf(values[i]));
            }

            // Table with ID for each patient
            Point record = Point.measurement("records_" + file.get("PatientID"))
                    .time(Util.serialTimeToLongDate(values[0]), TimeUnit.MILLISECONDS)
                    // TODO: Tag is indexable, use it
                    .tag("file_uuid", uuid)
                    .fields(keyValueMap) // Notice: fields are not indexable!
                    .build();
            records.point(record);
            batchCount++;
            totalCount++;
            if (batchCount >= BULK_INSERT_MAX) {
                influxDB.write(records);
                records = BatchPoints.database(dbName).consistency(ConsistencyLevel.ALL).build();
                batchCount = 0;
                System.out.println("finished " + totalCount + " records.");
            }
        }

        // Last batch haven't wrote to DB
        influxDB.write(records);
        System.out.println("finished " + totalCount + " records.");

        reader.close();
    }

    public static void main(String[] args) throws IOException, ParseException {

        String superDirectory = "E:\\Grad@Pitt\\TS ProjectData";
        String[] all_csvs = Util.getAllCsvFileInDirectory(superDirectory);

        for (String f_path : all_csvs) {
            System.out.println("Processing '" + f_path + "'");

            long startTime = System.currentTimeMillis();
            testSingleFileImport(f_path);
            long endTime = System.currentTimeMillis();

            System.out.println("running time: " + String.format("%.2f", (endTime - startTime) / 60000.0) + " min\n");
        }

    }

}
