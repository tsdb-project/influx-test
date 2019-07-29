package edu.pitt.medschool.service;

import static edu.pitt.medschool.framework.influxdb.InfluxUtil.generateIdbClient;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import edu.pitt.medschool.model.PatientTimeLine;
import edu.pitt.medschool.model.dao.*;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.pitt.medschool.config.InfluxappConfig;
import edu.pitt.medschool.controller.analysis.vo.ColumnJSON;
import edu.pitt.medschool.controller.analysis.vo.ExportVO;
import edu.pitt.medschool.framework.influxdb.InfluxUtil;
import edu.pitt.medschool.framework.influxdb.ResultTable;
import edu.pitt.medschool.framework.util.FileZip;
import edu.pitt.medschool.framework.util.Util;
import edu.pitt.medschool.model.DataTimeSpanBean;
import edu.pitt.medschool.model.dto.Downsample;
import edu.pitt.medschool.model.dto.DownsampleGroup;
import edu.pitt.medschool.model.dto.EEGChart;
import edu.pitt.medschool.model.dto.ExportWithBLOBs;
import edu.pitt.medschool.model.dto.MedicalDownsample;
import edu.pitt.medschool.model.dto.MedicalDownsampleGroup;
import edu.pitt.medschool.model.dto.Medication;
import okhttp3.OkHttpClient;

/**
 * Export functions
 */
@Service
public class AnalysisService {

    @Autowired
    ValidateCsvService validateCsvService;
    @Autowired
    UsersService usersService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${machine}")
    private String uuid;

    @Value("${load}")
    private double loadFactor;

    private final DownsampleDao downsampleDao;
    private final MedicalDownsampleDao medicalDownsampleDao;
    private final DownsampleGroupDao downsampleGroupDao;
    private final MedicalDownsampleGroupDao medicalDownsampleGroupDao;
    private final ExportDao exportDao;
    private final ColumnService columnService;
    private final MedicationDao medicationDao;
    private final InfluxSwitcherService iss;
    private final ImportedFileDao importedFileDao;
    private final VersionDao versionDao;
    private final ScheduledFuture<?> jobCheckerThread; // Thread for running managed jobs

    /**
     * Queue for managing jobs
     */
    private final LinkedBlockingQueue<ExportWithBLOBs> jobQueue = new LinkedBlockingQueue<>();

    /**
     * Indicate if a job should stop
     */
    private final ConcurrentHashMap<Integer, Boolean> jobStopIndicator = new ConcurrentHashMap<>();

    @Autowired
    public AnalysisService(DownsampleDao downsampleDao, MedicalDownsampleDao medicalDownsampleDao,
            DownsampleGroupDao downsampleGroupDao, MedicalDownsampleGroupDao medicalDownsampleGroupDao, ExportDao exportDao,
            ColumnService columnService, InfluxSwitcherService iss, ImportedFileDao importedFileDao,
            MedicationDao medicationDao, VersionDao versionDao) {
        this.downsampleDao = downsampleDao;
        this.medicalDownsampleDao = medicalDownsampleDao;
        this.downsampleGroupDao = downsampleGroupDao;
        this.medicalDownsampleGroupDao = medicalDownsampleGroupDao;
        this.exportDao = exportDao;
        this.columnService = columnService;
        this.iss = iss;
        this.importedFileDao = importedFileDao;
        this.medicationDao = medicationDao;
        this.versionDao = versionDao;
        // Check the job queue every 20 seconds and have a initial delay of 10s
        this.jobCheckerThread = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            Thread.currentThread().setName("JobCheckerThread");
            ExportWithBLOBs target = null;
            while ((target = this.jobQueue.poll()) != null) {
                logger.info("Start to process job #<{}>", target.getId());
                if (!target.getMedical()) {
                    logger.info("******************************General************************");
                    mainExportProcess(target);
                } else {
                    logger.info("***********************Medication********************");
                    mainMedicalExportProcess(target);
                }
                logger.info("Finished one job #<{}>", target.getId());
            }
        }, 10, 20, TimeUnit.SECONDS);
    }

    /**
     * Add an export job to the job queue
     *
     * @param jobId ID of the job
     */
    public boolean addOneExportJob(Integer jobId) {
        ExportWithBLOBs job = exportDao.selectByPrimaryKey(jobId);
        try {
            return this.jobQueue.add(job);
        } catch (Exception e) {
            logger.error("Add job failed.", e);
            return false;
        }
    }

    /**
     * Stop a running job
     */
    public int removeOneExportJob(Integer jobId) {
        // Should also mark the DB as finished (or deleted)
        this.jobStopIndicator.putIfAbsent(jobId, false);
        return exportDao.markAsCanceledById(jobId);
    }

    /**
     * Internal use only, export (downsample) a single query to files
     */
    private void mainExportProcess(ExportWithBLOBs job) {
        int jobId = job.getId();
        int queryId = job.getQueryId();
        Downsample exportQuery = downsampleDao.selectByPrimaryKey(queryId);
        boolean isPscRequired = job.getDbType().equals("psc");

        // Create output folder, if failed, finish this process
        File outputDir = generateOutputDir(exportQuery.getAlias(), null);
        if (outputDir == null)
            return;

        String pList = job.getPatientList();
        List<String> patientIDs;

        if (pList == null || pList.isEmpty()) {
            // Override UUID setting to match PSC database if necessary
            patientIDs = importedFileDao.selectAllImportedPidOnMachine(isPscRequired ? "realpsc" : this.uuid);
        } else {
            patientIDs = Arrays.stream(pList.split(",")).map(String::toUpperCase).collect(Collectors.toList());
        }

        // detect the number of patients and save it
        exportDao.updatePatientNum(job.getId(),patientIDs.size());


        // Get columns data
        List<DownsampleGroup> groups = downsampleGroupDao.selectAllDownsampleGroup(queryId);
        int labelCount = groups.size();
        List<List<String>> columns = new ArrayList<>(labelCount);
        List<String> columnLabelName = new ArrayList<>(labelCount);
        try {
            for (DownsampleGroup group : groups) {
                columns.add(parseAggregationGroupColumnsString(group.getColumns()));
                columnLabelName.add(group.getLabel());
            }
        } catch (IOException e) {
            logger.error("Parse aggregation group failed: {}", Util.stackTraceErrorToString(e));
            return;
        }

        // Init the `outputWriter`
        ExportOutput outputWriter;
        try {
            outputWriter = new ExportOutput(outputDir.getAbsolutePath(), columnLabelName, exportQuery, job);
        } catch (IOException e) {
            logger.error("Export writer failed to create: {}", Util.stackTraceErrorToString(e));
            jobClosingHandler(false, isPscRequired, job, outputDir, null, 0);
            return;
        }

        // This job marked as removed
        if (this.jobStopIndicator.containsKey(jobId)) {
            this.jobStopIndicator.remove(jobId);
            logger.warn("Job <{}> cancelled by user.", jobId);
            outputWriter.writeMetaFile(String.format("%nJob cancelled by user.%n"));
            jobClosingHandler(false, isPscRequired, job, outputDir, outputWriter, 0);
            return;
        }

        if (isPscRequired) {
            // Prep PSC instance
            iss.stopLocalInflux();
            // Local DB may take up to 10s to stop
            try {
                Thread.sleep(10_000);
            } catch (InterruptedException e) {
                logger.error("Stop local Influx failed, {}", Util.stackTraceErrorToString(e));
                return;
            }
            iss.setupRemoteInflux();
            if (!iss.getHasStartedPscInflux()) {
                // Psc not working, should exit
                logger.error("Selected PSC InfluxDB but failed to start!");
                jobClosingHandler(true, true, job, outputDir, null, 0);
                return;
            }
        } else {
            iss.stopRemoteInflux();
            // Remote DB may take up to 3s to stop
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                logger.error("Stop remote Influx failed, {}", Util.stackTraceErrorToString(e));
                return;
            }
            iss.setupLocalInflux();
            if (!iss.getHasStartedLocalInflux()) {
                // Local not working, should exit
                logger.error("Selected local InfluxDB but failed to start!");
                jobClosingHandler(true, false, job, outputDir, null, 0);
                return;
            }
        }

        int paraCount = determineParaNumber();
        // Dirty hack to migrate timeout problems, should remove this some time later
        if (exportQuery.getPeriod() < 20 || labelCount > 8)
            paraCount *= 0.8;
        paraCount = paraCount > 0 ? paraCount : 1;

        // Get some basic info for exporting
        int numP = getNumOfPatients();
        // Try again only once
        if (numP == -1)
            numP = getNumOfPatients();
        outputWriter.writeInitialMetaText(numP, patientIDs.size(), paraCount);

        // Final prep for running query task
        BlockingQueue<String> idQueue = new LinkedBlockingQueue<>(patientIDs);
        Map<String, Integer> errorCount = new HashMap<>();
        AtomicInteger validPatientCounter = new AtomicInteger(0);
        AtomicInteger finishedPatientCounter = new AtomicInteger(0);

        ExecutorService scheduler = generateNewThreadPool(paraCount);
        Runnable queryTask = () -> {
            InfluxDB influxDB = generateIdbClient(false);
            String patientId;
            while ((patientId = idQueue.poll()) != null) {
                try {
                    // This job marked as removed, so this thread should exit
                    if (this.jobStopIndicator.containsKey(jobId)) {
                        this.jobStopIndicator.put(jobId, true);
                        return;
                    }

                    // get version condition
                    List<PatientTimeLine> files = validateCsvService.getPatientTimeLinesByVersionID("realpsc",usersService.getVersionByUserName(job.getUsername()),patientId);
                    String versionCondition = versionDao.getVersionCondition(files);
                    if(files.isEmpty()){
                        outputWriter.writeMetaFile(String.format("  PID <%s> is not available in this version.%n", patientId));
                        finishedPatientCounter.getAndIncrement();
                        exportDao.updatePatientFinishedNum(job.getId(),finishedPatientCounter.get());
                        continue;
                    }
                    // First get the group by time offset
                    ResultTable[] testOffset = InfluxUtil.justQueryData(influxDB, true, String.format(
                            "SELECT time, count(Time) From \"%s\" WHERE (arType='%s') and "+versionCondition+" GROUP BY time(%ds) fill(none) ORDER BY time ASC LIMIT 1",
                            patientId, job.getAr() ? "ar" : "noar", exportQuery.getPeriod()));
                    if (testOffset.length != 1) {
                        outputWriter.writeMetaFile(String.format("  PID <%s> don't have enough data to export.%n", patientId));
                        finishedPatientCounter.getAndIncrement();
                        exportDao.updatePatientFinishedNum(job.getId(),finishedPatientCounter.get());
                        continue;
                    }
                    // Then fetch meta data regrading file segments and build the query string
                    List<DataTimeSpanBean> dtsb = AnalysisUtil.getPatientAllDataSpan(influxDB, logger, patientId,versionCondition);
                    // get the startTime eliminate first 30 rows
                    String startTime = AnalysisUtil.getPatientStartTime(influxDB,logger,patientId,job.getAr(),versionCondition);
                    ExportQueryBuilder eq = new ExportQueryBuilder(Instant.parse(startTime),
                            Instant.parse((String) testOffset[0].getDataByColAndRow(0, 0)), dtsb, groups, columns, exportQuery,
                            job.getAr(),versionCondition);
                    String finalQueryString = eq.getQueryString();
                    logger.info(finalQueryString);
                    if (finalQueryString.isEmpty()) {
                        outputWriter.writeMetaFile(String.format("  PID <%s> no available data.%n", patientId));
                        finishedPatientCounter.getAndIncrement();
                        exportDao.updatePatientFinishedNum(job.getId(),finishedPatientCounter.get());
                        continue;
                    }
                    logger.debug("Query for <{}>: {}", patientId, finalQueryString);
                    // Execuate the query
                    ResultTable[] res = InfluxUtil.justQueryData(influxDB, true, finalQueryString);

                    if (res.length != 1) {
                        outputWriter.writeMetaFile(String.format("  PID <%s> incorrect result from database.%n", patientId));
                        finishedPatientCounter.getAndIncrement();
                        exportDao.updatePatientFinishedNum(job.getId(),finishedPatientCounter.get());
                        continue;
                    }

                    outputWriter.writeForOnePatient(patientId, res[0], eq, dtsb);
                    validPatientCounter.getAndIncrement();
                    finishedPatientCounter.getAndIncrement();
                    exportDao.updatePatientFinishedNum(job.getId(),finishedPatientCounter.get());

                } catch (Exception ee) {
                    // All exception will be logged (disregarded) and corresponding PID will be tried again
                    logger.error(String.format("%s: %s", patientId, Util.stackTraceErrorToString(ee)));
                    int alreadyFailed = errorCount.getOrDefault(patientId, -1);
                    if (alreadyFailed == -1) {
                        errorCount.put(patientId, 0);
                        alreadyFailed = 0;
                    } else {
                        errorCount.put(patientId, ++alreadyFailed);
                    }
                    // Reinsert failed user into queue, but no more than 3 times
                    if (alreadyFailed < 3) {
                        idQueue.add(patientId);
                    } else {
                        logger.error(String.format("%s: Failed more than 3 times.", patientId));
                        outputWriter.writeMetaFile(
                                String.format("  PID <%s> failed multiple times, possible program error.%n", patientId));
                        idQueue.remove(patientId);
                        finishedPatientCounter.getAndIncrement();
                        exportDao.updatePatientFinishedNum(job.getId(),finishedPatientCounter.get());
                    }
                }

            }
            influxDB.close();
        };

        for (int i = 0; i < paraCount; ++i) {
            scheduler.submit(queryTask);
        }
        scheduler.shutdown();
        try {
            // TODO: PSC 2 day limit!
            scheduler.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error(Util.stackTraceErrorToString(e));
        } finally {
            if (this.jobStopIndicator.getOrDefault(jobId, false).equals(true)) {
                // Job removed special procedure
                this.jobStopIndicator.remove(jobId);
                outputWriter.writeMetaFile(String.format("%nJob cancelled by user during execution.%n"));
                logger.warn("Job <{}> cancelled by user during execution.", jobId);
            }
            // Normal exit procedure
            jobClosingHandler(false, isPscRequired, job, outputDir, outputWriter, validPatientCounter.get());
        }
    }

    /**
     * Export the result of medical query
     * 
     */
    private void mainMedicalExportProcess(ExportWithBLOBs job) {
        ZoneId utcTz = ZoneId.of("UTC");
        ZoneId nycTz = ZoneId.of("America/New_York");
        int jobId = job.getId();
        int queryId = job.getQueryId();
        MedicalDownsample exportQuery = medicalDownsampleDao.selectByPrimaryKey(queryId);
        boolean isPscRequired = job.getDbType().equals("psc");
        // create output folder, if failed finish this process
        File outputDir = generateOutputDir(exportQuery.getAlias(), null);
        if (outputDir == null) {
            return;
        }
        String pList = job.getPatientList();
        List<String> patientIDs;
        if (pList == null || pList.isEmpty()) {
            // Override UUID setting to match PSC database if necessary
            patientIDs = importedFileDao.selectAllImportedPidOnMachine(isPscRequired ? "realpsc" : this.uuid);
        } else {
            patientIDs = Arrays.stream(pList.split(",")).map(String::toUpperCase).collect(Collectors.toList());
        }
        // Get columns data
        List<MedicalDownsampleGroup> groups = medicalDownsampleGroupDao.selectAllMedicalDownsampleGroups(queryId);
        int labelCount = groups.size();
        List<List<String>> columns = new ArrayList<>(labelCount);
        List<String> columnLabelName = new ArrayList<>(labelCount);
        try {
            for (MedicalDownsampleGroup group : groups) {
                columns.add(parseAggregationGroupColumnsString(group.getColumns()));
                columnLabelName.add(group.getLabel());
            }
        } catch (IOException e) {
            logger.error("Parse aggregation group failed: {}", Util.stackTraceErrorToString(e));
            return;
        }

        //get version condition
        logger.info(job.getUsername());
        int version = usersService.getVersionByUserName(job.getUsername());
        logger.info(String.valueOf(version));

        // medicalRecordList save all medical records of all selected patients
        List<Medication> medicalRecordList = new ArrayList<Medication>();
        List<Medication> medicalRecordList_full = new ArrayList<Medication>();
        List<Medication> medicalRecordList_first = new ArrayList<Medication>();
        logger.info("patientIDS:" + Integer.toString(patientIDs.size()));
        medicalRecordList_full = medicationDao.selectAllbyMedications(exportQuery.getMedicine(), patientIDs,version);
        medicalRecordList_first = medicationDao.selectFirstbyMedications(exportQuery.getMedicine(), patientIDs,version);
        if (exportQuery.getOnlyFirst() && exportQuery.getDataBeforeMedicine()) {
            logger.info("only first and before first");
            medicalRecordList = medicalRecordList_first;
        } else if (exportQuery.getDataBeforeMedicine() && !exportQuery.getOnlyFirst()) {
            logger.info("not only first and before first");
            InfluxDB influxDB = generateIdbClient(false);
            Set<String> patients = new HashSet<String>();
            for (Medication medication : medicalRecordList_first) {
                List<PatientTimeLine> files = validateCsvService.getPatientTimeLinesByVersionID("realpsc",version,medication.getId());
                String versionCondition = versionDao.getVersionCondition(files);
                String startTime = AnalysisUtil.getPatientStartTime(influxDB,logger,medication.getId(),job.getAr(),versionCondition);
                if (Instant.parse(startTime).compareTo(medication.getChartDate().atZone(nycTz).withZoneSameInstant(utcTz).toInstant()) < 0) {
                    patients.add(medication.getId());
                }
            }

            for (Medication m : medicalRecordList_full) {
                if (patients.contains(m.getId())) {
                    medicalRecordList.add(m);
                }
            }

        } else if (exportQuery.getOnlyFirst() && !exportQuery.getDataBeforeMedicine()) {
            logger.info("only first and not before first");
            medicalRecordList = medicalRecordList_first;
        } else {
            logger.info("not only first and not before first");
            medicalRecordList = medicalRecordList_full;
        }

        logger.info("medicalRecordList:" + Integer.toString(medicalRecordList.size()));
        exportDao.updatePatientNum(job.getId(),medicalRecordList.size());

        ExportMedicalOutput outputWriter;
        try {
            logger.info("init output");
            outputWriter = new ExportMedicalOutput(outputDir.getAbsolutePath(), columnLabelName, exportQuery, job);
        } catch (IOException e) {
            logger.error("Export writer failed to create: {}", Util.stackTraceErrorToString(e));
            medicaljobClosingHandler(false, isPscRequired, job, outputDir, null, 0);
            return;
        }

        // This job marked as removed
        if (this.jobStopIndicator.containsKey(jobId)) {
            this.jobStopIndicator.remove(jobId);
            logger.warn("Job <{}> cancelled by user.", jobId);
            outputWriter.writeMetaFile(String.format("%nJob cancelled by user.%n"));
            medicaljobClosingHandler(false, isPscRequired, job, outputDir, outputWriter, 0);
            return;
        }

        if (isPscRequired) {
            // Prep PSC instance
            iss.stopLocalInflux();
            // Local DB may take up to 10s to stop
            try {
                Thread.sleep(10_000);
            } catch (InterruptedException e) {
                logger.error("Stop local Influx failed, {}", Util.stackTraceErrorToString(e));
                return;
            }
            iss.setupRemoteInflux();
            if (!iss.getHasStartedPscInflux()) {
                // Psc not working, should exit
                logger.error("Selected PSC InfluxDB but failed to start!");
                medicaljobClosingHandler(true, true, job, outputDir, null, 0);
                return;
            }
        } else {
            iss.stopRemoteInflux();
            // Remote DB may take up to 3s to stop
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                logger.error("Stop remote Influx failed, {}", Util.stackTraceErrorToString(e));
                return;
            }
            iss.setupLocalInflux();
            if (!iss.getHasStartedLocalInflux()) {
                // Local not working, should exit
                logger.error("Selected local InfluxDB but failed to start!");
                medicaljobClosingHandler(true, false, job, outputDir, null, 0);
                return;
            }
        }

        int paraCount = determineParaNumber();
        // Dirty hack to migrate timeout problems, should remove this some time later
        if (exportQuery.getPeriod() < 20 || labelCount > 8)
            paraCount *= 0.8;
        paraCount = paraCount > 0 ? paraCount : 1;

        // Get some basic info for exporting
        int numP = getNumOfPatients();
        // Try again only once
        if (numP == -1)
            numP = getNumOfPatients();
        outputWriter.writeInitialMetaText(numP, medicalRecordList.size(), paraCount);

        // Final prep for running query task
        BlockingQueue<Medication> idQueue = new LinkedBlockingQueue<>(medicalRecordList);
        Map<Medication, Integer> errorCount = new HashMap<>();
        AtomicInteger validPatientCounter = new AtomicInteger(0);
        AtomicInteger finishedPatientCounter = new AtomicInteger(0);

        ExecutorService scheduler = generateNewThreadPool(paraCount);
        Runnable queryTask = () -> {
            InfluxDB influxDB = generateIdbClient(false);
            Medication onerecord;
            while ((onerecord = idQueue.poll()) != null) {
                logger.info("date:" + onerecord.getChartDate().atZone(nycTz).withZoneSameInstant(utcTz));
                try {
                    // This job marked as removed, so this thread should exit
                    if (this.jobStopIndicator.containsKey(jobId)) {
                        this.jobStopIndicator.put(jobId, true);
                        return;
                    }

                    // set version condition
                    List<PatientTimeLine> files = validateCsvService.getPatientTimeLinesByVersionID("realpsc",version, onerecord.getId());
                    String versionCondition = versionDao.getVersionCondition(files);
                    if(files.isEmpty()){
                        outputWriter.writeMetaFile(String.format("  PID <%s> <%s> don't have enough data to export.%n", onerecord.getId(),onerecord.getChartDate().atZone(nycTz).withZoneSameInstant(utcTz).toInstant()));
                        finishedPatientCounter.getAndIncrement();
                        exportDao.updatePatientFinishedNum(job.getId(),finishedPatientCounter.get());
                        continue;
                    }

                    // First get the group by time offset
                    ResultTable[] testOffset = InfluxUtil.justQueryData(influxDB, true, String.format(
                            "SELECT time, count(Time) From \"%s\" WHERE (arType='%s') and "+versionCondition+" GROUP BY time(%ds) fill(none) ORDER BY time ASC LIMIT 1",
                            onerecord.getId(), job.getAr() ? "ar" : "noar", exportQuery.getPeriod()));
                    if (testOffset.length != 1) {
                        outputWriter.writeMetaFile(
                                String.format("  PID <%s> <%s> don't have enough data to export.%n", onerecord.getId(),onerecord.getChartDate().atZone(nycTz).withZoneSameInstant(utcTz).toInstant()));
                        finishedPatientCounter.getAndIncrement();
                        exportDao.updatePatientFinishedNum(job.getId(),finishedPatientCounter.get());
                        continue;
                    }
                    // Then fetch meta data regrading file segments and build the query string
                    List<DataTimeSpanBean> dtsb = AnalysisUtil.getPatientAllDataSpan(influxDB, logger, onerecord.getId(),versionCondition);
                    String startTime = AnalysisUtil.getPatientStartTime(influxDB,logger,onerecord.getId(),job.getAr(),versionCondition);
                    ExportMedicalQueryBuilder eq = new ExportMedicalQueryBuilder(Instant.parse(startTime),
                            Instant.parse((String) testOffset[0].getDataByColAndRow(0, 0)), dtsb, groups, columns, exportQuery,
                            job.getAr(), onerecord,versionCondition);
                    String finalQueryStrings = eq.getQueryString();
                    // logger.info("medical time out:"+onerecord.getChartDate().toString());
                    // logger.info("medical time in:"+eq.getMedicalTime().toEpochMilli());
                    // logger.info("query start:"+eq.getQueryStartTime().toEpochMilli());
                    // logger.info("expect start:"+eq.getExpectStartTime().toEpochMilli());
                    // logger.info("query end:"+ eq.getQueryEndTime().toEpochMilli());
                    // logger.info("expect end:"+eq.getExpectEndTime().toEpochMilli());
                    logger.info(finalQueryStrings);
                    if (finalQueryStrings.isEmpty()) {
                        outputWriter.writeMetaFile(String.format("  PID <%s> <%s> no available data.%n", onerecord.getId(),
                                onerecord.getChartDate().atZone(nycTz).withZoneSameInstant(utcTz).toInstant()));
                        finishedPatientCounter.getAndIncrement();
                        exportDao.updatePatientFinishedNum(job.getId(),finishedPatientCounter.get());
                        continue;
                    }
                    logger.debug("Query for <{}>: {}", onerecord.getId(), finalQueryStrings);
                    // Execuate the query
                    ResultTable[] res = InfluxUtil.justQueryData(influxDB, true, finalQueryStrings);
                    if (res.length != 1) {
                        outputWriter.writeMetaFile(String.format("  PID <%s> <%s> empty result from database.%n",
                                onerecord.getId(), onerecord.getChartDate().atZone(nycTz).withZoneSameInstant(utcTz).toInstant()));
                        finishedPatientCounter.getAndIncrement();
                        exportDao.updatePatientFinishedNum(job.getId(),finishedPatientCounter.get());
                        continue;
                    }
                    // write into csv file
                    outputWriter.writeForOnePatient(onerecord.getId(), res[0], eq, dtsb, onerecord);
                    validPatientCounter.getAndIncrement();
                    finishedPatientCounter.getAndIncrement();
                    exportDao.updatePatientFinishedNum(job.getId(),finishedPatientCounter.get());

                } catch (Exception ee) {
                    // All exception will be logged (disregarded) and corresponding PID will be tried again
                    logger.error(String.format("%s: %s", onerecord.getId(), Util.stackTraceErrorToString(ee)));
                    int alreadyFailed = errorCount.getOrDefault(onerecord, -1);
                    if (alreadyFailed == -1) {
                        errorCount.put(onerecord, 0);
                        alreadyFailed = 0;
                    } else {
                        errorCount.put(onerecord, ++alreadyFailed);
                    }
                    // Reinsert failed user into queue, but no more than 3 times
                    if (alreadyFailed < 3) {
                        idQueue.add(onerecord);
                    } else {
                        logger.error(String.format("%s: Failed more than 3 times.", onerecord.getId()));
                        outputWriter.writeMetaFile(String.format("  PID <%s> failed multiple times, possible program error.%n",
                                onerecord.getId()));
                        idQueue.remove(onerecord);
                        finishedPatientCounter.getAndIncrement();
                        exportDao.updatePatientFinishedNum(job.getId(),finishedPatientCounter.get());
                    }
                }

            }
            influxDB.close();
        };

        for (int i = 0; i < paraCount; ++i) {
            scheduler.submit(queryTask);
        }
        scheduler.shutdown();
        try {
            // TODO: PSC 2 day limit!
            scheduler.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error(Util.stackTraceErrorToString(e));
        } finally {
            if (this.jobStopIndicator.getOrDefault(jobId, false).equals(true)) {
                // Job removed special procedure
                this.jobStopIndicator.remove(jobId);
                outputWriter.writeMetaFile(String.format("%nJob cancelled by user during execution.%n"));
                logger.warn("Job <{}> cancelled by user during execution.", jobId);
            }
            // Normal exit procedure
            medicaljobClosingHandler(false, isPscRequired, job, outputDir, outputWriter, validPatientCounter.get());
        }

    }

    /**
     * Limit running time for get num of patients
     *
     * @return Patients#
     */
    private int getNumOfPatients() {
        FutureTask<Integer> task = new FutureTask<>(() -> {
            InfluxDB idb = InfluxDBFactory.connect(InfluxappConfig.IFX_ADDR, InfluxappConfig.IFX_USERNAME,
                    InfluxappConfig.IFX_PASSWD,
                    new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(20, TimeUnit.SECONDS));
            logger.info("Connected to InfluxDB...");
            int numOfPatient = AnalysisUtil.numberOfPatientInDatabase(idb, logger);
            logger.info("Basic info got, ready to process...");
            idb.close();
            return numOfPatient;
        });
        new Thread(task, "GetNumPaitnets").start();
        int numP = -1;
        try {
            numP = task.get(1, TimeUnit.MINUTES);
        } catch (Exception e) {
            logger.error("Timeout getting num of patients", e);
            // Force stop the get # task
            task.cancel(true);
        }
        return numP;
    }

    /**
     * Handler when a job is finished (With error or not)
     */
    private void jobClosingHandler(boolean idbError, boolean isPscNeeded, ExportWithBLOBs job, File outputDir, ExportOutput eo,
            int validPatientNumber) {
        // We will leave the local Idb running after the job
        if (isPscNeeded && !this.iss.stopRemoteInflux()) {
            idbError = true;
        }
        if (eo != null) {
            if (idbError) {
                eo.writeMetaFile(String.format("%nInfluxDB probably failed on <%s>.%n", job.getDbType()));
            }
            eo.close(validPatientNumber);
        }
        FileZip.zip(outputDir.getAbsolutePath(), InfluxappConfig.ARCHIVE_DIRECTORY + "/output_" + job.getId() + ".zip", "");

        //split file zip
        FileZip.zip(outputDir.getAbsolutePath() + "_split", InfluxappConfig.ARCHIVE_DIRECTORY + "/output_split_" + job.getId() + ".zip", "");

        job.setFinished(true);
        ExportWithBLOBs updateJob = new ExportWithBLOBs();
        updateJob.setId(job.getId());
        updateJob.setFinished(true);
        exportDao.updateByPrimaryKeySelective(updateJob);
    }

    /**
     * handler when a medical job is finished
     */
    private void medicaljobClosingHandler(boolean idbError, boolean isPscNeeded, ExportWithBLOBs job, File outputDir,
            ExportMedicalOutput eo, int validPatientNumber) {
        // We will leave the local Idb running after the job
        if (isPscNeeded && !this.iss.stopRemoteInflux()) {
            idbError = true;
        }
        if (eo != null) {
            if (idbError) {
                eo.writeMetaFile(String.format("%nInfluxDB probably failed on <%s>.%n", job.getDbType()));
            }
            eo.close(validPatientNumber);
        }
        FileZip.zip(outputDir.getAbsolutePath(), InfluxappConfig.ARCHIVE_DIRECTORY + "/output_" + job.getId() + ".zip", "");

        //split file zip
        FileZip.zip(outputDir.getAbsolutePath() + "_split", InfluxappConfig.ARCHIVE_DIRECTORY + "/output_split_" + job.getId() + ".zip", "");

        job.setFinished(true);
        ExportWithBLOBs updateJob = new ExportWithBLOBs();
        updateJob.setId(job.getId());
        updateJob.setFinished(true);
        exportDao.updateByPrimaryKeySelective(updateJob);
    }

    /**
     * Find if current queue has a job that requires PSC
     *
     * @return Return the Optional job
     */
    @SuppressWarnings("unused")
    private Optional<ExportWithBLOBs> queueHasNextPscJob() {
        return this.jobQueue.parallelStream().findAny().filter(job -> job.getDbType().equals("psc"));
    }

    List<String> parseAggregationGroupColumnsString(String columnsJson) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ColumnJSON json = mapper.readValue(columnsJson, ColumnJSON.class);
        return columnService.selectColumnsByAggregationGroupColumns(json);
    }

    /**
     * get the eeg data for chart.
     */
    public ArrayList<List<Object>> getEEGChartData(EEGChart eegChart, List<PatientTimeLine> files) {
        List<List<String>> columns = new ArrayList<>(1);
        List<DownsampleGroup> groups = new ArrayList<>(1);
        ArrayList<List<Object>> rows = new ArrayList<>();
        DownsampleGroup downsampleGroup = new DownsampleGroup();
        Downsample downsample = new Downsample();
        downsampleGroup.setAggregation(eegChart.getAggregationMethod());
        downsampleGroup.setDownsample(eegChart.getAggregationMethod());
        downsampleGroup.setColumns(eegChart.getColumns());
        downsample.setPeriod(eegChart.getPeriod());
        downsample.setMinBinRow(eegChart.getMinBinRow());
        downsample.setDownsampleFirst(eegChart.getDownsampleFirst());
        groups.add(downsampleGroup);
        downsample.setDuration(0);
        downsample.setOrigin(0);
        ResultTable[] res = new ResultTable[1];
        String versionCondition = versionDao.getVersionCondition(files);
        logger.info("vc: ",versionCondition);

        try {
            columns.add(parseAggregationGroupColumnsString(eegChart.getColumns()));
        } catch (IOException e) {
            logger.error("Parse aggregation group failed: {}", Util.stackTraceErrorToString(e));
        }

        InfluxDB influxDB = generateIdbClient(false);
        try {
            logger.info(eegChart.getPatientID());
            ResultTable[] testOffset = InfluxUtil.justQueryData(influxDB, true, String.format(
                    "SELECT time, count(Time) From \"%s\" WHERE (arType='%s') and "+versionCondition+" GROUP BY time(%ds) fill(none) ORDER BY time ASC LIMIT 1",
                    eegChart.getPatientID(), eegChart.isAr() ? "ar" : "noar", eegChart.getPeriod()));
            if (testOffset.length != 1) {
                logger.info("no enough data");
            }
            // Then fetch meta data regrading file segments and build the query string
            List<DataTimeSpanBean> dtsb = AnalysisUtil.getPatientAllDataSpan(influxDB, logger, eegChart.getPatientID(),versionCondition);
            String startTime = AnalysisUtil.getPatientStartTime(influxDB,logger,eegChart.getPatientID(),eegChart.isAr(),versionCondition);
            ExportQueryBuilder eq = new ExportQueryBuilder(Instant.parse(startTime), Instant.parse((String) testOffset[0].getDataByColAndRow(0, 0)), dtsb,
                    groups, columns, downsample, eegChart.isAr(), versionCondition);
            String finalQueryString = eq.getQueryString();
            if (finalQueryString.isEmpty()) {
                logger.info("fail to generate query");
            }
            logger.info(finalQueryString);
            // Execuate the query
            res = InfluxUtil.justQueryData(influxDB, true, finalQueryString);
            logger.info("res finished");
            // covert res to map or list and return list
            int dataRows = res[0].getRowCount();
            for (int i = 0; i < dataRows; i++) {
                List<Object> tmp = new ArrayList<>();
                tmp.add(res[0].getDatalistByRow(i).get(0));
                tmp.add(res[0].getDatalistByRow(i).get(1));
                rows.add(tmp);
            }
        } catch (Exception ee) {
            ee.printStackTrace();
            logger.info("get eeg chart data failed");
        }
        influxDB.close();
        return rows;
    }

    // Below are interactions with DAOs

    public int insertDownsample(Downsample downsample) throws Exception {
        return downsampleDao.insert(downsample);
    }

    public int insertMedicalDownsample(MedicalDownsample downsample) {
        return medicalDownsampleDao.insert(downsample);
    }

    public List<Downsample> selectAll() {
        return downsampleDao.selectAll();
    }

    public List<MedicalDownsample> selectmedicalAll() {
        return medicalDownsampleDao.selectAll();
    }

    public Downsample selectByPrimaryKey(int id) {
        return downsampleDao.selectByPrimaryKey(id);
    }

    public MedicalDownsample selectmedicalByPrimaryKey(int id) {
        return medicalDownsampleDao.selectByPrimaryKey(id);
    }

    public int updateByPrimaryKey(Downsample downsample) {
        return downsampleDao.updateByPrimaryKey(downsample);
    }

    public int updatemedicalByPrimaryKey(MedicalDownsample medicalDownsample) {
        return medicalDownsampleDao.updateByPrimaryKey(medicalDownsample);
    }

    public int deleteByPrimaryKey(int id) {
        return downsampleDao.deleteByPrimaryKey(id);
    }

    public List<DownsampleGroup> selectAllAggregationGroupByQueryId(Integer queryId) {
        return downsampleGroupDao.selectAllAggregationGroupByQueryId(queryId);
    }

    public List<MedicalDownsampleGroup> selectAllmedicalAggregationGroupByQueryId(Integer queryId) {
        return medicalDownsampleGroupDao.selectAllAggregationGroupByQueryId(queryId);
    }

    public boolean insertAggregationGroup(DownsampleGroup group) {
        return downsampleGroupDao.insertDownsampleGroup(group);
    }

    public boolean insertmedicalAggregationGroup(MedicalDownsampleGroup group) {
        return medicalDownsampleGroupDao.insertMedicalDownsampleGroup(group);
    }

    public int updateAggregationGroup(DownsampleGroup group) {
        return downsampleGroupDao.updateByPrimaryKeySelective(group);
    }

    public int updatemedicalAggregationGroup(MedicalDownsampleGroup group) {
        return medicalDownsampleGroupDao.updateByPrimaryKeySelective(group);
    }

    public DownsampleGroup selectAggregationGroupByGroupId(Integer groupId) {
        return downsampleGroupDao.selectDownsampleGroup(groupId);
    }

    public MedicalDownsampleGroup selectmedicalAggregationGroupById(Integer groupId) {
        return medicalDownsampleGroupDao.seleMedicalDownsampleGroup(groupId);
    }

    public int insertExportJob(ExportWithBLOBs job) {
        return exportDao.insertExportJob(job);
    }

    public List<ExportVO> selectAllExportJobOnLocalMachine() {
        return exportDao.selectAllExportJobOnLocalMachine();
    }

    public int deleteGroupByPrimaryKey(Integer groupId) {
        return downsampleGroupDao.deleteByPrimaryKey(groupId);
    }

    public int deletemedicalGroupByPrimaryKey(Integer groupId) {
        return medicalDownsampleGroupDao.deleteByPrimaryKey(groupId);
    }

    /**
     * Generate an object for output directory class
     *
     * @param purpose What's this dir for
     * @param uuid    UUID for this dir (Current time in RFC3339 if is null)
     */
    private File generateOutputDir(String purpose, String uuid) {
        String identifier = uuid;
        if (identifier == null)
            identifier = "_(" + LocalDateTime.now().toString() + ")";
        // Windows name restriction
        String path = InfluxappConfig.OUTPUT_DIRECTORY + purpose + identifier.replace(':', '.');
        File outputDir = new File(path);
        boolean dirCreationSuccess = true;

        if (!outputDir.exists()) {
            String err = "Failed to create 'Results' dir. ";
            try {
                if (!outputDir.mkdirs()) {
                    dirCreationSuccess = false;
                }
            } catch (SecurityException se) {
                err += se.getLocalizedMessage();
                dirCreationSuccess = false;
            }
            // Use a flag for flexible work flow
            if (!dirCreationSuccess) {
                logger.error(err);
                return null;
            }
            return outputDir;
        }
        // If the directory already exists (bad for us), we should consider this operation failed
        return null;
    }

    /**
     * How parallel should we go?
     */
    private int determineParaNumber() {
        int paraCount = (int) Math.round(loadFactor * InfluxappConfig.AvailableCores);
        return paraCount > 0 ? paraCount : 1;
    }

    /**
     * Generate a new fixed threadpool (when new request comes)
     */
    private ExecutorService generateNewThreadPool(int i) {
        return Executors.newFixedThreadPool(i);
    }

    /**
     * Stop periodically checking for jobs (DO NOT INVOKE UNLESS TESTING)
     *
     * @param shouldForce Should interrupt the running job
     */
    protected void stopScheduler(boolean shouldForce) {
        this.jobCheckerThread.cancel(shouldForce);
    }

    public List<String> selectAllMedicine() {
        // TODO Auto-generated method stub
        return medicationDao.selectAllMedication();
    }

}
