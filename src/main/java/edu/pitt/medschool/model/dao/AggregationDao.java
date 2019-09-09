package edu.pitt.medschool.model.dao;

import edu.pitt.medschool.model.dto.AggregationDatabase;
import edu.pitt.medschool.model.dto.AggregationDatabaseExample;
import edu.pitt.medschool.model.dto.AggregationDatabaseWithBLOBs;
import edu.pitt.medschool.model.mapper.AggregationDatabaseMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

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

    //todo: add version to db name
    //public String getInfluxDBName()
}
