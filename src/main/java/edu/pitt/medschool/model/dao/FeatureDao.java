/**
 * 
 */
package edu.pitt.medschool.model.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import edu.pitt.medschool.model.mapper.FeatureMapper;

/**
 * @author Isolachine
 *
 */
@Repository
public class FeatureDao {
    @Autowired
    FeatureMapper featureMapper;

    public List<String> selectAllMeasures() {
        return featureMapper.selectAllMeasures();
    }
}
