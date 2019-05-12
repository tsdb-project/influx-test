package edu.pitt.medschool.model.dao;

import java.time.LocalDateTime;
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
        criteria.andMachineEqualTo(machineId);
        criteria.andDeletedEqualTo(false);
        return csvFileMapper.selectByExample(example);
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
        csvFile.setDeleteTime(LocalDateTime.now());

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

}
