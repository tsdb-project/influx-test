package app.service;


import app.common.InfluxappConfig;
import app.common.Measurement;
import app.service.util.ImportProgressService;
import app.util.InfluxUtil;
import app.util.Util;
import com.opencsv.CSVReader;
import okhttp3.OkHttpClient;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Auto-parallel importing CSV data
 */
@Service
public class ImportCsvService {

    private final int availCores = Runtime.getRuntime().availableProcessors();
    private final String taskUUID = UUID.randomUUID().toString();
    private final AtomicLong totalAllSize = new AtomicLong(0);
    private final AtomicLong totalProcessedSize = new AtomicLong(0);
    private final Map<String, Long> everyFileSize = new HashMap<>();

    private static final String dbName = InfluxappConfig.IFX_DBNAME;

    private boolean importingLock = false;

    private final BlockingQueue<Path> fileQueue = new LinkedBlockingQueue<>();
    private final ImportProgressService ips = new ImportProgressService(this.taskUUID);

    /**
     * Get UUID for this task
     */
    public String GetUUID() {
        return this.taskUUID;
    }

    /**
     * Start multi-thread importing
     * Once start, this class should be locked
     */
    public void DoImport(double loadFactor) {
        if (importingLock) return;
        if (fileQueue.isEmpty()) return;

        int paraCount = (int) Math.round(loadFactor * availCores);
        paraCount = paraCount > 0 ? paraCount : 1;
        ExecutorService scheduler = Executors.newFixedThreadPool(paraCount);

        Runnable importTask = () -> {
            Path aFilePath;
            while ((aFilePath = fileQueue.poll()) != null) {
                internalImportMain(aFilePath);
            }
        };

        importingLock = true;
        for (int i = 0; i < paraCount; ++i)
            scheduler.submit(importTask);
        scheduler.shutdown();
    }

    /**
     * Add one file to the queue
     *
     * @param path File path
     */
    public void AddOneFile(String path) {
        if (importingLock) return;
        Path p = Paths.get(path);
        try {
            long currS = Files.size(p);
            totalAllSize.addAndGet(currS);
            everyFileSize.put(p.toString(), currS);
            fileQueue.offer(p);
        } catch (IOException e) {
            logFailureFiles(p.toString(), e.getLocalizedMessage(), 0, 0);
            e.printStackTrace();
        }
    }

    /**
     * Add a list of files into the queue
     *
     * @param paths List of files
     */
    public void AddArrayFiles(String[] paths) {
        if (importingLock) return;
        for (String aPath : paths) {
            AddOneFile(aPath);
        }
    }

    /**
     * Lots of info in Filename
     *
     * @param p NIO Paths
     * @return 0: PID; 1: ar/noar; 2: Has been uploaded [0,1,2] (Never, Duplicate, Failed)
     */
    private String[] checkerFromFilename(Path p) {
        String[] res = new String[3];
        String filename = p.getFileName().toString();
        // Has been uploaded successfully according to the log?
        Query q = new Query(
                "SELECT MAX(\"CurrentPercent\") AS A, \"status\" AS B FROM \""
                        + Measurement.SYS_FILE_IMPORT_PROGRESS + "\" WHERE \"filename\" = '"
                        + p.toString().replace("\\", "\\\\") + "' GROUP BY \"filename\";", InfluxappConfig.SYSTEM_DBNAME);
        Map<String, List<Object>> tmpQ1 = InfluxUtil.QueryResultToKV(InfluxappConfig.INFLUX_DB.query(q));
        if (tmpQ1.size() == 0) {
            // Never updated
            res[2] = "0";
        } else {
            if (!tmpQ1.get("B").get(0).equals(String.valueOf(ImportProgressService.FileProgressStatus.STATUS_FAIL))) {
                res[2] = "1";
            } else {
                // Uploaded but failed
                res[2] = "2";
            }
        }
        // PUH-20xx_xxx
        res[0] = filename.substring(0, 12).trim().toUpperCase();
        String fn_laterpart = filename.substring(12).toLowerCase();
        // Ar or NoAr
        if (fn_laterpart.contains("ar")) {
            res[1] = "ar";
        } else if (fn_laterpart.contains("noar")) {
            res[1] = "noar";
        }
        return res;
    }

    private String processFirstLineInCSV(String fLine, String pid) {
        if (!fLine.toUpperCase().contains(pid))
            throw new RuntimeException("Wrong PID in filename!");
        if (fLine.length() < 50)
            throw new RuntimeException("File UUID misformat!");
        return fLine.substring(fLine.length() - 40, fLine.length() - 4);
    }

    private String fileImport(InfluxDB idb, String ar_type, String pid, Path file) {
        try {
            BufferedReader reader = Files.newBufferedReader(file);

            String firstLine = reader.readLine();
            String fileUUID = processFirstLineInCSV(firstLine, pid);
            long currentProcessed = firstLine.length();

            // Next 6 lines no use.
            long tmp_size = 0;
            for (int i = 0; i < 6; i++) {
                tmp_size += reader.readLine().length();
            }
            currentProcessed += tmp_size;

            // 8th Line
            String eiL = reader.readLine();
            currentProcessed += eiL.length();
            String[] columnNames = eiL.split(",");
            int columnCount = columnNames.length,
                    bulkInsertMax = InfluxappConfig.PERFORMANCE_INDEX / columnCount,
                    batchCount = 0;

            BatchPoints records = BatchPoints.database(dbName).consistency(InfluxDB.ConsistencyLevel.ALL).build();
            String tableName = Measurement.DATA_PREFIX + pid;

            Map<String, String> dataTag = new HashMap<>(5);
            dataTag.put("fileUUID", fileUUID);
            dataTag.put("arType", ar_type);

            String aLine;
            while ((aLine = reader.readLine()) != null) {
                String[] values = aLine.split(",");
                if (columnCount != values.length)
                    throw new RuntimeException("File content inconsistent!");

                // Large initial cap for better importing performance
                Map<String, Object> lineKVMap = new HashMap<>((int) (columnCount / 0.70));
                for (int i = 1; i < values.length; i++) {
                    lineKVMap.put(columnNames[i], Double.valueOf(values[i]));
                }

                // Table with ID for each patient
                Point record = Point.measurement(tableName)
                        .time(Util.serialTimeToLongDate(values[0], null), TimeUnit.MILLISECONDS)
                        .tag(dataTag).fields(lineKVMap).build();
                records.point(record);
                batchCount++;
                currentProcessed += aLine.length();

                // Write batch into DB
                if (batchCount >= bulkInsertMax) {
                    idb.write(records);
                    // Reset batch point
                    records = BatchPoints.database(dbName).consistency(InfluxDB.ConsistencyLevel.ALL).build();
                    batchCount = 0;
                    totalProcessedSize.addAndGet(currentProcessed);
                    logImportingFile(file.toString(), everyFileSize.get(file.toString()), currentProcessed);
                }
            }

            // Write the last batch
            reader.close();
            idb.write(records);
            idb.close();
            totalProcessedSize.addAndGet(currentProcessed);

        } catch (Exception e) {
            e.printStackTrace();
            return e.getLocalizedMessage();
        }

        return "OK";
    }

    /**
     * Import file - internal method
     *
     * @param pFile File Path NIO class
     */
    private void internalImportMain(Path pFile) {
        String fileFullPath = pFile.toString();
        String[] fileInfo = checkerFromFilename(pFile);

        // Ar/NoAr Check & Response
        if (fileInfo[1] == null) {
            logFailureFiles(fileFullPath, "Ambiguous Ar/NoAr in file name.",
                    everyFileSize.get(fileFullPath), 0);
            return;
        }

        // Duplicate Check
        if (fileInfo[2].equals("1")) {
            //TODO: If the same file appears already, having a summary of comparing the contents of the new and old
            //TODO: file will be useful.  For example, if I generate a new set of CSVs using a novel signal processing
            //TODO: technique, I might want to concatenate the results with the existing time series.
            logFailureFiles(fileFullPath, "Older version (?) for the same file imported.",
                    everyFileSize.get(fileFullPath), 0);
            return;
        } else if (fileInfo[2].equals("2")) {
            //TODO: Clean the failed table?
            logFailureFiles(fileFullPath, "Corrupted import, TODO.",
                    everyFileSize.get(fileFullPath), 0);
            return;
        }

        // New file, all good, just import!
        String impStr = fileImport(generateIdb(), fileInfo[1], fileInfo[0], pFile);
        if (impStr.equals("OK")) {
            logSuccessFiles(fileFullPath, everyFileSize.get(fileFullPath), everyFileSize.get(fileFullPath));
        } else {
            logFailureFiles(fileFullPath, impStr, everyFileSize.get(fileFullPath), 0);
        }
    }

    private InfluxDB generateIdb() {
        InfluxDB idb = InfluxDBFactory.connect(InfluxappConfig.IFX_ADDR, InfluxappConfig.IFX_USERNAME,
                InfluxappConfig.IFX_PASSWD,
                new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(60, TimeUnit.SECONDS)
                        .writeTimeout(60, TimeUnit.SECONDS));
        // Disable GZip to save CPU
        idb.disableGzip();
        String dbName = InfluxappConfig.IFX_DBNAME;
        idb.createDatabase(dbName);
        return idb;
    }

    private void logSuccessFiles(String fn, long thisFileSize, long thisFileProcessedSize) {
        ips.UpdateFileProgress(fn, totalAllSize.get(), thisFileSize, thisFileProcessedSize, totalProcessedSize.get(),
                ImportProgressService.FileProgressStatus.STATUS_FINISHED, null);
    }

    private void logImportingFile(String fn, long thisFileSize, long thisFileProcessedSize) {
        ips.UpdateFileProgress(fn, totalAllSize.get(), thisFileSize, thisFileProcessedSize, totalProcessedSize.get(),
                ImportProgressService.FileProgressStatus.STATUS_INPROGRESS, null);
    }

    private void logFailureFiles(String fn, String reason, long thisFileSize, long thisFileProcessedSize) {
        ips.UpdateFileProgress(fn, totalAllSize.get(), thisFileSize, thisFileProcessedSize, totalProcessedSize.get(),
                ImportProgressService.FileProgressStatus.STATUS_FAIL, reason);
    }

    public static void main(String[] args) {
        ImportCsvService ics = new ImportCsvService();
        String[] allNoAr = Util.getAllCsvFileInDirectory("N:\\Test_NoAR\\");
        String[] allAr = Util.getAllCsvFileInDirectory("N:\\Test_AR\\");

        ics.AddArrayFiles(allNoAr);
        ics.AddArrayFiles(allAr);

        // Invoke this should finish fast, then the process is in the background
        ics.DoImport(0.01);
    }

}
