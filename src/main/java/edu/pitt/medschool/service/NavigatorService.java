package edu.pitt.medschool.service;

import static edu.pitt.medschool.framework.influxdb.InfluxUtil.generateIdbClient;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.influxdb.InfluxDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.pitt.medschool.config.InfluxappConfig;
import edu.pitt.medschool.controller.analysis.vo.ColumnJSON;
import edu.pitt.medschool.framework.influxdb.InfluxUtil;
import edu.pitt.medschool.framework.influxdb.ResultTable;
import edu.pitt.medschool.framework.util.FileZip;
import edu.pitt.medschool.framework.util.Util;
import edu.pitt.medschool.model.DataTimeSpanBean;
import edu.pitt.medschool.model.PatientTimeLine;
import edu.pitt.medschool.model.dao.AnalysisUtil;
import edu.pitt.medschool.model.dao.DownsampleDao;
import edu.pitt.medschool.model.dao.DownsampleGroupDao;
import edu.pitt.medschool.model.dao.ExportDao;
import edu.pitt.medschool.model.dao.ExportOutput;
import edu.pitt.medschool.model.dao.ExportQueryBuilder;
import edu.pitt.medschool.model.dao.ImportedFileDao;
import edu.pitt.medschool.model.dao.MedicalDownsampleDao;
import edu.pitt.medschool.model.dao.MedicalDownsampleGroupDao;
import edu.pitt.medschool.model.dao.MedicationDao;
import edu.pitt.medschool.model.dao.VersionDao;
import edu.pitt.medschool.model.dto.Downsample;
import edu.pitt.medschool.model.dto.DownsampleGroup;
import edu.pitt.medschool.model.dto.ExportWithBLOBs;

/**
 * 
 * @author HSX
 * 4/4/2020: call Python ML model to predict some patients
 *
 */
@Service
public class NavigatorService {

	@Autowired
    ValidateCsvService validateCsvService;
    @Autowired
    UsersService usersService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${machine}")
    private String uuid;

    @Value("${load}")
    private double loadFactor;

    private DownsampleDao downsampleDao;
    private DownsampleGroupDao downsampleGroupDao;
    private ExportDao exportDao;
    private ColumnService columnService;
    private InfluxSwitcherService iss;
    private ImportedFileDao importedFileDao;
    private VersionDao versionDao;
//    private final ScheduledFuture<?> jobCheckerThread; // Thread for running managed jobs
	
    @Autowired
    public NavigatorService(DownsampleDao downsampleDao, 
            DownsampleGroupDao downsampleGroupDao, ExportDao exportDao, ColumnService columnService, 
            InfluxSwitcherService iss, ImportedFileDao importedFileDao, VersionDao versionDao) {
        this.downsampleDao = downsampleDao;
        this.downsampleGroupDao = downsampleGroupDao;
        this.exportDao = exportDao;
        this.columnService = columnService;
        this.iss = iss;
        this.importedFileDao = importedFileDao;
        this.versionDao = versionDao;
    }
    
    
    
    
    
    
    
    
    
    
    List<String> parseAggregationGroupColumnsString(String columnsJson) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ColumnJSON json = mapper.readValue(columnsJson, ColumnJSON.class);
        return columnService.selectColumnsByAggregationGroupColumns(json);
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
//        FileZip.zip(outputDir.getAbsolutePath(), InfluxappConfig.ARCHIVE_DIRECTORY + "/output_" + job.getId() + ".zip", "");
//
//        //split file zip
//        FileZip.zip(outputDir.getAbsolutePath() + "_split", InfluxappConfig.ARCHIVE_DIRECTORY + "/output_split_" + job.getId() + ".zip", "");

        job.setFinished(true);
        ExportWithBLOBs updateJob = new ExportWithBLOBs();
        updateJob.setId(job.getId());
        updateJob.setFinished(true);
        exportDao.updateByPrimaryKeySelective(updateJob);
    }
    
    private File generateOutputDir(String purpose) {
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
	
	public String generateTestMatrix(ExportWithBLOBs job) throws IOException {
		String jobFromdb = new String("data");
//		int jobId = job.getId();
        int queryId = job.getQueryId();
        Downsample exportQuery = downsampleDao.selectByPrimaryKey(queryId);
        boolean isPscRequired = job.getDbType().equals("psc");

        // Create output folder, if failed, finish this process
        File outputDir = generateOutputDir("Predict");
        if (outputDir == null)
            return null;

        // get patient list
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

		List<DownsampleGroup> groups = new ArrayList<>();
        // hard code the 10 features same as in ML models （id=1~10)
		// there are these 10 groups in database.
        for (int i = 1; i <= 10; i++) {
        	groups.add(downsampleGroupDao.selectDownsampleGroup(i));
        }
        //prepare group sql
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
            return null;
        }

        // Init the `outputWriter`
        FileWriter outputWriter = new FileWriter(outputDir.getAbsolutePath() + "log.txt");
//        ExportOutput outputWriter;
//        try {
//            outputWriter = new ExportOutput(outputDir.getAbsolutePath(), columnLabelName, exportQuery, job);
//        } catch (IOException e) {
//            logger.error("Export writer failed to create: {}", Util.stackTraceErrorToString(e));
//            jobClosingHandler(false, isPscRequired, job, outputDir, null, 0);
//            return;
//        }

        // This job marked as removed
//        if (this.jobStopIndicator.containsKey(jobId)) {
//            this.jobStopIndicator.remove(jobId);
//            logger.warn("Job <{}> cancelled by user.", jobId);
//            outputWriter.writeMetaFile(String.format("%nJob cancelled by user.%n"));
//            jobClosingHandler(false, isPscRequired, job, outputDir, outputWriter, 0);
//            return;
//        }

//        if (isPscRequired) {
        // Prep PSC instance
        iss.stopLocalInflux();
        // Local DB may take up to 10s to stop
        try {
            Thread.sleep(10_000);
        } catch (InterruptedException e) {
            logger.error("Stop local Influx failed, {}", Util.stackTraceErrorToString(e));
            return null;
        }
        iss.setupRemoteInflux();
        if (!iss.getHasStartedPscInflux()) {
            // Psc not working, should exit
            logger.error("Selected PSC InfluxDB but failed to start!");
            jobClosingHandler(true, true, job, outputDir, null, 0);
            return null;
        }
        

//        int paraCount = determineParaNumber();
//        // Dirty hack to migrate timeout problems, should remove this some time later
//        if (exportQuery.getPeriod() < 20 || labelCount > 8)
//            paraCount *= 0.8;
//        paraCount = paraCount > 0 ? paraCount : 1;

//        // Get some basic info for exporting
//        int numP = getNumOfPatients();
//        // Try again only once
//        if (numP == -1)
//            numP = getNumOfPatients();
//        outputWriter.writeInitialMetaText(numP, patientIDs.size(), paraCount);

        // Final prep for running query task
        BlockingQueue<String> idQueue = new LinkedBlockingQueue<>(patientIDs);
        Map<String, Integer> errorCount = new HashMap<>();
        AtomicInteger validPatientCounter = new AtomicInteger(0);
        AtomicInteger finishedPatientCounter = new AtomicInteger(0);

//        ExecutorService scheduler = generateNewThreadPool(paraCount);
        Runnable queryTask = () -> {
            InfluxDB influxDB = generateIdbClient(false);
            String patientId;
            while ((patientId = idQueue.poll()) != null) {
                try {
                    // This job marked as removed, so this thread should exit
//                    if (this.jobStopIndicator.containsKey(jobId)) {
//                        this.jobStopIndicator.put(jobId, true);
//                        return;
//                    }

                    // get version condition
                    List<PatientTimeLine> files = validateCsvService.getPatientTimeLinesByVersionID("realpsc",usersService.getVersionByUserName(job.getUsername()),patientId);
                    //System.out.println("usersService.getVersionByUserName(job.getUsername()) = " + usersService.getVersionByUserName(job.getUsername()));
                    String versionCondition = versionDao.getVersionCondition(files);
                    if(files.isEmpty()){
                        outputWriter.write(String.format("  PID <%s> is not available in this version.%n", patientId));
                        finishedPatientCounter.getAndIncrement();
                        exportDao.updatePatientFinishedNum(job.getId(),finishedPatientCounter.get());
                        continue;
                    }
                   
                    // First get the group by time offset
                    //HSX here to change
//                    ResultTable[] testOffset = InfluxUtil.justQueryData(influxDB, true, String.format(
//                            "SELECT time, count(Time) From \"%s\" WHERE (arType='%s') and "+versionCondition+" GROUP BY time(%ds) fill(none) ORDER BY time ASC LIMIT 1",
//                            patientId, job.getAr() ? "ar" : "noar", exportQuery.getPeriod()));
                    ResultTable[] testOffset = null;
//                    if(jobFromdb.equals("data")) {
//                    	testOffset = InfluxUtil.justQueryData(influxDB, true, String.format(
//                              "SELECT time, count(Time) From \"%s\" WHERE (arType='%s') and "+versionCondition+" GROUP BY time(%ds) fill(none) ORDER BY time ASC LIMIT 1",
//                              patientId, job.getAr() ? "ar" : "noar", exportQuery.getPeriod()));
//                    	System.out.println(jobFromdb + ": " + testOffset.length);
//                    }
                    
                	testOffset = InfluxUtil.justQueryData(jobFromdb, influxDB, true, String.format(
                            "SELECT count(max_I1_1) From \"%s\" WHERE (arType='%s') GROUP BY time(%ds) fill(none) ORDER BY time ASC LIMIT 1",
                            patientId, job.getAr() ? "ar" : "noar", exportQuery.getPeriod()));
                    
                    if (testOffset.length != 1) {
                        outputWriter.write(String.format("  PID <%s> don't have enough data to export.%n", patientId));
                        finishedPatientCounter.getAndIncrement();
                        exportDao.updatePatientFinishedNum(job.getId(),finishedPatientCounter.get());
                        logger.info(String.format("write txt:  PID <%s> don't have enough data to export.%n", patientId));
                        continue;
                    }
                    // Then fetch meta data regrading file segments and build the query string
                    List<DataTimeSpanBean> dtsb = AnalysisUtil.getPatientAllDataSpan(influxDB, logger, patientId,versionCondition);
                    // get the startTime eliminate first 30 rows
                    String startTime = AnalysisUtil.getPatientStartTime(jobFromdb, influxDB,logger,patientId,job.getAr(),versionCondition);
                    ExportQueryBuilder eq = new ExportQueryBuilder(jobFromdb, Instant.parse(startTime),
                            Instant.parse((String) testOffset[0].getDataByColAndRow(0, 0)), dtsb, groups, columns, exportQuery,
                            job.getAr(),versionCondition);
                    String finalQueryString = eq.getQueryString();
                    //System.out.println("finalQueryString for " + patientId + ": " + finalQueryString);
                    logger.info(finalQueryString);
                    if (finalQueryString.isEmpty()) {
                        outputWriter.write(String.format("  PID <%s> no available data.%n", patientId));
                        finishedPatientCounter.getAndIncrement();
                        exportDao.updatePatientFinishedNum(job.getId(),finishedPatientCounter.get());
                        continue;
                    }
                    logger.debug("Query for <{}>: {}", patientId, finalQueryString);
                    // Execute the query
                    ResultTable[] res = InfluxUtil.justQueryData(jobFromdb, influxDB, true, finalQueryString);

                    if (res.length != 1) {
                        outputWriter.write(String.format("  PID <%s> incorrect result from database.%n", patientId));
                        finishedPatientCounter.getAndIncrement();
                        exportDao.updatePatientFinishedNum(job.getId(),finishedPatientCounter.get());
                        continue;
                    }
                    System.out.println(res.toString());

//                    outputWriter.writeForOnePatient(patientId, res[0], eq, dtsb);
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
                        try {
							outputWriter.write(String.format("  PID <%s> failed multiple times, possible program error.%n", patientId));
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                        idQueue.remove(patientId);
                        finishedPatientCounter.getAndIncrement();
                        exportDao.updatePatientFinishedNum(job.getId(),finishedPatientCounter.get());
                    }
                }

            }
            influxDB.close();
        };
		return null;

//        for (int i = 0; i < paraCount; ++i) {
//            scheduler.submit(queryTask);
//        }
//        scheduler.shutdown();
//        try {
//            // TODO: PSC 2 day limit!
//            scheduler.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
//        } catch (InterruptedException e) {
//            logger.error(Util.stackTraceErrorToString(e));
//        } finally {
//            if (this.jobStopIndicator.getOrDefault(jobId, false).equals(true)) {
//                // Job removed special procedure
//                this.jobStopIndicator.remove(jobId);
//                outputWriter.write(String.format("%nJob cancelled by user during execution.%n"));
//                logger.warn("Job <{}> cancelled by user during execution.", jobId);
//            }
//            // Normal exit procedure
//            jobClosingHandler(false, isPscRequired, job, outputDir, outputWriter, validPatientCounter.get());
//        }
		
	}
	
	public String predictWithExportFileViaPython(ExportWithBLOBs job) throws InterruptedException {
		String pList = job.getPatientList();
		List<String> patientIDs;
		if (pList == null || pList.isEmpty()) {
            // Override UUID setting to match PSC database if necessary
            patientIDs = importedFileDao.selectAllImportedPidOnMachine("realpsc");
        } else {
            patientIDs = Arrays.stream(pList.split(",")).map(String::toUpperCase).collect(Collectors.toList());
        }
		Integer jobId = job.getId();
		String path = InfluxappConfig.OUTPUT_DIRECTORY;
		File fileFolder = new File(path);
		FilenameFilter fileSuffixFilter = new FilenameFilter() {
			public boolean accept(File f, String name) 
            { 
                return name.endsWith("_" + (jobId) + "_split"); 
            } 
		};
		String selectedPath;
		File theFileFolder;
		while (true) {
			// wait until the folder is created.
			if (fileFolder.list(fileSuffixFilter).length == 0) {
				Thread.sleep(1000);
			}
			else {
				selectedPath = path + fileFolder.list(fileSuffixFilter)[0] + "/long/";
				theFileFolder = new File(selectedPath);
				break;
			}
		}
		
		String[] patientFiles;
		System.out.println("JobId: " + jobId);
		System.out.println("Folder: " + path + fileFolder.list(fileSuffixFilter)[0] + "/long/");
		while (true) {
			// wait until the patient.txt is created.
//			System.out.println(theFileFolder == null);
//			System.out.println(theFileFolder.list() == null);
//			System.out.println(job.getFinishedPatient() == null);
//			System.out.println(theFileFolder.list().length == 0);
//			System.out.println();
//			System.out.println();
//			exportDao.selectByPrimaryKey(jobId).getFinishedPatient() != theFileFolder.list().length
			if(exportDao.selectByPrimaryKey(jobId).getFinishedPatient() > 0) {
				Thread.sleep(3000);
			}
			else {
				patientFiles = theFileFolder.list();
				break;
			}
		}
		
		for (String patient_txt : patientFiles) {
			System.out.print(patient_txt + ", ");
		}
		System.out.println("End print.");
		
		return callPython(theFileFolder);
	}
	
	private String callPython(File path) throws InterruptedException {
      
      System.out.println("Path: " + path.getAbsolutePath());
      String[] files;
      while(path.list().length == 0) {
    	  Thread.sleep(1000);
      }
      files = path.list();
      String results = "";
      for (String file : files) {
    	  System.out.println(file);
    	  String cmd = String.format("python -W ignore D:\\DB\\brain_flux\\brainflux.py %s", path.getAbsolutePath() + "\\" + file);
    	  System.out.println("Executing python script file now:\n" + cmd);
    	  Process pcs;
    	  String result = null;
		try {
			System.out.println("enter try...");
			pcs = Runtime.getRuntime().exec(cmd);
			pcs.waitFor();
          
          BufferedInputStream in = new BufferedInputStream(pcs.getInputStream());

          BufferedReader br = new BufferedReader(new InputStreamReader(in));

          String lineStr = null;
          while ((lineStr = br.readLine()) != null) {
              result = lineStr;
              System.out.println(result);
          }
          // 关闭输入流
          br.close();
          in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("catch{ }");
			e.printStackTrace();
		}
    	results = results.concat(result + '\n'); 
      }
      System.out.println("End calling Python.");
      return results;
	}
	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		    
//		ExportWithBLOBs job = new ExportWithBLOBs();
//		job.setId(999);
//		job.setQueryId(1);
//		job.setQueryJson("{\"downsample\":{\"id\":1,\"alias\":\"test\",\"period\":900,\"duration\":3600,\"origin\":0,\"minBin\":0,\"minBinRow\":0,\"downsampleFirst\":false,\"createTime\":{\"dayOfMonth\":10,\"dayOfWeek\":\"TUESDAY\",\"dayOfYear\":70,\"month\":\"MARCH\",\"year\":2020,\"monthValue\":3,\"hour\":19,\"minute\":36,\"nano\":0,\"second\":17,\"chronology\":{\"id\":\"ISO\",\"calendarType\":\"iso8601\"}},\"updateTime\":{\"dayOfMonth\":4,\"dayOfWeek\":\"SATURDAY\",\"dayOfYear\":95,\"month\":\"APRIL\",\"year\":2020,\"monthValue\":4,\"hour\":14,\"minute\":42,\"nano\":0,\"second\":14,\"chronology\":{\"id\":\"ISO\",\"calendarType\":\"iso8601\"}},\"deleted\":false},\"groups\":[]}");
//		job.setPatientList("PUH-2018-300");
//		
//		NavigatorService navigatorService = new NavigatorService(new DownsampleDao(), 
//				new DownsampleGroupDao(), new ExportDao(), new ColumnService(),
//				new InfluxSwitcherService(), new ImportedFileDao(), new VersionDao());
//		navigatorService.generateTestMatrix(job);
		
		File f = new File("/tsdb/output/ML_test_10features_(2020-04-05T17.42.03.032)_25_split/long/");
		String[] txts = f.list();
		for (String patient_txt : txts) {
			System.out.print(patient_txt + ", ");
		}
		System.out.println("End print.");
		
		
		
		
		
//        Scanner input = new Scanner(System.in);
//        
//        // 在同一行输入两个数字，用空格分开，作为传入Python代码的命令行参数
//        System.out.println("Enter two integers(e.g. 12 34): ");
//        String integers = input.nextLine();
//        String[] numbers = integers.split(" ");
//        
//        // 定义传入Python脚本的命令行参数，将参数放入字符串数组里
//        String cmds = String.format("python D://test_argv.py %s %s", 
//                                    numbers[0], numbers[1]);
//
//        // 执行CMD命令
//        System.out.println("\nExecuting python script file now.");
//        Process pcs = Runtime.getRuntime().exec(cmds);
//        pcs.waitFor();
//
//        // 定义Python脚本的返回值
//        String result = null;
//        // 获取CMD的返回流
//        BufferedInputStream in = new BufferedInputStream(pcs.getInputStream());
//        // 字符流转换字节流
//        BufferedReader br = new BufferedReader(new InputStreamReader(in));
//        // 这里也可以输出文本日志
//
//        String lineStr = null;
//        while ((lineStr = br.readLine()) != null) {
//            result = lineStr;
//        }
//        // 关闭输入流
//        br.close();
//        in.close();
//
//        System.out.println(result);

    }
	

}
