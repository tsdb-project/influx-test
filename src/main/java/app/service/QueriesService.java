package app.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.springframework.stereotype.Service;

import app.common.DBConfiguration;
import app.common.InfluxappConfig;
import app.model.QueryResultBean;
import app.model.TimeSpan;
import app.util.InfluxUtil;

/**
 * Query related services
 */
@Service
public class QueriesService {

    private final InfluxDB influxDB = InfluxDBFactory.connect(InfluxappConfig.IFX_ADDR, InfluxappConfig.IFX_USERNAME, InfluxappConfig.IFX_PASSWD);
    private final PatientMetadataService patientMetadataService = new PatientMetadataService();

    private final String dbName = DBConfiguration.Data.DBNAME;

    public static void main(String[] args) {
        QueriesService qs = new QueriesService();
        List<QueryResultBean> a = qs.TypeAQuery("I100_1", 5, 10, null, null);
        List<QueryResultBean> b = qs.TypeBQuery("I100_1", "I101_1", 3, 5, null, null);
    }

    /**
     * Type A query
     *
     * @param colX       column X (eg. I10_1)
     * @param thrVal     threshold value Y (eg. 80)
     * @param thrSec     consecutive threshold Z (eg. 10)
     * @param customPids List for custom PIDs (Null for imported PIDs)
     * @param customAr   List for corresponding Ar/NoAr (Null for not customizing, 0: Only Ar, 1: Only NoAr, 2: Both)
     * @return Return a list of patients
     */
    public List<QueryResultBean> TypeAQuery(String colX, double thrVal, int thrSec, List<String> customPids, List<Integer> customAr) {
        String queryDesc = "Find all patients where values in column X exceed value Y in at least Z consecutive records in the first 8 hours of available data.";
        List<QueryResultBean> finalRes = new ArrayList<>();
        String template = "SELECT * FROM (SELECT COUNT(%s) AS c FROM \"%s\" WHERE %s > %f AND \"arType\"='%s' GROUP BY TIME(%ds)) WHERE c = %d";

        List<String> targetPid = generateTargetPid(customPids);

        if (customAr != null && customAr.size() != targetPid.size()) {
            throw new RuntimeException("Ar/NoAr and PID List not match.");
        }

        for (String pid : targetPid) {
            // TODO: AR or NoAR based on customAr
            String finalQ = String.format(template, colX, pid, colX, thrVal, "ar", thrSec, thrSec);
            QueryResultBean ar = checkOnePatientA(finalQ, pid, queryDesc, thrSec, true);

            finalQ = String.format(template, colX, pid, colX, thrVal, "noar", thrSec, thrSec);
            QueryResultBean noar = checkOnePatientA(finalQ, pid, queryDesc, thrSec, false);

            if (noar != null)
                finalRes.add(noar);
            if (ar != null)
                finalRes.add(ar);
        }

        return finalRes;
    }

    /**
     * Type B query
     *
     * @param colA     column X (eg. I10_1)
     * @param colB     column Y (eg. I11_1)
     * @param valDiff  difference tolerance Z, in % form (eg. 3)
     * @param hEp      hourly epochs Q (eg. 5)
     * @param customAr List for corresponding Ar/NoAr (Null for not customizing, 0: Only Ar, 1: Only NoAr, 2: Both)
     * @return Return a list of patients
     */
    public List<QueryResultBean> TypeBQuery(String colA, String colB, double valDiff, int hEp, List<String> customPids, List<Integer> customAr) {
        String queryDesc = "Find all patients where the hourly mean values in column X and column Y differ by at least Z% for at least Q hourly epochs.";
        List<QueryResultBean> finalRes = new ArrayList<>();
        String template = "SELECT * FROM (SELECT COUNT(diff) AS c FROM (" +
                "SELECT * FROM (SELECT (MEAN(%s) - MEAN(%s)) / MEAN(%s) AS diff FROM \"%s\" WHERE \"arType\"='%s' GROUP BY TIME(1h)) " +
                "WHERE diff > %f OR diff < - %f) GROUP BY TIME(%dh)) WHERE c = %d";

        List<String> targetPid = generateTargetPid(customPids);

        if (customAr != null && customAr.size() != targetPid.size()) {
            throw new RuntimeException("Ar/NoAr and PID List not match.");
        }

        valDiff /= 100;
        for (String pid : targetPid) {
            // TODO: AR or NoAR based on customAr
            String tableName = pid;
            String finalQ = String.format(template, colA, colB, colA, tableName, "ar", valDiff, valDiff, hEp, hEp);
            QueryResultBean ar = checkOnePatientB(finalQ, pid, queryDesc, hEp, true);

            tableName = pid;
            finalQ = String.format(template, colA, colB, colA, tableName, "noar", valDiff, valDiff, hEp, hEp);
            QueryResultBean noar = checkOnePatientB(finalQ, pid, queryDesc, hEp, false);

            if (noar != null)
                finalRes.add(noar);
            if (ar != null)
                finalRes.add(ar);
        }

        return finalRes;
    }

    /**
     * Run a assembled query on one data table(patient)
     *
     * @param queryString Assembled query string
     * @param pid         PID
     * @param queryN      Query Nickname
     * @param thrSec      Threshold cont. seconds
     * @param isAr        AR status
     * @return Query execuation results
     */
    private QueryResultBean checkOnePatientA(String queryString, String pid, String queryN, int thrSec, boolean isAr) {
        Query q = new Query(queryString, dbName);
        Map<String, List<Object>> res = InfluxUtil.QueryResultToKV(influxDB.query(q));

        // This patient doesn't need to be included.
        if (res.size() == 0)
            return null;

        QueryResultBean qrb = new QueryResultBean();
        qrb.setInterestPatient(patientMetadataService.GetById(pid.toUpperCase()).get(0));
        qrb.setQueryNickname(queryN);
        qrb.setAR(isAr);

        List<Object> occTime = res.get("time");
        qrb.setOccurTimes(occTime.size());

        // Do a type convert (Object -> Instant)
        List<TimeSpan> occTimes = new ArrayList<>(qrb.getOccurTimes());
        for (Object s : occTime) {
            TimeSpan timeSpan = new TimeSpan();
            timeSpan.setStart(Instant.parse((String) s));
            timeSpan.setEnd(Instant.parse((String) s).plusSeconds(thrSec));
            occTimes.add(timeSpan);
        }
        qrb.setOccurTime(occTimes);

        return qrb;
    }

    private QueryResultBean checkOnePatientB(String queryString, String pid, String queryN, int he, boolean isAr) {
        Query q = new Query(queryString, dbName);
        Map<String, List<Object>> res = InfluxUtil.QueryResultToKV(influxDB.query(q));

        // This patient doesn't need to be included.
        if (res.size() == 0)
            return null;

        QueryResultBean qrb = new QueryResultBean();
        qrb.setInterestPatient(patientMetadataService.GetById(pid.toUpperCase()).get(0));
        qrb.setQueryNickname(queryN);
        qrb.setAR(isAr);

        List<Object> occTime = res.get("time");
        qrb.setOccurTimes(occTime.size());

        List<TimeSpan> occTimes = new ArrayList<>(occTime.size());
        for (Object s : occTime) {
            TimeSpan timeSpan = new TimeSpan();
            timeSpan.setStart(Instant.parse((String) s));
            timeSpan.setEnd(Instant.parse((String) s).plusSeconds(he * 3600));
            occTimes.add(timeSpan);
        }
        qrb.setOccurTime(occTimes);

        return qrb;
    }

    private List<String> generateTargetPid(List<String> customPids) {
        List<String> targetPid;
        if (customPids == null) {
            targetPid = getCurrentPatientList();
        } else {
            targetPid = customPids;
        }
        return targetPid;
    }

    private List<String> getCurrentPatientList() {
        // Focus on tables that have been imported
        return InfluxUtil.getAllTables(dbName);
    }
}
