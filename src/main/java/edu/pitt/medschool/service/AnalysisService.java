package edu.pitt.medschool.service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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
import edu.pitt.medschool.model.dao.AnalysisUtil;
import edu.pitt.medschool.model.dao.DownsampleDao;
import edu.pitt.medschool.model.dao.DownsampleGroupDao;
import edu.pitt.medschool.model.dao.ExportDao;
import edu.pitt.medschool.model.dao.ExportOutput;
import edu.pitt.medschool.model.dao.ExportQueryBuilder;
import edu.pitt.medschool.model.dao.ImportedFileDao;
import edu.pitt.medschool.model.dao.PatientDao;
import edu.pitt.medschool.model.dto.Downsample;
import edu.pitt.medschool.model.dto.DownsampleGroup;
import edu.pitt.medschool.model.dto.ExportWithBLOBs;
import okhttp3.OkHttpClient;

/**
 * Export functions
 */
@Service
public class AnalysisService {

    @Value("${machine}")
    private String uuid;

    @Value("${load}")
    private double loadFactor;

    private final static String DIRECTORY = "/tsdb/output/";

    @Autowired
    DownsampleDao downsampleDao;
    @Autowired
    DownsampleGroupDao downsampleGroupDao;
    @Autowired
    ExportDao exportDao;
    @Autowired
    ColumnService columnService;

    /*
     * Be able to restrict the epochs for which data are exported (e.g. specify to export up to the first 36 hours of available data, but truncate
     * data thereafter). Be able to specify which columns are exported (e.g. I10_*, I10_2 only, all data, etc) Be able to export down sampled data
     * (e.g. hourly mean, median, variance, etc)
     */
    @Autowired
    PatientDao patientDao;
    @Autowired
    ImportedFileDao importedFileDao;

    private InfluxDB influxDB = InfluxDBFactory.connect(InfluxappConfig.IFX_ADDR, InfluxappConfig.IFX_USERNAME, InfluxappConfig.IFX_PASSWD,
            new OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS).readTimeout(300, TimeUnit.SECONDS).writeTimeout(120, TimeUnit.SECONDS));
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private Map<String, Integer> errorCount = new HashMap<>();

    /**
     * Export (downsample) a single query to files (Could be called mutiple times)
     */
    public void exportToFile(Integer exportId, Boolean testRun) throws IOException {
        ExportWithBLOBs job = exportDao.selectByPrimaryKey(exportId);
        int queryId = job.getQueryId();
        Downsample exportQuery = downsampleDao.selectByPrimaryKey(queryId);

        // Create Folder
        File outputDir = generateOutputDir(exportQuery.getAlias(), null);
        if (outputDir == null)
            return;

        String pList = job.getPatientList();
        List<String> patientIDs;
        if (pList == null || pList.isEmpty()) {
            // No user-defined, get patient list by uuid
            patientIDs = importedFileDao.selectAllImportedPidOnMachine(testRun ? "jetest" : uuid);
        } else {
            // Init list with user-defined
            patientIDs = Arrays.stream(pList.split(",")).map(String::toUpperCase).collect(Collectors.toList());
        }
        BlockingQueue<String> idQueue = new LinkedBlockingQueue<>(patientIDs);
        String projectRootFolder = outputDir.getAbsolutePath();
        AtomicInteger validPatientCounter = new AtomicInteger(0);

        // Get columns data
        List<DownsampleGroup> groups = downsampleGroupDao.selectAllDownsampleGroup(queryId);
        int labelCount = groups.size();
        List<List<String>> columns = new ArrayList<>(labelCount);
        List<String> columnLabelName = new ArrayList<>(labelCount);
        for (DownsampleGroup group : groups) {
            columns.add(parseAggregationGroupColumnsString(group.getColumns()));
            columnLabelName.add(group.getLabel());
        }

        int paraCount = determineParaNumber();
        ExportOutput outputWriter = new ExportOutput(projectRootFolder, columnLabelName, exportQuery);
        outputWriter.writeInitialMetaText(AnalysisUtil.numberOfPatientInDatabase(influxDB, logger), patientIDs.size(), job.getAr(), paraCount);

        ExecutorService scheduler = generateNewThreadPool(paraCount);
        // Parallel query task
        Runnable queryTask = () -> {
            String patientId;
            while ((patientId = idQueue.poll()) != null) {
                try {
                    List<DataTimeSpanBean> dtsb = AnalysisUtil.getPatientAllDataSpan(influxDB, logger, patientId);
                    ResultTable r = InfluxUtil.justQueryData(influxDB, true, String.format(
                            "SELECT time, count(Time) From \"%s\" WHERE (arType='%s') GROUP BY time(%ds) fill(none) ORDER BY time ASC LIMIT 1",
                            patientId, job.getAr() ? "ar" : "noar", exportQuery.getPeriod()))[0];

                    ExportQueryBuilder eq = new ExportQueryBuilder(Instant.parse((String) r.getDataByColAndRow(0, 0)), dtsb, groups, columns, exportQuery, job.getAr());
                    String finalQueryString = eq.getQueryString();
                    if (finalQueryString.isEmpty()) {
                        outputWriter.writeMetaFile(String.format("  PID '%s' no available data.%n", patientId));
                        return;
                    }
                    logger.debug("Query for {}: {}", patientId, finalQueryString);
                    ResultTable[] res = InfluxUtil.justQueryData(influxDB, true, finalQueryString);

                    if (res.length != 1) {
                        outputWriter.writeMetaFile(String.format("  PID '%s' incorrect result from database.%n", patientId));
                        return;
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
                        outputWriter.writeMetaFile(String.format("  PID '%s' failed more than 3 times, possible program error.%n", patientId));
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
            outputWriter.close(validPatientCounter.get());
            FileZip.zip(outputDir.getAbsolutePath(), "output/output_" + job.getId() + ".zip", "");
            job.setFinished(true);
            ExportWithBLOBs updateJob = new ExportWithBLOBs();
            updateJob.setId(job.getId());
            updateJob.setFinished(true);
            exportDao.updateByPrimaryKeySelective(updateJob);
        }
    }

    private List<String> parseAggregationGroupColumnsString(String columnsJson) throws IOException {
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
        String path = DIRECTORY + purpose + identifier.replace(':', '.');
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

    public int insertExportJob(ExportWithBLOBs job) {
        return exportDao.insertExportJob(job);
    }

    public List<ExportVO> selectAllExportJobOnLocalMachine() {
        return exportDao.selectAllExportJobOnLocalMachine();
    }

}
