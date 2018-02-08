package app.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import app.InfluxappConfig;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDB.ConsistencyLevel;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;

import app.util.Util;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.springframework.stereotype.Service;

/**
 * Importing CSV data into InfluxDB
 */
@Service
public class ImportCsvService {

    private static long fileLine = 0;
    private final static Random rnd = new Random();

    // TODO: return JobStatus class
    public static void singleFileImport(String file_path, boolean hasAr) throws IOException, ParseException {
        InfluxDB influxDB = InfluxDBFactory.connect(InfluxappConfig.IFX_ADDR, InfluxappConfig.IFX_USERNAME, InfluxappConfig.IFX_PASSWD);
        String dbName = InfluxappConfig.IFX_DBNAME;
        influxDB.createDatabase(dbName);

        File file_info = new File(file_path);
        // Extract PID from file name (Always first 12 chars)
        String file_name = file_info.getName();
        String pid, file_uuid = "unknown";
        if (file_name.length() >= 12)
            pid = file_name.substring(0, 12).toUpperCase();
        else
            throw new RuntimeException("Wrong PID in filename!");

        FileReader reader = new FileReader(file_info);
        BufferedReader bufferReader = new BufferedReader(reader, 4096000);

        int lineNumber = 1;
        String timestamp = "";
        while (bufferReader.ready() && (lineNumber <= 6)) {
            String line = bufferReader.readLine();
            switch (lineNumber) {
                case 1:
                    // Check PID in the first line? If not, error
                    if (!line.toUpperCase().contains(pid))
                        throw new RuntimeException("Wrong PID in filename!");
                    file_uuid = line.substring(line.length() - 40, line.length() - 4);
                    break;
                case 2:
                case 3:
                case 4:
                    // Line 2-4 rely on seperated metadata
                    break;
                case 5:
                    timestamp = line.split(",")[1];
                    break;
                case 6:
                    timestamp += " " + line.split(",")[1];
                    break;
                default:
                    throw new IndexOutOfBoundsException("Line number out of range");
            }
            lineNumber++;
        }

        boolean isNewPatient = isNewPatient(influxDB, pid);

        // File metadata table
        Builder filemetaBuilder = Point.measurement(InfluxappConfig.IFX_TABLE_FILES).time(Util.dateTimeFormatToTimestamp(timestamp, "yyyy.MM.dd HH:mm:ss"), TimeUnit.MILLISECONDS);
        filemetaBuilder.addField("uuid", file_uuid);
        Point point = filemetaBuilder.build();
        BatchPoints fileInfo = BatchPoints.database(dbName).consistency(ConsistencyLevel.ALL).build();
        fileInfo.point(point);
        influxDB.write(fileInfo);

        if (isNewPatient) {
            // Patient metadata table
            Builder patientBuilder = Point.measurement(InfluxappConfig.IFX_TABLE_PATIENTS).time(System.currentTimeMillis(), TimeUnit.MILLISECONDS);
            patientBuilder.tag("pid", pid);
            patientBuilder.addField("age", rnd.nextInt(90));
            patientBuilder.addField("gender", rnd.nextBoolean() ? "M" : "F");
            Point patientPoint = patientBuilder.build();
            BatchPoints patientInfo = BatchPoints.database(dbName).consistency(ConsistencyLevel.ALL).build();
            patientInfo.point(patientPoint);
            influxDB.write(patientInfo);
        }

        // TODO: 7th line
        String[] introColumns = bufferReader.readLine().split(",");
        // Read the column name
        String[] columnNames = bufferReader.readLine().split(",");
        int columnCount = columnNames.length;
        System.out.println("Number of columns: " + columnCount);
        int bulkInsertMax = 1550000 / columnCount;

        // File records table
        BatchPoints records = BatchPoints.database(dbName).consistency(ConsistencyLevel.ALL).build();
        int batchCount = 0, totalCount = 0;
        String tableName = InfluxappConfig.IFX_DATA_PREFIX + pid + (hasAr ? "_ar" : "_noar");

        // Avoid duplicate import
        if (!isNewPatient) {
            if (Util.getAllTables(influxDB).contains(tableName)) {
                if (Util.getDataTableRows(influxDB, tableName) - fileLine < 0.001) {
                    // Already imported
                    System.out.println("Already imported this dataset!");
                    bufferReader.close();
                    reader.close();
                    return;
                }
            }
        }

        while (bufferReader.ready()) {
            String[] values = bufferReader.readLine().split(",");
            if (columnCount != values.length)
                throw new RuntimeException("File content inconsistent!");
            Map<String, Object> lineKVMap = new HashMap<>();
            for (int i = 1; i < values.length; i++) {
                lineKVMap.put(columnNames[i], Double.valueOf(values[i]));
            }

            // Table with ID for each patient
            Point record = Point.measurement(tableName)
                    .time(Util.serialTimeToLongDate(values[0]), TimeUnit.MILLISECONDS)
                    .tag("file_uuid", file_uuid)
                    .fields(lineKVMap)
                    .build();
            records.point(record);
            batchCount++;
            totalCount++;
            if (batchCount >= bulkInsertMax) {
                influxDB.write(records);
                records = BatchPoints.database(dbName).consistency(ConsistencyLevel.ALL).build();
                batchCount = 0;
                String percent = String.format("%.2f%%", (totalCount * 100.0 / fileLine));
                System.out.println("Processed " + percent + " (" + totalCount + " records)");
            }
        }
        bufferReader.close();
        reader.close();

        // Last batch haven't wrote to DB
        influxDB.write(records);
        System.out.println("Finished...");
    }

    private static boolean isNewPatient(InfluxDB idb, String id) {
        return (idb.query(new Query(
                "SELECT pid FROM " + InfluxappConfig.IFX_TABLE_PATIENTS
                        + " WHERE pid = '" + id.toUpperCase() + "'", InfluxappConfig.IFX_DBNAME))
                .getResults().get(0).getSeries()) == null;
    }

    public static void main(String[] args) throws IOException, ParseException {

//        String superDirectory = "E:\\Grad@Pitt\\TS ProjectData";

//        String superDirectory = "data//1.csv";
//        String[] all_csvs = Util.getAllCsvFileInDirectory(superDirectory);
//
//        for (String f_path : all_csvs) {
//            System.out.println("Processing '" + f_path + "'");
//
        String f_path = "N:\\Exported Files without Artifact Reduction\\PUH-2010-093_01noar.csv";
        // Spend lots of time, but accruate for progress
        fileLine = Files.lines(Paths.get(f_path)).count();
        System.out.println("Number of records: " + (fileLine - 8));

        long startTime = System.currentTimeMillis();
        singleFileImport(f_path, false);
        long endTime = System.currentTimeMillis();

        System.out.println("Import time: " + String.format("%.2f", (endTime - startTime) / 60000.0) + " min\n");
//        }

    }

}
