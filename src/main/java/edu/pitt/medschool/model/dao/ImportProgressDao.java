package edu.pitt.medschool.model.dao;

import java.util.List;

import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import edu.pitt.medschool.controller.load.vo.ActivityVO;
import edu.pitt.medschool.model.dto.ImportLog;
import edu.pitt.medschool.model.dto.ImportLogExample;
import edu.pitt.medschool.model.dto.ImportProgress;
import edu.pitt.medschool.model.dto.ImportProgressExample;
import edu.pitt.medschool.model.mapper.ImportLogMapper;
import edu.pitt.medschool.model.mapper.ImportProgressMapper;
import edu.pitt.medschool.service.ImportProgressService;

/**
 * Import progress DAO
 */
@Repository
public class ImportProgressDao {

    @Autowired
    ImportProgressMapper iProgessMapper;
    @Autowired
    ImportLogMapper importLogMapper;

    public int insert(ImportLog i) throws Exception {
        // Timestamp filled by MySQL
        return importLogMapper.insertSelective(i);
    }

    public double OverallProgress(String uuid, String batchId) {
        ImportLogExample example = new ImportLogExample();
        example.createCriteria().andUuidEqualTo(uuid).andStatusNotEqualTo(ImportProgressService.FileProgressStatus.STATUS_FAIL.toString()).andBatchIdEqualTo(batchId);
        example.setOrderByClause("timestamp DESC");
        List<ImportLog> tmp = importLogMapper.selectByExampleWithRowbounds(example, new RowBounds(0, 1));
        if (tmp.isEmpty()) {
            return 1.00;
        }
        return tmp.get(0).getAllPercent();
    }

    public List<ImportProgress> GetTaskDetailProgress(String uuid, String batchId) {
        ImportProgressExample example = new ImportProgressExample();
        example.createCriteria().andUuidEqualTo(uuid).andBatchIdEqualTo(batchId);
        return iProgessMapper.selectByExample(example);
    }

    /**
     * @param uuid 
     * @return
     */
    public List<ActivityVO> getActivityList(String uuid) {
        return iProgessMapper.getActivityList(uuid);
    }

    public String selectDatabaseVersion(String uuid) {
        return iProgessMapper.selectDatabaseVersion(uuid);
    }

}
