package edu.pitt.medschool.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import edu.pitt.medschool.config.DBConfiguration;
import edu.pitt.medschool.config.InfluxappConfig;
import edu.pitt.medschool.framework.util.Util;
import edu.pitt.medschool.model.dao.ImportedFileDao;
import edu.pitt.medschool.model.dto.ImportedFile;
import okhttp3.OkHttpClient;

/**
 * Auto-parallel importing CSV data
 */
@Service
public class ImportCsvService {

    private final int availCores = Runtime.getRuntime().availableProcessors();
    @Value("${machine}")
    private String taskUUID;
    private final AtomicLong totalAllSize = new AtomicLong(0);
    private final AtomicLong totalProcessedSize = new AtomicLong(0);
    private final Map<String, Long> everyFileSize = new HashMap<>();
    private final Map<String, Integer> importFailCounter = new HashMap<>();

    private final String dbName = DBConfiguration.Data.DBNAME;
    private double loadFactor = 0.5;

    private final AtomicBoolean importingLock = new AtomicBoolean(false);

    private final BlockingQueue<Path> fileQueue = new LinkedBlockingQueue<>();

    @Autowired
    private ImportedFileDao ifd;

    @Autowired
    private ImportProgressService ips;

    public double GetLoadFactor() {
        return loadFactor;
    }

    /**
     * Set a load factor for importing
     */
    public void SetLoadFactor(double loadFactor) {
        this.loadFactor = loadFactor;
    }

    public String GetUUID() {
        return this.taskUUID;
    }

    public void _test() throws InterruptedException {
        String[] allNoAr = Util.getAllCsvFileInDirectory("N:\\1\\");
        String[] allAr = Util.getAllCsvFileInDirectory("N:\\2\\");

        AddArrayFiles(allNoAr);
        AddArrayFiles(allAr);

        System.out.println("Sleep 3s...");
        Thread.sleep(3000);

        // Add again
        AddOneFile("N:\\Test_AR\\PUH-2010-076_07ar.csv");

        System.out.println("Main exited, worker running...");
    }

    /**
     * Add one file to the queue (Blocking)
     *
     * @param path
     *            File path
     */
    public void AddOneFile(String path) {
        this.internalAddOne(path, false);
    }

    /**
     * Add a list of files into the queue (Blocking)
     *
     * @param paths
     *            List of files
     */
    public void AddArrayFiles(String[] paths) {
        for (String aPath : paths) {
            this.internalAddOne(aPath, true);
        }
        this.startImport();
    }

    private void internalAddOne(String path, boolean isInvokedByAddArrayFiles) {
        Path p = Paths.get(path);
        try {
            long currS = Files.size(p);
            totalAllSize.addAndGet(currS);
            everyFileSize.put(p.toString(), currS);
            fileQueue.offer(p);
            if (!isInvokedByAddArrayFiles)
                this.startImport();
        } catch (IOException e) {
            // TODO: A separate log table for system failures
            // logFailureFiles(p.toString(), e.getLocalizedMessage(), 0, 0);
            e.printStackTrace();
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
     * @return 0: PID; 1: ar/noar; 2: Has been uploaded [0,1,2] (Never, Duplicate, Failed(ignored))
     */
    private String[] checkerFromFilename(String filename, long filesize) {
        String[] res = new String[3];
        // Has been uploaded successfully according to the log?
        boolean hasImported = ifd.checkHasImported(taskUUID, filename, filesize);
        if (!hasImported) {
            // Never updated
            res[2] = "0";
        } else {
            // Ignore fails (res[2]="2")
            res[2] = "1";
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
     * The main importing logic (In InfluxDB)
     *
     * @return Obj[0]: Err msg; Obj[1]: Processed size.
     */
    private Object[] fileImport(InfluxDB idb, String ar_type, String pid, Path file, long aFileSize) {
        long currentProcessed = 0;
        String filename = file.getFileName().toString();
        long totalLines = 0;

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
            int columnCount = columnNames.length, bulkInsertMax = InfluxappConfig.PERFORMANCE_INDEX / columnCount, batchCount = 0;
            currentProcessed += eiL.length();
            totalProcessedSize.addAndGet(eiL.length());

            BatchPoints records = BatchPoints.database(dbName).tag("fileUUID", fileUUID).tag("arType", ar_type).tag("fileName", filename.substring(0, filename.length() - 4)).build();

            String aLine;
            while ((aLine = reader.readLine()) != null) {
                String[] values = aLine.split(",");
                if (columnCount != values.length)
                    throw new RuntimeException("File content columns length inconsistent!");

                totalLines++;
                // Set initial capacity for slightly better performance
                Map<String, Object> lineKVMap = new HashMap<>((int) (columnCount / 0.70));
                for (int i = 1; i < values.length; i++) {
                    lineKVMap.put(columnNames[i], Double.valueOf(values[i]));
                }

                // Measurement is PID
                Point record = Point.measurement(pid).time(Util.serialTimeToLongDate(values[0], null), TimeUnit.MILLISECONDS).fields(lineKVMap).build();
                records.point(record);
                batchCount++;
                currentProcessed += aLine.length();
                totalProcessedSize.addAndGet(aLine.length());

                // Write batch into DB
                if (batchCount >= bulkInsertMax) {
                    idb.write(records);
                    // Reset batch point
                    records = BatchPoints.database(dbName).tag("fileUUID", fileUUID).tag("arType", ar_type).tag("fileName", filename.substring(0, filename.length() - 4)).build();
                    batchCount = 0;
                    logImportingFile(file.toString(), aFileSize, currentProcessed);
                }
            }

            // Write the last batch
            reader.close();
            idb.write(records);
            idb.close();

        } catch (Exception e) {
            return new Object[] { Util.stackTraceErrorToString(e), currentProcessed };
        }

        return new Object[] { "OK", currentProcessed, totalLines };
    }

    /**
     * Import file - internal method
     */
    private void internalImportMain(Path pFile) {
        long thisFileSize = everyFileSize.get(pFile.toString());
        String fileFullPath = pFile.toString(), fileName = pFile.getFileName().toString();
        String[] fileInfo = checkerFromFilename(fileName, thisFileSize);

        // Ar/NoAr Check & Response
        if (fileInfo[1] == null) {
            logFailureFiles(fileFullPath, "Ambiguous Ar/NoAr in file name.", thisFileSize, 0);
            return;
        }

        // Duplication Check
        if (fileInfo[2].equals("1")) {
            // TODO: If the same file appears already, having a summary of comparing the contents of the new and old
            logFailureFiles(fileFullPath, "The same file has been imported.", thisFileSize, 0);
            return;
        }

        // New file, all good, just import!
        Object[] impStr = fileImport(generateIdbClient(), fileInfo[1], fileInfo[0], pFile, thisFileSize);
        if (impStr[0].equals("OK")) {
            try {
                ImportedFile iff = new ImportedFile();
                iff.setFilename(fileName);
                iff.setFilepath(fileFullPath);
                iff.setFilesize(thisFileSize);
                iff.setPid(fileInfo[0]);
                iff.setIsar(fileInfo[1].equals("ar"));
                iff.setFilelines(((Long) impStr[2]).intValue());
                iff.setUuid(taskUUID);
                ifd.insert(iff);
            } catch (Exception e) {
                System.out.println("File name is: " + fileFullPath);
                e.printStackTrace();
            }
            logSuccessFiles(fileFullPath, thisFileSize, thisFileSize);
            importFailCounter.remove(fileFullPath);
        } else {
            long procedSize = (long) impStr[1];
            logFailureFiles(fileFullPath, (String) impStr[0], thisFileSize, procedSize);

            if (importFailCounter.containsKey(fileFullPath)) {
                int current_fails = importFailCounter.get(fileFullPath);
                // Only retry 3 times
                if (++current_fails <= 3) {
                    internalAddOne(fileFullPath, false);
                    importFailCounter.put(fileFullPath, current_fails);
                }
            } else {
                importFailCounter.put(fileFullPath, 1);
            }
        }
    }

    private void startImport() {
        if (importingLock.get())
            return;

        int paraCount = (int) Math.round(loadFactor * availCores);
        paraCount = paraCount > 0 ? paraCount : 1;
        ExecutorService scheduler = Executors.newFixedThreadPool(paraCount);

        Runnable importTask = () -> {
            Path aFilePath;
            while ((aFilePath = fileQueue.poll()) != null) {
                internalImportMain(aFilePath);
            }
            // Queue empty now, safe to re-import
            importingLock.set(false);
        };

        // importingLock must be false (not locked) to start new threads
        if (!importingLock.compareAndSet(false, true))
            return;

        for (int i = 0; i < paraCount; ++i)
            scheduler.submit(importTask);
        scheduler.shutdown();
    }

    private void logSuccessFiles(String fn, long thisFileSize, long thisFileProcessedSize) {
        ips.UpdateFileProgress(fn, totalAllSize.get(), thisFileSize, thisFileProcessedSize, totalProcessedSize.get(), ImportProgressService.FileProgressStatus.STATUS_FINISHED, null);
    }

    private void logImportingFile(String fn, long thisFileSize, long thisFileProcessedSize) {
        ips.UpdateFileProgress(fn, totalAllSize.get(), thisFileSize, thisFileProcessedSize, totalProcessedSize.get(), ImportProgressService.FileProgressStatus.STATUS_INPROGRESS, null);
    }

    private void logFailureFiles(String fn, String reason, long thisFileSize, long thisFileProcessedSize) {
        totalAllSize.addAndGet(thisFileProcessedSize - thisFileSize);
        ips.UpdateFileProgress(fn, totalAllSize.get(), thisFileSize, thisFileProcessedSize, totalProcessedSize.get(), ImportProgressService.FileProgressStatus.STATUS_FAIL, reason);
    }

    /**
     * Generate IdbClient for Importing CSVs
     */
    private InfluxDB generateIdbClient() {
        InfluxDB idb = InfluxDBFactory.connect(InfluxappConfig.IFX_ADDR, InfluxappConfig.IFX_USERNAME, InfluxappConfig.IFX_PASSWD, new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(60, TimeUnit.SECONDS).writeTimeout(60, TimeUnit.SECONDS));
        // Disable GZip to save CPU
        idb.disableGzip();
        BatchOptions bo = BatchOptions.DEFAULTS.consistency(InfluxDB.ConsistencyLevel.ALL)
                // Flush every 2000 Points, at least every 100ms, buffer for failed oper is 2200
                .actions(2000).flushDuration(100).bufferLimit(2200);
        idb.enableBatch(bo);
        idb.createDatabase(dbName);
        return idb;
    }

}
