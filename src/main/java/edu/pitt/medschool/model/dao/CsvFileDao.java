package edu.pitt.medschool.model.dao;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.pitt.medschool.model.PatientTimeLine;
import edu.pitt.medschool.model.dto.CsvFile;
import edu.pitt.medschool.model.dto.CsvFileExample;
import edu.pitt.medschool.model.dto.CsvFileExample.Criteria;
import edu.pitt.medschool.model.dto.TimeLine;
import edu.pitt.medschool.model.mapper.CsvFileMapper;

@Repository
public class CsvFileDao {
    @Autowired
    CsvFileMapper csvFileMapper;

//    @Autowired
//    CsvLogMapper csvLogMapper;

    @Value("${machine}")
    private String machineId;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Transactional(rollbackFor = Exception.class)
    public int insert(CsvFile csvFile) throws Exception {
        return csvFileMapper.insertSelective(csvFile);
    }

    public List<PatientTimeLine> getPatientTimeLines(String machine) {
        List<TimeLine> array = csvFileMapper.getPatientTimeLines(machine);
        List<PatientTimeLine> patientTimeLines = new ArrayList<PatientTimeLine>();
        for (int i = 0; i < array.size(); i++) {
            //System.out.println(array.get(i).getFilename());
            patientTimeLines.add(array.get(i).toPatientTimeLine());
        }
        return patientTimeLines;
    }

    public List<PatientTimeLine> getLatestPatientTimeLinesByVersion(int version,String machineId){
        List<TimeLine> array = csvFileMapper.getLatestPatientTimeLines(version,machineId);
        List<PatientTimeLine> patientTimeLines = new ArrayList<PatientTimeLine>();
        for (int i = 0; i < array.size(); i++) {
            patientTimeLines.add(array.get(i).toPatientTimeLine());
        }
        return patientTimeLines;
    }

    public List<PatientTimeLine> getPatientTimeLinesByVersion(int version,String machineId){
        List<TimeLine> array = csvFileMapper.getPatientTimeLinesByVersion(version,machineId);
        List<PatientTimeLine> patientTimeLines = new ArrayList<PatientTimeLine>();
        for (int i = 0; i < array.size(); i++) {
            patientTimeLines.add(array.get(i).toPatientTimeLine());
        }
        return patientTimeLines;
    }

    public List<PatientTimeLine> getPatientTimeLinesByVersionID(int version,String machineId,String pid){
        List<TimeLine> array = csvFileMapper.getPatientTimeLinesByVersionID(version,machineId,pid);
        List<PatientTimeLine> patientTimeLines = new ArrayList<PatientTimeLine>();
        for (int i = 0; i < array.size(); i++) {
            patientTimeLines.add(array.get(i).toPatientTimeLine());
        }
        return patientTimeLines;
    }

    public List<CsvFile> selectByPatientId(String patientId,int currentVersion) {
        CsvFileExample example = new CsvFileExample();
        Criteria criteria = example.createCriteria();
        criteria.andPidEqualTo(patientId);
//        set constrain on machine ID
//        criteria.andMachineEqualTo(machineId);
//        criteria.andMachineEqualTo("realpsc");
//        criteria.andDeletedEqualTo(false);

//        using the different datawarehouse structure
        criteria.andStatusNotEqualTo(1);
        criteria.andStatusLessThanOrEqualTo(currentVersion);
        criteria.andEndVersionGreaterThan(currentVersion);

        return csvFileMapper.selectByExample(example);
    }


    @Transactional(rollbackFor = Exception.class)
    public int changeComment(CsvFile file) throws Exception {
        ZoneId utcTz = ZoneId.of("UTC");
        ZoneId nycTz = ZoneId.of("America/New_York");
        file.setStartTime(file.getStartTime().atZone(nycTz).withZoneSameInstant(utcTz).toLocalDateTime());
        file.setEndTime(file.getEndTime().atZone(nycTz).withZoneSameInstant(utcTz).toLocalDateTime());

        ZoneId america = ZoneId.of("America/New_York");
        file.setLastUpdate(LocalDateTime.now(america));

        int changeCommentResult = csvFileMapper.updateByPrimaryKey(file);
        try {
            if (changeCommentResult == 0) {
                throw new Exception();
            }
        } catch (Exception e) {
            logger.error("No CSV file record available!");
            throw e;
        }
        return changeCommentResult;
    }


    @Transactional(rollbackFor = Exception.class)
    public int resolveFileByFile(CsvFile file) throws Exception {
        CsvFileExample csvFileExample = new CsvFileExample();
        Criteria csvFileCriteria = csvFileExample.createCriteria();
        csvFileCriteria.andIdEqualTo(file.getId());


        CsvFile csvFile = new CsvFile();
        if(file.getConflictResolved()){
            csvFile.setConflictResolved(false);
        }else{
            csvFile.setConflictResolved(true);
        }
        ZoneId america = ZoneId.of("America/New_York");
        csvFile.setLastUpdate(LocalDateTime.now(america));

        int resolveFileResult = csvFileMapper.updateByExampleSelective(csvFile, csvFileExample);
        try {
            if (resolveFileResult == 0) {
                throw new Exception();
            }
        } catch (Exception e) {
            logger.error("No CSV file record available!");
            throw e;
        }
        return resolveFileResult;
    }

    @Transactional(rollbackFor = Exception.class)
    public int resolveAllFilesByPid(String pid) throws Exception {
        CsvFileExample csvFileExample = new CsvFileExample();
        Criteria csvFileCriteria = csvFileExample.createCriteria();
        csvFileCriteria.andPidEqualTo(pid);

//        using the different data warehouse structure
        csvFileCriteria.andStatusNotEqualTo(1);

        CsvFile csvFile = new CsvFile();
        csvFile.setConflictResolved(true);
        ZoneId america = ZoneId.of("America/New_York");
        csvFile.setLastUpdate(LocalDateTime.now(america));

        int resolveResult = csvFileMapper.updateByExampleSelective(csvFile, csvFileExample);
        try {
            if (resolveResult == 0) {
                throw new Exception();
            }
        } catch (Exception e) {
            logger.error("No CSV file record available!");
            throw e;
        }
        return resolveResult;
    }

    @Transactional(rollbackFor = Exception.class)
    public int deletePatientDataByFile(CsvFile file) throws Exception {
        int deleteResult = csvFileMapper.deleteByPrimaryKey(file.getId());
        try {
            if (deleteResult == 0) {
                throw new Exception();
            }
        } catch (Exception e) {
            logger.error("No CSV file record available!");
            throw e;
        }
        return deleteResult;
    }

    @Transactional(rollbackFor = Exception.class)
    public int pseudoDeleteFile(CsvFile file) throws Exception {
        CsvFileExample csvFileExample = new CsvFileExample();
        Criteria csvFileCriteria = csvFileExample.createCriteria();
        csvFileCriteria.andIdEqualTo(file.getId());

        CsvFile csvFile = new CsvFile();
        csvFile.setStatus(1);
        ZoneId america = ZoneId.of("America/New_York");
        csvFile.setLastUpdate(LocalDateTime.now(america));

        int deleteResult = csvFileMapper.updateByExampleSelective(csvFile, csvFileExample);
        try {
            if (deleteResult == 0) {
                throw new Exception();
            }
        } catch (Exception e) {
            logger.error("No CSV file record available!");
            throw e;
        }
        return deleteResult;
    }

    //    method for select delected files
//    public List<String> selectDeletedFilesByPatientId(String patientId) {
//        return csvFileMapper.selectDeletedFilesByPatientId(patientId);
//    }

    public List<CsvFile> getAllchanges(){
        CsvFileExample example = new CsvFileExample();
        Criteria criteria = example.createCriteria();
        criteria.andStatusGreaterThanOrEqualTo(1);
        List<CsvFile> csvFiles =  csvFileMapper.selectByExample(example);
        ZoneId utcTz = ZoneId.of("UTC");
        ZoneId nycTz = ZoneId.of("America/New_York");
        for(CsvFile csvFile:csvFiles){
            csvFile.setStartTime(csvFile.getStartTime().atZone(utcTz).withZoneSameInstant(nycTz).toLocalDateTime());
            csvFile.setEndTime(csvFile.getEndTime().atZone(utcTz).withZoneSameInstant(nycTz).toLocalDateTime());
        }

        return csvFiles;
    }

    public CsvFile getElementByID(Integer id){
        return csvFileMapper.selectByPrimaryKey(id);
    }

    public int updateStatus(CsvFile csvFile){
        CsvFileExample csvFileExample = new CsvFileExample();
        Criteria criteria = csvFileExample.createCriteria();
        criteria.andIdEqualTo(csvFile.getId());
        CsvFile csvFile1 = new CsvFile();
        csvFile1.setStatus(0);
        return csvFileMapper.updateByExampleSelective(csvFile1,csvFileExample);
    }

    public int deleteRecord(Integer id){
        return csvFileMapper.deleteByPrimaryKey(id);
    }

    public int addCsvFileHearderWidth(CsvFile file){
        CsvFileExample csvFileExample = new CsvFileExample();
        Criteria criteria = csvFileExample.createCriteria();
        criteria.andFilenameEqualTo(file.getFilename());
        criteria.andUuidEqualTo(file.getUuid());
        criteria.andPidEqualTo(file.getPid());
        CsvFile csvFile = new CsvFile();
        csvFile.setWidth(file.getWidth());
        csvFile.setHeaderTime(file.getHeaderTime());
        return csvFileMapper.updateByExampleSelective(csvFile,csvFileExample);
    }

    public List<CsvFile> selectByUuidPidFileName(CsvFile file){
        CsvFileExample csvFileExample = new CsvFileExample();
        Criteria criteria = csvFileExample.createCriteria();
        criteria.andFilenameEqualTo(file.getFilename());
        criteria.andUuidEqualTo(file.getUuid());
        criteria.andPidEqualTo(file.getPid());
//        criteria.andMachineEqualTo(machineId);

//        Right now machineId is 'realpsc', need to change later
        criteria.andMachineEqualTo("realpsc");
        return csvFileMapper.selectByExample(csvFileExample);
    }

    public int updateCsvFileWidth(CsvFile file){
        CsvFileExample csvFileExample = new CsvFileExample();
        Criteria criteria = csvFileExample.createCriteria();
        criteria.andFilenameEqualTo(file.getFilename());
        criteria.andUuidEqualTo(file.getUuid());
        criteria.andPidEqualTo(file.getPid());
//        criteria.andMachineEqualTo(machineId);

//        Right now machineId is 'realpsc', need to change later
        criteria.andMachineEqualTo("realpsc");
        return csvFileMapper.updateByExampleSelective(file,csvFileExample);
    }


    public List<CsvFile> selectByFileName(String fileName) {
        CsvFileExample example = new CsvFileExample();
        Criteria criteria = example.createCriteria();
        criteria.andFilenameEqualTo(fileName);
        return csvFileMapper.selectByExample(example);
    }


    public Long getCsvDelete(int CurrentVersion) {
        CsvFileExample csvFileExample = new CsvFileExample();
        Criteria criteria = csvFileExample.createCriteria();
        criteria.andEndVersionEqualTo(CurrentVersion);
        return csvFileMapper.countByExample(csvFileExample);
    }

    public Long getCsvNumber(int CurrentVersion) {
        CsvFileExample csvFileExample = new CsvFileExample();
        Criteria criteria = csvFileExample.createCriteria();
        criteria.andStartVersionLessThanOrEqualTo(CurrentVersion);
        criteria.andEndVersionGreaterThan(CurrentVersion);
        return csvFileMapper.countByExample(csvFileExample);
    }

    public Long getCsvIncrease(int currentVersion) {
        CsvFileExample csvFileExample = new CsvFileExample();
        Criteria criteria = csvFileExample.createCriteria();
        criteria.andStartVersionEqualTo(currentVersion);
        return csvFileMapper.countByExample(csvFileExample);
    }

    public int publishNewData(int currentVersion) {
        CsvFileExample csvFileExample = new CsvFileExample();
        Criteria criteria = csvFileExample.createCriteria();
        criteria.andStartVersionEqualTo(0);
        CsvFile csvFile = new CsvFile();
        csvFile.setStartVersion(currentVersion);
        return csvFileMapper.updateByExampleSelective(csvFile,csvFileExample);
    }

    public int updateEndVersion(CsvFile file) {
        CsvFileExample csvFileExample = new CsvFileExample();
        Criteria criteria = csvFileExample.createCriteria();
        criteria.andIdEqualTo(file.getId());
        CsvFile csvFile = new CsvFile();
        csvFile.setEndVersion(file.getEndVersion());
        csvFile.setStatus(file.getStatus());
        return csvFileMapper.updateByExampleSelective(csvFile,csvFileExample);
    }

    public List<CsvFile> getElementByName(String fileName) {
        CsvFileExample csvFileExample = new CsvFileExample();
        Criteria criteria = csvFileExample.createCriteria();
        criteria.andFilenameEqualTo(fileName);
        return csvFileMapper.selectByExample(csvFileExample);
    }
}
