package edu.pitt.medschool.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import edu.pitt.medschool.framework.util.TimeUtil;
import edu.pitt.medschool.model.PatientTimeLine;
import edu.pitt.medschool.model.dao.CsvFileDao;
import edu.pitt.medschool.model.dto.CsvFile;
import edu.pitt.medschool.model.dto.GraphFilter;

@Service
public class ValidateCsvService {

    @Value("${machine}")
    private String machineId;
    private final CsvFileDao csvFileDao;

    public ValidateCsvService(CsvFileDao csvFileDao) {
        this.csvFileDao = csvFileDao;
    }

    private Date strToDate(String str) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        Date date = null;
        try {
            date = format.parse(str);
        } catch (ParseException pe) {
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

    public CsvFile analyzeCsv(String dir) {
        CsvFile validateBean = new CsvFile();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(dir));
            String line;
            String firstline = reader.readLine();
            for (int i = 0; i < 3; i++) {
                reader.readLine();
            }
            String header_time = reader.readLine().split(",")[1];
            header_time = header_time + " " + reader.readLine().split(",")[1];
            String pid = firstline.split("\\\\")[2];
            File file = new File(dir);
            int count = 1;
            for (int j = 0; j < 2; j++) {
                reader.readLine();
            }
            Double start_time = Double.valueOf(reader.readLine().split(",")[0]);
            String end_time = null;
            while ((line = reader.readLine()) != null) {
                end_time = line;
                count += 1;
            }
            reader.close();

            ZoneId zoneId = ZoneId.of("America/New_York");

            validateBean.setSize((int) file.length());
            validateBean.setPath(dir);
            validateBean.setFilename(file.getName());
            validateBean.setLength(count);
            LocalDateTime start = LocalDateTime
                    .ofInstant(TimeUtil.serialTimeToDate(start_time, TimeUtil.nycTimeZone).toInstant(), zoneId);
            validateBean.setStartTime(start);
            LocalDateTime end = LocalDateTime.ofInstant(
                    TimeUtil.serialTimeToDate(Double.valueOf(end_time.split(",")[0]), TimeUtil.nycTimeZone).toInstant(),
                    zoneId);
            validateBean.setEndTime(end);
            validateBean.setPid(pid);
            validateBean.setUuid(processFirstLineInCSV(firstline, validateBean.getPid()));
            validateBean.setHeaderTime(LocalDateTime.ofInstant(strToDate(header_time).toInstant(), zoneId));
            validateBean.setMachine(machineId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return validateBean;
    }

    public int insertCsvFile(CsvFile csvFile) throws Exception {
        return csvFileDao.insert(csvFile);
    }

    public List<PatientTimeLine> getPatientTimeLines(String machine) {
        return csvFileDao.getPatientTimeLines(machine);
    }

    public String getFilteredtPatientTimeLines(String machine, GraphFilter filter) throws Exception {
        throw new NumberFormatException();
    }

}
