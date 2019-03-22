package edu.pitt.medschool.model.dao;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.pitt.medschool.framework.util.Util;
import edu.pitt.medschool.model.dto.DownsampleGroup;
import edu.pitt.medschool.model.dto.MedicalDownsampleExample;
import edu.pitt.medschool.model.dto.MedicalDownsampleGroup;
import edu.pitt.medschool.model.dto.MedicalDownsampleGroupExample;
import edu.pitt.medschool.model.mapper.MedicalDownsampleGroupMapper;

@Repository
public class MedicalDownsampleGroupDao {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	MedicalDownsampleGroupMapper medicalDownsampleGroupMapper;
	public int insert(MedicalDownsampleGroup group) throws Exception{
		medicalDownsampleGroupMapper.insertSelective(group);
		return group.getId();
	}
	
	@Transactional(rollbackFor = Exception.class)
	public boolean insertMedicalDownsampleGroup(MedicalDownsampleGroup group) {
		try {
			this.insert(group);
		}catch (Exception e) {
			// TODO: handle exception
			logger.error(Util.stackTraceErrorToString(e));
			return false;
		}
		return true;
	}
	
	public List<MedicalDownsampleGroup> selectAllMedicalDownsampleGroups(int queryId) {
		MedicalDownsampleGroupExample example = new MedicalDownsampleGroupExample();
		example.createCriteria().andQueryIdEqualTo(queryId);
		return medicalDownsampleGroupMapper.selectByExampleWithBLOBs(example);
	}
	
	// ???????????
	@SuppressWarnings("null")
	public List<DownsampleGroup> translateList(List<MedicalDownsampleGroup> list){
		List<DownsampleGroup> res = new ArrayList<DownsampleGroup>();
		DownsampleGroup dsg = new DownsampleGroup();
		for (int i=0; i<list.size();i++) {
			dsg.setAggregation(list.get(i).getAggregation());
			dsg.setColumns(list.get(i).getColumns());
			dsg.setCreateTime(list.get(i).getCreateTime());
			dsg.setDeleted(list.get(i).getDeleted());
			dsg.setDownsample(list.get(i).getDownsample());
			dsg.setId(list.get(i).getId());
			dsg.setLabel(list.get(i).getLabel());
			dsg.setQueryId(list.get(i).getQueryId());
			dsg.setUpdateTime(list.get(i).getUpdateTime());
			res.add(dsg);
			
		}
		return res;
	}
	
	public MedicalDownsampleGroup seleMedicalDownsampleGroup(Integer groupId) {
		return medicalDownsampleGroupMapper.selectByPrimaryKey(groupId);
	}
	
	public int deleteByPrimaryKey(Integer groupId) {
		return medicalDownsampleGroupMapper.deleteByPrimaryKey(groupId);
	}
	
	public int updateByPrimaryKeySelective(MedicalDownsampleGroup group) {
		return medicalDownsampleGroupMapper.updateByPrimaryKeySelective(group);
	}
	
	public List<MedicalDownsampleGroup> selectAllAggregationGroupByQueryId(Integer queryId){
		List<MedicalDownsampleGroup> groups = this.selectAllMedicalDownsampleGroups(queryId);
		return groups;
	}
	
	
	
}
