package edu.pitt.medschool.model.dao;

import java.sql.Date;
import java.util.List;
import static java.lang.Math.toIntExact;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.pitt.medschool.model.dto.CsvFile;
import edu.pitt.medschool.model.dto.Downsample;
import edu.pitt.medschool.model.dto.MedicalDownsample;
import edu.pitt.medschool.model.dto.MedicalDownsampleExample;
import edu.pitt.medschool.model.dto.Medication;
import edu.pitt.medschool.model.dto.Patient;
import edu.pitt.medschool.model.mapper.MedicalDownsampleMapper;


@Repository
public class MedicalDownsampleDao {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	MedicalDownsampleMapper medicalDownsampleMapper;
	
	public List<MedicalDownsample> selectAll(){
		MedicalDownsampleExample example = new MedicalDownsampleExample();
		example.createCriteria();
		List<MedicalDownsample> list = medicalDownsampleMapper.selectByExample(example);
		return list;
	}

	@Transactional(rollbackFor = Exception.class)
	public int updateByPrimaryKey(MedicalDownsample medicalDownsample) {
		return medicalDownsampleMapper.updateByPrimaryKeySelective(medicalDownsample);
	}
	
	@Transactional(rollbackFor = Exception.class)
	public int insert(MedicalDownsample medicalDownsample) {
		return medicalDownsampleMapper.insertSelective(medicalDownsample);
	}
	
	public MedicalDownsample selectByPrimaryKey(int id) {
		return medicalDownsampleMapper.selectByPrimaryKey(id);
	}
	
	public int deleteByPrimaryKey(int id) {
		return medicalDownsampleMapper.deleteByPrimaryKey(id);
	}
}
