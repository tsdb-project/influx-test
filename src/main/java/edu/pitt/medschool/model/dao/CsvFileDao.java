package edu.pitt.medschool.model.dao;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import edu.pitt.medschool.model.dto.CsvLog;
import edu.pitt.medschool.model.mapper.CsvLogMapper;
import org.apache.commons.lang3.ObjectUtils;
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
            patientTimeLines.add(array.get(i).toPatientTimeLine());
        }
        return patientTimeLines;
    }

    public List<CsvFile> selectByPatientId(String patientId) {
        CsvFileExample example = new CsvFileExample();
        Criteria criteria = example.createCriteria();
        criteria.andPidEqualTo(patientId);
//        set constrain on machine ID
//        criteria.andMachineEqualTo(machineId);
        criteria.andMachineEqualTo("realpsc");
        criteria.andDeletedEqualTo(false);

//        using the different datawarehouse structure
//        criteria.andStatusNotEqualTo(1);


        return csvFileMapper.selectByExample(example);
    }

//    method for select delected files
    public List<String> selectDeletedFilesByPatientId(String patientId) {
        return csvFileMapper.selectDeletedFilesByPatientId(patientId);
    }


    @Transactional(rollbackFor = Exception.class)
    public int changeComment(CsvFile file) throws Exception {
        CsvFileExample csvFileExample = new CsvFileExample();
        Criteria csvFileCriteria = csvFileExample.createCriteria();
        csvFileCriteria.andPidEqualTo(file.getPid());
        csvFileCriteria.andFilenameEqualTo(file.getFilename());
//        csvFileCriteria.andMachineEqualTo(machineId);
        csvFileCriteria.andUuidEqualTo(file.getUuid());

        int changeComment = csvFileMapper.updateByExample(file, csvFileExample);
        try {
            if (changeComment == 0) {
                throw new Exception();
            }
        } catch (Exception e) {
            logger.error("No CSV file record available!");
            throw e;
        }
        return changeComment;
    }


    @Transactional(rollbackFor = Exception.class)
    public int resolveFileByFile(CsvFile file) throws Exception {
        CsvFileExample csvFileExample = new CsvFileExample();
        Criteria csvFileCriteria = csvFileExample.createCriteria();
        csvFileCriteria.andPidEqualTo(file.getPid());
        csvFileCriteria.andFilenameEqualTo(file.getFilename());
//        csvFileCriteria.andMachineEqualTo(machineId);
        csvFileCriteria.andUuidEqualTo(file.getUuid());
        csvFileCriteria.andDeletedEqualTo(false);


        CsvFile csvFile = new CsvFile();
        if(file.getConflictResolved()){
            csvFile.setConflictResolved(false);
        }else{
            csvFile.setConflictResolved(true);
        }

//        csvFile.setUpdateTime(LocalDateTime.now());

        int changeCommentResult = csvFileMapper.updateByExampleSelective(csvFile, csvFileExample);
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
    public int resolveAllFilesByPid(String pid) throws Exception {
        CsvFileExample csvFileExample = new CsvFileExample();
        Criteria csvFileCriteria = csvFileExample.createCriteria();
        csvFileCriteria.andPidEqualTo(pid);
        csvFileCriteria.andDeletedEqualTo(false);

//        using the different datawarehouse structure
//        csvFileCriteria.andStatusNotEqualTo(1);

        CsvFile csvFile = new CsvFile();
        csvFile.setConflictResolved(true);

//        csvFile.setUpdateTime(LocalDateTime.now());

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
        CsvFileExample csvFileExample = new CsvFileExample();
        Criteria csvFileCriteria = csvFileExample.createCriteria();
        csvFileCriteria.andPidEqualTo(file.getPid());
        csvFileCriteria.andFilenameEqualTo(file.getFilename());
        csvFileCriteria.andMachineEqualTo(machineId);
        csvFileCriteria.andUuidEqualTo(file.getUuid());
        csvFileCriteria.andDeletedEqualTo(false);

        CsvFile csvFile = new CsvFile();
        csvFile.setDeleted(true);
//      csvFile.setStatus(1);
//      csvFile.setUpdateTime(LocalDateTime.now());
        csvFile.setDeleteTime(LocalDateTime.now());

//        save to csv_log
//        CsvLog csvLog = new CsvLog();
//        csvLog.setStatus(1);
//        csvLog.setStatus(0);
//        csvLog.setFilename(file.getFilename());
//        csvLog.setStatTime(file.getStartTime());
//        csvLog.setEndTime(file.getEndTime());
//        csvLog.setTimestamp(LocalDateTime.now());

        int deleteResult = csvFileMapper.updateByExampleSelective(csvFile, csvFileExample);
        try {
//            int deleteResult = csvFileMapper.updateByExampleSelective(csvFile, csvFileExample)* csvLogMapper.insertSelective(csvLog);
            if (deleteResult == 0) {
                throw new Exception();
            }
        } catch (Exception e) {
            logger.error("No CSV file record available!");
            throw e;
        }
        return deleteResult;
    }

    public List<CsvFile> getAllchanges(){
        return csvFileMapper.getAllChanges();
    }

    public CsvFile getElementByID(Integer id){
        return csvFileMapper.selectByPrimaryKey(id);
    }

    public int updateStatus(CsvFile csvFile){
        return csvFileMapper.updateByPrimaryKey(csvFile);
    }

    public int deleteRecord(Integer id){
        return csvFileMapper.deleteByPrimaryKey(id);
    }
}
