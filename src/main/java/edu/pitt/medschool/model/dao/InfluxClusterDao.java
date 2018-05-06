/**
 * 
 */
package edu.pitt.medschool.model.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import edu.pitt.medschool.model.dto.InfluxCluster;
import edu.pitt.medschool.model.dto.InfluxClusterExample;
import edu.pitt.medschool.model.mapper.InfluxClusterMapper;

/**
 * @author Isolachine
 *
 */
@Repository
public class InfluxClusterDao {
    @Autowired
    InfluxClusterMapper influxClusterMapper;

    public InfluxCluster selectByMachineId(String machineId) {
        InfluxClusterExample example = new InfluxClusterExample();
        example.createCriteria().andMachineIdEqualTo(machineId);
        List<InfluxCluster> list = influxClusterMapper.selectByExample(example);
        for (InfluxCluster influxCluster : list) {
            return influxCluster;
        }
        return null;
    }
}
