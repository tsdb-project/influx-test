package edu.pitt.medschool.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.opencsv.CSVWriter;

import edu.pitt.medschool.config.DBConfiguration;
import edu.pitt.medschool.config.InfluxappConfig;
import edu.pitt.medschool.model.dao.DownsampleDao;
import edu.pitt.medschool.model.dto.Downsample;
import edu.pitt.medschool.model.dto.DownsampleGroup;
import edu.pitt.medschool.model.dto.DownsampleGroupColumn;

/**
 * Export functions
 */
@Service
public class AnalysisService {
    @Autowired
    DownsampleDao downsampleDao;

    /*
     * Be able to restrict the epochs for which data are exported (e.g. specify to export up to the first 36 hours of available data, but truncate data thereafter). Be able to specify which columns are exported (e.g.
     * I10_*, I10_2 only, all data, etc) Be able to export down sampled data (e.g. hourly mean, median, variance, etc)
     */

    private InfluxDB influxDB = InfluxDBFactory.connect(InfluxappConfig.IFX_ADDR, InfluxappConfig.IFX_USERNAME, InfluxappConfig.IFX_PASSWD);

    private final static String dbName = DBConfiguration.Data.DBNAME;
    private final static String DIRECTORY = "/tsdb/output/";

    public void exportFromPatientsWithDownsampling(List<String> patients, String column, String method, String interval, String time) throws IOException {
        File dir = new File(DIRECTORY + LocalDateTime.now().toString());
        if (!dir.exists()) {
            try {
                dir.mkdirs();
            } catch (SecurityException se) {
                System.out.println("Failed to create dir \"/results\"");
            }
        }

        for (String patientId : patients) {
            String queryString = "SELECT " + method + "(\"" + column + "\")" + " FROM \"" + patientId + "\" ";
            boolean timeGiven = (interval == null || interval.isEmpty());
            // if (timeGiven) {
            String lastRecordTimeQuery = "select \"I1_1\" from \"" + patientId + "\" order by desc limit 1";
            QueryResult recordResult = influxDB.query(new Query(lastRecordTimeQuery, dbName));
            String lastRecordTime = recordResult.getResults().get(0).getSeries().get(0).getValues().get(0).get(0).toString();
            queryString += "WHERE time <= '" + lastRecordTime + "' ";
            // }
            queryString += "GROUP BY time(" + interval + "s) ";
            if (!timeGiven) {
                queryString += "LIMIT " + (Integer.valueOf(time) * 3600 / Integer.valueOf(interval));
            }
            System.out.println(queryString);
            Query query = new Query(queryString, dbName);
            QueryResult result = influxDB.query(query);
            System.out.println(result);

            System.out.println(dir.getAbsolutePath() + '/');
            CSVWriter writer = new CSVWriter(new FileWriter(dir.getAbsolutePath() + '/' + patientId + ".csv"));
            Object[] columns = result.getResults().get(0).getSeries().get(0).getColumns().toArray();
            String[] entries = Arrays.asList(columns).toArray(new String[columns.length]);
            writer.writeNext(entries);

            List<List<Object>> res = result.getResults().get(0).getSeries().get(0).getValues();
            for (List<Object> values : res) {
                String[] vals = new String[values.size()];
                for (int i = 0; i < values.size(); i++) {
                    if (values.get(i) == null) {
                        vals[i] = "N/A";
                    } else {
                        vals[i] = values.get(i).toString();
                    }
                }
                writer.writeNext(vals);
            }
            writer.close();
        }
    }

    public int insert(Downsample downsample) throws Exception {
        return downsampleDao.insert(downsample);
    }

    public List<Downsample> selectAll() {
        return downsampleDao.selectAll();
    }

    public Downsample selectByPrimaryKey(int id) {
        return downsampleDao.selectByPrimaryKey(id);
    }

    public int insertAggregationGroup(Downsample query, DownsampleGroup group, List<DownsampleGroupColumn> columns) {
        // TODO: Implementation of method
        // should be transactional, a transactional example can be found in DownsampleDao.java, or at
        // https://spring.io/guides/gs/managing-transactions/
        return 0;
    }
    
    public int insertMetaFilter(Downsample query, String key, String value) {
        // TODO: Implementation of method
        // should be transactional
        return 0;
    }

    public static void main(String[] args) {

    }

}
