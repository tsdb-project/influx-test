package edu.pitt.medschool.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import edu.pitt.medschool.framework.influxdb.ResultTable;
import edu.pitt.medschool.model.dao.*;
import org.apache.commons.lang3.StringUtils;
import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.opencsv.CSVReader;

import edu.pitt.medschool.config.DBConfiguration;
import edu.pitt.medschool.config.InfluxappConfig;
import edu.pitt.medschool.framework.influxdb.InfluxUtil;
import edu.pitt.medschool.framework.util.FileLockUtil;
import edu.pitt.medschool.framework.util.TimeUtil;
import edu.pitt.medschool.framework.util.Util;
import edu.pitt.medschool.model.dto.CsvFile;
import edu.pitt.medschool.model.dto.Feature;
import edu.pitt.medschool.model.dto.ImportedFile;

import static edu.pitt.medschool.service.PerformanceTest.idb;

/**
 * Auto-parallel importing CSV data
 */
@Service
public class ImportCsvService {

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

    @Value("${SUCCESSFILES}")
    private String SUCCESSFILES;

    @Value("${FAILEDFILES}")
    private String FAILEDFILES;

    private final AtomicBoolean importingLock = new AtomicBoolean(false);

    private final BlockingQueue<Path> fileQueue = new LinkedBlockingQueue<>();
    private final Set<Path> processingSet = new HashSet<>();

    @Autowired
    private ImportedFileDao importedFileDao;

    @Autowired
    private ImportProgressService importProgressService;

    @Autowired
    private ValidateCsvService validateCsvService;

    @Autowired
    private FeatureDao featureDao;

    @Autowired
    InfluxClusterDao influxClusterDao;

    @Autowired
    VersionControlService versionControlService;

    @Autowired
    CsvFileDao csvFileDao;

    @Autowired
    VersionDao versionDao;

    public double GetLoadFactor() {
        return loadFactor;
    }

    public String GetUUID() {
        return this.taskUUID;
    }

    public String getBatchId() {
        return this.batchId;
    }

    public static void main(String[] args) throws IOException {
//        String[] testFiles = Util.getAllCsvFileInDirectory("/tsdb/ar");
//        String f = "UAB-236_02c_ ar_File A.csv";
//        String influxFilename = f.substring(0, StringUtils.lastIndexOfIgnoreCase(f, "ar") + 2);
//        System.out.println(influxFilename);
//        for (String filename : testFiles) {
//            Path file = Paths.get(filename);
//            BufferedReader reader = Files.newBufferedReader(file);
//            CSVReader csvReader = new CSVReader(reader);
//            if (!filename.equals("/tsdb/ar/PUH-2011-108_09ar.csv")) {
//                // continue;
//            }
//
//            reader.readLine();
//
//            // Next 6 lines no use expect time date
//            for (int i = 0; i < 5; i++) {
//                reader.readLine();
//            }
//
//            /* Line 7 is the column text names. This segment of code is added on 5/20/2019, due to the reason that we discovered
//             * that the split files of the files that went OOM in the Persyst export process were actually split in a vertical
//             * fashion, not as we have been expecting in a long time. */
//            String[] col = csvReader.readNext();
//            if (col.length != NORMAL_CSV_COLUMN_COUNT) {
//
//            } else {
//                System.out.println("OKAY");
//            }
//
//            csvReader.close();
//        }


//        Two ways to handle DST
        String start = "2015.08.24 04:56:00";
        String end = "2015.08.24 21:40:22";
        int length = 32377;
//        String values = "41571.8739583333";

        try{

//            // Old fashion
//            Date testStartTime = TimeUtil.dateTimeFormatToDate(testDate, "yyyy.MM.ddHH:mm:ss", TimeUtil.nycTimeZone);
//            long testStartTimeEpoch = testStartTime.getTime();
//
//            // Special operations when on DST shifting days
//            if (TimeUtil.isThisDayOnDstShift(TimeUtil.nycTimeZone, testStartTime)) {
//                // Auto-fix the time according to month
//                Calendar calendar = Calendar.getInstance();
//                calendar.setTime(testStartTime);
//                if (calendar.get(Calendar.MONTH) < Calendar.JUNE) {
//                    testStartTimeEpoch = TimeUtil.addOneHourToTimestamp(testStartTimeEpoch); // Mar
//                } else {
//                    testStartTimeEpoch = TimeUtil.subOneHourToTimestamp(testStartTimeEpoch); // Nov
//                }
//            }
//
//            // new fashion
            ZonedDateTime startTime = LocalDateTime.parse(start,DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss")).atZone(ZoneId.of("UTC"));
//            long startSecond = startTime.atZone(ZoneId.of("UTC")).getSecond();

            ZonedDateTime endTime = LocalDateTime.parse(end,DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss")).atZone(ZoneId.of("UTC"));
//            long endSecond = endTime.atZone(ZoneId.of("UTC")).getSecond();

            Long diff = Duration.between(startTime,endTime).getSeconds();

            double density = length*1.0 / diff;

//            System.out.println("Old fashion: " + testStartTimeEpoch);
            System.out.println("start time: " + startTime);
            System.out.println("end time: " + endTime);
            System.out.println("difference: " + diff);
            System.out.println("density: " + density);

//            // check date converting
//
//            String dayoffset = values.split("\\.")[0];
//            Double secondoffset = Double.parseDouble("0." + values.split("\\.")[1]);
//
//            // fix 3 hours shift
//            double sTime = Double.parseDouble(values) - 0.125;
//            Date measurementDate = TimeUtil.serialTimeToDate(sTime, null);
//            long measurementEpoch = measurementDate.getTime();
//
//            LocalDateTime newdate = LocalDateTime
//                    .of(1899,12, 30,0,0,0)
//                    .plusDays(Long.valueOf(dayoffset))
//                    .plusSeconds((long) (secondoffset*86400));
//
//            long newdatesecond = newdate.atZone(ZoneId.of("UTC")).toInstant().toEpochMilli();
//
//            System.out.println("test date: " + measurementDate.toInstant());
//            System.out.println("test date second: " + measurementEpoch);
//            System.out.println("new date: " + newdate.atZone(ZoneId.of("UTC")).toInstant());
//            System.out.println("new date second: " + newdatesecond);

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void _test() throws InterruptedException, IOException {
        String[] testFiles = Util.getAllCsvFileInDirectory("/tsdb/ar");

        AddArrayFiles(testFiles);

        logger.debug("Sleep 3s...");
        Thread.sleep(3000);

        logger.debug("Main exited, worker running...");
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
            System.out.println(aPath);
            this.internalAddOne(aPath, true);
        }
        this.startImport();
    }

    private void internalAddOne(String path, boolean isInvokedByAddArrayFiles) {
        Path p = Paths.get(path);

        try {
            long currS = Files.size(p);

            if (FileLockUtil.isLocked(p.toFile())) {
                throw new RuntimeException(p.toString() + " is currently being imported.");
            }

            if (FileLockUtil.aquire(p.toFile())) {
                totalAllSize.addAndGet(currS);
                everyFileSize.put(p.toString(), currS);
                fileQueue.offer(p);
                logQueuedFile(p.toString(), currS);
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
    private Object[] fileImport(String ar_type, String pid, Path file, long aFileSize,String filename) {
        InfluxDB idb = generateIdbClient();
        long currentProcessed = 0;
        long totalLines = 0;
        String fileUUID = "";

        // fixing 3 hours time shift
//        double offset = -3 *1.0 / 24;
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

            // Test date in the headers is in EASTERN Time
//            Date testStartTime = TimeUtil.dateTimeFormatToDate(testDate.toString(), "yyyy.MM.ddHH:mm:ss", TimeUtil.nycTimeZone);
//            long testStartTimeEpoch = testStartTime.getTime();
//
//            // Special operations when on DST shifting days
//            if (TimeUtil.isThisDayOnDstShift(TimeUtil.nycTimeZone, testStartTime)) {
//                String errTemp = "%s hour on DST shift days";
//                String oper = "Add";
//                // Auto-fix the time according to month
//                Calendar calendar = Calendar.getInstance();
//                calendar.setTime(testStartTime);
//                if (calendar.get(Calendar.MONTH) < Calendar.JUNE) {
//                    testStartTimeEpoch = TimeUtil.addOneHourToTimestamp(testStartTimeEpoch); // Mar
//                } else {
//                    testStartTimeEpoch = TimeUtil.subOneHourToTimestamp(testStartTimeEpoch); // Nov
//                    oper = "Sub";
//                }
//                logFailureFiles(file.toString(), String.format(errTemp, oper), aFileSize, currentProcessed, true);
//            }

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
                    logFailureFiles(file.toString(), err_text, aFileSize, currentProcessed, true,true);
                    currentProcessed += lengthOfThisLine;
                    totalProcessedSize.addAndGet(lengthOfThisLine);
                    continue;
                }

                // Compare date on every measures (They are all UTCs)
                double sTime = Double.parseDouble(values[0]);
                Date measurementDate = TimeUtil.serialTimeToDate(sTime, null);
                long measurementEpoch = measurementDate.getTime();

                // Measurement time should be later than test start time
                if (measurementEpoch < testStartTimeEpoch) {
                    String err_text = String.format("Measurement time earlier than test start time on line %d!",
                            totalLines + 8);
                    logFailureFiles(file.toString(), err_text, aFileSize, currentProcessed, true,true);
                    continue;
                }

                // To avoid some problematic files where measurement date is not reliable
//                if (!TimeUtil.dateIsSameDay(measurementDate, testStartTime)) {
//                     logger.warn(String.format("Measurement accross day on line %d!", totalLines + 8));
//                }

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
                    logFailureFiles(file.toString(), err_text, aFileSize, currentProcessed, true,true);
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
    private void internalImportMain(Path pFile) {
        long thisFileSize = everyFileSize.get(pFile.toString());
        String fileFullPath = pFile.toString(), fileName = pFile.getFileName().toString();
        // normalize the filename
        fileName = fileName.substring(0, StringUtils.lastIndexOfIgnoreCase(fileName, "ar") + 2) + ".csv";
        String[] fileInfo = checkerFromFilename(fileName, thisFileSize);
        processingSet.add(pFile);

        // Ar/NoAr Check & Response
        if (fileInfo[1] == null) {
            logFailureFiles(fileFullPath, "Ambiguous Ar/NoAr in file name.", thisFileSize, 0, false,false);
            FileLockUtil.release(fileFullPath);
            processingSet.remove(pFile);
            transferFailedFiles(pFile);
            return;
        }

        // Duplication Check
        Object[] impStr;
        if (fileInfo[2].equals("1")) {
            // TODO: If the same file appears already, having a summary of comparing the contents of the new and old
            List<CsvFile> files = csvFileDao.getElementByName(fileName);
            if(files.get(0).getEndVersion()==999){
                logFailureFiles(fileFullPath, "The same file has been imported.", thisFileSize, 0, false,false);
                FileLockUtil.release(fileFullPath);
                processingSet.remove(pFile);
                return;
            }else {
                // the file is deleted after publish, add version to the file name and import
                logger.info("first import");
                fileName ="V"+(versionDao.getLatestVersion()+1)+"_"+fileName;
                impStr = fileImport(fileInfo[1], fileInfo[0], pFile, thisFileSize,fileName);
            }
        }else {
            List<CsvFile> files = csvFileDao.getElementByName(fileName);
            if(files.size()==0){
                // New file, all good, just import!
                logger.info("second import");
                impStr = fileImport(fileInfo[1], fileInfo[0], pFile, thisFileSize,pFile.getFileName().toString());
            }else if(files.get(0).getStartVersion()!=0 && files.get(0).getEndVersion()==999){
                logFailureFiles(fileFullPath, "Old file should be stopped before import a complete version.", thisFileSize, 0, false,false);
                FileLockUtil.release(fileFullPath);
                processingSet.remove(pFile);
                return;
            }else if(files.get(0).getEndVersion()!=999){
                // the file is deleted after publish, add version to the file name and import
                logger.info("third import");
                fileName ="V"+(versionDao.getLatestVersion()+1)+"_"+fileName;
                impStr = fileImport(fileInfo[1], fileInfo[0], pFile, thisFileSize,fileName);
            }else {
                // add another part to an exist file
                logger.info("fourth import");
                impStr = fileImport(fileInfo[1], fileInfo[0], pFile, thisFileSize,pFile.getFileName().toString());
            }

        }

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
                ZoneId america = ZoneId.of("America/New_York");
                LocalDateTime americaDateTime = LocalDateTime.now(america);
                iff.setTimestamp(americaDateTime);


                List<ImportedFile> importedFiles = importedFileDao.selectByFileNameDeleted(iff);
                if(importedFiles.isEmpty()){
                    importedFileDao.insert(iff);
                }else{
                    iff.setFilesize(iff.getFilesize() + importedFiles.get(0).getFilesize());
                    importedFileDao.updateImportedSize(iff);
                }


                // add data to csv_file table
                CsvFile csvFile = validateCsvService.analyzeCsv(fileFullPath, fileName);
                logger.info(fileName.substring(0, StringUtils.lastIndexOfIgnoreCase(fileName, "ar") + 2));
                logger.info(csvFile.getPid());
                CsvFile result = InfluxUtil.getTimeRangeandRows(csvFile.getPid(),fileName.substring(0, StringUtils.lastIndexOfIgnoreCase(fileName, "ar") + 2));
                csvFile.setDensity((result.getLength()*1.0)/(Duration.between(result.getStartTime(),result.getEndTime()).getSeconds()));
                csvFile.setStartTime(result.getStartTime());
                csvFile.setEndTime(result.getEndTime());
                csvFile.setLength(result.getLength());
                csvFile.setConflictResolved(false);
                csvFile.setStatus(2);
                validateCsvService.insertCsvFile(csvFile);
                // add data to csv_log
                versionControlService.setLog(csvFile, "Pending");

                //Move file to other folder
                moveAndDelete(fileFullPath,true);



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
            logFailureFiles(fileFullPath, (String) impStr[0], thisFileSize, procedSize, false,false);

            FileLockUtil.release(fileFullPath);
            processingSet.remove(pFile);
            logger.error((String) impStr[0]);
            transferFailedFiles(pFile);
        }
    }

    private void startImport() {
        if (importingLock.get())
            return;

        int paraCount = (int) Math.round(loadFactor * InfluxappConfig.AvailableCores);
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
        importProgressService.UpdateFileProgress(batchId, fn, totalAllSize.get(), thisFileSize, 0, totalProcessedSize.get(),
                ImportProgressService.FileProgressStatus.STATUS_QUEUED, null,false);
    }

    private void logSuccessFiles(String fn, long thisFileSize, long thisFileProcessedSize) {
        importProgressService.UpdateFileProgress(batchId, fn, totalAllSize.get(), thisFileSize, thisFileProcessedSize,
                totalProcessedSize.get(), ImportProgressService.FileProgressStatus.STATUS_FINISHED, null,false);

    }

    private void logImportingFile(String fn, long thisFileSize, long thisFileProcessedSize, long currentLine) {
        importProgressService.UpdateFileProgress(batchId, fn, totalAllSize.get(), thisFileSize, thisFileProcessedSize,
                totalProcessedSize.get(), ImportProgressService.FileProgressStatus.STATUS_INPROGRESS,
                String.valueOf(currentLine),false);
    }

    private void logFailureFiles(String fn, String reason, long thisFileSize, long thisFileProcessedSize, boolean isSoftError, Boolean lost) {
        if (!isSoftError) {
            totalAllSize.addAndGet(thisFileProcessedSize - thisFileSize);
        }
        importProgressService.UpdateFileProgress(batchId, fn, totalAllSize.get(), thisFileSize, thisFileProcessedSize,
                totalProcessedSize.get(), ImportProgressService.FileProgressStatus.STATUS_FAIL, reason,lost);
        moveAndDelete(fn,false);
    }

    private void logMySQLFail(String fn){
        CsvFile file = new CsvFile();
        file.setFilename(fn);
        versionControlService.setLog(file,"Fail_Import");
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

    private void moveAndDelete(String path, Boolean success){
        System.out.println(path);
        try {
            File afile = new File(path);
            String destnation;
            if(success){
                destnation = SUCCESSFILES;
//                destnation = "d:/eegdata/"+kind+"/"+ afile.getName();
            }else {
                destnation = FAILEDFILES;
//                destnation = "d:/eegdata/failed/"+afile.getName();
            }
            if (moveFile(destnation,afile)) {
                System.out.println("File "+ afile.getName()+" is moved successful!");
            } else {
                System.out.println("File "+afile.getName()+" is failed to move!");
            }
            String txtpath = path.replace(".csv",".txt");
            System.out.println(txtpath);
            File txt = new File(txtpath);
            if(txt.exists()){
                txt.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean moveFile(String path, File file){
        File folder = new File(path);
        String year = file.getName().substring(4,8);
        String pid = file.getName().substring(0,12);
        Queue<File> fileQueue = new LinkedList<>();
        if(folder.listFiles()!=null){
            for(File f: folder.listFiles()){
                if(f.getName().equals(year)){
                    fileQueue.add(f);
                }

            }
        }
        while (!fileQueue.isEmpty()){
            File current = fileQueue.poll();
            if(current.getName().equals(year)){
                for(File f: current.listFiles()){
                    fileQueue.add(f);
                }
            }else if(current.getName().equals(pid)){
                return file.renameTo(new File(current.getPath()+"/"+file.getName()));
            }
        }
        File newFolder = new File(path+"/"+year);
        newFolder.mkdir();
        File subFolder = new File(path+"/"+year+"/"+pid);
        subFolder.mkdir();
        return file.renameTo(new File(subFolder.getPath()+"/"+file.getName()));
    }
}
