/**
 *
 */
package app.service;

import java.text.ParseException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.springframework.stereotype.Service;

import app.common.InfluxappConfig;
import app.common.Measurement;
import app.model.RawData;
import app.util.Util;

/**
 * @author Isolachine
 */
@Service
public class RawDataService {
    public List<RawData> selectAllRawDataInColumns(String patientTable, List<String> columnNames) throws ParseException {
        String columns = String.join(", ", columnNames);
        String queryString = "Select " + columns + " from \"" + Measurement.DATA_PREFIX + patientTable + "_ar\"";
        Query q = new Query(queryString, InfluxappConfig.IFX_DBNAME);
        QueryResult result = InfluxappConfig.INFLUX_DB.query(q);

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

    public static void main(String[] args) throws ParseException {
        RawDataService rawDataService = new RawDataService();
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
