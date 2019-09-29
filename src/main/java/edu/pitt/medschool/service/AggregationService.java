package edu.pitt.medschool.service;

import edu.pitt.medschool.config.InfluxappConfig;
import edu.pitt.medschool.framework.util.Util;
import edu.pitt.medschool.model.dao.AggregationDao;
import edu.pitt.medschool.model.dao.ExportDao;
import edu.pitt.medschool.model.dao.ImportedFileDao;
import edu.pitt.medschool.model.dao.VersionDao;
import edu.pitt.medschool.model.dto.AggregationDatabase;
import edu.pitt.medschool.model.dto.AggregationDatabaseWithBLOBs;
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
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
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
    @Autowired
    AggregationDao aggregationDao;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private final LinkedBlockingQueue<AggregationDatabaseWithBLOBs> jobQueue = new LinkedBlockingQueue<>();
    private final ScheduledFuture<?> jobCheckerThread;

    @Autowired
    public AggregationService() {
        this.jobCheckerThread = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            Thread.currentThread().setName("JobCheckerThread");
            AggregationDatabaseWithBLOBs target = null;
            while ((target = this.jobQueue.poll()) != null) {
                logger.info("Start to process job #<{}>", target.getId());
                startAgg(target);
                logger.info("Finished one job #<{}>", target.getId());
            }
        }, 10, 20, TimeUnit.SECONDS);
    }


    // todo add version condition into aggregation query
    private void startAgg(AggregationDatabaseWithBLOBs job){
        System.out.println("start aggregation");
        String time = getAggTime(job.getAggregateTime());
        final String DIR = "aggregationDBLog";


        List<String> patientIDs;
        String plist = job.getPidList();
        
        if(plist == null || plist.isEmpty()){
            //todo new way to get all pids from csv_file table
            patientIDs = importedFileDao.selectAllImportedPidOnMachine("realpsc");
        }else {
            patientIDs = Arrays.asList(job.getPidList().split(","));
        }

        // todo update total
        List<String> patients = new ArrayList<>();

        // recover job after break down
        //get finished pids
        String pathname = "/tsdb/output/"+DIR+"/"+job.getDbName()+".txt";
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
//                System.out.println(finishedPid.size());
//                System.out.println(allPid.size());
            }catch (IOException e){
                e.printStackTrace();
            }
        }else{
            patients = patientIDs;
        }

        // update the total number of patients of this job
        aggregationDao.updateTotalnumber(job.getId(),patients.size());
        
        // count the finished number
        AtomicInteger finishedPatientCounter = new AtomicInteger(0);
        BlockingQueue<String> idQueue = new LinkedBlockingQueue<>(patients);

        // get all 6037 columns
        List<String> columns = getColumns();

        // get selection condition from 6037 columns, now each file is splited into 9 parts
        List<String> selection = getSelection(columns,job);
        int paraCount = determineParaNumber();
        ExecutorService scheduler = generateNewThreadPool(paraCount);
        try{
            this.bufferedWriter = new BufferedWriter(new FileWriter(pathname,true));
            this.bufferedWriter.write("Cores: "+paraCount);
            this.bufferedWriter.newLine();
            this.bufferedWriter.flush();
//            InfluxDB influxDB = generateIdbClient(false);
//            String command = "create database " + job.getDbName().replace(" ","_")+"_V"+job.getVersion();
//            influxDB.query(new Query(command));
//            influxDB.close();
        }catch (IOException e){
            e.printStackTrace();
            return;
        }

        LocalDateTime start_Time = LocalDateTime.now();
        Runnable queryTask = () -> {
            String pid;
            InfluxDB influxDB = generateIdbClient(false);
            while ((pid=idQueue.poll())!=null){
                try{
                    // generate query
//                    QueryResult res1 = influxDB.query(new Query(String.format("select first(\"max_I1_1\") from \"%s\" where arType='ar'", pid),"aggdata"));
//                    QueryResult res2 = influxDB.query(new Query(String.format("select last(\"max_I1_1\") from \"%s\" where arType='ar'", pid),"aggdata"));
                    String i11 = job.getFromDb().equals("data")?"I1_1":"max_I1_1";
                    QueryResult res1 = influxDB.query(new Query(String.format("select first(\"%s\") from \"%s\" where arType='ar'",i11, pid),job.getFromDb()));
                    //QueryResult res2 = influxDB.query(new Query(String.format("select last(\"I1_1\") from \"%s\" where arType='ar'", pid),"data"));
                    String startTime = res1.getResults().get(0).getSeries().get(0).getValues().get(0).get(0).toString();
                    // only do 72 hours
                    DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
                    String endTime = LocalDateTime.parse(startTime,df).plusDays(3).toString()+"Z";
                    List<String> queries = new ArrayList<>();
                    for(int count=0;count<selection.size();count++){
                        //queries.add(String.format("select %s into \"%s\".\"autogen\".\"%s\" from \"%s\" where arType='ar' AND time<='%s' AND time>='%s' group by time(%s), arType", selection.get(count), job.getDbName().replace(" ","_")+"_V"+job.getVersion(),pid, pid,endTime,startTime,time));
//                        queries.add(String.format("select %s into \"%s\".\"autogen\".\"%s\" from \"%s\" where arType='ar' AND time<='%s' AND time>='%s' group by time(%s), arType", selection.get(count), "aggdata",pid, pid,endTime,startTime,time));
                        queries.add(String.format("select %s into \"%s\".\"autogen\".\"%s\" from \"%s\" where arType='ar' AND time<='%s' AND time>='%s' group by time(%s), arType", selection.get(count), job.getDbName(),pid, pid,endTime,startTime,time));
                    }

                    // run query
                    for(int count=0;count<selection.size();count++){
                        //QueryResult rs = influxDB.query(new Query(queries.get(count),"aggdata"));
                        QueryResult rs = influxDB.query(new Query(queries.get(count),job.getFromDb()));
                    }
                    this.bufferedWriter.write("Success: "+pid);
                    this.bufferedWriter.newLine();
                    this.bufferedWriter.flush();
                    finishedPatientCounter.getAndIncrement();
                    aggregationDao.updatePatientFinishedNum(job.getId(),finishedPatientCounter.get());

                }catch (Exception e){
                    logger.info(pid);
                    recordError(pid,e);
                    finishedPatientCounter.getAndIncrement();
                    aggregationDao.updatePatientFinishedNum(job.getId(),finishedPatientCounter.get());
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
                aggregationDao.updateStatus(job.getId(),"Success");
            }catch (IOException e){
                e.printStackTrace();
            }
        }


    }


    private String getAggTime(Integer aggregateTime) {
        int h = 3600;
        int m = 60;
        if(aggregateTime%h==0){
            return aggregateTime/h+"h";
        }
        if(aggregateTime%m==0){
            return aggregateTime/m+"m";
        }
        return aggregateTime+"s";
    }

    public List<String> getColumns(){
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

    private List<String> getSelection(List<String> columns,AggregationDatabaseWithBLOBs job){
        List<String> res= new ArrayList<>();
        StringBuilder onepart = new StringBuilder();
        for(int count=0;count<41;count++){
            for(int j=count*144;j<(count+1)*144;j++){
                //onepart.append(String.format("max(\"%s\") as max_%s , min(\"%s\") as min_%s,", "max_"+columns.get(j), columns.get(j), "min_"+columns.get(j),columns.get(j)));
                onepart.append(getAggregations(job,columns.get(j)));
            }
            res.add(onepart.substring(0,onepart.length()-2));
            onepart = new StringBuilder();
        }
        for(int j=41*144;j<columns.size();j++){
            //onepart.append(String.format("max(\"%s\") as max_%s , min(\"%s\") as min_%s,", "max_"+columns.get(j), columns.get(j), "min_"+columns.get(j),columns.get(j)));
            onepart.append(getAggregations(job,columns.get(j)));
        }
        res.add(onepart.substring(0,onepart.length()-2));
        return res;
    }

    private int determineParaNumber() {
        int paraCount = (int) Math.round(loadFactor* InfluxappConfig.AvailableCores);
        return paraCount > 0 ? paraCount : 1;
    }

    private void recordError(String pid,Exception ex){
        try{
            this.bufferedWriter.write("Failed PID: "+pid +ex.getMessage());
            this.bufferedWriter.newLine();
            this.bufferedWriter.flush();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private ExecutorService generateNewThreadPool(int i) {
        return Executors.newFixedThreadPool(i);
    }

    public String databaseNavigator(String query){
        String dbname="";

        return dbname;
    }

    public List<AggregationDatabase> selectAllAvailableDBs() {
        return aggregationDao.selectAllAvailableDBs();
    }

    public boolean completeJobAndInsert(AggregationDatabaseWithBLOBs database) {
        String dbname = database.getDbName()+"_V"+database.getVersion();
        List<AggregationDatabase> databaseList = aggregationDao.selectByname(dbname);
        if(!databaseList.isEmpty()){
            AggregationDatabaseWithBLOBs db = new AggregationDatabaseWithBLOBs();
            db.setId(databaseList.get(0).getId());
            db.setDbName(dbname);
            if(database.getMean()){
                db.setMean(true);
            }
            if(database.getSum()){
                db.setSum(true);
            }
            if(database.getMax()){
                db.setMax(true);
            }
            if(database.getMedian()){
                db.setMedian(true);
            }
            if(database.getQ1()){
                db.setQ1(true);
            }
            if(database.getQ3()){
                db.setQ3(true);
            }
            if(database.getSd()){
                db.setSd(true);
            }
            if(database.getMin()){
                db.setMin(true);
            }
            db.setFinished(0);
            db.setCreateTime(LocalDateTime.now());
            return aggregationDao.updateAggretaionMethods(db) !=0;
        }else {
            database.setVersion(versionDao.getLatestVersion());
            database.setStatus("processing");
            database.setDbName(database.getDbName()+"_V"+database.getVersion());
            database.setCreateTime(LocalDateTime.now(ZoneId.of("America/New_York")));
            return aggregationDao.setNewDB(database) != 0;
        }


    }

    public List<AggregationDatabase> selectAllOnGoing() {
        return aggregationDao.selectOngoing();
    }

    public boolean addOneAggregationJob(Integer jobId) {
        AggregationDatabaseWithBLOBs databaseWithBLOBs = aggregationDao.selectByPrimaryKey(jobId);
        try {
            System.out.println("successfully added to the job Queue!");
            return this.jobQueue.add(databaseWithBLOBs);
        } catch (Exception e) {
            logger.error("Add job failed.", e);
            return false;
        }
    }

    public void checkIntegrity(AggregationDatabaseWithBLOBs job){
        String path = "/tsdb/output/Integrity/"+job.getDbName()+".txt";
        try{
            BufferedWriter writer = new BufferedWriter(new FileWriter(path,true));
            List<String> pids = importedFileDao.selectAllImportedPidOnMachine("realpsc");
            InfluxDB influxDB = generateIdbClient(false);
            int count = 0;
            if(job.getMean()){
                count++;
            }
            if(job.getSum()){
                count++;
            }
            if(job.getMax()){
                count++;
            }
            if(job.getMedian()){
                count++;
            }
            if(job.getQ1()){
                count++;
            }
            if(job.getQ3()){
                count++;
            }
            if(job.getSd()){
                count++;
            }
            if(job.getMin()){
                count++;
            }
            for(int i=0;i<pids.size();i++){
                String query = String.format("show field keys from \"%s\"",pids.get(i));
                QueryResult rs = influxDB.query(new Query(query,"data"));
                QueryResult rs2 = influxDB.query(new Query(query,job.getDbName()));
                int keys = rs.getResults().get(0).getSeries().get(0).getValues().size();
                int keys2 = rs2.getResults().get(0).getSeries().get(0).getValues().size();
                if((keys-1)*count != keys2){
                    writer.write(pids.get(i)+":"+keys+","+keys2);
                    writer.newLine();
                    writer.flush();
                }
            }
            influxDB.close();
            writer.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public String getAggregations(AggregationDatabaseWithBLOBs job,String col){
        StringBuilder builder = new StringBuilder();
        if(job.getFromDb().equals("data")){
            if(job.getMax()){
                builder.append(String.format("max(\"%s\") as max_%s, ",col,col));
            }
            if(job.getMin()){
                builder.append(String.format("min(\"%s\") as min_%s, ",col,col));
            }
            if(job.getMedian()){
                builder.append(String.format("median(\"%s\") as median_%s, ",col,col));
            }
            if(job.getQ1()){
                builder.append(String.format("percentile(\"%s\",25) as p25_%s, ",col,col));
            }
            if(job.getQ3()){
                builder.append(String.format("percentile(\"%s\",75) as p75_%s, ",col,col));
            }
            if(job.getSd()){
                builder.append(String.format("stddev(\"%s\") as std_%s, ",col,col));
            }
            if(job.getSum()){
                builder.append(String.format("sum(\"%s\") as sum_%s, ",col,col));
            }
            if(job.getMean()){
                builder.append(String.format("mean(\"%s\") as mean_%s, ",col,col));
            }
        }else {
            if(job.getMax()){
                builder.append(String.format("max(\"%s\") as max_%s, ","max_"+col,col));
            }
            if(job.getMin()){
                builder.append(String.format("min(\"%s\") as min_%s, ","min_"+col,col));
            }
            if(job.getMedian()){
                builder.append(String.format("median(\"%s\") as median_%s, ","median_"+col,col));
            }
            if(job.getQ1()){
                builder.append(String.format("percentile(\"%s\",25) as p25_%s, ","p25_"+col,col));
            }
            if(job.getQ3()){
                builder.append(String.format("percentile(\"%s\",75) as p75_%s, ","p75_"+col,col));
            }
            if(job.getSd()){
                builder.append(String.format("stddev(\"%s\") as std_%s, ","mean_"+col,col));
            }
            if(job.getSum()){
                builder.append(String.format("sum(\"%s\") as sum_%s, ",col,col));
            }
            if(job.getMean()){
                builder.append(String.format("mean(\"%s\") as mean_%s, ",col,col));
            }
        }
        String finalq = builder.toString();
        return finalq;
    }
}
