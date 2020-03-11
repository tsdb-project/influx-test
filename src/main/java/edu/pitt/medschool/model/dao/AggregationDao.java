package edu.pitt.medschool.model.dao;

import edu.pitt.medschool.model.dto.*;
import edu.pitt.medschool.model.mapper.AggregationDatabaseMapper;
import edu.pitt.medschool.model.mapper.AggregationDbMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.script.ScriptEngine;
import java.util.List;

@Repository
public class AggregationDao {

    @Autowired
    AggregationDatabaseMapper aggregationDatabaseMapper;

    @Autowired
    AggregationDbMapper aggregationDbMapper;

    public int updatePatientFinishedNum(Integer id, int i) {
        AggregationDb e = new AggregationDb();
        e.setId(id);
        e.setFinished(i);
        return aggregationDbMapper.updateByPrimaryKeySelective(e);
    }

    public List<AggregationDb> selectAllAvailableDBs() {
        AggregationDbExample example = new AggregationDbExample();
        AggregationDbExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo("success");
        return aggregationDbMapper.selectByExample(example);
    }
    
    /*
     * Tianfang Ma
     * Navigator in 2019
     * By duration, start time, interval
     * */
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

//    public int setNewDB(AggregationDatabaseWithBLOBs database) {
//        return aggregationDatabaseMapper.insert(database);
//    }

    public int insertNewDB(AggregationDb job){
        return aggregationDbMapper.insert(job);
    }

    public List<AggregationDb> selectOngoing() {
        AggregationDbExample example = new AggregationDbExample();
        AggregationDbExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo("processing");
        return aggregationDbMapper.selectByExample(example);
    }

//    public AggregationDatabaseWithBLOBs selectByPrimaryKey(int id) {
//        return aggregationDatabaseMapper.selectByPrimaryKey(id);
//    }

    public AggregationDb selectByPrimaryKey(int id){
        return aggregationDbMapper.selectByPrimaryKey(id);
    }

    public int updateStatus(int id, String status) {
        AggregationDb e = new AggregationDb();
        e.setId(id);
        e.setStatus(status);
        return aggregationDbMapper.updateByPrimaryKeySelective(e);
    }

    public int updateTotalnumber(int id,int size) {
        AggregationDb e = new AggregationDb();
        e.setId(id);
        e.setTotal(size);
        return aggregationDbMapper.updateByPrimaryKeySelective(e);
    }


    // public int updateTimeCost(int id, String time){
    //     AggregationDatabaseWithBLOBs e = new AggregationDatabaseWithBLOBs();
    //     e.setId(id);
    //     e.setTimeCost(time);
    //     return aggregationDatabaseMapper.updateByPrimaryKeySelective(e);
    // }

    // public List<AggregationDatabase> selectByname(String dbname) {
    //     AggregationDatabaseExample example = new AggregationDatabaseExample();
    //     AggregationDatabaseExample.Criteria criteria = example.createCriteria();
    //     criteria.andDbNameEqualTo(dbname);
    //     return aggregationDatabaseMapper.selectByExample(example);
    // }



    public int updateTimeCost(int id, String time){
        AggregationDb e = new AggregationDb();
        e.setId(id);
        e.setTimeCost(time);
        return aggregationDbMapper.updateByPrimaryKeySelective(e);
    }
    
    // HSX 
    // aggdb change comment function
    public int updateComment(AggregationDb aggregationdb) {
    	AggregationDbExample aggregationDbExample = new AggregationDbExample();
    	AggregationDbExample.Criteria criteria = aggregationDbExample.createCriteria();
        criteria.andIdEqualTo(aggregationdb.getId());
        AggregationDb aggregationDb1 = new AggregationDb();
        aggregationDb1.setComment(aggregationdb.getComment());
        return aggregationDbMapper.updateByExampleSelective(aggregationDb1,aggregationDbExample);
    }


//    public List<AggregationDatabase> selectByname(String dbname) {
//        AggregationDatabaseExample example = new AggregationDatabaseExample();
//        AggregationDatabaseExample.Criteria criteria = example.createCriteria();
//        criteria.andDbNameEqualTo(dbname);
//        return aggregationDatabaseMapper.selectByExample(example);
//    }
//
//    public int updateAggretaionMethods(AggregationDatabaseWithBLOBs db) {
//        return aggregationDatabaseMapper.updateByPrimaryKeySelective(db);
//    }
//
//    public int selectJobIdByName(String dbname) {
//        AggregationDatabaseExample example = new AggregationDatabaseExample();
//        AggregationDatabaseExample.Criteria criteria = example.createCriteria();
//        criteria.andDbNameEqualTo(dbname);
//        return aggregationDatabaseMapper.selectByExample(example).get(0).getId();
//    }




    //todo: add version to db name
    //public String getInfluxDBName()
}
