/**
 * 
 */
package edu.pitt.medschool.model.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.pitt.medschool.model.dto.Downsample;
import edu.pitt.medschool.model.dto.DownsampleExample;
import edu.pitt.medschool.model.mapper.DownsampleMapper;

/**
 * @author Isolachine
 *
 */
@Repository
public class DownsampleDao {
    @Autowired
    DownsampleMapper downsampleMapper;

    public List<Downsample> selectAll() {
        DownsampleExample example = new DownsampleExample();
        example.createCriteria();
        List<Downsample> list = downsampleMapper.selectByExample(example);
        return list;
    }

    @Transactional(rollbackFor = Exception.class)
    public int insert(Downsample downsample) throws Exception {
        return downsampleMapper.insertSelective(downsample);
    }
    
    public Downsample selectByPrimaryKey(int id) {
        return downsampleMapper.selectByPrimaryKey(id);
    }

}
