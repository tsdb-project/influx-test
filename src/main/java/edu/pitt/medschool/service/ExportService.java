package edu.pitt.medschool.service;

import edu.pitt.medschool.config.InfluxappConfig;
import edu.pitt.medschool.framework.util.RunThread;
import edu.pitt.medschool.framework.util.Util;
import edu.pitt.medschool.model.dao.*;
import edu.pitt.medschool.model.dto.CsvFileExample;
import okhttp3.OkHttpClient;
import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.pitt.medschool.controller.analysis.vo.DownsampleVO;
import edu.pitt.medschool.controller.analysis.vo.MedicalDownsampleVO;
import edu.pitt.medschool.model.dto.ExportWithBLOBs;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.*;

@Service
public class ExportService {

    @Value("${machine}")
    private String uuid;
    @Value("${load}")
    private double loadFactor;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ExportDao exportDao;
    private final CsvFileDao csvFileDao;
    private final ImportProgressDao importProgressDao;
    private final DownsampleDao downsampleDao;
    private final DownsampleGroupDao downsampleGroupDao;
    private final MedicalDownsampleDao medicalDownsampleDao;
    private final MedicalDownsampleGroupDao medicalDownsampleGroupDao;

    @Autowired
    public ExportService(ExportDao exportDao,CsvFileDao csvFileDao, ImportProgressDao importProgressDao, DownsampleDao downsampleDao, DownsampleGroupDao downsampleGroupDao, MedicalDownsampleDao medicalDownsampleDao, MedicalDownsampleGroupDao medicalDownsampleGroupDao) {
        this.exportDao = exportDao;
        this.csvFileDao = csvFileDao;
        this.importProgressDao = importProgressDao;
        this.downsampleDao = downsampleDao;
        this.downsampleGroupDao = downsampleGroupDao;
        this.medicalDownsampleDao = medicalDownsampleDao;
        this.medicalDownsampleGroupDao = medicalDownsampleGroupDao;
    }

    public int completeJobAndInsert(ExportWithBLOBs job) throws JsonProcessingException {
        DownsampleVO downsampleVO = new DownsampleVO();
        downsampleVO.setDownsample(downsampleDao.selectByPrimaryKey(job.getQueryId()));
        downsampleVO.setGroups(downsampleGroupDao.selectAllAggregationGroupByQueryId(job.getQueryId()));
        ObjectMapper mapper = new ObjectMapper();
        job.setQueryJson(mapper.writeValueAsString(downsampleVO));
        job.setMachine(uuid);
        job.setDbVersion(importProgressDao.selectDatabaseVersion(uuid));
        job.setMedical(false);

        return exportDao.insertExportJob(job);
    }
    
    public int completeMedicalJobAndInsert(ExportWithBLOBs job)throws JsonProcessingException{
    	MedicalDownsampleVO medicalDownsampleVO = new MedicalDownsampleVO();
    	medicalDownsampleVO.setMedicalDownsample(medicalDownsampleDao.selectByPrimaryKey(job.getQueryId()));
    	medicalDownsampleVO.setGroups(medicalDownsampleGroupDao.selectAllAggregationGroupByQueryId(job.getQueryId()));
    	ObjectMapper mapper = new ObjectMapper();
    	job.setQueryJson(mapper.writeValueAsString(medicalDownsampleVO));
    	job.setMachine(uuid);
    	job.setDbVersion(importProgressDao.selectDatabaseVersion(uuid));
    	job.setMedical(true);
    	return exportDao.insertExportJob(job);
    }

    public int deleteExportJobById(Integer exportId) {
        return this.exportDao.markAsDeletedById(exportId);
    }

    public Boolean startExportingEEG(String year){
        try {
            this.ExportEEGByYear(year);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private void ExportEEGByYear(String year) {
        List<String> patientIDs;

        CsvFileExample ce = new CsvFileExample();
        CsvFileExample.Criteria cec = ce.createCriteria();
        cec.andPidLike("PUH-" + year + "%");
        patientIDs = csvFileDao.selectIdByCustom(ce);

        int paraCount = (int) Math.round(loadFactor * InfluxappConfig.AvailableCores);
        paraCount = paraCount > 0 ? paraCount : 1;

        BlockingQueue<String> idQueue = new LinkedBlockingQueue<>(patientIDs);
        ExecutorService scheduler = Executors.newFixedThreadPool(paraCount);

        String exportBaseDir = "/Volumes/INFLUX_RAID/First_12_hours/";

        Runnable queryTask = () -> {
            String pid;
            InfluxDB influxDB = generateIdbClient(false);
            while ((pid=idQueue.poll())!=null){

                logger.info("current PID: " + pid);

                QueryResult res1 = influxDB.query(new Query(String.format("select first(\"I1_1\") from \"%s\" where arType='ar'", pid),"data"));
                if(res1.getResults().get(0).getSeries().size() > 1){
                    String startTime = res1.getResults().get(0).getSeries().get(0).getValues().get(0).get(0).toString();
                    logger.info("start time: " + startTime);
                    DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
                    String endTime = LocalDateTime.parse(startTime,df).plusHours(12).toString()+"Z";


                    String query = String.format("select \u002A from /%s/ where arType='ar' AND time<'%s'",pid,endTime);
                    String exportDir = exportBaseDir + pid + "-12hours.csv";
                    ProcessBuilder pd = new ProcessBuilder("influx", "-execute", query, "-database","data","-precision","rfc3339","-format","csv");
                    pd.redirectOutput(new File(exportDir));
//                    String command = String.format("influx -execute \"%s\" -database data -precision rfc3339 -format csv > \"%s\" ",query,exportDir);
//                    logger.info("query is: " +  command);

                    try {
                        Process process = pd.start();
                        new RunThread(process.getInputStream(), "INFO").start();
                        new RunThread(process.getErrorStream(), "ERR").start();
                        if (process.waitFor() == 0) {
                            logger.info("process file successfully: " + exportDir);
                        } else {
                            logger.info("process file failed: " + exportDir);
                        }
                    } catch (Exception e) {
                        logger.info("process file error occurred: " + exportDir);
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
            scheduler.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error(Util.stackTraceErrorToString(e));
        }
    }

    private InfluxDB generateIdbClient(Boolean needGzip) {
        // Disable GZip to save CPU
        InfluxDB idb = InfluxDBFactory.connect(InfluxappConfig.IFX_ADDR, InfluxappConfig.IFX_USERNAME,
                InfluxappConfig.IFX_PASSWD, new OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS)
                        .readTimeout(1, TimeUnit.HOURS).writeTimeout(1, TimeUnit.HOURS));
        if (needGzip) {
            idb.enableGzip();
        } else {
            idb.disableGzip();
        }
        BatchOptions bo = BatchOptions.DEFAULTS.consistency(InfluxDB.ConsistencyLevel.ALL)
                // Flush every 2000 Points, at least every 100ms, buffer for failed oper is 2200
                .actions(2000).flushDuration(500).bufferLimit(10000).jitterDuration(200)
                .exceptionHandler((p, t) -> logger.warn("Write point failed", t));
        idb.enableBatch(bo);

        return idb;
    }
}
