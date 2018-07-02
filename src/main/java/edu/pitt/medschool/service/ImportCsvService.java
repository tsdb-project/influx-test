package edu.pitt.medschool.service;

import edu.pitt.medschool.config.DBConfiguration;
import edu.pitt.medschool.config.InfluxappConfig;
import edu.pitt.medschool.framework.util.FileLockUtil;
import edu.pitt.medschool.framework.util.TimeUtil;
import edu.pitt.medschool.framework.util.Util;
import edu.pitt.medschool.model.dao.ImportedFileDao;
import edu.pitt.medschool.model.dao.InfluxClusterDao;
import edu.pitt.medschool.model.dto.ImportedFile;
import okhttp3.OkHttpClient;
import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Auto-parallel importing CSV data
 */
@Service
public class ImportCsvService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private final int availCores = Runtime.getRuntime().availableProcessors();
    @Value("${machine}")
    private String taskUUID;
    private String batchId = UUID.randomUUID().toString();
    private final AtomicLong totalAllSize = new AtomicLong(0);
    private final AtomicLong totalProcessedSize = new AtomicLong(0);
    private final Map<String, Long> everyFileSize = new HashMap<>();
    private final Map<String, Integer> importFailCounter = new HashMap<>();

    private final String dbName = DBConfiguration.Data.DBNAME;
    @Value("${load}")
    private double loadFactor;
    @Value("${soft-timeout}")
    private int softTimeout;
    @Value("${soft-timeout-sleep}")
    private long timeoutSleep;

    private final AtomicBoolean importingLock = new AtomicBoolean(false);

    private final BlockingQueue<Path> fileQueue = new LinkedBlockingQueue<>();
    private final Set<Path> processingSet = new HashSet<>();

    // private static final int FAILURE_RETRY = 3;

    @Autowired
    private ImportedFileDao ifd;

    @Autowired
    private ImportProgressService ips;

    @Autowired
    InfluxClusterDao influxClusterDao;

    public double GetLoadFactor() {
        return loadFactor;
    }

    public String GetUUID() {
        return this.taskUUID;
    }

    public String getBatchId() {
        return this.batchId;
    }

    public void _test() throws InterruptedException {
        String[] testFiles = Util.getAllCsvFileInDirectory("/home/tonyz-remote/Desktop/E/je_test_data/");

        AddArrayFiles(testFiles);

        System.out.println("Sleep 3s...");
        Thread.sleep(3000);

        System.out.println("Main exited, worker running...");
    }

    /**
     * Add one file to the queue (Blocking)
     *
     * @param path File path
     */
    public void AddOneFile(String path) {
        this.internalAddOne(path, false);
    }

    private void newBatch() {
        batchId = UUID.randomUUID().toString();
        totalAllSize.set(0);
        totalProcessedSize.set(0);
        everyFileSize.clear();
        importFailCounter.clear();
    }

    /**
     * Add a list of files into the queue (Blocking)
     *
     * @param paths List of files
     */
    public void AddArrayFiles(String[] paths) {
        if (processingSet.isEmpty()) {
            newBatch();
        }
        for (String aPath : paths) {
            this.internalAddOne(aPath, true);
        }
        this.startImport();
    }

    private void internalAddOne(String path, boolean isInvokedByAddArrayFiles) {
        Path p = Paths.get(path);

        try {
            long currS = Files.size(p);

            if (FileLockUtil.isLocked(p.toFile())) {
                throw new RuntimeException(path + " is currently being imported.");
            }

            if (FileLockUtil.aquire(p.toFile())) {
                totalAllSize.addAndGet(currS);
                everyFileSize.put(p.toString(), currS);
                fileQueue.offer(p);
                logQueuedFile(path, currS);
                if (!isInvokedByAddArrayFiles)
                    this.startImport();
            }
        } catch (IOException e) {
            logger.error(Util.stackTraceErrorToString(e));
            FileLockUtil.release(p.toFile());
        } catch (RuntimeException e) {
            logger.error(Util.stackTraceErrorToString(e));
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

            // Next 6 lines no use expect time date
            long tmp_size = 0;
            String test_date = "";
            for (int i = 0; i < 6; i++) {
                String tmp = reader.readLine();
                tmp_size += tmp.length();
                switch (i) {
                    case 3:
                    case 4:
                        test_date += tmp.split(",")[1].trim();
                        break;
                }
            }
            Date testStartDate = TimeUtil.dateTimeFormatToDate(test_date, "yyyy.MM.ddHH:mm:ss", TimeUtil.nycTimeZone);
            long test_start_time = testStartDate.getTime();
            boolean isTestOnDstShift = TimeUtil.isThisDayOnDstShift(TimeUtil.nycTimeZone, testStartDate);

            currentProcessed += tmp_size;
            totalProcessedSize.addAndGet(tmp_size);

            // 8th Line is column header line
            String eiL = reader.readLine();
            String[] columnNames = eiL.split(",");
            int columnCount = columnNames.length, bulkInsertMax = InfluxappConfig.PERFORMANCE_INDEX / columnCount, batchCount = 0;

            // More integrity checking
            if (!columnNames[0].toLowerCase().equals("clockdatetime")) {
                throw new RuntimeException("Wriong first column!");
            }

            currentProcessed += eiL.length();
            totalProcessedSize.addAndGet(eiL.length());

            BatchPoints records = BatchPoints.database(dbName).tag("fileUUID", fileUUID).tag("arType", ar_type).tag("fileName", filename.substring(0, filename.length() - 4)).build();

            String aLine;
            // Process every data lines
            while ((aLine = reader.readLine()) != null) {
                String[] values = aLine.split(",");
                int this_line_length = aLine.length();

                totalLines++;

                if (columnCount != values.length) {
                    String err_text = String.format("File content columns length inconsistent on line %d!", totalLines + 8);
                    logFailureFiles(file.toString(), err_text, aFileSize, currentProcessed, true);
                    currentProcessed += this_line_length;
                    totalProcessedSize.addAndGet(this_line_length);
                    continue;
                }

                // Compare date on every measures (They are all UTCs)
                double sTime = Double.valueOf(values[0]);
                Date measurement_date = TimeUtil.serialTimeToDate(sTime, null);
                long measurement_epoch_time = measurement_date.getTime();
                if (!TimeUtil.dateIsSameDay(measurement_date, testStartDate)) {
                    // To avoid some problematic files, that measurement date is not reliable
                    String err_text = String.format("Measurement date differs from test start date on line %d!", totalLines + 8);
                    logFailureFiles(file.toString(), err_text, aFileSize, currentProcessed, true);
                    continue;
                }
                // At this time, we can arbitrarily add or sub one hour
                if (isTestOnDstShift) {
                    // IT'S THE US LAW
                    if (measurement_date.getMonth() == Calendar.MARCH)
                        measurement_epoch_time = TimeUtil.addOneHourToTimestamp(measurement_epoch_time);
                    else if (measurement_date.getMonth() == Calendar.NOVEMBER)
                        measurement_epoch_time = TimeUtil.subOneHourToTimestamp(measurement_epoch_time);
                }
                // Measurement time should be later than test start time
                if (measurement_epoch_time < test_start_time) {
                    String err_text = String.format("Measurement time earlier than test start time on line %d!", totalLines + 8);
                    logFailureFiles(file.toString(), err_text, aFileSize, currentProcessed, true);
                    continue;
                }

                // Set initial capacity for slightly better performance
                Map<String, Object> lineKVMap = new HashMap<>((int) (columnCount / 0.70));
                boolean gotParseProblem = false;
                for (int i = 1; i < values.length; i++) {
                    try {
                        lineKVMap.put(columnNames[i], Double.valueOf(values[i]));
                    } catch (NumberFormatException nfe) {
                        currentProcessed += this_line_length;
                        totalProcessedSize.addAndGet(this_line_length);
                        gotParseProblem = true;
                        break;
                    }
                }
                if (gotParseProblem) {
                    String err_text = String.format("Failed to parse number on line %d!", totalLines + 8);
                    logFailureFiles(file.toString(), err_text, aFileSize, currentProcessed, true);
                    continue;
                }

                // Measurement is PID
                Point record = Point.measurement(pid).time(measurement_epoch_time, TimeUnit.MILLISECONDS).fields(lineKVMap).build();
                records.point(record);
                batchCount++;
                currentProcessed += this_line_length;
                totalProcessedSize.addAndGet(this_line_length);

                // Write batch into DB
                if (batchCount >= bulkInsertMax) {
                    long start = System.currentTimeMillis();
                    idb.write(records);
                    long end = System.currentTimeMillis();
                    logger.debug("used " + (end - start) / 1000.0 + "s");

                    // Reset batch point
                    records = BatchPoints.database(dbName).tag("fileUUID", fileUUID).tag("arType", ar_type).tag("fileName", filename.substring(0, filename.length() - 4)).build();
                    batchCount = 0;
                    logImportingFile(file.toString(), aFileSize, currentProcessed, totalLines);

                    if (end - start > softTimeout) {
                        logger.debug("Sleeping for " + timeoutSleep + " seconds");
                        TimeUnit.SECONDS.sleep(timeoutSleep);
                    }
                }
            }

            // Write the last batch
            reader.close();
            idb.write(records);
            idb.close();

        } catch (Exception e) {
            try {
                logger.debug("Sleeping for " + timeoutSleep + " seconds");
                TimeUnit.SECONDS.sleep(timeoutSleep);
            } catch (InterruptedException e1) {
                logger.error(Util.stackTraceErrorToString(e1));
            }
            return new Object[]{Util.stackTraceErrorToString(e), currentProcessed};
        }

        return new Object[]{"OK", currentProcessed, totalLines};
    }

    /**
     * Import file - internal method
     */
    private void internalImportMain(Path pFile) {
        long thisFileSize = everyFileSize.get(pFile.toString());
        String fileFullPath = pFile.toString(), fileName = pFile.getFileName().toString();
        String[] fileInfo = checkerFromFilename(fileName, thisFileSize);
        processingSet.add(pFile);

        // Ar/NoAr Check & Response
        if (fileInfo[1] == null) {
            logFailureFiles(fileFullPath, "Ambiguous Ar/NoAr in file name.", thisFileSize, 0, false);
            FileLockUtil.release(fileFullPath);
            processingSet.remove(pFile);
            transferFailedFiles(pFile);
            return;
        }

        // Duplication Check
        if (fileInfo[2].equals("1")) {
            // TODO: If the same file appears already, having a summary of comparing the contents of the new and old
            logFailureFiles(fileFullPath, "The same file has been imported.", thisFileSize, 0, false);
            FileLockUtil.release(fileFullPath);
            processingSet.remove(pFile);
            return;
        }

        // New file, all good, just import!
        Object[] impStr = fileImport(generateIdbClient(), fileInfo[1], fileInfo[0], pFile, thisFileSize);
        if (impStr[0].equals("OK")) {
            // Import success
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
                logger.error("File name is: " + fileFullPath + "\n" + Util.stackTraceErrorToString(e));
            }
            // keep processed size consistent with the actual file size once a file is done with the import process
            totalProcessedSize.addAndGet(thisFileSize - (Long) impStr[1]);
            processingSet.remove(pFile);
            logSuccessFiles(fileFullPath, thisFileSize, thisFileSize);
            FileLockUtil.release(fileFullPath);
            importFailCounter.remove(fileFullPath);
        } else {
            // Import fail
            long procedSize = (long) impStr[1];
            logFailureFiles(fileFullPath, (String) impStr[0], thisFileSize, procedSize, false);
            FileLockUtil.release(fileFullPath);
            processingSet.remove(pFile);
            logger.error((String) impStr[0]);
            transferFailedFiles(pFile);

            // if (importFailCounter.containsKey(fileFullPath)) {
            // int current_fails = importFailCounter.get(fileFullPath);
            // // Only retry 3 times
            // if (++current_fails <= FAILURE_RETRY) {
            // internalAddOne(fileFullPath, false);
            // importFailCounter.put(fileFullPath, current_fails);
            // } else {
            // transferFailedFiles(pFile);
            // }
            // } else {
            // importFailCounter.put(fileFullPath, 1);
            // }
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

    private void logQueuedFile(String fn, long thisFileSize) {
        ips.UpdateFileProgress(batchId, fn, totalAllSize.get(), thisFileSize, 0, totalProcessedSize.get(), ImportProgressService.FileProgressStatus.STATUS_QUEUED, null);
    }

    private void logSuccessFiles(String fn, long thisFileSize, long thisFileProcessedSize) {
        ips.UpdateFileProgress(batchId, fn, totalAllSize.get(), thisFileSize, thisFileProcessedSize, totalProcessedSize.get(), ImportProgressService.FileProgressStatus.STATUS_FINISHED, null);
    }

    private void logImportingFile(String fn, long thisFileSize, long thisFileProcessedSize, long currentLine) {
        ips.UpdateFileProgress(batchId, fn, totalAllSize.get(), thisFileSize, thisFileProcessedSize, totalProcessedSize.get(), ImportProgressService.FileProgressStatus.STATUS_INPROGRESS,
                String.valueOf(currentLine));
    }

    private void logFailureFiles(String fn, String reason, long thisFileSize, long thisFileProcessedSize, boolean isSoftError) {
        if (!isSoftError) {
            totalAllSize.addAndGet(thisFileProcessedSize - thisFileSize);
        }
        ips.UpdateFileProgress(batchId, fn, totalAllSize.get(), thisFileSize, thisFileProcessedSize, totalProcessedSize.get(), ImportProgressService.FileProgressStatus.STATUS_FAIL, reason);
    }

    private void transferFailedFiles(Path path) {
        String dumpPath = influxClusterDao.selectByMachineId(taskUUID).getFailPath();
        try {
            Path newPath = Paths.get(dumpPath + path.getFileName());
            Files.move(path, newPath, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            logger.error(Util.stackTraceErrorToString(e));
        }
    }

    /**
     * Generate IdbClient for Importing CSVs
     */
    private InfluxDB generateIdbClient() {
        InfluxDB idb = InfluxDBFactory.connect(InfluxappConfig.IFX_ADDR, InfluxappConfig.IFX_USERNAME, InfluxappConfig.IFX_PASSWD,
                new OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS).readTimeout(120, TimeUnit.SECONDS).writeTimeout(120, TimeUnit.SECONDS));
        // Disable GZip to save CPU
        idb.disableGzip();
        BatchOptions bo = BatchOptions.DEFAULTS.consistency(InfluxDB.ConsistencyLevel.ALL)
                // Flush every 2000 Points, at least every 100ms, buffer for failed oper is 2200
                .actions(2000).flushDuration(100).bufferLimit(2200);
        idb.enableBatch(bo);
        idb.query(new Query(String.format("CREATE DATABASE \"%s\"", dbName), dbName));

        return idb;
    }

}
