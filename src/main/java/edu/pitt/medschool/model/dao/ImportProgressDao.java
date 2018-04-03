package edu.pitt.medschool.model.dao;

import edu.pitt.medschool.controller.load.vo.ProgressVO;
import edu.pitt.medschool.model.dto.ImportProgress;
import edu.pitt.medschool.model.dto.ImportProgressExample;
import edu.pitt.medschool.model.mapper.ImportProgressMapper;
import edu.pitt.medschool.service.ImportProgressService;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Import progress DAO
 */
@Repository
public class ImportProgressDao {

    @Autowired
    ImportProgressMapper iProgessMapper;

    public int insert(ImportProgress i) throws Exception {
        // Timestamp(6) filled by MySQL
        return iProgessMapper.insertSelective(i);
    }

    public double OverallProgress(String uuid) {
        ImportProgressExample ipe = new ImportProgressExample();
        ipe.createCriteria()
                .andStatusNotEqualTo(
                        ImportProgressService.FileProgressStatus.STATUS_FAIL.toString());
        ipe.setOrderByClause("timestamp DESC");
        List<ImportProgress> tmp = iProgessMapper.selectByExampleWithRowbounds(
                ipe, new RowBounds(0, 1));
        return tmp.get(0).getAllPercent();
    }

    public List<ProgressVO> GetTaskDetailProgress(String uuid) {
        return iProgessMapper.selectTaskDetailProgress(uuid);
    }

}
