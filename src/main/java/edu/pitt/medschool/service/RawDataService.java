package edu.pitt.medschool.service;

import org.influxdb.InfluxDB;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.springframework.stereotype.Service;

import edu.pitt.medschool.config.DBConfiguration;
import edu.pitt.medschool.config.InfluxappConfig;
import edu.pitt.medschool.framework.util.InfluxUtil;
import edu.pitt.medschool.framework.util.Util;
import edu.pitt.medschool.model.TSData.RawData;

import java.text.ParseException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Isolachine
 */
@Service
public class RawDataService {

    private final InfluxDB influxDB = InfluxappConfig.INFLUX_DB;
    private final String dbDataName = DBConfiguration.Data.DBNAME;

    /**
     * Return the first available data for a patient
     *
     * @return Time
     */
    public Instant GetFirstAvailData(String pid, boolean hasAr) {
        String qT = "SELECT \"time\",\"Time\" FROM \"%s\" WHERE \"arType\"='%s' ORDER BY \"time\" ASC LIMIT 1";
        return availDataTimeQ(qT, pid.toUpperCase(), hasAr);
    }

    /**
     * Return the last available data for a patient
     *
     * @return Time
     */
    public Instant GetLastAvailData(String pid, boolean hasAr) {
        String qT = "SELECT \"time\",\"Time\" FROM \"%s\" WHERE \"arType\"='%s' ORDER BY \"time\" DESC LIMIT 1";
        return availDataTimeQ(qT, pid.toUpperCase(), hasAr);
    }

    public List<RawData> selectAllRawDataInColumns(String patientTable, List<String> columnNames) throws ParseException {
        String columns = String.join(", ", columnNames);
        String queryString = "Select " + columns + " from \"" + patientTable + "\"";
        Query q = new Query(queryString, dbDataName);
        QueryResult result = influxDB.query(q);

        List<RawData> data = new ArrayList<>();
        if (!result.hasError() && !result.getResults().get(0).hasError()) {
            for (List<Object> res : result.getResults().get(0).getSeries().get(0).getValues()) {
                RawData aRow = new RawData();
                aRow.setTime(Instant.ofEpochMilli(Util.dateTimeFormatToTimestamp(res.get(0).toString(), "yyyy-MM-dd'T'HH:mm:ss'Z'", null)));
                aRow.setColumnNames(columnNames);
                List<Double> values = new ArrayList<>();
                for (int i = 1; i < res.size(); i++) {
                    values.add(Double.valueOf(res.get(i).toString()));
                }
                aRow.setValues(values);

                data.add(aRow);
            }

        }
        return data;
    }

    private Instant availDataTimeQ(String qT, String pid, boolean hasAr) {
        Query q = new Query(String.format(qT, pid, hasAr ? "ar" : "noar"), dbDataName);
        Map<String, List<Object>> res = InfluxUtil.QueryResultToKV(influxDB.query(q));

        // Table does not exist
        if (res.size() == 0) return null;
        return Instant.parse(res.get("time").get(0).toString());
    }

    public static void main(String[] args) throws ParseException {
        RawDataService rawDataService = new RawDataService();

        Instant a;
        a = rawDataService.GetFirstAvailData("PUH-2010-087", true);
        a = rawDataService.GetLastAvailData("PUH-2010-087", true);

        List<String> list = new ArrayList<>();
        list.add("I1_1");
        list.add("I1_2");
        List<RawData> news = rawDataService.selectAllRawDataInColumns("PUH-2010-014", list);

        for (int i = 0; i < news.size(); i++) {
            System.out.print(news.get(i).getTime().getEpochSecond());
            System.out.println(" :  " + news.get(i).getValues().get(0));
        }
    }
}
