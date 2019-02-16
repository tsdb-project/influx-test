package edu.pitt.medschool.service;

import edu.pitt.medschool.framework.util.TimeUtil;
import edu.pitt.medschool.model.PatientTimeLine;
import edu.pitt.medschool.model.dao.CsvFileDao;
import edu.pitt.medschool.model.dto.CsvFile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ValidateCsvService {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Value("${machine}")
    private String machineId;
    private final CsvFileDao csvFileDao;
    
    public ValidateCsvService(CsvFileDao csvFileDao) {
    	this.csvFileDao = csvFileDao;
    }

    private Date strToDate(String str){
        SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        Date date = null;
        try{
            date = format.parse(str);
        }catch (ParseException pe){
            pe.printStackTrace();
        }
        return date;
    }
    private String processFirstLineInCSV(String fLine, String pid) {
        if (!fLine.toUpperCase().contains(pid))
            throw new RuntimeException("Wrong PID in filename!");
        if (fLine.length() < 50)
            throw new RuntimeException("File UUID misformat!");
        if (!fLine.substring(fLine.length() - 40, fLine.length() - 4)
                .matches("([\\w\\d]){8}-([\\w\\d]){4}-([\\w\\d]){4}-([\\w\\d]){4}-([\\w\\d]){12}"))
            throw new RuntimeException("File does not have a valid UUID!");
        return fLine.substring(fLine.length() - 40, fLine.length() - 4);
    }
    public CsvFile analyzeCsv(String dir){
        CsvFile validateBean = new CsvFile();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(dir));
            String line;
            String firstline = reader.readLine();
            for(int i=0;i<3;i++){
                reader.readLine();
            }
            String header_time = reader.readLine().split(",")[1];
            header_time= header_time+" "+ reader.readLine().split(",")[1];
            String pid = firstline.split("\\\\")[2];
            File file = new File(dir);
            int count = 1;
            for(int j=0;j<2;j++){
                reader.readLine();
            }
            Double start_time = Double.valueOf(reader.readLine().split(",")[0]);
            String end_time=null;
            while((line=reader.readLine())!=null){
                end_time = line;
                count+=1;
            }
            validateBean.setSize((int) file.length());
            validateBean.setPath(dir);
            validateBean.setFilename(file.getName());
            validateBean.setLength(count);
            TimeUtil timeUtil = new TimeUtil();
            validateBean.setStartTime(timeUtil.serialTimeToDate(start_time,timeUtil.nycTimeZone));
            validateBean.setEndTime(timeUtil.serialTimeToDate(Double.valueOf(end_time.split(",")[0]),timeUtil.nycTimeZone));
            validateBean.setPid(pid);
            validateBean.setUuid(processFirstLineInCSV(firstline, validateBean.getPid()));
            validateBean.setHeaderTime(strToDate(header_time));
            validateBean.setMachine(machineId);
        }catch (Exception e){
            e.printStackTrace();
        }
        return validateBean;
    }
//    public Boolean addValidateResult(ValidateBean validateBean){
//        System.out.println("add validate result...");
//        String sql = "insert into csv_file (pid, filename, path, size, uuid, header_time, start_time, end_time, length)"+"values(?,?,?,?,?,?,?,?,?)";
//        try {
//            KeyHolder keyHolder = new GeneratedKeyHolder();
//            jdbcTemplate.update(connection -> {
//                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
//                ps.setString(1,validateBean.getPid());
//                ps.setString(2,validateBean.getFilename());
//                ps.setString(3,validateBean.getPath());
//                ps.setInt(4,validateBean.getSize());
//                ps.setString(5,validateBean.getUuid());
//                ps.setTimestamp(6, new java.sql.Timestamp(validateBean.getHeader_time().getTime()));
//                ps.setTimestamp(7, new java.sql.Timestamp(validateBean.getStart_time().getTime()));
//                ps.setTimestamp(8, new java.sql.Timestamp(validateBean.getEnd_time().getTime()));
//                ps.setInt(9,validateBean.getLines());
//                return ps;
//            },keyHolder);
//            System.out.println("Success");
//            return true;
//        }catch (Exception e){
//            e.printStackTrace();
//            return false;
//        }
//    }
    
    public int insertCsvFile(CsvFile csvFile) throws Exception{
    	return csvFileDao.insert(csvFile);
    }

    //get all patients Timelines
//    public List<PatientTimeLine> getPatientTimelines(){
//        String sql = "select c.filename as filename,c.start_time as start_time,c.end_time as end_time,p.arrestdate as arrestdate,c.length as len, p.arresttime as arresttime from csv_file c , patient p where c.pid = p.id and c.machine='shl174'";
//        return this.jdbcTemplate.query(sql,new PatientRowMapper());
//    }
    
    public ArrayList<PatientTimeLine> getPatientTimeLines(String machine){
    	return csvFileDao.getPatientTimeLines(machine);
    }
 
}
