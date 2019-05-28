package edu.pitt.medschool.model.dao;

import edu.pitt.medschool.model.dto.CsvLog;
import edu.pitt.medschool.model.mapper.CsvLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class CsvLogDao {
    @Autowired
    CsvLogMapper csvLogMapper;

    public int addLog(CsvLog csvLog){
        return csvLogMapper.insert(csvLog);
    }

}
