package app.service;


import app.common.DBConfiguration;
import app.common.InfluxappConfig;
import app.service.util.ImportProgressService;
import app.util.InfluxUtil;
import app.util.Util;
import okhttp3.OkHttpClient;
import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    private static final String dbName = DBConfiguration.Data.DBNAME;

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

    public static void main(String[] args) throws InterruptedException {
        ImportCsvService ics = new ImportCsvService();
        String[] allNoAr = Util.getAllCsvFileInDirectory("N:\\Test_NoAR\\");
        String[] allAr = Util.getAllCsvFileInDirectory("N:\\Test_AR\\");

        ics.AddArrayFiles(allNoAr);
        ics.AddArrayFiles(allAr);

        // Invoke this should finish fast, then the process is in the background
        ics.DoImport(0.01);

        // Wait 3s to get some results
        Thread.sleep(3000);
        double ovr = ImportProgressService.GetTaskOverallProgress(ics.GetUUID());
        Map<String, List<Object>> ss = ImportProgressService.GetTaskAllFileProgress(ics.GetUUID());

        System.out.println("Main exited, worker running...");
    }

    /**
     * Add one file to the queue (Blocking)
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
     * Add a list of files into the queue (Blocking)
     *
     * @param paths List of files
     */
    public void AddArrayFiles(String[] paths) {
        if (importingLock) return;
        for (String aPath : paths) {
            AddOneFile(aPath);
        }
    }

    private String processFirstLineInCSV(String fLine, String pid) {
        if (!fLine.toUpperCase().contains(pid))
            throw new RuntimeException("Wrong PID in filename!");
        if (fLine.length() < 50)
            throw new RuntimeException("File UUID misformat!");
        return fLine.substring(fLine.length() - 40, fLine.length() - 4);
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
                        + DBConfiguration.Sys.SYS_FILE_IMPORT_PROGRESS + "\" WHERE \"filename\" = '"
                        + p.toString().replace("\\", "\\\\") + "' GROUP BY \"filename\";", DBConfiguration.Sys.DBNAME);
        Map<String, List<Object>> tmpQ1 = InfluxUtil.QueryResultToKV(InfluxappConfig.INFLUX_DB.query(q));
        if (tmpQ1.size() == 0) {
            // Never updated
            res[2] = "0";
        } else {
            if (!tmpQ1.get("B").get(0).equals(String.valueOf(ImportProgressService.FileProgressStatus.STATUS_FAIL))) {
                // Already loaded before
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
        if (fn_laterpart.contains("noar")) {
            res[1] = "noar";
        } else if (fn_laterpart.contains("ar")) {
            res[1] = "ar";
        }
        return res;
    }

    /**
     * The main importing logic
     *
     * @return Obj[0]: Err msg; Obj[1]: Processed size.
     */
    private Object[] fileImport(InfluxDB idb, String ar_type, String pid, Path file, long aFileSize) {
        long currentProcessed = 0;
        String filename = file.getFileName().toString();

        try {
            BufferedReader reader = Files.newBufferedReader(file);

            // First line contains some curcial info
            String firstLine = reader.readLine();
            String fileUUID = processFirstLineInCSV(firstLine, pid);
            currentProcessed = firstLine.length();
            totalProcessedSize.addAndGet(firstLine.length());

            // Next 6 lines no use.
            long tmp_size = 0;
            for (int i = 0; i < 6; i++) {
                tmp_size += reader.readLine().length();
            }
            currentProcessed += tmp_size;
            totalProcessedSize.addAndGet(tmp_size);

            // 8th Line is column header line
            String eiL = reader.readLine();
            String[] columnNames = eiL.split(",");
            int columnCount = columnNames.length,
                    bulkInsertMax = InfluxappConfig.PERFORMANCE_INDEX / columnCount,
                    batchCount = 0;
            currentProcessed += eiL.length();
            totalProcessedSize.addAndGet(eiL.length());

            BatchPoints records = BatchPoints.database(dbName)
                    .tag("fileUUID", fileUUID)
                    .tag("arType", ar_type)
                    .tag("fileName", filename.substring(0, filename.length() - 4))
                    .build();

            String aLine;
            while ((aLine = reader.readLine()) != null) {
                String[] values = aLine.split(",");
                if (columnCount != values.length)
                    throw new RuntimeException("File content columns length inconsistent!");

                // Set initial capacity for slightly better performance
                Map<String, Object> lineKVMap = new HashMap<>((int) (columnCount / 0.70));
                for (int i = 1; i < values.length; i++) {
                    lineKVMap.put(columnNames[i], Double.valueOf(values[i]));
                }

                // Measurement is PID
                Point record = Point.measurement(pid)
                        .time(Util.serialTimeToLongDate(values[0], null), TimeUnit.MILLISECONDS)
                        .fields(lineKVMap).build();
                records.point(record);
                batchCount++;
                currentProcessed += aLine.length();
                totalProcessedSize.addAndGet(aLine.length());

                // Write batch into DB
                if (batchCount >= bulkInsertMax) {
                    idb.write(records);
                    // Reset batch point
                    records = BatchPoints.database(dbName)
                            .tag("fileUUID", fileUUID)
                            .tag("arType", ar_type)
                            .tag("fileName", filename.substring(0, filename.length() - 4))
                            .build();
                    batchCount = 0;
                    logImportingFile(file.toString(), aFileSize, currentProcessed);
                }
            }

            // Write the last batch
            reader.close();
            idb.write(records);
            idb.close();

        } catch (Exception e) {
            return new Object[]{Util.stackTraceErrorToString(e), currentProcessed};
        }

        return new Object[]{"OK", currentProcessed};
    }

    /**
     * Import file - internal method
     */
    private void internalImportMain(Path pFile) {
        long thisFileSize = everyFileSize.get(pFile.toString());
        String fileFullPath = pFile.toString();
        String[] fileInfo = checkerFromFilename(pFile);

        // Ar/NoAr Check & Response
        if (fileInfo[1] == null) {
            logFailureFiles(fileFullPath, "Ambiguous Ar/NoAr in file name.",
                    thisFileSize, 0);
            return;
        }

        // Duplication Check
        if (fileInfo[2].equals("1")) {
            //TODO: If the same file appears already, having a summary of comparing the contents of the new and old
            //TODO: file will be useful.  For example, if I generate a new set of CSVs using a novel signal processing
            //TODO: technique, I might want to concatenate the results with the existing time series.
            logFailureFiles(fileFullPath, "Older version (?) for the same file imported.",
                    thisFileSize, 0);
            return;
        } else if (fileInfo[2].equals("2")) {
            //TODO: Clean the failed table?
            logFailureFiles(fileFullPath, "Corrupted or finished import, TODO.",
                    thisFileSize, 0);
            return;
        }

        // New file, all good, just import!
        Object[] impStr = fileImport(generateIdbClient(), fileInfo[1], fileInfo[0], pFile, thisFileSize);
        if (impStr[0].equals("OK")) {
            logSuccessFiles(fileFullPath, thisFileSize, thisFileSize);
        } else {
            long procedSize = (long) impStr[1];
            totalAllSize.addAndGet(procedSize - thisFileSize);
            logFailureFiles(fileFullPath, (String) impStr[0], thisFileSize, procedSize);
        }
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

    private InfluxDB generateIdbClient() {
        InfluxDB idb = InfluxDBFactory.connect(
                InfluxappConfig.IFX_ADDR, InfluxappConfig.IFX_USERNAME, InfluxappConfig.IFX_PASSWD,
                new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(60, TimeUnit.SECONDS)
                        .writeTimeout(60, TimeUnit.SECONDS));
        // Disable GZip to save CPU
        idb.disableGzip();
        BatchOptions bo = BatchOptions.DEFAULTS
                .consistency(InfluxDB.ConsistencyLevel.ALL)
                // Flush every 2000 Points, at least every 100ms, buffer for failed oper is 2200
                .actions(2000).flushDuration(100).bufferLimit(2200);
        idb.enableBatch(bo);
        idb.createDatabase(dbName);
        return idb;
    }

}
