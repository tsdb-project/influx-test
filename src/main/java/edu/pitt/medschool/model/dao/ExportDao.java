package edu.pitt.medschool.model.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import edu.pitt.medschool.model.dto.Export;
import edu.pitt.medschool.model.mapper.ExportMapper;

@Repository
public class ExportDao {
    @Autowired
    ExportMapper exportMapper;

    public int insertExportJob(Export job) {
        return exportMapper.insertSelective(job);
    }

    public Export selectByPrimaryKey(Integer exportId) {
        return exportMapper.selectByPrimaryKey(exportId);
    }

}
