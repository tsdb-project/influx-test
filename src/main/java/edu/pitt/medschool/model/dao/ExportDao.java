package edu.pitt.medschool.model.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import edu.pitt.medschool.model.dto.ExportWithBLOBs;
import edu.pitt.medschool.model.mapper.ExportMapper;

@Repository
public class ExportDao {
    @Autowired
    ExportMapper exportMapper;

    public int insertExportJob(ExportWithBLOBs job) {
        return exportMapper.insertSelective(job);
    }

    public ExportWithBLOBs selectByPrimaryKey(Integer exportId) {
        return exportMapper.selectByPrimaryKey(exportId);
    }

}
