package app.service;

import app.common.DBConfiguration;
import app.common.InfluxappConfig;
import app.model.Patient;
import app.model.QueryResultBean;
import app.model.TimeSpan;
import app.util.InfluxUtil;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.impl.InfluxDBResultMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Query related services
 */
@Service
public class QueriesService {

    private final InfluxDB influxDB = InfluxDBFactory.connect(InfluxappConfig.IFX_ADDR, InfluxappConfig.IFX_USERNAME, InfluxappConfig.IFX_PASSWD);
    private final InfluxDBResultMapper resultMapper = new InfluxDBResultMapper();

    private final PatientFilteringService patientS = new PatientFilteringService();
    private final CsvFileService csvFileS = new CsvFileService();

    private final String dbName = DBConfiguration.Data.DBNAME;

    // TODO: Patient AR or Not AR
    private final List<String> patientList;
    private final List<Integer> arStatus;

    private List<Object> getColNames(String tableName) {
        Query q = new Query("SHOW FIELD KEYS FROM \"" + tableName + "\"", dbName);
        Map<String, List<Object>> res = InfluxUtil.QueryResultToKV(influxDB.query(q));
        return res.get("fieldKey");
    }

    /**
     * Query all available patients with ar/noar
     */
    public QueriesService() {
        // TODO: Query only interested patients
        List<Patient> all = patientS.SelectAll();
        patientList = new ArrayList<>(all.size());
        arStatus = new ArrayList<>(0);
        for (Patient one : all) {
            String pid = one.getPid();
            patientList.add(pid);
        }
    }

    /**
     * Run a assembled query on one data table(patient)
     *
     * @param queryString Assembled query string
     * @param pid         PID
     * @param queryN      Query Nickname
     * @param thrSec
     * @param isAr        AR status
     * @return Query execuation results
     */
    private QueryResultBean checkOnePatient(String queryString, String pid, String queryN, int thrSec, boolean isAr) {
        //TODO: AR NoAR
        Query q = new Query(queryString, dbName);
        Map<String, List<Object>> res = InfluxUtil.QueryResultToKV(influxDB.query(q));

        // This patient doesn't need to be included.
        if (res.size() == 0)
            return null;

        QueryResultBean qrb = new QueryResultBean();
        qrb.setInterestPatient(patientS.FindById(pid.toUpperCase()).get(0));
        qrb.setQueryNickname(queryN);
        qrb.setAR(isAr);

        List<Object> occTime = res.get("time");
        qrb.setOccurTimes(occTime.size());

        List<TimeSpan> occTimes = new ArrayList<>(occTime.size());
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
        qrb.setInterestPatient(patientS.FindById(pid.toUpperCase()).get(0));
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

    /**
     * Type A query
     *
     * @param colX   column X (eg. I10_1)
     * @param thrVal threshold value Y (eg. 80)
     * @param thrSec consecutive threshold Z (eg. 10)
     * @return Return a list of patients
     */
    public List<QueryResultBean> TypeAQuery(String colX, double thrVal, int thrSec) {
        String queryDesc = "Find all patients where values in column X exceed value Y in at least Z consecutive records in the first 8 hours of available data.";
        List<QueryResultBean> finalRes = new ArrayList<>();
        String template = "SELECT * FROM (SELECT COUNT(%s) AS c FROM \"%s\" WHERE %s > %f GROUP BY TIME(%ds)) WHERE c = %d";

        for (String pid : patientList) {
            // TODO: AR or NoAR?
            String tableName = pid;
            String finalQ = String.format(template, colX, tableName, colX, thrVal, thrSec, thrSec);
            QueryResultBean ar = checkOnePatient(finalQ, pid, queryDesc, thrSec, true);

            finalQ = String.format(template, colX, tableName, colX, thrVal, thrSec, thrSec);
            QueryResultBean noar = checkOnePatient(finalQ, pid, queryDesc, thrSec, false);

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
     * @param colA    column X (eg. I10_1)
     * @param colB    column Y (eg. I11_1)
     * @param valDiff difference tolerance Z, in % form (eg. 3)
     * @param hEp     hourly epochs Q (eg. 5)
     * @return Return a list of patients
     */
    public List<QueryResultBean> TypeBQuery(String colA, String colB, double valDiff, int hEp) {
        String queryDesc = "Find all patients where the hourly mean values in column X and column Y differ by at least Z% for at least Q hourly epochs.";
        List<QueryResultBean> finalRes = new ArrayList<>();
        String template = "SELECT * FROM (SELECT COUNT(diff) AS c FROM (" + "SELECT * FROM (SELECT (MEAN(%s) - MEAN(%s)) / MEAN(%s) AS diff FROM \"%s\" GROUP BY TIME(1h)) " + "WHERE diff > %f OR diff < - %f) GROUP BY TIME(%dh)) WHERE c = %d";

        valDiff /= 100;
        for (String pid : patientList) {
            // TODO: AR or NoAR?
            String tableName = pid;
            String finalQ = String.format(template, colA, colB, colA, tableName, valDiff, valDiff, hEp, hEp);
            QueryResultBean ar = checkOnePatientB(finalQ, pid, queryDesc, hEp, true);

            tableName = pid;
            finalQ = String.format(template, colA, colB, colA, tableName, valDiff, valDiff, hEp, hEp);
            QueryResultBean noar = checkOnePatientB(finalQ, pid, queryDesc, hEp, false);

            if (noar != null)
                finalRes.add(noar);
            if (ar != null)
                finalRes.add(ar);
        }

        return finalRes;
    }

    public static void main(String[] args) {

        QueriesService qs = new QueriesService();
        List<QueryResultBean> a = qs.TypeAQuery("I1_1", 80, 10);
        List<QueryResultBean> b = qs.TypeBQuery("I10_1", "I11_1", 3, 5);

    }
}
