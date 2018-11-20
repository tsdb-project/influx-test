package edu.pitt.medschool.model.dao;

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
public class AnalysisUtil {

    /**
     * Total number of patients in database
     */
    public static int numberOfPatientInDatabase(InfluxDB i, Logger logger) {
        logger.info("Getting number of patients...");
        int patientCount = justQueryData(i, true, "SHOW MEASUREMENTS;")[0].getRowCount();
        logger.info("Number of patients is: " + patientCount);
        return patientCount;
    }

    /**
     * Get all the data periods for a patient
     */
    public static List<DataTimeSpanBean> getPatientAllDataSpan(InfluxDB i, Logger logger, String pid) {
        pid = pid.toUpperCase().trim();
        String uuidSearchQuery = "show tag values from \"" + pid + "\" with key = fileUUID";
        ResultTable[] patientUuids = justQueryData(i, true, uuidSearchQuery);
        // No data for this patient
        if (patientUuids.length == 0) return new ArrayList<>(0);

        List<Object> uuids = patientUuids[0].getDatalistByColumnName("value");
        List<DataTimeSpanBean> res = new ArrayList<>(uuids.size());
        for (Object uuid : uuids) {
            // Query 4 at the same time to save some requests
            String template = "SELECT time,Time FROM \"" + pid + "\" WHERE fileUUID = '%s' ORDER BY time %s LIMIT 1; ";
            template += "SELECT time,Time FROM \"" + pid + "\" WHERE fileUUID = '%s' ORDER BY time %s LIMIT 1; ";
            template += "show tag values from \"" + pid + "\" with key = arType where fileUUID = '%s';";
            template += "SELECT count(Time) FROM \"" + pid + "\" WHERE fileUUID = '%s';";

            DataTimeSpanBean dts = new DataTimeSpanBean();
            ResultTable[] table = justQueryData(i, true,
                    String.format(template, uuid, "ASC", uuid, "DESC", uuid, uuid));

            if (table.length < 2) {
                logger.error("DataTimeSpanBean get failed for <{}>, result length <{}> bad.", pid, table.length);
                return null;
            }

            Instant start = Instant.parse((CharSequence) table[0].getDataByColAndRow(0, 0)),
                    end = Instant.parse((CharSequence) table[1].getDataByColAndRow(0, 0));
            List<Object> arType = table[2].getDatalistByColumnName("value");
            long count = Math.round((double) table[3].getDataByColAndRow(1, 0)),
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

        // Cache this object into MySQL if necessary
        return res;
    }

    /**
     * Calc the total valid available time span for a patient
     *
     * @return Milliseconds
     */
    public static long dataValidTotalSpan(List<Integer> good, List<DataTimeSpanBean> dts) {
        long totalTime = 0;
        for (Integer i : good) {
            totalTime += dts.get(i).getDelta();
        }
        return totalTime;
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
