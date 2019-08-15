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
import org.apache.poi.ss.formula.functions.Now;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
        ExportWithBLOBs export = new ExportWithBLOBs();
        export.setAr(true);
        List<String> patientIDs;
        patientIDs = importedFileDao.selectAllImportedPidOnMachine("realpsc");
        export.setAllPatient(patientIDs.size());
        export.setMachine("realpsc");
        export.setQueryId(90);
        exportDao.insertExportJob(export);
        int jobid = export.getId();
        AtomicInteger finishedPatientCounter = new AtomicInteger(0);
        BlockingQueue<String> idQueue = new LinkedBlockingQueue<>();
        idQueue.add("PUH-2015-015");
        List<String> columns = getColumns();
        String selection = getSelection(columns);
        int paraCount = determineParaNumber();
        ExecutorService scheduler = generateNewThreadPool(paraCount);
        try{
            FileUtils.forceMkdir(new File("d:/eegdata/"+this.dir+"/"));
            this.bufferedWriter = new BufferedWriter(new FileWriter("d:/eegdata/"+this.dir+"/"+this.dir+".txt"));
        }catch (IOException e){
            e.printStackTrace();
            return;
        }
        String[] head = new String[18113];
        head[0] = "pid";
        head[1] = "time";
        for(int i=0;i<6037;i++){
            head[i*3+2] = "mean("+columns.get(i)+")";
            head[i*3+3] = "max("+columns.get(i)+")";
            head[i*3+4] = "min("+columns.get(i)+")";
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

                String query = String.format("select %s from \"%s\" where arType='ar' AND time<='%s' AND time>='%s' group by time(%s)", selection, pid,endTime,startTime,time);
                //System.out.println(query);
                // run query
                try{
                    ResultTable[] res = InfluxUtil.justQueryData(influxDB, true, query);
                    // write result into csv
                    CSVWriter writer = new CSVWriter(new BufferedWriter(new FileWriter("d:/eegdata/"+this.dir+"/"+pid+".csv")));
                    writer.writeNext(head);
                    writeOnePatinet(res[0],pid,writer);
                    writer.close();
                }catch (Exception e){
                    e.printStackTrace();
                }

                finishedPatientCounter.getAndIncrement();
                exportDao.updatePatientFinishedNum(jobid,finishedPatientCounter.get());

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
                this.bufferedWriter.close();
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
    private String getSelection(List<String> columns){
        String res="";
        for(String col : columns){
            String onepart = String.format("mean(\"%s\"),max(\"%s\"),min(\"%s\"),", col,col,col);
            res +=onepart;
        }
        res = res.substring(0,res.length()-1);
        return res;
    }

    private void writeOnePatinet(ResultTable res, String pid,CSVWriter writer){
        int len = res.getRowCount();
        String[] oneRow = new String[18113];
        for(int i=0;i<len;i++){
            oneRow[0] = pid;
            List<Object> row = res.getDatalistByRow(i);
            for(int j=0;j<18112;j++){
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
        int paraCount = (int) Math.round(0.4*InfluxappConfig.AvailableCores);
        return paraCount > 0 ? paraCount : 1;
    }
}
