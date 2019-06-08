package edu.pitt.medschool.service;


import edu.pitt.medschool.config.DBConfiguration;
import edu.pitt.medschool.config.InfluxappConfig;
import edu.pitt.medschool.framework.util.FileLockUtil;
import edu.pitt.medschool.framework.util.Util;
import edu.pitt.medschool.model.dto.CsvFile;
import edu.pitt.medschool.model.dto.ImportedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TemplateService {
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
    private ValidateCsvService validateCsvService;

    public void AddArrayFiles(String[] paths) {
        if(processingSet.isEmpty()){
            newBatch();
        }
        for(String apath:paths){
            this.internalAddOne(apath,true);
        }
        this.startImport();
    }

    private void newBatch() {
        batchId = UUID.randomUUID().toString();
        totalAllSize.set(0);
        totalProcessedSize.set(0);
        everyFileSize.clear();
        importFailCounter.clear();
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

    private void startImport() {
        if (importingLock.get())
            return;

        int paraCount = Runtime.getRuntime().availableProcessors();
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


    /**
     * Import file - internal method
     */
    private void internalImportMain(Path pFile) {
        long thisFileSize = everyFileSize.get(pFile.toString());
        String fileFullPath = pFile.toString(), fileName = pFile.getFileName().toString();

        processingSet.add(pFile);

        try {
            // add data to csv_file table
            CsvFile csvFile = validateCsvService.analyzeCsv(fileFullPath, fileName);
            // change to update
            validateCsvService.addCsvFileHearderWidth(csvFile);


        } catch (Exception e) {
            logger.error(String.format("Filename '%s' failed to write to MySQL:%n%s", fileFullPath,
                    Util.stackTraceErrorToString(e)));
        }
        // keep processed size consistent with the actual file size once a file is done with the import process
        processingSet.remove(pFile);
        FileLockUtil.release(fileFullPath);
        importFailCounter.remove(fileFullPath);

    }
}


