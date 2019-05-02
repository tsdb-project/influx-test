/**
 * 
 */
package edu.pitt.medschool.model.dao;

import java.util.ArrayList;
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
        return featureMapper.selectAllMeasures();
    }

    public List<ColumnVO> selectColumnVOsBySet(String electrode) {
        return featureMapper.selectColumnVOsBySet(electrode);
    }

    public List<Feature> selectByMeasure(String measure) {
        if (measure.equals("Electrode Signal Quality")) {
            return featureMapper.selectByMeasureElectrodeSignalQuality();
        }
        FeatureExample example = new FeatureExample();
        example.createCriteria().andTypeEqualTo(measure);
        return featureMapper.selectByExample(example);
    }

    public List<String> selectColumnsByAggregationGroupColumns(ColumnJSON json) {
        if (json.getType().equals("Electrode Signal Quality")) {
            String electrodes = Util.wrapAndConcatStringList("'", ", ", json.getElectrodes());
            return featureMapper.selectColumnVOsByElectrodeSignalQualityElectrodes(electrodes);
        }
        if (json.getType().equals("Asymmetry EASI/REASI")) {
            String notes = Util.wrapAndConcatStringList("'", ", ", json.getColumns());
            return featureMapper.selectColumnVOsByElectrodeAsymmetry(notes, json.getElectrodes().get(0));
        }

        String colString = Util.wrapAndConcatStringList("'", ", ", json.getColumns());
        String elecString;
        boolean querySid;
        if (json.getElectrodes().size() == 1 && json.getElectrodes().get(0).startsWith("*")) {
            String[] components = json.getElectrodes().get(0).split(" ");
            List<String> sids = new ArrayList<>();
            for (int i = Integer.parseInt(components[2].substring(1, components[2].length())); i <= Integer
                    .valueOf(components[4].substring(1, components[4].length())); i++) {
                sids.add("I" + i);
            }
            elecString = Util.wrapAndConcatStringList("'", ", ", sids);
            querySid = true;
        } else {
            elecString = Util.wrapAndConcatStringList("'", ", ", json.getElectrodes());
            querySid = false;
        }
        return featureMapper.selectColumnsByAggregationGroupColumns(json.getType(), elecString, colString, querySid);
    }

    public List<String> selectPredefinedKVAsym() {
        return featureMapper.selectPredefinedKVAsym();
    }

    public List<ColumnVO> selectColumnVOsAsymmetry(String electrode) {
        return featureMapper.selectColumnVOsAsymmetry(electrode);
    }

    public List<String> selectAllColumnCodes() {
        FeatureExample example = new FeatureExample();
        example.createCriteria().andSidNotEqualTo("I270");
        List<Feature> features = featureMapper.selectByExample(example);
        List<String> cols = new ArrayList<>();
        for (Feature f : features) {
            for (int i = 1; i <= f.getSidCount(); i++) {
                cols.add(f.getSid() + '_' + i);
            }
        }
        return cols;
    }
}
