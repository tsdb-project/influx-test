package edu.pitt.medschool.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.pitt.medschool.controller.analysis.vo.DownsampleVO;
import edu.pitt.medschool.controller.analysis.vo.MedicalDownsampleVO;
import edu.pitt.medschool.model.dao.DownsampleDao;
import edu.pitt.medschool.model.dao.DownsampleGroupDao;
import edu.pitt.medschool.model.dao.ExportDao;
import edu.pitt.medschool.model.dao.ImportProgressDao;
import edu.pitt.medschool.model.dao.MedicalDownsampleDao;
import edu.pitt.medschool.model.dao.MedicalDownsampleGroupDao;
import edu.pitt.medschool.model.dto.DownsampleGroup;
import edu.pitt.medschool.model.dto.ExportWithBLOBs;

@Service
public class ExportService {

    @Value("${machine}")
    private String uuid;

    private final ExportDao exportDao;
    private final ImportProgressDao importProgressDao;
    private final DownsampleDao downsampleDao;
    private final DownsampleGroupDao downsampleGroupDao;
    private final MedicalDownsampleDao medicalDownsampleDao;
    private final MedicalDownsampleGroupDao medicalDownsampleGroupDao;

    @Autowired
    public ExportService(ExportDao exportDao, ImportProgressDao importProgressDao, DownsampleDao downsampleDao, DownsampleGroupDao downsampleGroupDao, MedicalDownsampleDao medicalDownsampleDao, MedicalDownsampleGroupDao medicalDownsampleGroupDao) {
        this.exportDao = exportDao;
        this.importProgressDao = importProgressDao;
        this.downsampleDao = downsampleDao;
        this.downsampleGroupDao = downsampleGroupDao;
        this.medicalDownsampleDao = medicalDownsampleDao;
        this.medicalDownsampleGroupDao = medicalDownsampleGroupDao;
    }

    public int completeJobAndInsert(ExportWithBLOBs job) throws JsonProcessingException {
        DownsampleVO downsampleVO = new DownsampleVO();
        downsampleVO.setDownsample(downsampleDao.selectByPrimaryKey(job.getQueryId()));
        downsampleVO.setGroups(downsampleGroupDao.selectAllAggregationGroupByQueryId(job.getQueryId()));
        ObjectMapper mapper = new ObjectMapper();
        job.setQueryJson(mapper.writeValueAsString(downsampleVO));
        job.setMachine(uuid);
        job.setDbVersion(importProgressDao.selectDatabaseVersion(uuid));
        job.setMedical(false);

        return exportDao.insertExportJob(job);
    }
    /**
     * By HSX
     */
    public int completePredictJobAndInsert(ExportWithBLOBs job) throws JsonProcessingException {
    	//when to predict, the DSGroup is hard-coded 10 features to match the ML model
        DownsampleVO downsampleVO = new DownsampleVO();
        downsampleVO.setDownsample(downsampleDao.selectByPrimaryKey(job.getQueryId()));
        List<DownsampleGroup> groups = new ArrayList<>();
        // hard code the 10 features same as in ML models （id=1~10)
		// there are these 10 groups in database.
        for (int i = 1; i <= 10; i++) {
        	groups.add(downsampleGroupDao.selectDownsampleGroup(i));
        }
        downsampleVO.setGroups(groups);
        ObjectMapper mapper = new ObjectMapper();
        job.setQueryJson(mapper.writeValueAsString(downsampleVO));
        job.setMachine(uuid);
        job.setDbVersion(importProgressDao.selectDatabaseVersion(uuid));
        job.setMedical(false);

        return exportDao.insertExportJob(job);
    }
    
    public int completeMedicalJobAndInsert(ExportWithBLOBs job)throws JsonProcessingException{
    	MedicalDownsampleVO medicalDownsampleVO = new MedicalDownsampleVO();
    	medicalDownsampleVO.setMedicalDownsample(medicalDownsampleDao.selectByPrimaryKey(job.getQueryId()));
    	medicalDownsampleVO.setGroups(medicalDownsampleGroupDao.selectAllAggregationGroupByQueryId(job.getQueryId()));
    	ObjectMapper mapper = new ObjectMapper();
    	job.setQueryJson(mapper.writeValueAsString(medicalDownsampleVO));
    	job.setMachine(uuid);
    	job.setDbVersion(importProgressDao.selectDatabaseVersion(uuid));
    	job.setMedical(true);
    	return exportDao.insertExportJob(job);
    }

    public int deleteExportJobById(Integer exportId) {
        return this.exportDao.markAsDeletedById(exportId);
    }

}
