/**
 *
 */
package edu.pitt.medschool.model.dao;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.pitt.medschool.framework.util.Util;
import edu.pitt.medschool.model.dto.DownsampleGroup;
import edu.pitt.medschool.model.dto.DownsampleGroupExample;
import edu.pitt.medschool.model.mapper.DownsampleGroupMapper;

/**
 * @author Isolachine
 */
@Repository
public class DownsampleGroupDao {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    DownsampleGroupMapper downsampleGroupMapper;

    public int insert(DownsampleGroup group) throws Exception {
        downsampleGroupMapper.insertSelective(group);
        return group.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean insertDownsampleGroup(DownsampleGroup group) {
        try {
            this.insert(group);
        } catch (Exception e) {
            logger.error(Util.stackTraceErrorToString(e));
            return false;
        }
        return true;
    }

    public List<DownsampleGroup> selectAllDownsampleGroup(int queryId) {
        DownsampleGroupExample example = new DownsampleGroupExample();
        example.createCriteria().andQueryIdEqualTo(queryId);
        return downsampleGroupMapper.selectByExampleWithBLOBs(example);
    }

    public DownsampleGroup selectDownsampleGroup(Integer groupId) {
        return downsampleGroupMapper.selectByPrimaryKey(groupId);
    }

    public int deleteByPrimaryKey(Integer groupId) {
        return downsampleGroupMapper.deleteByPrimaryKey(groupId);
    }

    public int updateByPrimaryKeySelective(DownsampleGroup group) {
        return downsampleGroupMapper.updateByPrimaryKeySelective(group);
    }

    public List<DownsampleGroup> selectAllAggregationGroupByQueryId(Integer queryId) {
        List<DownsampleGroup> groups = this.selectAllDownsampleGroup(queryId);
        return groups;
    }

}
