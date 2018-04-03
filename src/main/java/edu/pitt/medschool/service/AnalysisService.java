package edu.pitt.medschool.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opencsv.CSVWriter;

import edu.pitt.medschool.config.DBConfiguration;
import edu.pitt.medschool.config.InfluxappConfig;
import edu.pitt.medschool.controller.analysis.vo.DownsampleGroupVO;
import edu.pitt.medschool.model.dao.DownsampleDao;
import edu.pitt.medschool.model.dao.DownsampleGroupAggrDao;
import edu.pitt.medschool.model.dao.DownsampleGroupColumnDao;
import edu.pitt.medschool.model.dao.DownsampleGroupDao;
import edu.pitt.medschool.model.dao.DownsampleMetaDao;
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
    @Autowired
    DownsampleGroupDao downsampleGroupDao;
    @Autowired
    DownsampleGroupColumnDao downsampleGroupColumnDao;

    @Autowired
    DownsampleMetaDao downsampleMetaDao;

    @Autowired
    DownsampleGroupAggrDao downsampleGroupAggrDao;

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

    public int insertAggregationGroup(Downsample query, DownsampleGroup group, List<DownsampleGroupColumn> columns) throws Exception {
        // TODO: Implementation of method
        for (DownsampleGroupColumn dgc : columns) {

        }
        return 0;
    }

    public int insertMetaFilter(Downsample query, String key, String value) throws Exception {
        // TODO: Implementation of method
        return 0;
    }

    public static void main(String[] args) {

    }

    public int updateByPrimaryKey(Downsample downsample) {
        return downsampleDao.updateByPrimaryKey(downsample);
    }

    /**
     * @param queryId
     * @return
     */
    public List<DownsampleGroupVO> selectAllAggregationGroupByQueryId(Integer queryId) {
        List<DownsampleGroupVO> groups = downsampleGroupDao.selectAllDownsampleGroupVO(queryId);
        return groups;
    }

    /**
     * @param group
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean insertAggregationGroup(DownsampleGroupVO group) {
        try {
            downsampleGroupDao.insert(group.getGroup());
            int queryGroupId = group.getGroup().getId();
            for (String columnName : group.getColumns()) {
                DownsampleGroupColumn column = new DownsampleGroupColumn();
                column.setQueryGroupId(queryGroupId);
                column.setColumnName(columnName);
                downsampleGroupColumnDao.insert(column);
            }
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
        return true;
    }

    /**
     * @param group
     * @return
     */
    public int updateAggregationGroup(DownsampleGroupVO group) {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * @param pids
     * @param downsample
     * @param downsampleGroups
     * @throws IOException
     */
    public void exportFromPatientsWithDownsamplingGroups(List<String> pids, Downsample downsample, List<DownsampleGroupVO> downsampleGroups) throws IOException {
        File dir = new File(DIRECTORY + LocalDateTime.now().toString());
        if (!dir.exists()) {
            try {
                dir.mkdirs();
            } catch (SecurityException se) {
                System.out.println("Failed to create dir \"/results\"");
            }
        }

        String fields = "mean(\"I1_1\")";
        List<String> fieldList = new ArrayList<>();
        for (DownsampleGroupVO downsampleGroupVO : downsampleGroups) {
            String field = "";// downsampleGroupVO.getGroup().getAggregation();
            for (String column : downsampleGroupVO.getColumns()) {
                field += "mean" + "(\"" + column + "\")" + "+";
            }
            field = field.substring(0, field.length() - 1) + " as \"" + downsampleGroupVO.getGroup().getAggregation() + "(" + String.join(", ", downsampleGroupVO.getColumns()) + ")\"";
            fieldList.add(field);
        }
        fields = String.join(", ", fieldList);

        for (String patientId : pids) {
            String queryString = "SELECT " + fields + " FROM \"" + patientId + "\" ";
            boolean timeGiven = (downsample.getPeriod() == null || downsample.getPeriod() == 0);
            // if (timeGiven) {
            String lastRecordTimeQuery = "select \"I1_1\" from \"" + patientId + "\" order by desc limit 1";
            QueryResult recordResult = influxDB.query(new Query(lastRecordTimeQuery, dbName));
            String lastRecordTime = recordResult.getResults().get(0).getSeries().get(0).getValues().get(0).get(0).toString();
            queryString += "WHERE time <= '" + lastRecordTime + "' ";
            // }
            queryString += "GROUP BY time(" + downsample.getPeriod() + "s) ";
            if (!timeGiven) {
                queryString += "LIMIT " + (Integer.valueOf(downsample.getDuration()) * 3600 / Integer.valueOf(downsample.getPeriod()));
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

}
