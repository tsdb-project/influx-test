package edu.pitt.medschool.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.pitt.medschool.controller.analysis.vo.DownsampleVO;
import edu.pitt.medschool.model.dao.DownsampleDao;
import edu.pitt.medschool.model.dao.DownsampleGroupDao;
import edu.pitt.medschool.model.dao.ExportDao;
import edu.pitt.medschool.model.dao.ImportProgressDao;
import edu.pitt.medschool.model.dto.ExportWithBLOBs;

@Service
public class ExportService {

    @Value("${machine}")
    private String uuid;

    private final ExportDao exportDao;
    private final ImportProgressDao importProgressDao;
    private final DownsampleDao downsampleDao;
    private final DownsampleGroupDao downsampleGroupDao;

    @Autowired
    public ExportService(ExportDao exportDao, ImportProgressDao importProgressDao, DownsampleDao downsampleDao, DownsampleGroupDao downsampleGroupDao) {
        this.exportDao = exportDao;
        this.importProgressDao = importProgressDao;
        this.downsampleDao = downsampleDao;
        this.downsampleGroupDao = downsampleGroupDao;
    }

    public int completeJobAndInsert(ExportWithBLOBs job) throws JsonProcessingException {
        DownsampleVO downsampleVO = new DownsampleVO();
        downsampleVO.setDownsample(downsampleDao.selectByPrimaryKey(job.getQueryId()));
        downsampleVO.setGroups(downsampleGroupDao.selectAllAggregationGroupByQueryId(job.getQueryId()));
        ObjectMapper mapper = new ObjectMapper();
        job.setQueryJson(mapper.writeValueAsString(downsampleVO));

        job.setMachine(uuid);
        job.setDbVersion(importProgressDao.selectDatabaseVersion(uuid));

        return exportDao.insertExportJob(job);
    }

    public int deleteExportJobById(Integer exportId) {
        return this.exportDao.deleteById(exportId);
    }

}
