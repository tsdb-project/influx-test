package edu.pitt.medschool.service;

import edu.pitt.medschool.config.InfluxappConfig;
import edu.pitt.medschool.framework.influxdb.InfluxUtil;
import edu.pitt.medschool.framework.influxdb.ResultTable;
import edu.pitt.medschool.framework.util.Util;
import edu.pitt.medschool.model.dao.ExportDao;
import edu.pitt.medschool.model.dao.ImportedFileDao;
import edu.pitt.medschool.model.dao.VersionDao;
import edu.pitt.medschool.model.dto.ExportWithBLOBs;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static edu.pitt.medschool.framework.influxdb.InfluxUtil.generateIdbClient;

@Service
public class AggregationService {
    @Value("${load}")
    private double loadFactor;

    private BufferedWriter bufferedWriter;
    private String dir;
    @Autowired
    ExportDao exportDao;
    @Autowired
    ImportedFileDao importedFileDao;
    @Autowired
    ValidateCsvService validateCsvService;
    @Autowired
    VersionDao versionDao;

    private Logger logger = LoggerFactory.getLogger(this.getClass());


    public void startAgg(String time){
        System.out.println("start aggregation");
        this.dir = time+"_new_agg";

        // add this job into the export table
        ExportWithBLOBs export = new ExportWithBLOBs();
        export.setAr(true);
        List<String> patientIDs;
        patientIDs = importedFileDao.selectAllImportedPidOnMachine("realpsc");
        List<String> patients = new ArrayList<>();

        //get finished pids
        String pathname = "/tsdb/output/"+this.dir+"/"+this.dir+".txt";
        File filename = new File(pathname);
        if(filename.exists()){
            try{
                InputStreamReader reader = new InputStreamReader(
                        new FileInputStream(filename));
                BufferedReader br = new BufferedReader(reader);
                HashSet<String> finishedPid = new HashSet<>();
                String line = br.readLine();
                while (line != null) {
                    String[] record = line.split(":");
                    if(record[0].equals("Success")){
                        finishedPid.add(record[1].trim());
                    }
                    line = br.readLine();
                }
                HashSet<String> allPid = new HashSet<>(patientIDs);
                allPid.removeAll(finishedPid);
                patients = new ArrayList<>(allPid);
                System.out.println(finishedPid.size());
                System.out.println(allPid.size());
            }catch (IOException e){
                e.printStackTrace();
            }
        }else{
            patients = patientIDs;
        }
        export.setAllPatient(patients.size());
        export.setMachine("realpsc");
        export.setQueryId(83);
        exportDao.insertExportJob(export);
        int jobid = export.getId();

        // count the finished number
        AtomicInteger finishedPatientCounter = new AtomicInteger(0);
        BlockingQueue<String> idQueue = new LinkedBlockingQueue<>(patients);

        // get all 6037 columns
        List<String> columns = getColumns();

        // get selection condition from 6037 columns, now each file is splited into 9 parts
        List<String> selection = getSelection(columns);
        int paraCount = determineParaNumber();
        ExecutorService scheduler = generateNewThreadPool(paraCount);
        try{
            FileUtils.forceMkdir(new File("/tsdb/output/"+this.dir+"/"));
            this.bufferedWriter = new BufferedWriter(new FileWriter("/tsdb/output/"+this.dir+"/"+this.dir+".txt",true));
            this.bufferedWriter.write("Cores: "+paraCount);
            this.bufferedWriter.newLine();
            this.bufferedWriter.flush();
            InfluxDB influxDB = generateIdbClient(false);
            influxDB.query(new Query("create database aggdata"));
            influxDB.close();
        }catch (IOException e){
            e.printStackTrace();
            return;
        }

        LocalDateTime start_Time = LocalDateTime.now();
        Runnable queryTask = () -> {
            String pid;
            InfluxDB influxDB = generateIdbClient(false);

            while ((pid=idQueue.poll())!=null){
                // generate query
                QueryResult res1 = influxDB.query(new Query(String.format("select first(\"I1_1\") from \"%s\" where arType='ar'", pid),"data"));
                QueryResult res2 = influxDB.query(new Query(String.format("select last(\"I1_1\") from \"%s\" where arType='ar'", pid),"data"));
                String startTime = res1.getResults().get(0).getSeries().get(0).getValues().get(0).get(0).toString();
                String endTime = res2.getResults().get(0).getSeries().get(0).getValues().get(0).get(0).toString();
                List<String> queries = new ArrayList<>();
                for(int count=0;count<selection.size();count++){
                    queries.add(String.format("select %s into \"%s\".\"autogen\".\"%s\" from \"%s\" where arType='ar' AND time<='%s' AND time>='%s' group by time(%s), arType", selection.get(count), "aggdata",pid, pid,endTime,startTime,time));
                }
                
                //System.out.println(query);
                // run query
                try{
                    for(int count=0;count<selection.size();count++){
                        QueryResult rs = influxDB.query(new Query(queries.get(count),"data"));
                    }
                    this.bufferedWriter.write("Success: "+pid);
                    this.bufferedWriter.newLine();
                    this.bufferedWriter.flush();
                    finishedPatientCounter.getAndIncrement();
                    exportDao.updatePatientFinishedNum(jobid,finishedPatientCounter.get());

                }catch (Exception e){
                    logger.info(pid);
                    recordError(pid);
                    e.printStackTrace();
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
        finally {
            try{
                LocalDateTime end_Time = LocalDateTime.now();
                this.bufferedWriter.write(String.valueOf(Duration.between(start_Time,end_Time)).replace("PT","Run Time: "));
                this.bufferedWriter.newLine();
                this.bufferedWriter.flush();
                this.bufferedWriter.close();
                System.out.println("Job finished");
            }catch (IOException e){
                e.printStackTrace();
            }
        }


    }

    private List<String> getColumns(){
        List<String> columns = new ArrayList<>();
        int[][] x = {{1,1},{2,2},{3,3},{4,42},{43,81},{82,120},{121,159},{160,171},{172,176},{177,191},{192,230},{231,269},{270,270}};
        int[][] y = {{1,3},{1,24},{1,1},{1,40},{1,5},{1,1},{1,97},{1,1},{1,34},{1,1},{1,1},{1,5},{1,1}};
        for(int k=0;k<x.length;k++){
            for(int i=x[k][0];i<=x[k][1];i++){
                for(int j=y[k][0];j<=y[k][1];j++){
                    columns.add("I"+i+"_"+j);
                }
            }
        }
        return columns;
    }

    private List<String> getSelection(List<String> columns){
        List<String> res= new ArrayList<>();
        StringBuilder onepart = new StringBuilder();
        for(int count=0;count<15;count++){
            for(int j=count*380;j<(count+1)*380;j++){
                onepart.append(String.format("mean(\"%s\") as mean_%s , max(\"%s\") as max_%s , min(\"%s\") as min_%s,", columns.get(j), columns.get(j), columns.get(j),columns.get(j),columns.get(j),columns.get(j)));
            }
            res.add(onepart.substring(0,onepart.length()-1));
            onepart = new StringBuilder();
        }
        for(int j=15*380;j<columns.size();j++){
            onepart.append(String.format("mean(\"%s\") as mean_%s , max(\"%s\") as max_%s , min(\"%s\") as min_%s,", columns.get(j), columns.get(j), columns.get(j),columns.get(j),columns.get(j),columns.get(j)));
        }
        res.add(onepart.substring(0,onepart.length()-1));
        return res;
    }

    private int determineParaNumber() {
        int paraCount = (int) Math.round(loadFactor* InfluxappConfig.AvailableCores);
        return paraCount > 0 ? paraCount : 1;
    }

    private void recordError(String pid){
        try{
            this.bufferedWriter.write("Failed PID: "+pid);
            this.bufferedWriter.newLine();
            this.bufferedWriter.flush();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private ExecutorService generateNewThreadPool(int i) {
        return Executors.newFixedThreadPool(i);
    }

    public String seletDatabase(String query){
        String dbname="";

        return dbname;
    }
}
