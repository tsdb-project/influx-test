/**
 *
 */
package app.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.springframework.stereotype.Service;

import app.common.InfluxappConfig;

/**
 * service for returning column information of data
 * @author Isolachine
 */
@Service
public class ColumnService {
    private InfluxDB influxDB = InfluxDBFactory.connect(InfluxappConfig.IFX_ADDR, InfluxappConfig.IFX_USERNAME, InfluxappConfig.IFX_PASSWD);
    private String tableName = "columns";

    public List<String> selectAllCategory() {
        Query query = new Query("select distinct(\"name\") from " + tableName, InfluxappConfig.IFX_DBNAME);
        QueryResult results = influxDB.query(query);
        List<String> categories = new ArrayList<>();
        for (List<Object> result : results.getResults().get(0).getSeries().get(0).getValues()) {
            categories.add(result.get(1).toString());
        }
        return categories;
    }

    public Map<String, List<String>> selectAllColumnAndCategory() {
        Query query = new Query("select \"name\", \"column\" from " + tableName, InfluxappConfig.IFX_DBNAME);
        QueryResult results = influxDB.query(query);
        Map<String, List<String>> columns = new HashMap<>();
        for (List<Object> result : results.getResults().get(0).getSeries().get(0).getValues()) {
            if (!columns.containsKey(result.get(1).toString())) {
                columns.put(result.get(1).toString(), new ArrayList<String>());
            }
            columns.get(result.get(1).toString()).add(result.get(2).toString());
        }
        return columns;
    }

    public List<String> selectAllColumn() {
        Query query = new Query("select \"name\", \"column\" from " + tableName, InfluxappConfig.IFX_DBNAME);
        QueryResult results = influxDB.query(query);
        List<String> columns = new ArrayList<>();
        for (List<Object> result : results.getResults().get(0).getSeries().get(0).getValues()) {
            columns.add(result.get(2).toString());
        }
        return columns;
    }

    //TODO: What's this for?
    @SuppressWarnings("unused")
    private static void insertColumns() {
        String[] names = {"Artifact Intensity", "Seizure Detections", "Rhythmicity Spectrogram, Left Hemisphere", "Rhythmicity Spectrogram, Right Hemisphere", "FFT Spectrogram, Left Hemisphere", "FFT Spectrogram, Right Hemisphere", "Asymmetry, Relative Spectrogram, Asym Hemi", "Asymmetry, Absolute Index (EASI), 1 - 18 Hz, Asym Hemi", "Asymmetry, Relative Index (REASI)01, 1 - 18 Hz, Asym Hemi", "aEEG, Left Hemisphere", "aEEG, Right Hemisphere", "Suppression Ratio, Left Hemisphere",
                "Suppression Ratio, Right Hemisphere", "Time_Column"};
        int[] columnsNumbers = {4, 1, 97, 97, 40, 40, 34, 1, 1, 5, 5, 1, 1, 1};
        InfluxDB influxDB = InfluxDBFactory.connect(InfluxappConfig.IFX_ADDR, InfluxappConfig.IFX_USERNAME, InfluxappConfig.IFX_PASSWD);
        BatchPoints records = BatchPoints.database(InfluxappConfig.IFX_DBNAME).consistency(InfluxDB.ConsistencyLevel.ALL).build();

        for (int i = 1; i <= names.length; i++) {
            for (int j = 1; j <= columnsNumbers[i - 1]; j++) {
                Point record = Point.measurement("columns").time(System.currentTimeMillis(), TimeUnit.MILLISECONDS).addField("name", names[i - 1]).tag("column", "I" + i + "_" + j).build();
                records.point(record);
            }
            influxDB.write(records);
            records = BatchPoints.database(InfluxappConfig.IFX_DBNAME).consistency(InfluxDB.ConsistencyLevel.ALL).build();
        }
    }

    public static void main(String[] args) {
        ColumnService columnDao = new ColumnService();
        columnDao.selectAllColumn();
    }
}
