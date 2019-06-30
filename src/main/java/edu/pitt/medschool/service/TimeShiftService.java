package edu.pitt.medschool.service;

import com.opencsv.CSVReader;
import edu.pitt.medschool.config.DBConfiguration;
import edu.pitt.medschool.config.InfluxappConfig;
import edu.pitt.medschool.framework.influxdb.InfluxUtil;
import edu.pitt.medschool.framework.util.FileBean;
import edu.pitt.medschool.framework.util.FileLockUtil;
import edu.pitt.medschool.framework.util.TimeUtil;
import edu.pitt.medschool.framework.util.Util;
import edu.pitt.medschool.model.dao.*;
import edu.pitt.medschool.model.dto.*;
import org.apache.commons.lang3.StringUtils;
import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TimeShiftService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final int NORMAL_CSV_COLUMN_COUNT = 6039;
    private static final List<Integer> CSV_COLUMN_NAME_DUPLICATE_INDICES = Arrays.asList(166, 188, 190);

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

    @Autowired
    CsvLogDao csvLogDao;

    @Autowired
    FeatureDao featureDao;

    @Autowired
    ImportedFileDao importedFileDao;

    @Autowired
    CsvFileDao csvFileDao;

    @Autowired
    ValidateCsvService validateCsvService;

    @Autowired
    ImportProgressService importProgressService;

    @Autowired
    VersionControlService versionControlService;

    @Autowired
    InfluxClusterDao influxClusterDao;

    public String GetUUID() {
        return this.taskUUID;
    }

    public String getBatchId() {
        return this.batchId;
    }

    public void fixTimeDrift(){

        // files path
        String[] FilesPaths = new String[]{"/Volumes/16TB Drive 2/PUH CSV AR/","/Volumes/16TB Drive 2/PUH CSV NOAR/"};

        int deleteResult = 0;
        try {
            HashMap<String,Long> driftMap = analyzeCSV();

            for (String path : FilesPaths){
//                find corresponding files in this folder
            List<CsvFile> driftARFileList = findCorrespondingFiles(driftMap,path);

//                delete those files from both databases
             deleteResult = deleteOriginalFiles(driftARFileList);

             if (deleteResult !=0){
                reimport(driftARFileList,driftMap);
            }

            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private List<CsvFile> findCorrespondingFiles(HashMap<String,Long> driftMap, String directory){
        List<FileBean> files = Util.filesInFolder(directory);
        List<CsvFile> foundFiles = new ArrayList<>();

        for(FileBean file: files){
            if(driftMap.containsKey(file.getName())){
                List<CsvFile> csvFileList = csvFileDao.selectByFileName(file.getName());

                if(csvFileList.size() == 1){
                    csvFileList.get(0).setPath(file.getDirectory()+file.getName());
                    foundFiles.add(csvFileList.get(0));

                }else {
                    System.out.println("Cannot find the unique file: " + file.getName());
                    break;
                }
            }
        }

        System.out.println("number of files not found: " + (driftMap.size() - foundFiles.size()));
        return foundFiles;
    };

    private int deleteOriginalFiles(List<CsvFile> filesList){
        int deleteResult = 1;
        for (CsvFile file : filesList){
            deleteResult=setLog(file,"Delete_Origin_Start")* deletePatientDataByFile(file)* setLog(file,"Delete_Origin_End");
        }
        return deleteResult;
    }


    private int reimport(List<CsvFile> filesList,HashMap<String,Long> driftMap){
        List<String> Paths = new ArrayList<>();
        for(CsvFile file: filesList){
            Paths.add(file.getPath());
        }
        String[] Paths1 = new String[Paths.size()];
        Paths1 = Paths.toArray(Paths1);

        AddArrayFiles(Paths1,driftMap);

        return 1;
    }

    private HashMap<String,Long> analyzeCSV (){
        //        handle the time drift problem
        String filename = "Time DriftTest_batch_12.csv";
        String fileName = "";
        LocalDateTime sqlTime;
        LocalDateTime correctTime;
        File file = new File(filename);

        HashMap<String,Long> fileShiftList = new HashMap<>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            reader.readLine();
            String aLine;

            while ((aLine= reader.readLine()) != null) {
                String[] values = aLine.split(",");
                fileName = values[1];

                String[] sqlTimePart = values[2].split(" ");
                String[] sqlDatePart = sqlTimePart[0].split("/");
                String dayTime = sqlDatePart[0].length() == 1 ? "0" + sqlDatePart[0] : sqlDatePart[0];
                String monthTime = sqlDatePart[1].length() == 1 ? "0" + sqlDatePart[1] : sqlDatePart[1];
                String sqlHMS = sqlTimePart[1].length() == 4 ? "0" + sqlTimePart[1] : sqlTimePart[1];
                String normalizedSqlTime = dayTime + "/" + monthTime + "/" + sqlDatePart[2] + " " + sqlHMS + ":00";

                String correctDatePart = values[3];
                String correctHMS = values[4].length() == 7 ? "0" + values[4] : values[4];
                String normalizedCorrectTime = correctDatePart + " "+ correctHMS;

                sqlTime = LocalDateTime.parse(normalizedSqlTime,DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss")).atZone(ZoneId.of("UTC")).toLocalDateTime();
                correctTime = LocalDateTime.parse(normalizedCorrectTime,DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                        .atZone(ZoneId.of("America/New_York"))
                        .withZoneSameInstant(ZoneId.of("UTC"))
                        .toLocalDateTime();

                Duration duration = Duration.between(sqlTime,correctTime);
                if (!duration.isZero()){
                    fileShiftList.put(fileName + "ar.csv",duration.toHours());
                    fileShiftList.put(fileName + "noar.csv",duration.toHours());
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        System.out.println("all files number: " + fileShiftList.size());
        return fileShiftList;
    }


    public int setLog(CsvFile csvFile, String status){
        CsvLog csvLog = new CsvLog();
        ZoneId america = ZoneId.of("America/New_York");
        LocalDateTime americaDateTime = LocalDateTime.now(america);
        csvLog.setActivity(status);
        csvLog.setEndTime(csvFile.getEndTime());
        csvLog.setStatus(csvFile.getStatus());
        csvLog.setFilename(csvFile.getFilename());
        csvLog.setStatTime(csvFile.getStartTime());
        csvLog.setTimestamp(americaDateTime);
        csvLog.setComment(csvFile.getComment());
        return csvLogDao.addLog(csvLog);
    }

    public int deletePatientDataByFile(CsvFile file) {
        Map<String, String> tags = new HashMap<>();
        tags.put("fileName",file.getFilename().replace(".csv",""));
        tags.put("fileUUID",file.getUuid());
        int deleteResult = 1;

        try {
            //      delete from influxDB
            boolean deleteInfluxDataResult = true;
            deleteInfluxDataResult = InfluxUtil.deleteDataByTagValues(file.getPid(), tags);

            if (deleteInfluxDataResult) {
                deleteResult =  importedFileDao.deletePatientDataByFile(file)*csvFileDao.deletePatientDataByFile(file);
                System.out.println("delete from influx success");
            }
            if (deleteResult == 0) {
                throw new Exception();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return deleteResult;
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
    public void AddArrayFiles(String[] paths,HashMap<String,Long> driftMap) {
        if (processingSet.isEmpty()) {
            newBatch();
        }
        for (String aPath : paths) {
            this.internalAddOne(aPath);
        }
        this.startImport(driftMap);
    }

    private void internalAddOne(String path) {
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
        if (!fLine.substring(fLine.length() - 40, fLine.length() - 4)
                .matches("([\\w\\d]){8}-([\\w\\d]){4}-([\\w\\d]){4}-([\\w\\d]){4}-([\\w\\d]){12}"))
            throw new RuntimeException("File does not have a valid UUID!");
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
        boolean hasImported = importedFileDao.checkHasImported(taskUUID, filename, filesize);
        if (!hasImported) {
            // Never updated
            res[2] = "0";
        } else {
            // Ignore fails (res[2]="2")
            res[2] = "1";
        }
        String fn_laterpart;
        // PUH-20xx_xxx
        // UAB-010_xx
        // TBI-1001_xxx
        if (filename.startsWith("PUH-")) {
            res[0] = filename.substring(0, 12).trim().toUpperCase();
            fn_laterpart = filename.substring(12).toLowerCase();
        } else if (filename.startsWith("UAB")) {
            res[0] = filename.substring(0, 7).trim().toUpperCase();
            fn_laterpart = filename.substring(7).toLowerCase();
        } else {
            res[0] = filename.substring(0, 8).trim().toUpperCase();
            fn_laterpart = filename.substring(8).toLowerCase();
        }
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
    private Object[] fileImport(String ar_type, String pid, Path file, long aFileSize,HashMap<String,Long> driftMap) {
        InfluxDB idb = generateIdbClient();
        long currentProcessed = 0;
        String filename = file.getFileName().toString();
        long totalLines = 0;
        String fileUUID = "";
        double offset = driftMap.get(filename) *1.0 / 24;

        try {
            BufferedReader reader = Files.newBufferedReader(file);
            CSVReader csvReader = new CSVReader(reader);

            // First line contains some curcial info
            String firstLine = reader.readLine();
            fileUUID = processFirstLineInCSV(firstLine, pid);
            currentProcessed = firstLine.length();
            totalProcessedSize.addAndGet(firstLine.length());

            // Next 6 lines no use expect time date
            long headerSize = 0;
            StringBuilder testDate = new StringBuilder();
            for (int i = 0; i < 5; i++) {
                String tmp = reader.readLine();
                headerSize += tmp.length();
                if (i == 3 || i == 4) {
                    testDate.append(tmp.split(",")[1].trim());
                }
            }


            LocalDateTime headerTime = LocalDateTime.parse(testDate,DateTimeFormatter.ofPattern("yyyy.MM.ddHH:mm:ss"));
            long testStartTimeEpoch = headerTime.atZone(ZoneId.of("America/New_York")).toInstant().toEpochMilli();

            currentProcessed += headerSize;
            totalProcessedSize.addAndGet(headerSize);

            /* Line 7 is the column text names. This segment of code is added on 5/20/2019, due to the reason that we discovered
             * that the split files of the files that went OOM in the Persyst export process were actually split in a vertical
             * fashion, not as we have been expecting in a long time. */
            String[] colText = csvReader.readNext();
            int addition = 0;
            if (colText.length != NORMAL_CSV_COLUMN_COUNT) {
                int startCol = 0;
                Feature feature = null;
                for (int i = 0; i < colText.length; i++) {
                    String col = colText[i];
                    if (!col.equals("")) {
                        feature = featureDao.selectByColumnText(col);
                        if (feature != null && CSV_COLUMN_NAME_DUPLICATE_INDICES.contains(feature.getCsvId())) {
                            if (!colText[i + 1].equals(colText[i])) {
                                feature.setCsvId(feature.getCsvId() - 1);
                            }
                        }
                        startCol = i;
                        break;
                    }
                }

                if (feature == null) {
                    csvReader.close();
                    throw new RuntimeException("Can't locate the column");
                } else {
                    if (startCol == 2) {
                        addition = feature.getCsvId() - 1;
                    } else {
                        addition = feature.getCsvId() - 2;
                    }
                }
            }

            // 8th Line is column header line
            String eiL = reader.readLine();
            String[] columnNames = eiL.split(",");
            int columnCount = columnNames.length;
            int bulkInsertMax = InfluxappConfig.PERFORMANCE_INDEX / columnCount;
            int batchCount = 0;

            // More integrity checking
            if (!columnNames[0].equalsIgnoreCase("clockdatetime")) {
                csvReader.close();
                throw new RuntimeException("Wrong first column!");
            }

            if (addition != 0) {
                for (int i = 2; i < columnNames.length; i++) {
                    Integer sid = Integer.parseInt(StringUtils.substringBetween(columnNames[i], "I", "_")) + addition;
                    columnNames[i] = "I" + sid + "_" + StringUtils.substringAfter(columnNames[i], "_");
                }
            }

            currentProcessed += eiL.length();
            totalProcessedSize.addAndGet(eiL.length());

            String influxFilename = filename.substring(0, StringUtils.lastIndexOfIgnoreCase(filename, "ar") + 2);

            BatchPoints records = BatchPoints.database(dbName).tag("fileUUID", fileUUID).tag("arType", ar_type)
                    .tag("fileName", influxFilename).build();

            long previousMeasurementEpoch = testStartTimeEpoch - 1000;
            String aLine;
            // Process every data lines
            while ((aLine = reader.readLine()) != null) {
                String[] values = aLine.split(",");
                int lengthOfThisLine = aLine.length();

                totalLines++;

                if (columnCount != values.length) {
                    String err_text = String.format("File content columns length inconsistent on line %d!", totalLines + 8);
                    logFailureFiles(file.toString(), err_text, aFileSize, currentProcessed, true);
                    currentProcessed += lengthOfThisLine;
                    totalProcessedSize.addAndGet(lengthOfThisLine);
                    continue;
                }

                // Compare date on every measures (They are all UTCs)


//                use offset to fix time drift
                double sTime = Double.parseDouble(values[0]) + offset;

                Date measurementDate = TimeUtil.serialTimeToDate(sTime, null);
                long measurementEpoch = measurementDate.getTime();

                // Measurement time should be later than test start time
                if (measurementEpoch < testStartTimeEpoch) {
                    String err_text = String.format("Measurement time earlier than test start time on line %d!",
                            totalLines + 8);
//                    logFailureFiles(file.toString(), err_text, aFileSize, currentProcessed, true);
                    continue;
                }


                // Overlap?
                if (measurementEpoch < previousMeasurementEpoch) {
                    logger.warn(String.format("Measurement time overlap on line %d!", totalLines + 8));
                }

                // Set initial capacity for better performance
                Map<String, Object> lineKVMap = new HashMap<>(columnCount + 1, 1.0f);
                boolean gotParseProblem = false;
                for (int i = 1; i < values.length; i++) {
                    try {
                        lineKVMap.put(columnNames[i], Double.valueOf(values[i]));
                    } catch (NumberFormatException nfe) {
                        currentProcessed += lengthOfThisLine;
                        totalProcessedSize.addAndGet(lengthOfThisLine);
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
                Point record = Point.measurement(pid).time(measurementEpoch, TimeUnit.MILLISECONDS).fields(lineKVMap).build();
                records.point(record);
                batchCount++;
                currentProcessed += lengthOfThisLine;
                totalProcessedSize.addAndGet(lengthOfThisLine);
                previousMeasurementEpoch = measurementEpoch;

                // Write batch into DB
                if (batchCount >= bulkInsertMax) {
                    long start = System.currentTimeMillis();
                    idb.write(records);
                    long end = System.currentTimeMillis();
                    logger.info("Write to InfluxDB used {}s.", (end - start) / 1000.0);

                    // Reset batch point
                    records = BatchPoints.database(dbName).tag("fileUUID", fileUUID).tag("arType", ar_type)
                            .tag("fileName", influxFilename).build();
                    batchCount = 0;
                    logImportingFile(file.toString(), aFileSize, currentProcessed, totalLines);

                    if (end - start > softTimeout) {
                        logger.warn(String.format("Sleeping for %ds", timeoutSleep));
                        TimeUnit.SECONDS.sleep(timeoutSleep);
                    }
                }
            }

            // Write the last batch
            idb.write(records);
            csvReader.close();
            reader.close();

        } catch (Exception e) {
            try {
                logger.debug(String.format("Sleeping for %ds", timeoutSleep));
                TimeUnit.SECONDS.sleep(timeoutSleep);
            } catch (InterruptedException e1) {
                logger.error(Util.stackTraceErrorToString(e1));
            }
            return new Object[] { Util.stackTraceErrorToString(e), currentProcessed };
        } finally {
            idb.close();
        }

        return new Object[] { "OK", currentProcessed, totalLines, fileUUID };
    }

    /**
     * Import file - internal method
     */
    private void internalImportMain(Path pFile,HashMap<String,Long> driftMap) {
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
        Object[] impStr = fileImport(fileInfo[1], fileInfo[0], pFile, thisFileSize,driftMap);

        // Main import function returned, doing cleanups
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
                importedFileDao.insert(iff);

                // add data to csv_file table
                CsvFile csvFile = validateCsvService.analyzeCsv(fileFullPath, fileName);
                CsvFile result = InfluxUtil.getTimeRangeandRows(csvFile.getPid(),fileName.substring(0, StringUtils.lastIndexOfIgnoreCase(fileName, "ar") + 2));
                csvFile.setDensity((result.getLength()*1.0)/(Duration.between(result.getStartTime(),result.getEndTime()).getSeconds()));
                csvFile.setStartTime(result.getStartTime());
                csvFile.setEndTime(result.getEndTime());
                csvFile.setLength(result.getLength());
                csvFile.setConflictResolved(false);
                csvFile.setStatus(2);
                validateCsvService.insertCsvFile(csvFile);
//                 add data to csv_log
                versionControlService.setLog(csvFile, "ReimportTest");

            } catch (Exception e) {
                logger.error(String.format("Filename '%s' failed to write to MySQL:%n%s", fileName,
                        Util.stackTraceErrorToString(e)));
                logMySQLFail(fileName);

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
        }
    }

    private void startImport(HashMap<String,Long> driftMap) {
        if (importingLock.get())
            return;

        int paraCount = (int) Math.round(loadFactor * InfluxappConfig.AvailableCores);
        paraCount = paraCount > 0 ? paraCount : 1;
        ExecutorService scheduler = Executors.newFixedThreadPool(paraCount);
        Runnable importTask = () -> {
            Path aFilePath;
            while ((aFilePath = fileQueue.poll()) != null) {
                internalImportMain(aFilePath,driftMap);
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
        importProgressService.UpdateFileProgress(batchId, fn, totalAllSize.get(), thisFileSize, 0, totalProcessedSize.get(),
                ImportProgressService.FileProgressStatus.STATUS_QUEUED, null);
    }

    private void logSuccessFiles(String fn, long thisFileSize, long thisFileProcessedSize) {
        importProgressService.UpdateFileProgress(batchId, fn, totalAllSize.get(), thisFileSize, thisFileProcessedSize,
                totalProcessedSize.get(), ImportProgressService.FileProgressStatus.STATUS_FINISHED, null);

    }

    private void logImportingFile(String fn, long thisFileSize, long thisFileProcessedSize, long currentLine) {
        importProgressService.UpdateFileProgress(batchId, fn, totalAllSize.get(), thisFileSize, thisFileProcessedSize,
                totalProcessedSize.get(), ImportProgressService.FileProgressStatus.STATUS_INPROGRESS,
                String.valueOf(currentLine));
    }

    private void logFailureFiles(String fn, String reason, long thisFileSize, long thisFileProcessedSize, boolean isSoftError) {
        if (!isSoftError) {
            totalAllSize.addAndGet(thisFileProcessedSize - thisFileSize);
        }
        importProgressService.UpdateFileProgress(batchId, fn, totalAllSize.get(), thisFileSize, thisFileProcessedSize,
                totalProcessedSize.get(), ImportProgressService.FileProgressStatus.STATUS_FAIL, reason);
    }

    private void logMySQLFail(String fn){
        CsvFile file = new CsvFile();
        file.setFilename(fn);
        versionControlService.setLog(file,"Fail_REImport");
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
        // Disable GZip to save CPU
        InfluxDB idb = InfluxUtil.generateIdbClient(true);
        BatchOptions bo = BatchOptions.DEFAULTS.consistency(InfluxDB.ConsistencyLevel.ALL)
                // Flush every 2000 Points, at least every 100ms, buffer for failed oper is 2200
                .actions(2000).flushDuration(500).bufferLimit(10000).jitterDuration(200)
                .exceptionHandler((p, t) -> logger.warn("Write point failed", t));
        idb.enableBatch(bo);
        idb.query(new Query(String.format("CREATE DATABASE \"%s\"", dbName), dbName));

        return idb;
    }


}
