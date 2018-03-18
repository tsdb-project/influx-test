/**
 *
 */
package app.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import app.common.DBConfiguration;
import app.common.InfluxappConfig;

/**
 * service for returning column information of data
 * 
 * @author Isolachine
 */
@Service
public class ColumnService {
    @Autowired
    JdbcTemplate jdbcTemplate;

    public List<String> selectAllMeasures() {
        return jdbcTemplate.queryForList("SELECT DISTINCT(f.type) FROM feature f", String.class);
    }

    public List<String> selectElectrodesByMeasures(List<String> measures) {
        for (int i = 0; i < measures.size(); i++) {
            measures.set(i, "'" + measures.get(i) + "'");
        }
        String measure = String.join(",", measures);
        String sql = String.format("SELECT electrode FROM feature f WHERE f.type IN (%s)", measure);
        return jdbcTemplate.queryForList(sql, String.class);
    }

    public List<String> selectColumnsByMeasuresAndElectrodes(String measures, List<String> electrodes) {
        for (int i = 0; i < electrodes.size(); i++) {
            electrodes.set(i, "'" + electrodes.get(i) + "'");
        }
        String electrodeString = String.join(",", electrodes);
        String sql = String.format("SELECT SID, SID_count FROM feature f WHERE f.type = '%s' AND f.electrode IN (%s)", measures, electrodeString);
        System.out.println(sql);

        Map<String, Object> map = jdbcTemplate.queryForMap(sql);
        List<String> result = new ArrayList<>();
        for (int i = 1; i <= (int) map.get("SID_count"); i++) {
            result.add(map.get("SID").toString() + "_" + i);
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
