package edu.pitt.medschool.model.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.pitt.medschool.model.dto.Downsample;
import edu.pitt.medschool.model.dto.DownsampleMeta;
import edu.pitt.medschool.model.mapper.DownsampleMetaMapper;

@Repository
public class DownsampleMetaDao {

    @Autowired
    DownsampleMetaMapper downsampleMetaMapper;

    @Transactional(rollbackFor = Exception.class)
    public int insert(Downsample ds, String k, String v) throws Exception {
        DownsampleMeta dm = new DownsampleMeta();
        dm.setQueryId(ds.getId());
        dm.setKey(k);
        dm.setValue(v);
        return downsampleMetaMapper.insertSelective(dm);
    }

}
