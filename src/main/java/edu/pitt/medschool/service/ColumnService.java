/**
 *
 */
package edu.pitt.medschool.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import edu.pitt.medschool.config.DBConfiguration;
import edu.pitt.medschool.config.InfluxappConfig;
import edu.pitt.medschool.controller.analysis.vo.ColumnVO;
import edu.pitt.medschool.controller.analysis.vo.ElectrodeVO;
import edu.pitt.medschool.model.dao.FeatureDao;
import edu.pitt.medschool.model.dto.Feature;

/**
 * service for returning column information of data
 * 
 * @author Isolachine
 */
@Service
public class ColumnService {
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    FeatureDao featureDao;

    Set<String> electrodeMeasures = new HashSet<>(Arrays.asList(new String[] { "FFT Spectrogram", "aEEG", "PeakEnvelope", "Rhythmicity Spectrogram",
            "Suppression Ratio", "aEEG+ (0.16 - 25Hz) (LFF 1 sec, HFF 25 Hz, custom (off))" }));

    public List<String> selectAllMeasures() {
        return featureDao.selectAllMeasures();
    }

    public ElectrodeVO selectElectrodesByMeasures(List<String> measures) {
        String measure = measures.get(0);

        List<String> predefined = new ArrayList<>();
        if (electrodeMeasures.contains(measures.get(0))) {
            String intervalsSql = String
                    .format("SELECT MIN(SID) AS avMin, MAX(SID) AS avMax FROM " + "(SELECT CONVERT(SUBSTRING(SID, 2), SIGNED INTEGER) AS SID "
                            + "FROM feature f WHERE f.type = '%s' AND f.electrode LIKE '%%-Av17') AS T", measure);
            Map<String, Object> map = jdbcTemplate.queryForMap(intervalsSql);
            String xAv = "* [X-Av] I" + map.get("avMin") + " ~ I" + map.get("avMax");
            predefined.add(xAv);

            intervalsSql = String
                    .format("SELECT MIN(SID) AS avMin, MAX(SID) AS avMax FROM " + "(SELECT CONVERT(SUBSTRING(SID, 2), SIGNED INTEGER) AS SID "
                            + "FROM feature f WHERE f.type = '%s' AND f.electrode LIKE '%%-%%' AND f.electrode NOT LIKE '%%-Av17') AS T", measure);
            map = jdbcTemplate.queryForMap(intervalsSql);
            String bipolar = "* [Bipolar] I" + map.get("avMin") + " ~ I" + map.get("avMax");
            predefined.add(bipolar);
        }

        List<Feature> list = featureDao.selectByMeasure(measure);

        ElectrodeVO electrodeVO = new ElectrodeVO();
        electrodeVO.setPredefined(predefined);
        electrodeVO.setElectrodes(list);
        return electrodeVO;
    }

    public List<ColumnVO> selectColumnsByMeasuresAndElectrodes(List<String> measures, List<String> electrodes) {
        List<ColumnVO> result = new ArrayList<>();
        String electrode = electrodes.get(0);
        if (electrode.startsWith("* ")) {
            String[] components = electrode.split(" ");
            result = featureDao.selectColumnVOsBySet(components[2]);
        } else {
            result = featureDao.selectColumnVOsBySet(electrode);
        }
        return result;
    }

    private InfluxDB influxDB = InfluxDBFactory.connect(InfluxappConfig.IFX_ADDR, InfluxappConfig.IFX_USERNAME, InfluxappConfig.IFX_PASSWD);

    private final static String dbName = DBConfiguration.Data.DBNAME;

    public List<String> selectAllColumn() {
        List<String> columns = new ArrayList<>();
        Query measurements = new Query("show measurements", dbName);
        QueryResult measurementsRes = influxDB.query(measurements);
        if (measurementsRes.getResults().get(0).getSeries() != null) {
            String measurementName = measurementsRes.getResults().get(0).getSeries().get(0).getValues().get(0).get(0).toString();
            Query query = new Query("show field keys from " + '"' + measurementName + '"', dbName);
            QueryResult results = influxDB.query(query);
            if (results.getResults().size() > 0 && results.getResults().get(0).getSeries() != null) {
                for (List<Object> result : results.getResults().get(0).getSeries().get(0).getValues()) {
                    columns.add(result.get(0).toString());
                }
            }
        }

        return columns;
    }

    public static void main(String[] args) {
    }
}
