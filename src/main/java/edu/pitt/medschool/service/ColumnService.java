/**
 *
 */
package edu.pitt.medschool.service;

import edu.pitt.medschool.config.DBConfiguration;
import edu.pitt.medschool.controller.analysis.vo.ColumnJSON;
import edu.pitt.medschool.controller.analysis.vo.ColumnVO;
import edu.pitt.medschool.controller.analysis.vo.ElectrodeVO;
import edu.pitt.medschool.framework.influxdb.InfluxUtil;
import edu.pitt.medschool.model.dao.FeatureDao;
import edu.pitt.medschool.model.dto.Feature;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

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

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    Set<String> electrodeMeasures = new HashSet<>(Arrays.asList(new String[]{"FFT Spectrogram", "aEEG", "PeakEnvelope", "Rhythmicity Spectrogram",
            "Suppression Ratio", "aEEG+ (0.16 - 25Hz) (LFF 1 sec, HFF 25 Hz, custom (off))"}));
    Set<String> miscMeasures = new HashSet<>(Arrays.asList(new String[]{"Artifact Intensity", "Seizure Probability"}));

    public List<String> selectAllMeasures() {
        return featureDao.selectAllMeasures();
    }

    public class PredefinedKV extends Object {
        public String key;
        public String value;
    }

    public ElectrodeVO selectElectrodesByMeasures(List<String> measures) {
        String measure = measures.get(0);

        List<PredefinedKV> predefined = new ArrayList<>();
        if (electrodeMeasures.contains(measure)) {
            String intervalsSql = String
                    .format("SELECT MIN(SID) AS avMin, MAX(SID) AS avMax FROM " + "(SELECT CONVERT(SUBSTRING(SID, 2), SIGNED INTEGER) AS SID "
                            + "FROM feature f WHERE f.type = '%s' AND f.electrode LIKE '%%-Av17') AS T", measure);
            logger.debug(intervalsSql);
            Map<String, Object> map = jdbcTemplate.queryForMap(intervalsSql);
            PredefinedKV xAv = new PredefinedKV();
            xAv.key = "* [X-Av] I" + map.get("avMin") + " ~ I" + map.get("avMax");
            xAv.value = xAv.key;
            predefined.add(xAv);

            intervalsSql = String
                    .format("SELECT MIN(SID) AS avMin, MAX(SID) AS avMax FROM " + "(SELECT CONVERT(SUBSTRING(SID, 2), SIGNED INTEGER) AS SID "
                            + "FROM feature f WHERE f.type = '%s' AND f.electrode LIKE '%%-%%' AND f.electrode NOT LIKE '%%-Av17') AS T", measure);
            logger.debug(intervalsSql);
            map = jdbcTemplate.queryForMap(intervalsSql);

            PredefinedKV bipolar = new PredefinedKV();
            bipolar.key = "* [Bipolar] I" + map.get("avMin") + " ~ I" + map.get("avMax");
            bipolar.value = bipolar.key;
            predefined.add(bipolar);
        } else if (miscMeasures.contains(measure)) {
            ElectrodeVO miscVO = new ElectrodeVO();
            PredefinedKV misc = new PredefinedKV();
            misc.key = measure;
            misc.value = measure.equals("Artifact Intensity") ? "I1" : "I3";
            predefined.add(misc);
            miscVO.setPredefined(predefined);
            miscVO.setElectrodes(new ArrayList<>());
            return miscVO;
        } else if (measure.equals("Relative Asymmetry Spectrogram")) {
            ElectrodeVO relativeAsymVO = new ElectrodeVO();
            relativeAsymVO.setElectrodes(new ArrayList<>());
            List<PredefinedKV> list = new ArrayList<>();
            List<Feature> features = featureDao.selectByMeasure(measure);
            for (Feature feature : features) {
                PredefinedKV kv = new PredefinedKV();
                kv.key = feature.getElectrode();
                kv.value = feature.getSid();
                list.add(kv);
            }
            relativeAsymVO.setPredefined(list);
            return relativeAsymVO;
        } else if (measure.equals("Asymmetry EASI/REASI")) {
            ElectrodeVO relativeAsymVO = new ElectrodeVO();
            relativeAsymVO.setElectrodes(new ArrayList<>());
            List<String> elecs = featureDao.selectPredefinedKVAsym();
            List<PredefinedKV> list = new ArrayList<>();
            for (String elec : elecs) {
                PredefinedKV kv = new PredefinedKV();
                kv.key = elec;
                kv.value = elec;
                list.add(kv);
            }
            relativeAsymVO.setPredefined(list);
            return relativeAsymVO;
        }

        List<Feature> list = featureDao.selectByMeasure(measure);

        ElectrodeVO electrodeVO = new ElectrodeVO();
        electrodeVO.setPredefined(predefined);
        electrodeVO.setElectrodes(list);
        return electrodeVO;
    }

    public List<ColumnVO> selectColumnsByMeasuresAndElectrodes(List<String> measures, List<String> electrodes) {
        List<ColumnVO> result = new ArrayList<>();
        if (measures.get(0).equals("Electrode Signal Quality")) {
            String measure = "Electrode Signal Quality";
            ColumnVO columnVO = new ColumnVO();
            columnVO.setColumn(measure);
            columnVO.setRepresentation(measure);
            return Arrays.asList(columnVO);
        }
        if (measures.get(0).equals("Asymmetry EASI/REASI")) {
            String electrode = electrodes.get(0);
            return featureDao.selectColumnVOsAsymmetry(electrode);
        }
        String electrode = electrodes.get(0);
        if (electrode.startsWith("* ")) {
            String[] components = electrode.split(" ");
            result = featureDao.selectColumnVOsBySet(components[2]);
        } else {
            result = featureDao.selectColumnVOsBySet(electrode);
        }
        return result;
    }

    private final static String dbName = DBConfiguration.Data.DBNAME;

    public List<String> selectAllColumn() {
        //TODO: Proper handle
        InfluxDB influxDB = InfluxUtil.generateIdbClient(true, true);
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
        influxDB.close();
        return columns;
    }

    public List<String> selectColumnsByAggregationGroupColumns(ColumnJSON json) {
        return featureDao.selectColumnsByAggregationGroupColumns(json);
    }
}
