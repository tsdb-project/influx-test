package edu.pitt.medschool.service;


import edu.pitt.medschool.framework.util.TimeUtil;
import edu.pitt.medschool.model.ValidateBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class ValidateCsvService {
    private Date strToDate(String str){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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
            FileReader fileReader = new FileReader(dir);
            BufferedReader reader = new BufferedReader(fileReader);
            File file = new File(dir);
            String line;
            line = reader.readLine();
            String firstLine = line;
            line = reader.readLine();
            line = reader.readLine();
            validateBean.setPid(line.split(",")[1]);
            validateBean.setUuid(processFirstLineInCSV(firstLine, validateBean.getPid()));
            String header_time;
            line = reader.readLine();
            line = reader.readLine();
            header_time = line.split(",")[1];
            line = reader.readLine();
            header_time = header_time+" "+line.split(",")[1];
            line = reader.readLine();
            line = reader.readLine();
            int count = 0;
            line = reader.readLine();
            Double start_time = Double.valueOf(line.split(",")[0]);
            while((line = reader.readLine())!=null){
                count++;
            }
            Double end_time = Double.valueOf(line.split(",")[0]);
            int lines = count+1;
            TimeUtil timeUtil = new TimeUtil();
            validateBean.setStart_time(timeUtil.serialTimeToDate(start_time,timeUtil.nycTimeZone));
            validateBean.setEnd_time(timeUtil.serialTimeToDate(end_time,timeUtil.nycTimeZone));
            validateBean.setHeader_time(strToDate(header_time));
            validateBean.setLines(lines);
            validateBean.setFilename(file.getName());
            validateBean.setSize((int) file.length());
            validateBean.setPath(dir);
        }catch (Exception e){
            System.out.println("read error");
        }
        return validateBean;
    }
    public Boolean insertMsql(ValidateBean validateBean){
        try {
            ///// instert sql
            return true;
        }catch (Exception e){
            return false;
        }
    }
}
