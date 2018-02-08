package app.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
import okhttp3.OkHttpClient;
import org.springframework.stereotype.Service;

/**
 * Importing CSV data into InfluxDB
 */
@Service
public class ImportCsvService {

    private final static Random rnd = new Random();

    private final static okhttp3.OkHttpClient.Builder importHttpClient =
            new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS);

    /**
     * Process a file object
     *
     * @param file_info File object
     * @param hasAr     AR?
     * @param fileSize  File size to estimate progress
     */
    private static void importProc(File file_info, boolean hasAr, double fileSize, String taskName) throws IOException, ParseException {
        InfluxDB influxDB = InfluxDBFactory.connect(InfluxappConfig.IFX_ADDR, InfluxappConfig.IFX_USERNAME, InfluxappConfig.IFX_PASSWD, importHttpClient);
        influxDB.disableGzip();
        String dbName = InfluxappConfig.IFX_DBNAME;
        influxDB.createDatabase(dbName);

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
        filemetaBuilder.tag("pid", pid);
        filemetaBuilder.addField("uuid", file_uuid);
        filemetaBuilder.addField("name", file_name);
        filemetaBuilder.addField("size", (long) fileSize);
        Point point = filemetaBuilder.build();
        BatchPoints fileInfo = BatchPoints.database(dbName).consistency(ConsistencyLevel.ALL).build();
        fileInfo.point(point);
        influxDB.write(fileInfo);

        // On-demand patient metadata table
        if (isNewPatient) {
            Builder patientBuilder = Point.measurement(InfluxappConfig.IFX_TABLE_PATIENTS).time(System.currentTimeMillis(), TimeUnit.MILLISECONDS);
            patientBuilder.tag("pid", pid);
            patientBuilder.addField("age", rnd.nextInt(80) + 10);
            patientBuilder.addField("gender", rnd.nextBoolean() ? "M" : "F");
            Point patientPoint = patientBuilder.build();
            BatchPoints patientInfo = BatchPoints.database(dbName).consistency(ConsistencyLevel.ALL).build();
            patientInfo.point(patientPoint);
            influxDB.write(patientInfo);
        }

        // 7th line
        String svL = bufferReader.readLine();
        // 8th Line
        String eiL = bufferReader.readLine();

        String[] introColumns = svL.split(",");
        String[] columnNames = eiL.split(",");
        int columnCount = columnNames.length;
        SOP(file_name + " columns: " + columnCount);
        int bulkInsertMax = 1550000 / columnCount;

        // File records table
        BatchPoints records = BatchPoints.database(dbName).consistency(ConsistencyLevel.ALL).build();
        int batchCount = 0, totalCount = 0;
        double processedSize = svL.length() + eiL.length();
        String tableName = InfluxappConfig.IFX_DATA_PREFIX + pid + (hasAr ? "_ar" : "_noar");

        // Avoid duplicate import
        if (!isNewPatient) {
            if (Util.getAllTables(influxDB).contains(tableName)) {
                if (isFileUUIDExist(influxDB, file_uuid)) {
                    SOP("Already imported '" + file_uuid + "'");
                    bufferReader.close();
                    reader.close();
                    return;
                }
            }
        }

        while (bufferReader.ready()) {
            String aLine = bufferReader.readLine();
            String[] values = aLine.split(",");
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
            processedSize += aLine.length();
            if (batchCount >= bulkInsertMax) {
                // Insert in BULK
                influxDB.write(records);
                records = BatchPoints.database(dbName).consistency(ConsistencyLevel.ALL).build();
                batchCount = 0;
                String percent = String.format("%.2f%%", processedSize / fileSize * 100.0);
                SOP(taskName + " processed " + percent + " (" + totalCount + " records), " + file_name);
            }
        }
        bufferReader.close();
        reader.close();

        // Last batch haven't wrote to DB
        influxDB.write(records);
        SOP("Finished for '" + file_name + "'");
    }

    private static boolean isNewPatient(InfluxDB idb, String id) {
        return (idb.query(new Query(
                "SELECT * FROM " + InfluxappConfig.IFX_TABLE_PATIENTS
                        + " WHERE pid = '" + id.toUpperCase() + "'", InfluxappConfig.IFX_DBNAME))
                .getResults().get(0).getSeries()) == null;
    }

    private static boolean isFileUUIDExist(InfluxDB idb, String uuid) {
        return (idb.query(new Query(
                "SHOW TAG VALUES WITH KEY = \"file_uuid\" WHERE \"file_uuid\" = '" + uuid + "'",
                InfluxappConfig.IFX_DBNAME)).getResults().get(0).getSeries()) != null;
    }

    public static void ImportByFile(String oneCsv, boolean hasAr, String threadName) {

        try {
            File file_info = new File(oneCsv);
            SOP(threadName + " processing '" + file_info.getName() + "'");

            long startTime = System.currentTimeMillis();
            importProc(file_info, hasAr, file_info.length(), threadName);
            long endTime = System.currentTimeMillis();

            SOP(oneCsv + ". Import time: " + String.format("%.2f", (endTime - startTime) / 60000.0) + " min\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void ImportByList(String[] allCsv, boolean hasAr, String nkName) {

        int cores = Runtime.getRuntime().availableProcessors(),
                queueLen = allCsv.length;

        // TODO: Parallel importing

        for (String f_path : allCsv) {
            ImportByFile(f_path, hasAr, nkName);
        }
    }

    // Dummy System.out.println
    private static void SOP(String s) {
        System.out.println(s);
    }

    public static void main(String[] args) {

        String[] allAR = Util.getAllCsvFileInDirectory("N:\\Test_AR\\");
        String[] allNoAR = Util.getAllCsvFileInDirectory("N:\\Test_NoAR\\");

        Thread thread1 = new Thread(new Runnable() {

            @Override
            public void run() {
                ImportByList(allNoAR, false, "NoART");
            }

        });

        Thread thread2 = new Thread(new Runnable() {

            @Override
            public void run() {
                ImportByList(allAR, true, "ART");
            }
        });

        thread1.start();
        thread2.start();

    }

}
