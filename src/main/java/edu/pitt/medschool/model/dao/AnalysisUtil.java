package edu.pitt.medschool.model.dao;

import edu.pitt.medschool.config.DBConfiguration;
import edu.pitt.medschool.framework.influxdb.ResultTable;
import edu.pitt.medschool.model.DataTimeSpanBean;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.slf4j.Logger;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static edu.pitt.medschool.framework.influxdb.InfluxUtil.justQueryData;

/**
 * Logic regarding exports
 */
public class AnalysisUtil {
    private final static String dbName = DBConfiguration.Data.DBNAME;

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
            String template = "SELECT time,Time FROM \"" + pid + "\" WHERE fileUUID = '%s' ORDER BY time %s LIMIT 1 tz('America/New_York');";
            template += "SELECT time,Time FROM \"" + pid + "\" WHERE fileUUID = '%s' ORDER BY time %s LIMIT 1 tz('America/New_York');";
            template += "show tag values from \"" + pid + "\" with key = arType where fileUUID = '%s';";
            template += "SELECT count(Time) FROM \"" + pid + "\" WHERE fileUUID = '%s';";

            String query = String.format(template, uuid, "ASC", uuid, "DESC", uuid, uuid);
            logger.info(query);
            DataTimeSpanBean dts = new DataTimeSpanBean();
            ResultTable[] table = justQueryData(i, true,query);

            Instant start = Instant.parse(((String)table[0].getDataByColAndRow(0, 0)).substring(0,19)+"Z"),
                    end = Instant.parse(((String)table[1].getDataByColAndRow(0, 0)).substring(0,19)+"Z");
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

    // get the start time eliminate first 30 rows
    public static String getPatientStartTime(InfluxDB i, Logger logger, String patientId, Boolean ar){
        String artype = ar? "ar" : "noar";
        logger.debug("<" + patientId + "> STARTED PROCESSING ");
        String firstRecordTimeQuery = "select \"I3_1\" from \"" + patientId
                + "\" where arType = \'" +artype+ "\' limit 1 offset 30 tz('America/New_York')";
        QueryResult recordResult = i.query(new Query(firstRecordTimeQuery, dbName));
        logger.info(firstRecordTimeQuery);
        String startTime = recordResult.getResults().get(0).getSeries().get(0).getValues().get(0).get(0).toString();
        logger.info(startTime);
        return startTime;
    }

}
