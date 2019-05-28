package edu.pitt.medschool.service;

import edu.pitt.medschool.model.dao.CsvFileDao;
import edu.pitt.medschool.model.dao.CsvLogDao;
import edu.pitt.medschool.model.dto.CsvFile;
import edu.pitt.medschool.model.dto.CsvLog;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class VersionControlService {
    private final CsvFileDao csvFileDao;
    private final CsvLogDao csvLogDao;

    public VersionControlService(CsvFileDao csvFileDao, CsvLogDao csvLogDao) {
        this.csvFileDao = csvFileDao;
        this.csvLogDao = csvLogDao;
    }

    public List<CsvFile> getAllChanges(){
        return csvFileDao.getAllchanges();
    }

    public CsvFile getElementByID(Integer id){
        return csvFileDao.getElementByID(id);
    }

    public int updateStatus(CsvFile csvFile){
        return csvFileDao.updateStatus(csvFile);
    }

    public int deleteRecord(Integer id){
        return csvFileDao.deleteRecord(id);
    }

    public int setLog(CsvFile csvFile, Integer status){
        CsvLog csvLog = new CsvLog();
        ZoneId america = ZoneId.of("America/New_York");
        LocalDateTime americaDateTime = LocalDateTime.now(america);
        csvLog.setActivity(status);
        csvLog.setEndTime(csvFile.getEndTime());
        csvLog.setStatus(csvFile.getStatus());
        csvLog.setFilename(csvFile.getFilename());
        csvLog.setStatTime(csvFile.getStartTime());
        csvLog.setTimestamp(americaDateTime);
        return csvLogDao.addLog(csvLog);
    }
}
