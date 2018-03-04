/**
 *
 */
package app.service;

import java.util.ArrayList;
import java.util.List;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.springframework.stereotype.Service;

import app.common.DBConfiguration;
import app.common.InfluxappConfig;

/**
 * service for returning column information of data
 * @author Isolachine
 */
@Service
public class ColumnService {
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
        ColumnService columnDao = new ColumnService();
        columnDao.selectAllColumn();
    }
}
