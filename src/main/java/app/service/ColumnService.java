/**
 * 
 */
package app.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.springframework.stereotype.Service;

import app.common.InfluxappConfig;

/**
 * @author Isolachine
 *
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

    public static void main(String[] args) {
        ColumnService columnDao = new ColumnService();
        columnDao.selectAllColumn();
    }
}
