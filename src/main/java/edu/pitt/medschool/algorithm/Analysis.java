package edu.pitt.medschool.algorithm;

import edu.pitt.medschool.framework.influxdb.ResultTable;
import edu.pitt.medschool.model.DataTimeSpan;
import org.influxdb.InfluxDB;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static edu.pitt.medschool.framework.influxdb.InfluxUtil.justQueryData;

/**
 * Logic regarding exports
 */
public class Analysis {

    /**
     * Get all the data periods for a patient
     */
    public static List<DataTimeSpan> getPatientAllDataSpan(InfluxDB i, String pid) {
        pid = pid.toUpperCase().trim();
        String uuidSearchQuery = "show tag values from \"" + pid + "\" with key = fileUUID";
        List<Object> uuids = justQueryData(i, uuidSearchQuery).get(0).getDatalistByColumnName("value");

        List<DataTimeSpan> res = new ArrayList<>(uuids.size());
        for (Object uuid : uuids) {
            // Query 2 at same time to save some requests
            String template = "SELECT time,Time FROM \"" + pid + "\" WHERE fileUUID = '%s' ORDER BY time %s LIMIT 1; ";
            template += "SELECT time,Time FROM \"" + pid + "\" WHERE fileUUID = '%s' ORDER BY time %s LIMIT 1;";

            DataTimeSpan dts = new DataTimeSpan();
            List<ResultTable> table = justQueryData(i, String.format(template, uuid, "ASC", uuid, "DESC"));

            dts.setFileUuid((String) uuid);
            dts.setPid(pid);
            dts.setStart(Instant.parse((CharSequence) table.get(0).getDatalistByColumnName("time").get(0)));
            dts.setEnd(Instant.parse((CharSequence) table.get(1).getDatalistByColumnName("time").get(0)));

            res.add(dts);
        }

        return res;
    }

}
