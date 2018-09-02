/**
 * 
 */
package edu.pitt.medschool.model.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import edu.pitt.medschool.controller.analysis.vo.ColumnJSON;
import edu.pitt.medschool.controller.analysis.vo.ColumnVO;
import edu.pitt.medschool.framework.util.Util;
import edu.pitt.medschool.model.dto.Feature;
import edu.pitt.medschool.model.dto.FeatureExample;
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
        List<String> list = featureMapper.selectAllMeasures();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equals("Time")) {
                list.remove(i);
            }
        }
        return list;
    }

    public List<ColumnVO> selectColumnVOsBySet(String electrode) {
        return featureMapper.selectColumnVOsBySet(electrode);
    }

    public List<Feature> selectByMeasure(String measure) {
        FeatureExample example = new FeatureExample();
        example.createCriteria().andTypeEqualTo(measure);
        return featureMapper.selectByExample(example);
    }

    public List<String> selectColumnsByAggregationGroupColumns(ColumnJSON json) {
        String colString = Util.wrapAndConcatStringList("'", ", ", json.getColumns());
        String elecString = Util.wrapAndConcatStringList("'", ", ", json.getElectrodes());
        return featureMapper.selectColumnsByAggregationGroupColumns(json.getType(), elecString, colString);
    }

}
