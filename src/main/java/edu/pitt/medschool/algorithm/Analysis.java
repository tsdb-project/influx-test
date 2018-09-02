package edu.pitt.medschool.algorithm;

import edu.pitt.medschool.framework.influxdb.ResultTable;
import edu.pitt.medschool.model.DataTimeSpanBean;
import org.influxdb.InfluxDB;
import org.slf4j.Logger;

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
    public static List<DataTimeSpanBean> getPatientAllDataSpan(InfluxDB i, Logger logger, String pid) {
        pid = pid.toUpperCase().trim();
        String uuidSearchQuery = "show tag values from \"" + pid + "\" with key = fileUUID";
        List<Object> uuids = justQueryData(i, uuidSearchQuery).get(0).getDatalistByColumnName("value");

        List<DataTimeSpanBean> res = new ArrayList<>(uuids.size());
        for (Object uuid : uuids) {
            // Query 3 at same time to save some requests
            String template = "SELECT time,Time FROM \"" + pid + "\" WHERE fileUUID = '%s' ORDER BY time %s LIMIT 1; ";
            template += "SELECT time,Time FROM \"" + pid + "\" WHERE fileUUID = '%s' ORDER BY time %s LIMIT 1; ";
            template += "show tag values from \"" + pid + "\" with key = arType where fileUUID = '%s';";
            template += "SELECT count(Time) AS C FROM \"" + pid + "\" WHERE fileUUID = '%s';";

            DataTimeSpanBean dts = new DataTimeSpanBean();
            List<ResultTable> table = justQueryData(i, String.format(template, uuid, "ASC", uuid, "DESC", uuid, uuid));

            Instant start = Instant.parse((CharSequence) table.get(0).getDatalistByColumnName("time").get(0)),
                    end = Instant.parse((CharSequence) table.get(1).getDatalistByColumnName("time").get(0));
            List<Object> arType = table.get(2).getDatalistByColumnName("value");
            long count = Math.round((double) table.get(3).getDatalistByColumnName("C").get(0)),
                    timeDelta = end.toEpochMilli() - start.toEpochMilli();

            // Determine Ar/NoAr status
            if (arType.contains("ar") && arType.contains("noar")) {
                if (count % 2 != 0) {
                    String msg = String.format("Ar and NoAr data skewed. ('%s', '%s')", pid, uuid);
                    logger.error(msg);
                }
                dts.setArStat(DataTimeSpanBean.ArStatus.Both);
                count /= 2;
            } else if (arType.contains("ar") && !arType.contains("noar")) {
                dts.setArStat(DataTimeSpanBean.ArStatus.ArOnly);
            } else if (!arType.contains("ar") && arType.contains("noar")) {
                dts.setArStat(DataTimeSpanBean.ArStatus.NoArOnly);
            } else throw new RuntimeException("Ar type fatal error!");

            dts.setEffectiveDataCount(count);
            dts.setFileUuid((String) uuid);
            dts.setPid(pid);
            dts.setStart(start);
            dts.setEnd(end);
            dts.setDelta(timeDelta);
            dts.setEffectiveDataPerSecond(count / (1.0 * timeDelta / 1000));

            res.add(dts);
        }

        // Cache this res if necessary
        return res;
    }

    /**
     * Calc the total available time span (for a patient)
     *
     * @return Milliseconds
     */
    public static long dataTotalSpan(List<DataTimeSpanBean> dts) {
        long totalTime = 0;
        for (DataTimeSpanBean d : dts) totalTime += d.getDelta();
        return totalTime;
    }

}
