package edu.pitt.medschool.service;

import edu.pitt.medschool.framework.util.TimeUtil;
import edu.pitt.medschool.model.ValidateBean;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Map;

@Service
public class ValidateCsvService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

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
    public ValidateBean analyzeCsv(String dir){
        ValidateBean validateBean = new ValidateBean();
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
            validateBean.setLines(count);
            TimeUtil timeUtil = new TimeUtil();
            validateBean.setStart_time(timeUtil.serialTimeToDate(start_time,timeUtil.nycTimeZone));
            validateBean.setEnd_time(timeUtil.serialTimeToDate(Double.valueOf(end_time.split(",")[0]),timeUtil.nycTimeZone));
            validateBean.setPid(pid);
            validateBean.setUuid(processFirstLineInCSV(firstline, validateBean.getPid()));
            validateBean.setHeader_time(strToDate(header_time));
        }catch (Exception e){
            e.printStackTrace();
        }
        return validateBean;
    }
    public Boolean addValidateResult(ValidateBean validateBean){
        System.out.println("add validate result...");
        String sql = "insert into csv_file (pid, filename, path, size, uuid, header_time, start_time, end_time, length)"+"values(?,?,?,?,?,?,?,?,?)";
        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1,validateBean.getPid());
                ps.setString(2,validateBean.getFilename());
                ps.setString(3,validateBean.getPath());
                ps.setInt(4,validateBean.getSize());
                ps.setString(5,validateBean.getUuid());
                ps.setTimestamp(6, new java.sql.Timestamp(validateBean.getHeader_time().getTime()));
                ps.setTimestamp(7, new java.sql.Timestamp(validateBean.getStart_time().getTime()));
                ps.setTimestamp(8, new java.sql.Timestamp(validateBean.getEnd_time().getTime()));
                ps.setInt(9,validateBean.getLines());
                return ps;
            },keyHolder);
            System.out.println("Success");
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    //get all patients method
    public List<ValidateBean> getAllPatient(){
        System.out.println("get all patients ...");
        String sql = "select * from csv_file";
        return this.jdbcTemplate.query(sql,new PatientRowMapper());
    }
}