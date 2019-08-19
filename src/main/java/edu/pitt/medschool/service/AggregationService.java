package edu.pitt.medschool.service;

import com.opencsv.CSVWriter;
import edu.pitt.medschool.config.InfluxappConfig;
import edu.pitt.medschool.framework.influxdb.InfluxUtil;
import edu.pitt.medschool.framework.influxdb.ResultTable;
import edu.pitt.medschool.framework.util.TimeUtil;
import edu.pitt.medschool.framework.util.Util;
import edu.pitt.medschool.model.dao.*;
import edu.pitt.medschool.model.dto.ExportWithBLOBs;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.poi.ss.formula.functions.Now;
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

    public void aggregate(String time) {
        System.out.println("start aggregation");
        this.dir = time+"_aggregation";
        // add this job into the export table
        ExportWithBLOBs export = new ExportWithBLOBs();
        export.setAr(true);
        List<String> patientIDs;
        patientIDs = importedFileDao.selectAllImportedPidOnMachine("realpsc");
        export.setAllPatient(patientIDs.size());
        export.setMachine("realpsc");
        export.setQueryId(83);
        exportDao.insertExportJob(export);
        int jobid = export.getId();
        // count the finished number
        AtomicInteger finishedPatientCounter = new AtomicInteger(0);
        BlockingQueue<String> idQueue = new LinkedBlockingQueue<>(patientIDs);

        // get all 6037 columns
        List<String> columns = getColumns();

        // get selection condition from 6037 columns, now each file is splited into 9 parts
        List<String> selection = getSelection(columns);
        int paraCount = determineParaNumber();
        ExecutorService scheduler = generateNewThreadPool(paraCount);
        try{
            FileUtils.forceMkdir(new File("/tsdb/output/"+this.dir+"/"));
            this.bufferedWriter = new BufferedWriter(new FileWriter("/tsdb/output/"+this.dir+"/"+this.dir+".txt"));
            this.bufferedWriter.write("Cores: "+paraCount);
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
                    queries.add(String.format("select %s from \"%s\" where arType='ar' AND time<='%s' AND time>='%s' group by time(%s)", selection.get(count), pid,endTime,startTime,time));
                }
                //System.out.println(query);
                // run query
                try{
                    FileUtils.forceMkdir(new File("/tsdb/output/"+this.dir+"/"+pid+"/"));
                    for(int count=0;count<selection.size();count++){
                        ResultTable[] res = InfluxUtil.justQueryData(influxDB, true, queries.get(count));
                        // write result into csv
                        CSVWriter writer = new CSVWriter(new BufferedWriter(new FileWriter("/tsdb/output/"+this.dir+"/"+pid+"/"+pid+"_"+ (count + 1) +".csv")));
                        String[] head = selection.get(count).split(",");
                        String[] info = {"pid","time"};
                        writer.writeNext(ArrayUtils.addAll(info,head));
                        writeOnePart(res[0],pid,writer);
                        writer.flush();
                        writer.close();

                    }
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
                this.bufferedWriter.write(String.valueOf(Duration.between(start_Time,end_Time)));
                this.bufferedWriter.flush();
                this.bufferedWriter.close();
                System.out.println("Job finished");
            }catch (IOException e){
                e.printStackTrace();
            }
        }

    }

    private ExecutorService generateNewThreadPool(int i) {
        return Executors.newFixedThreadPool(i);
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
        String onepart = "";
        for(int count=0;count<8;count++){
            for(int j=count*671;j<(count+1)*671;j++){
                onepart+=String.format("mean(\"%s\"),max(\"%s\"),min(\"%s\"),", columns.get(j),columns.get(j),columns.get(j));
            }
            res.add(onepart.substring(0,onepart.length()-1));
            onepart="";
        }
        for(int j=8*671;j<columns.size();j++){
            onepart+= String.format("mean(\"%s\"),max(\"%s\"),min(\"%s\"),", columns.get(j),columns.get(j),columns.get(j));
        }
        res.add(onepart.substring(0,onepart.length()-1));
        return res;
    }


    private void writeOnePart(ResultTable res, String pid,CSVWriter writer){
        int len = res.getRowCount();
        int col = res.getColCount();
        String[] oneRow = new String[col+1];
        for(int i=0;i<len;i++){
            oneRow[0] = pid;
            List<Object> row = res.getDatalistByRow(i);
            for(int j=0;j<col;j++){
                if(row.get(j)==null){
                    oneRow[j+1]="NA";
                }else {
                    oneRow[j+1] = String.valueOf(row.get(j));
                }
            }

            //System.out.println(oneRow[0]+oneRow[1]+oneRow[2]+oneRow[3]);
            synchronized (this){
                writer.writeNext(oneRow);
            }
        }
    }

    private int determineParaNumber() {
        int paraCount = (int) Math.round(loadFactor*InfluxappConfig.AvailableCores);
        return paraCount > 0 ? paraCount : 1;
    }

    private void recordError(String pid){
        try{
            this.bufferedWriter.write("Failed PID: "+pid);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
