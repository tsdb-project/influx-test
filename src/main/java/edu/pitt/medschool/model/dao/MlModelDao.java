package edu.pitt.medschool.model.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.pitt.medschool.model.dto.AggregationDb;
import edu.pitt.medschool.model.dto.AggregationDbExample;
import edu.pitt.medschool.model.dto.MlModel;
import edu.pitt.medschool.model.dto.MlModelExample;
import edu.pitt.medschool.model.mapper.MlModelMapper;

@Repository
public class MlModelDao {

	@Autowired
	MlModelMapper mlModelMapper;
	
	@Transactional
	public List<MlModel> selectMlModelsByTimeLevelAndAggMethod(List<Integer> aggLevels, List<String> aggMethods){
        MlModelExample example = new MlModelExample();
        MlModelExample.Criteria criteria = example.createCriteria();
        criteria.andAggLevelIn(aggLevels);
        criteria.andAggMethodIn(aggMethods);
        return mlModelMapper.selectByExample(example);
	    
		
	}
	
	@Transactional
	public int insertNewMlModel(MlModel mlModel) {
		return mlModelMapper.insert(mlModel);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
