package edu.pitt.medschool.model.dao;

import edu.pitt.medschool.model.dto.AggregationDatabase;
import edu.pitt.medschool.model.dto.AggregationDatabaseExample;
import edu.pitt.medschool.model.dto.AggregationDatabaseWithBLOBs;
import edu.pitt.medschool.model.mapper.AggregationDatabaseMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.script.ScriptEngine;
import java.util.List;

@Repository
public class AggregationDao {

    @Autowired
    AggregationDatabaseMapper aggregationDatabaseMapper;

    public int updatePatientFinishedNum(Integer id, int i) {
        AggregationDatabaseWithBLOBs e = new AggregationDatabaseWithBLOBs();
        e.setId(id);
        e.setFinished(i);
        return aggregationDatabaseMapper.updateByPrimaryKeySelective(e);
    }

    public List<AggregationDatabase> selectAllAvailableDBs() {
        AggregationDatabaseExample example = new AggregationDatabaseExample();
        AggregationDatabaseExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo("success");
        return aggregationDatabaseMapper.selectByExample(example);
    }
    
    public List<AggregationDatabase> selectAllUsefulDBs(Integer period, Integer origin, Integer duration,
    		String max, String min, String mean, String median, String std, String fq, String tq, String sum) {
    	AggregationDatabaseExample example = new AggregationDatabaseExample();
        AggregationDatabaseExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo("success");
        criteria.andAggregateTimeLessThanOrEqualTo(period);
        criteria.anddAggregateTimeAliquot(period);
        if(origin != 0) {
        	criteria.anddAggregateTimeAliquot(origin);
        }
        if(duration != 0) {
        	criteria.andAggregateTimeLessThanOrEqualTo(duration);
        }
        if(max.equals("true")) {
        	criteria.andMaxEqualTo("1");
        }
        if(min.equals("true")) {
        	criteria.andMinEqualTo("1");
        }
        if(mean.equals("true")) {
        	criteria.andMeanEqualTo("1");
        }
        if(median.equals("true")) {
        	criteria.andMedianEqualTo("1");
        }
        if(fq.equals("true")) {
        	criteria.andQ1EqualTo("1");
        }
        if(tq.equals("true")) {
        	criteria.andQ3EqualTo("1");
        }
        if(sum.equals("true")) {
        	criteria.andSumEqualTo("1");
        }
        // If need to export standard deviation, the only databases useful is base data
        if(std.equals("true")) {
        	criteria.andDbNameEqualTo("data");
        }
        return aggregationDatabaseMapper.selectByExample(example);
    }

    public int setNewDB(AggregationDatabaseWithBLOBs database) {
        return aggregationDatabaseMapper.insert(database);
    }

    public List<AggregationDatabase> selectOngoing() {
        AggregationDatabaseExample example = new AggregationDatabaseExample();
        AggregationDatabaseExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo("processing");
        return aggregationDatabaseMapper.selectByExample(example);
    }

    public AggregationDatabaseWithBLOBs selectByPrimaryKey(int id) {
        return aggregationDatabaseMapper.selectByPrimaryKey(id);
    }

    public int updateStatus(int id, String status) {
        AggregationDatabaseWithBLOBs e = new AggregationDatabaseWithBLOBs();
        e.setId(id);
        e.setStatus(status);
        return aggregationDatabaseMapper.updateByPrimaryKeySelective(e);
    }

    public int updateTotalnumber(int id,int size) {
        AggregationDatabaseWithBLOBs e = new AggregationDatabaseWithBLOBs();
        e.setId(id);
        e.setTotal(size);
        return aggregationDatabaseMapper.updateByPrimaryKeySelective(e);
    }

    public int updateTimeCost(int id, String time){
        AggregationDatabaseWithBLOBs e = new AggregationDatabaseWithBLOBs();
        e.setId(id);
        e.setTimeCost(time);
        return aggregationDatabaseMapper.updateByPrimaryKeySelective(e);
    }

    public List<AggregationDatabase> selectByname(String dbname) {
        AggregationDatabaseExample example = new AggregationDatabaseExample();
        AggregationDatabaseExample.Criteria criteria = example.createCriteria();
        criteria.andDbNameEqualTo(dbname);
        return aggregationDatabaseMapper.selectByExample(example);
    }

    public int updateAggretaionMethods(AggregationDatabaseWithBLOBs db) {
        return aggregationDatabaseMapper.updateByPrimaryKeySelective(db);
    }

    public int selectJobIdByName(String dbname) {
        AggregationDatabaseExample example = new AggregationDatabaseExample();
        AggregationDatabaseExample.Criteria criteria = example.createCriteria();
        criteria.andDbNameEqualTo(dbname);
        return aggregationDatabaseMapper.selectByExample(example).get(0).getId();
    }



    //todo: add version to db name
    //public String getInfluxDBName()
}
