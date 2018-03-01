package app.service;


import app.common.InfluxappConfig;
import app.service.util.ImportProgressService;
import app.util.Util;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * Auto-parallel importing CSV data
 */
@Service
public class ImportCsvService {

    private final int availCores = Runtime.getRuntime().availableProcessors();
    private final String taskUUID = UUID.randomUUID().toString();

    private boolean importingLock = false;

    private final BlockingQueue<String> fileQueue = new LinkedBlockingQueue<>();

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
        ImportProgressService ips = new ImportProgressService(this.taskUUID);

        Runnable importTask = () -> {
            String aFilePath;
            while ((aFilePath = fileQueue.poll()) != null) {

                System.out.println(aFilePath);
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
        fileQueue.offer(path);
    }

    /**
     * Add a list of files into the queue
     *
     * @param paths List of files
     */
    public void AddListFiles(String[] paths) {
        if (importingLock) return;
        for (String aPath : paths) {
            AddOneFile(aPath);
        }
    }

    private void internalImportMain(Files file_info) {

    }

    public static void main(String[] args) {
        ImportCsvService ics = new ImportCsvService();
        String[] allNoAr = Util.getAllCsvFileInDirectory("N:\\Test_NoAR\\");
        String[] allAr = Util.getAllCsvFileInDirectory("N:\\Test_AR\\");

        ics.AddListFiles(allNoAr);
        ics.AddListFiles(allAr);

        // Invoke this should finish really fast
        ics.DoImport(0.5);
    }

}
