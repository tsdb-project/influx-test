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
import edu.pitt.medschool.model.dao.FeatureDao;

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

    public List<String> selectElectrodesByMeasures(List<String> measures) {
        String measure = measures.get(0);

        List<String> list = new ArrayList<>();
        if (electrodeMeasures.contains(measures.get(0))) {
            String intervalsSql = String
                    .format("SELECT MIN(SID) AS avMin, MAX(SID) AS avMax FROM " + "(SELECT CONVERT(SUBSTRING(SID, 2), SIGNED INTEGER) AS SID "
                            + "FROM feature f WHERE f.type = '%s' AND f.electrode LIKE '%%-Av17') AS T", measure);
            Map<String, Object> map = jdbcTemplate.queryForMap(intervalsSql);
            list.add("* [X-Av] I" + map.get("avMin") + " ~ I" + map.get("avMax"));

            intervalsSql = String
                    .format("SELECT MIN(SID) AS avMin, MAX(SID) AS avMax FROM " + "(SELECT CONVERT(SUBSTRING(SID, 2), SIGNED INTEGER) AS SID "
                            + "FROM feature f WHERE f.type = '%s' AND f.electrode LIKE '%%-%%' AND f.electrode NOT LIKE '%%-Av17') AS T", measure);
            map = jdbcTemplate.queryForMap(intervalsSql);
            list.add("* [Bipolar] I" + map.get("avMin") + " ~ I" + map.get("avMax"));
        }
        for (int i = 0; i < measures.size(); i++) {
            measures.set(i, "'" + measures.get(i) + "'");
        }
        String sql = String.format("SELECT electrode FROM feature f WHERE f.type IN ('%s')", measure);

        list.addAll(jdbcTemplate.queryForList(sql, String.class));
        return list;
    }

    public List<String> selectColumnsByMeasuresAndElectrodes(List<String> measures, List<String> electrodes) {
        List<String> result = new ArrayList<>();
        String electrode = electrodes.get(0);
        if (electrode.startsWith("* ")) {
            String[] components = electrode.split(" ");
            String countSql = String.format("SELECT SID_Count FROM feature f WHERE f.SID = '%s';", components[2]);
            int count = jdbcTemplate.queryForObject(countSql, Integer.class);
            for (int i = 1; i <= count; i++) {
                result.add("Ix_" + i);
            }
        } else {
            for (int i = 0; i < electrodes.size(); i++) {
                electrodes.set(i, "'" + electrodes.get(i) + "'");
            }
            String electrodeString = String.join(",", electrodes);
            String measureString = String.join(",", measures);
            String sql = String.format("SELECT SID, SID_count FROM feature f WHERE f.type = '%s' AND f.electrode IN (%s)", measureString,
                    electrodeString);
            System.out.println(sql);

            Map<String, Object> map = jdbcTemplate.queryForMap(sql);
            for (int i = 1; i <= (int) map.get("SID_count"); i++) {
                result.add(map.get("SID").toString() + "_" + i);
            }
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
