package edu.pitt.medschool.model.dao;

import edu.pitt.medschool.model.dto.Downsample;
import edu.pitt.medschool.model.dto.DownsampleMeta;
import edu.pitt.medschool.model.mapper.DownsampleMetaMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class DownsampleMetaDao {

    @Autowired
    DownsampleMetaMapper downsampleMetaMapper;

    @Transactional(rollbackFor = Exception.class)
    public int insertMeta(Downsample ds, String k, String v) throws Exception {
        DownsampleMeta dm = new DownsampleMeta();
        dm.setQueryId(ds.getId());
        dm.setKey(k);
        dm.setValue(v);
        return downsampleMetaMapper.insertSelective(dm);
    }

}
