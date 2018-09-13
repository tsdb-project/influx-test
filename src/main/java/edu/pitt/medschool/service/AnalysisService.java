package edu.pitt.medschool.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriter;
import edu.pitt.medschool.algorithm.AnalysisUtil;
import edu.pitt.medschool.algorithm.ExportQuery;
import edu.pitt.medschool.config.InfluxappConfig;
import edu.pitt.medschool.controller.analysis.vo.ColumnJSON;
import edu.pitt.medschool.controller.analysis.vo.DownsampleGroupVO;
import edu.pitt.medschool.framework.influxdb.InfluxUtil;
import edu.pitt.medschool.framework.influxdb.ResultTable;
import edu.pitt.medschool.framework.util.Util;
import edu.pitt.medschool.model.DataTimeSpanBean;
import edu.pitt.medschool.model.dao.*;
import edu.pitt.medschool.model.dto.Downsample;
import okhttp3.OkHttpClient;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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
    DownsampleGroupColumnDao downsampleGroupColumnDao;
    @Autowired
    DownsampleMetaDao downsampleMetaDao;
    @Autowired
    DownsampleGroupAggrDao downsampleGroupAggrDao;
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

    private BlockingQueue<String> idQueue = new LinkedBlockingQueue<>();

    private Map<String, Integer> errorCount = new HashMap<>();

    /**
     * Export (downsample) a single query to files (Could be called mutiple times)
     */
    public void exportToFile(Integer queryId, Boolean testRun) throws IOException {
        Downsample exportQuery = downsampleDao.selectByPrimaryKey(queryId);

        // Create Folder
        File outputDir = generateOutputDir(exportQuery.getAlias());
        if (outputDir == null)
            return;
        // Create sub-folder for every patient's file
        if (!new File(outputDir.getAbsolutePath() + "/patients/").mkdirs())
            return;

        String pList = exportQuery.getPatientlist();
        List<String> patientIDs;
        if (pList == null || pList.isEmpty()) {
            // No user-defined, get patient list by uuid
            patientIDs = importedFileDao.selectAllImportedPidOnMachine(testRun ? "jetest" : uuid);
        } else {
            // Init list with user-defined
            patientIDs = Arrays.stream(pList.split(",")).map(String::toUpperCase).collect(Collectors.toList());
        }
        idQueue = new LinkedBlockingQueue<>(patientIDs);
        String projectRootFolder = outputDir.getAbsolutePath();
        AtomicInteger validPatientCounter = new AtomicInteger(0);

        // Get columns data
        List<DownsampleGroupVO> groups = downsampleGroupDao.selectAllDownsampleGroupVO(queryId);
        int labelCount = groups.size();
        List<List<String>> columns = new ArrayList<>(labelCount);
        List<String> columnLabelName = new ArrayList<>(labelCount);
        for (DownsampleGroupVO group : groups) {
            //TODO: More testing as it's not stable
            columns.add(parseAggregationGroupColumnsString(group.getColumns()));
            columnLabelName.add(group.getGroup().getLabel());
        }

        // CSV output
        String[] pHeader = new String[labelCount + 3],
                mainHeader = new String[labelCount + 4];
        populateHeaderNames(columnLabelName, pHeader, mainHeader);
        CSVWriter mainCsvWriter = new CSVWriter(new FileWriter(projectRootFolder + "/output.csv"));
        mainCsvWriter.writeNext(mainHeader);

        // Export meta-data writer
        BufferedWriter bw = new BufferedWriter(new FileWriter(projectRootFolder + "/output_meta.txt"));
        bw.write(String.format("EXPORT '%s' (#%d) STARTED ON '%s'%n%n", exportQuery.getAlias(), exportQuery.getId(), Instant.now()));
        bw.write(String.format("Total patients in database: %d%n", AnalysisUtil.numberOfPatientInDatabase(influxDB, logger)));
        bw.write(String.format("Ar status is: %s%n", exportQuery.getNeedar() ? "AR" : "NoAR"));
        bw.write(String.format("Number of patients for initial export: %d%n%n", patientIDs.size()));
        bw.flush();

        int paraCount = determineParaNumber();
        ExecutorService scheduler = generateNewThreadPool(paraCount);
        // Parallel query task
        Runnable queryTask = () -> {
            String patientId;
            while ((patientId = idQueue.poll()) != null) {
                try {
                    List<DataTimeSpanBean> dtsb = AnalysisUtil.getPatientAllDataSpan(influxDB, logger, patientId);
                    int minEveryBinSeconds = exportQuery.getMinEveryBinThershold() * 60;
                    double dropoutPercent = 1.0 * exportQuery.getMinTotalBinThreshold() / 100;

                    ExportQuery eq = new ExportQuery(dtsb, groups, columns, exportQuery);
                    ResultTable[] res = InfluxUtil.justQueryData(influxDB, true, eq.toQuery());
                    logger.debug(String.format("%s query: %s", patientId, eq.toQuery()));

                    if (res.length == 0) return;

                    int pHeadSize = pHeader.length, mainHeadSize = mainHeader.length;

                    // Analyze bins first
                    List<Integer> goodIDs = eq.getGoodDataTimeId();
                    long totalValidSeconds = AnalysisUtil.dataValidTotalSpan(goodIDs, dtsb) / 1000;
                    int totalBins = (int) Math.ceil(1.0 * totalValidSeconds / exportQuery.getPeriod());

                    bw.write(String.format(" '%s': '%d' seconds and '%d' bins.%n", patientId, totalValidSeconds, totalBins));

                    writeOutCsvFiles(projectRootFolder, pHeader, mainCsvWriter,
                            patientId, dtsb, res, pHeadSize, mainHeadSize, goodIDs);

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
            bw.write(String.format("%n%n# of valid patients: %d%n", validPatientCounter.get()));
            bw.write(String.format("END ON '%s'", Instant.now()));
            bw.close();
            mainCsvWriter.close();
        }
    }

    /**
     * Write indiviual csv and main.csv
     */
    private void writeOutCsvFiles(String projectRootFolder, String[] pHeader, CSVWriter mainCsvWriter,
                                  String patientId, List<DataTimeSpanBean> dtsb, ResultTable[] res,
                                  int pHeadSize, int mainHeadSize, List<Integer> goodIDs) throws IOException {
        CSVWriter pWriter = new CSVWriter(new FileWriter(String.format("%s/patients/%s.csv", projectRootFolder, patientId)));
        pWriter.writeNext(pHeader);
        for (int i = 0; i < res.length; i++) {
            ResultTable r = res[i];
            //TODO: Is this good?
            for (int j = 0; j < r.getRowCount(); j++) {
                List<Object> row = r.getDatalistByRow(j);
                String[] resultDataRow = row.stream().map(Object::toString).toArray(String[]::new);
                String[] pData = new String[pHeadSize], mainData = new String[mainHeadSize];
                mainData[0] = patientId;
                for (int k = 0; k < resultDataRow.length; k++) {
                    mainData[k + 1] = pData[k] = resultDataRow[k];
                }
                mainData[pHeadSize] = pData[pHeadSize - 1] = dtsb.get(goodIDs.get(i)).getFileUuid();
                pWriter.writeNext(pData);
                mainCsvWriter.writeNext(mainData);
            }
        }
        pWriter.close();
    }

    /**
     * Populate header names for main csv and indiviual csv
     * pHeader (Time,xxxx,Count,fileUUID)
     * mainHeader (PID,Time,xxxx,Count,fileUUID)
     */
    private void populateHeaderNames(List<String> columnLabelName, String[] pHeader, String[] mainHeader) {
        pHeader[0] = "Timestamp";
        pHeader[pHeader.length - 2] = "Count";
        pHeader[pHeader.length - 1] = "fileUUID";
        mainHeader[0] = "PID";
        mainHeader[1] = "Timestamp";
        mainHeader[mainHeader.length - 1] = "fileUUID";
        mainHeader[mainHeader.length - 2] = "Count";
        for (int i = 0; i < columnLabelName.size(); i++) {
            pHeader[i + 1] = columnLabelName.get(i);
            mainHeader[i + 2] = columnLabelName.get(i);
        }
    }

    public List<String> parseAggregationGroupColumnsString(String columnsJson) throws IOException {
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

    public List<DownsampleGroupVO> selectAllAggregationGroupByQueryId(Integer queryId) {
        return downsampleGroupDao.selectAllAggregationGroupByQueryId(queryId);
    }

    public boolean insertAggregationGroup(DownsampleGroupVO group) {
        return downsampleGroupDao.insertAggregationGroup(group);
    }

    public int updateAggregationGroup(DownsampleGroupVO group) {
        return downsampleGroupDao.updateByPrimaryKeyWithBLOBs(group.getGroup());
    }

    public DownsampleGroupVO selectAggregationGroupByGroupId(Integer groupId) {
        return downsampleGroupDao.selectDownsampleGroupVO(groupId);
    }

    public int deleteGroupByPrimaryKey(Integer groupId) {
        return downsampleGroupDao.deleteByPrimaryKey(groupId);
    }

    /**
     * Generate an object for output directory class
     */
    private File generateOutputDir(String purpose) {
        String rfc3339 = "_(" + LocalDateTime.now().toString() + ")";
        // Workaround for Windows name restriction
        String path = DIRECTORY + purpose + rfc3339.replace(':', '.');
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

}
