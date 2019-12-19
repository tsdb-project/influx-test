package edu.pitt.medschool.model.dao;

import edu.pitt.medschool.model.dto.AggregationDatabase;
import edu.pitt.medschool.model.dto.AggregationDatabaseExample;
import edu.pitt.medschool.model.dto.AggregationDatabaseWithBLOBs;
import edu.pitt.medschool.model.dto.AggregationDb;
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

    public List<AggregationDatabase> selectAllAvailableDBs() {
        AggregationDatabaseExample example = new AggregationDatabaseExample();
        AggregationDatabaseExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo("success");
        return aggregationDatabaseMapper.selectByExample(example);
    }

    public int setNewDB(AggregationDatabaseWithBLOBs database) {
        return aggregationDatabaseMapper.insert(database);
    }

    public int insertNewDB(AggregationDb job){
        return aggregationDbMapper.insert(job);
    }

    public List<AggregationDatabase> selectOngoing() {
        AggregationDatabaseExample example = new AggregationDatabaseExample();
        AggregationDatabaseExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo("processing");
        return aggregationDatabaseMapper.selectByExample(example);
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



    public int updateTimeCost(int id, String time){
        AggregationDb e = new AggregationDb();
        e.setId(id);
        e.setTimeCost(time);
        return aggregationDbMapper.updateByPrimaryKeySelective(e);
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
