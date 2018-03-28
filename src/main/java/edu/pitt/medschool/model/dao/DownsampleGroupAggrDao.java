package edu.pitt.medschool.model.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.pitt.medschool.model.dto.DownsampleGroup;
import edu.pitt.medschool.model.mapper.DownsampleGroupMapper;

@Repository
public class DownsampleGroupAggrDao {

    @Autowired
    DownsampleGroupMapper downsampleGroupMapper;

    @Transactional(rollbackFor = Exception.class)
    public int insert(DownsampleGroup dsg) throws Exception {
        return downsampleGroupMapper.insertSelective(dsg);
    }

}
