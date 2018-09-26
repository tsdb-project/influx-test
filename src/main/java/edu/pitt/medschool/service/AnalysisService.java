package edu.pitt.medschool.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.pitt.medschool.config.InfluxappConfig;
import edu.pitt.medschool.controller.analysis.vo.ColumnJSON;
import edu.pitt.medschool.controller.analysis.vo.ExportVO;
import edu.pitt.medschool.framework.influxdb.InfluxUtil;
import edu.pitt.medschool.framework.influxdb.ResultTable;
import edu.pitt.medschool.framework.util.FileZip;
import edu.pitt.medschool.framework.util.Util;
import edu.pitt.medschool.model.DataTimeSpanBean;
import edu.pitt.medschool.model.dao.*;
import edu.pitt.medschool.model.dto.Downsample;
import edu.pitt.medschool.model.dto.DownsampleGroup;
import edu.pitt.medschool.model.dto.ExportWithBLOBs;
import okhttp3.OkHttpClient;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Export functions
 */
@Service
public class AnalysisService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${machine}")
    private String uuid;

    @Value("${load}")
    private double loadFactor;

    @Autowired
    DownsampleDao downsampleDao;
    @Autowired
    DownsampleGroupDao downsampleGroupDao;
    @Autowired
    ExportDao exportDao;
    @Autowired
    ColumnService columnService;
    @Autowired
    InfluxSwitcherService iss;

    /*
     * Be able to restrict the epochs for which data are exported (e.g. specify to export up to the first 36 hours of available data, but truncate
     * data thereafter). Be able to specify which columns are exported (e.g. I10_*, I10_2 only, all data, etc) Be able to export down sampled data
     * (e.g. hourly mean, median, variance, etc)
     */
    @Autowired
    PatientDao patientDao;
    @Autowired
    ImportedFileDao importedFileDao;

    private Map<String, Integer> errorCount = new HashMap<>();

    /**
     * Export (downsample) a single query to files (Could be called mutiple times)
     */
    public void exportToFile(Integer exportId) {
        ExportWithBLOBs job = exportDao.selectByPrimaryKey(exportId);
        int queryId = job.getQueryId();
        Downsample exportQuery = downsampleDao.selectByPrimaryKey(queryId);
        boolean isPscRequired = job.getDbType().equals("psc");

        // Create Folder
        File outputDir = generateOutputDir(exportQuery.getAlias(), null);
        if (outputDir == null)
            return;

        String projectRootFolder = outputDir.getAbsolutePath();
        AtomicInteger validPatientCounter = new AtomicInteger(0);
        String pList = job.getPatientList();
        List<String> patientIDs;

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

        // Init the `outputWriter` ASAP
        ExportOutput outputWriter;
        try {
            outputWriter = new ExportOutput(projectRootFolder, columnLabelName, exportQuery, job);
        } catch (IOException e) {
            logger.error("Export writer failed to create: {}", Util.stackTraceErrorToString(e));
            jobClosingHandler(false, isPscRequired, job, outputDir, null, 0);
            return;
        }

        if (isPscRequired) {
            // Prep PSC instance
            iss.stopLocalInflux();
            if (pList == null || pList.isEmpty()) {
                // Override UUID setting to match PSC database
                patientIDs = importedFileDao.selectAllImportedPidOnMachine("realpsc");
            } else {
                patientIDs = Arrays.stream(pList.split(",")).map(String::toUpperCase).collect(Collectors.toList());
            }
            // Local DB may take up to 10s to stop
            try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException e) {
                logger.error("Stop local Influx failed, {}", Util.stackTraceErrorToString(e));
                return;
            }
            iss.setupRemoteInflux();
            if (!iss.getHasStartedPscInflux()) {
                // Psc not working, should exit
                logger.error("Selected PSC InfluxDB but failed to start!");
                jobClosingHandler(true, isPscRequired, job, outputDir, null, 0);
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
                jobClosingHandler(true, isPscRequired, job, outputDir, null, 0);
                return;
            }
            if (pList == null || pList.isEmpty()) {
                patientIDs = importedFileDao.selectAllImportedPidOnMachine(uuid);
            } else {
                patientIDs = Arrays.stream(pList.split(",")).map(String::toUpperCase).collect(Collectors.toList());
            }
        }

        int paraCount = determineParaNumber();
        outputWriter.writeInitialMetaText(AnalysisUtil.numberOfPatientInDatabase(InfluxappConfig.INFLUX_DB, logger), patientIDs.size(), paraCount);
        BlockingQueue<String> idQueue = new LinkedBlockingQueue<>(patientIDs);

        ExecutorService scheduler = generateNewThreadPool(paraCount);
        // Parallel query task
        Runnable queryTask = () -> {
            InfluxDB influxDB = generateIdbClient(false);
            String patientId;
            while ((patientId = idQueue.poll()) != null) {
                try {
                    ResultTable[] testOffset = InfluxUtil.justQueryData(influxDB, true, String.format(
                            "SELECT time, count(Time) From \"%s\" WHERE (arType='%s') GROUP BY time(%ds) fill(none) ORDER BY time ASC LIMIT 1",
                            patientId, job.getAr() ? "ar" : "noar", exportQuery.getPeriod()));
                    if (testOffset.length != 1) {
                        outputWriter.writeMetaFile(String.format("  PID <%s> don't have enough data to export.%n", patientId));
                        continue;
                    }
                    List<DataTimeSpanBean> dtsb = AnalysisUtil.getPatientAllDataSpan(influxDB, logger, patientId);
                    ExportQueryBuilder eq = new ExportQueryBuilder(Instant.parse((String) testOffset[0].getDataByColAndRow(0, 0)), dtsb, groups,
                            columns, exportQuery, job.getAr());
                    String finalQueryString = eq.getQueryString();
                    if (finalQueryString.isEmpty()) {
                        outputWriter.writeMetaFile(String.format("  PID <%s> no available data.%n", patientId));
                        continue;
                    }
                    logger.debug("Query for <{}>: {}", patientId, finalQueryString);
                    ResultTable[] res = InfluxUtil.justQueryData(influxDB, true, finalQueryString);

                    if (res.length != 1) {
                        outputWriter.writeMetaFile(String.format("  PID <%s> incorrect result from database.%n", patientId));
                        continue;
                    }

                    outputWriter.writeForOnePatient(patientId, res[0], eq, dtsb);
                    validPatientCounter.getAndIncrement();

                } catch (Exception ee) {
                    logger.error(String.format("%s: %s", patientId, Util.stackTraceErrorToString(ee)));
                    int alreadyFailed = this.errorCount.getOrDefault(patientId, -1);
                    if (alreadyFailed == -1) {
                        this.errorCount.put(patientId, 0);
                        alreadyFailed = 0;
                    } else {
                        this.errorCount.put(patientId, ++alreadyFailed);
                    }
                    // Reinsert failed user into queue, but no more than 3 times
                    if (alreadyFailed < 3) {
                        if (!idQueue.offer(patientId))
                            logger.error(String.format("%s: Re-queue failed.", patientId));
                    } else {
                        logger.error(String.format("%s: Failed more than 3 times.", patientId));
                        outputWriter.writeMetaFile(String.format("  PID <%s> failed multiple times, possible program error.%n", patientId));
                        idQueue.remove(patientId);
                    }
                }
            }
        };

        for (int i = 0; i < paraCount; ++i) {
            scheduler.submit(queryTask);
        }
        scheduler.shutdown();
        try {
            scheduler.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error(Util.stackTraceErrorToString(e));
        } finally {
            jobClosingHandler(false, isPscRequired, job, outputDir, outputWriter, validPatientCounter.get());
        }
    }

    /**
     * Handler when a job is finished (With error or not)
     */
    private void jobClosingHandler(boolean idbError, boolean isPscNeeded, ExportWithBLOBs job, File outputDir, ExportOutput eo, int validPatientNumber) {
        // We will leave the local Idb running after the job
        if (isPscNeeded) {
            if (!this.iss.stopRemoteInflux()) idbError = true;
        }
        if (eo != null) {
            if (idbError) {
                eo.writeMetaFile(String.format("InfluxDB probably failed on <%s>.%n", job.getDbType()));
            }
            eo.close(validPatientNumber);
        }
        FileZip.zip(outputDir.getAbsolutePath(), InfluxappConfig.ARCHIVE_DIRECTORY + "/output_" + job.getId() + ".zip", "");
        job.setFinished(true);
        ExportWithBLOBs updateJob = new ExportWithBLOBs();
        updateJob.setId(job.getId());
        updateJob.setFinished(true);
        exportDao.updateByPrimaryKeySelective(updateJob);
    }

    List<String> parseAggregationGroupColumnsString(String columnsJson) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ColumnJSON json = mapper.readValue(columnsJson, ColumnJSON.class);
        return columnService.selectColumnsByAggregationGroupColumns(json);
    }

    // Below are interactions with DAOs

    public int insertDownsample(Downsample downsample) throws Exception {
        return downsampleDao.insert(downsample);
    }

    public List<Downsample> selectAll() {
        return downsampleDao.selectAll();
    }

    public Downsample selectByPrimaryKey(int id) {
        return downsampleDao.selectByPrimaryKey(id);
    }

    public int updateByPrimaryKey(Downsample downsample) {
        return downsampleDao.updateByPrimaryKey(downsample);
    }

    public int deleteByPrimaryKey(int id) {
        return downsampleDao.deleteByPrimaryKey(id);
    }

    public List<DownsampleGroup> selectAllAggregationGroupByQueryId(Integer queryId) {
        return downsampleGroupDao.selectAllAggregationGroupByQueryId(queryId);
    }

    public boolean insertAggregationGroup(DownsampleGroup group) {
        return downsampleGroupDao.insertDownsampleGroup(group);
    }

    public int updateAggregationGroup(DownsampleGroup group) {
        return downsampleGroupDao.updateByPrimaryKeySelective(group);
    }

    public DownsampleGroup selectAggregationGroupByGroupId(Integer groupId) {
        return downsampleGroupDao.selectDownsampleGroup(groupId);
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
     * Generate one IdbClient for one thread when doing exports
     *
     * @param needGzip Unless Idb not running with Brainflux, you should disable GZip
     */
    private InfluxDB generateIdbClient(boolean needGzip) {
        InfluxDB idb = InfluxDBFactory.connect(InfluxappConfig.IFX_ADDR, InfluxappConfig.IFX_USERNAME, InfluxappConfig.IFX_PASSWD,
                new OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS).readTimeout(120, TimeUnit.MINUTES).writeTimeout(120,
                        TimeUnit.SECONDS));
        if (needGzip) {
            idb.enableGzip();
        } else {
            idb.disableGzip();
        }
        return idb;
    }

}
