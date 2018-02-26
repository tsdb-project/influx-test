package app.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDB.ConsistencyLevel;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.influxdb.dto.Query;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import app.common.InfluxappConfig;
import app.common.Measurement;
import app.util.InfluxUtil;
import app.util.Util;
import okhttp3.OkHttpClient;

/**
 * Importing CSV data into InfluxDB
 */
@Service
public class ImportCsvService {

    private final static Random rnd = new Random();

    private final static File progressDir = InfluxappConfig.TMP_DIR.getDir("imp_progress");

    public String currentUUID = "";
    public String currentFile = "";
    public double totalProgress = 0.0;
    public boolean progressState = false;
    private long totalSize = 0;
    private long totalProcessedSize = 0;

    /**
     * Process a file object
     *
     * @param file_info File object
     * @param hasAr     AR?
     * @param fileSize  File size to estimate progress
     */
    private void importProc(File file_info, boolean hasAr, double fileSize, String taskName, String statusUUID) throws IOException, ParseException {
        InfluxDB influxDB = InfluxDBFactory.connect(InfluxappConfig.IFX_ADDR, InfluxappConfig.IFX_USERNAME, InfluxappConfig.IFX_PASSWD, new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(60, TimeUnit.SECONDS).writeTimeout(60, TimeUnit.SECONDS));
        influxDB.disableGzip();
        String dbName = InfluxappConfig.IFX_DBNAME;
        influxDB.createDatabase(dbName);

        // Import progress starting...
        File progressFile = new File(progressDir, gen_progress_file_name(statusUUID));
        BufferedWriter bw = new BufferedWriter(Files.newBufferedWriter(progressFile.toPath(), Charset.defaultCharset(), StandardOpenOption.CREATE));
        bw.write(Instant.now().toString() + ",0\n");
        bw.flush();

        // Extract PID from file name (Always first 12 chars)
        String file_name = file_info.getName();
        String pid, file_uuid = "unknown";
        if (file_name.length() >= 12)
            pid = file_name.substring(0, 12).toUpperCase();
        else
            throw new RuntimeException("Wrong PID in filename!");

        SOP("Progress for '" + file_name + "': " + progressFile.getAbsolutePath());
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
                    if (line.length() < 40)
                        throw new RuntimeException("File UUID misformat!");
                    file_uuid = line.substring(line.length() - 40, line.length() - 4);
                    if (file_uuid.contains("."))
                        throw new RuntimeException("File UUID misformat!");
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

        // On-demand patient metadata table
        if (isNewPatient) {
            Builder patientBuilder = Point.measurement(Measurement.PATIENTS).time(System.currentTimeMillis(), TimeUnit.MILLISECONDS);
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
        // Oversize will cause TCP socket to break
        int bulkInsertMax = 1500000 / columnCount;

        // File records table
        BatchPoints records = BatchPoints.database(dbName).consistency(ConsistencyLevel.ALL).build();
        int batchCount = 0, totalCount = 0;
        double processedSize = svL.length() + eiL.length();
        String tableName = Measurement.DATA_PREFIX + pid + (hasAr ? "_ar" : "_noar");

        // Avoid duplicate import
        if (!isNewPatient) {
            if (InfluxUtil.getAllTables(influxDB).contains(tableName)) {
                if (isFileUUIDExist(influxDB, file_uuid)) {
                    SOP("Already imported '" + file_uuid + "'");
                    bufferReader.close();
                    reader.close();
                    bw.close();
                    progressFile.delete();
                    return;
                }
            }
        }

        Map<String, String> dataTag = new HashMap<>(5);
        dataTag.put("fileUUID", file_uuid);
        dataTag.put("isAR", String.valueOf(hasAr ? 1 : 0));

        while (bufferReader.ready()) {
            String aLine = bufferReader.readLine();
            String[] values = aLine.split(",");
            if (columnCount != values.length)
                throw new RuntimeException("File content inconsistent!");
            // Large initial cap for better importing performance
            Map<String, Object> lineKVMap = new HashMap<>((int) (columnCount / 0.75));
            for (int i = 1; i < values.length; i++) {
                lineKVMap.put(columnNames[i], Double.valueOf(values[i]));
            }

            // Table with ID for each patient
            Point record = Point.measurement(tableName).time(Util.serialTimeToLongDate(values[0], null), TimeUnit.MILLISECONDS).tag(dataTag).fields(lineKVMap).build();
            records.point(record);
            batchCount++;
            totalCount++;
            processedSize += aLine.length();
            if (batchCount >= bulkInsertMax) {
                // Insert in BULK
                influxDB.write(records);
                records = BatchPoints.database(dbName).consistency(ConsistencyLevel.ALL).build();
                batchCount = 0;
                bw.write(Instant.now().toString() + "," + String.format("%.4f", processedSize / fileSize) + "\n");
                bw.flush();

                totalProgress = (totalProcessedSize + processedSize) * 1.0 / totalSize;

            }
        }
        bufferReader.close();
        reader.close();

        // Last batch haven't wrote to DB
        influxDB.write(records);

        // File metadata table, move this part to final because: only success imports will be in the File table
        Builder filemetaBuilder = Point.measurement(Measurement.FILES).time(Util.dateTimeFormatToTimestamp(timestamp, "yyyy.MM.dd HH:mm:ss", null), TimeUnit.MILLISECONDS);
        filemetaBuilder.tag("pid", pid);
        filemetaBuilder.addField("isAR", hasAr);
        filemetaBuilder.addField("uuid", file_uuid);
        filemetaBuilder.addField("name", file_name);
        filemetaBuilder.addField("path", file_info.getAbsolutePath());
        filemetaBuilder.addField("size", (long) fileSize);
        Point point = filemetaBuilder.build();
        BatchPoints fileInfo = BatchPoints.database(dbName).consistency(ConsistencyLevel.ALL).build();
        fileInfo.point(point);
        influxDB.write(fileInfo);

        bw.write(Instant.now().toString() + ",1");
        bw.close();
        SOP("Finished for '" + file_name + "' (" + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime()) + ")");
    }

    private static boolean isNewPatient(InfluxDB idb, String id) {
        return (idb.query(new Query("SELECT * FROM " + Measurement.PATIENTS + " WHERE pid = '" + id.toUpperCase() + "'", InfluxappConfig.IFX_DBNAME)).getResults().get(0).getSeries()) == null;
    }

    private static boolean isFileUUIDExist(InfluxDB idb, String uuid) {
        return (idb.query(new Query("SHOW TAG VALUES WITH KEY = \"file_uuid\" WHERE \"file_uuid\" = '" + uuid + "'", InfluxappConfig.IFX_DBNAME)).getResults().get(0).getSeries()) != null;
    }

    /**
     * Import a CSV by file path
     *
     * @param oneCsv     CSV file path
     * @param hasAr      AR or NoAR
     * @param threadName Running in which thread (Log purpose)
     * @param statusUUID UUID for Status file (Can be null)
     */
    public String ImportByFile(String oneCsv, boolean hasAr, String threadName, @Nullable String statusUUID) {

        if (statusUUID == null)
            statusUUID = UUID.randomUUID().toString();

        try {
            File file_info = new File(oneCsv);
            SOP(threadName + " processing '" + file_info.getName() + "'");

            currentUUID = statusUUID;
            currentFile = file_info.getName();

            long startTime = System.currentTimeMillis();
            importProc(file_info, hasAr, file_info.length(), threadName, statusUUID);
            long endTime = System.currentTimeMillis();

            SOP(oneCsv + ". Import time: " + String.format("%.2f", (endTime - startTime) / 60000.0) + " min\n");
        } catch (Exception e) {
            // TODO: Import error will remove currently imported data
            e.printStackTrace();
        }
        return statusUUID;
    }

    /**
     * Import all path provided
     *
     * @param allCsv An array with full path to a CSV file
     * @param hasAr  AR or NoAR
     * @param nkName Process nickname
     */
    public void ImportByList(String[] allCsv, boolean hasAr, String nkName) {

        // TODO: Parallel importing
        int cores = Runtime.getRuntime().availableProcessors(), queueLen = allCsv.length;

        totalSize = 0;
        totalProcessedSize = 0;
        progressState = false;
        totalProgress = 0.0;

        for (String f_path : allCsv) {
            totalSize += FileUtils.sizeOf(new File(f_path));
        }

        for (String f_path : allCsv) {
            long tempSize = totalProcessedSize;
            // Every file should have a different UUID
            String f_uuid = ImportByFile(f_path, hasAr, nkName, UUID.randomUUID().toString());
            totalProcessedSize = tempSize + FileUtils.sizeOf(new File(f_path));
            totalProgress = totalProcessedSize * 1.0 / totalSize;
        }
        progressState = true;
    }

    /**
     * Check the import progress for a file
     *
     * @param uuid Progress UUID
     * @return double (0~1)
     */
    public double CheckProgressWithUUID(String uuid) {
        if (!make_progress_dir())
            return 0;
        File interestProg = new File(progressDir, gen_progress_file_name(uuid));
        // No such file means finished
        if (!interestProg.exists())
            return 1;

        List<String> prgF;
        try {
            prgF = Files.readAllLines(interestProg.toPath());
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }

        // eg: 2018-02-11T18:43:42.320Z,0.7974
        return Double.parseDouble(prgF.get(prgF.size() - 1).split(",")[1].trim());
    }

    // Dummy System.out.println
    private static void SOP(String s) {
        System.out.println(s);
    }

    private static String gen_progress_file_name(String uuid) {
        return uuid + ".id";
    }

    private static boolean make_progress_dir() {
        if (!progressDir.exists()) {
            return progressDir.mkdirs();
        }
        return true;
    }

    public static void main(String[] args) {

        // String uuid = ImportByFile("/Users/Isolachine/tsdb/test", true, "ART", null);
        // System.out.println(CheckProgressWithUUID("b3422428-91e1-4ea1-ad38-1e13c0aa1a67"));
        // String[] allAR = Util.getAllCsvFileInDirectory("/Users/Isolachine/tsdb/test");
        // String[] allNoAR = Util.getAllCsvFileInDirectory("N:\\Test_NoAR\\");
        // String[] debugPth = Util.getAllCsvFileInDirectory("N:\\BK_NAR\\");
        //
        // Thread thread1 = new Thread(new Runnable() {
        //
        // @Override
        // public void run() {
        // ImportByList(allNoAR, false, "NoART");
        // }
        //
        // });
        //
        // Thread thread2 = new Thread(new Runnable() {
        //
        // @Override
        // public void run() {
        // ImportCsvService importCsvService = new ImportCsvService();
        // importCsvService.ImportByList(debugPth, false, "Debug");
        // }
        // });
        //
        // thread1.start();
        // thread2.start();

    }

}
