package edu.pitt.medschool.model.dao;

import edu.pitt.medschool.model.dto.Downsample;
import edu.pitt.medschool.model.dto.DownsampleGroup;
import edu.pitt.medschool.model.dto.DownsampleGroupColumn;
import edu.pitt.medschool.model.dto.DownsampleMeta;
import edu.pitt.medschool.model.mapper.DownsampleGroupMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class DownsampleGroupAggrDao {

    @Autowired
    DownsampleGroupMapper downsampleGroupMapper;

    @Transactional(rollbackFor = Exception.class)
    public int insertGroupAggr(DownsampleGroup dsg) throws Exception {
        return downsampleGroupMapper.insertSelective(dsg);
    }

}
