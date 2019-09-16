package edu.pitt.medschool.model.dao;

import edu.pitt.medschool.model.dto.AggregationDatabase;
import edu.pitt.medschool.model.mapper.AggregationDatabaseMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AggregationDao {

    @Autowired
    AggregationDatabaseMapper aggregationDatabaseMapper;

//    public List<AggregationDatabase> selectAllAvailableDBs() {
//
//    }
}
