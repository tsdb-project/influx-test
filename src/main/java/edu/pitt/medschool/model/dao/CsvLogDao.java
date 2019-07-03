package edu.pitt.medschool.model.dao;

import edu.pitt.medschool.model.dto.CsvFile;
import edu.pitt.medschool.model.dto.CsvLog;
import edu.pitt.medschool.model.dto.CsvLogExample;
import edu.pitt.medschool.model.dto.CsvLogExample.Criteria;
import edu.pitt.medschool.model.mapper.CsvLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CsvLogDao {
    @Autowired
    CsvLogMapper csvLogMapper;

    public int addLog(CsvLog csvLog){
        return csvLogMapper.insert(csvLog);
    }

    public List<CsvLog> selectByFileNameActivity(CsvFile file,String status){
        CsvLogExample csvLogExample = new CsvLogExample();
        Criteria criteria = csvLogExample.createCriteria();
        criteria.andFilenameEqualTo(file.getFilename());
        criteria.andActivityEqualTo(status);
        return csvLogMapper.selectByExample(csvLogExample);
    }

}
