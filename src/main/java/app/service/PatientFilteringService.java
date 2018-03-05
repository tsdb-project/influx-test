package app.service;

import java.util.ArrayList;
import java.util.List;

import org.influxdb.InfluxDB;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.springframework.stereotype.Service;

import app.bean.PatientFilterBean;
import app.common.DBConfiguration;
import app.common.InfluxappConfig;
import app.util.InfluxUtil;

/**
 * Filter out patient's PID services
 */
@Service
public class PatientFilteringService {

    private final InfluxDB influxDB = InfluxappConfig.INFLUX_DB;

    private final String dbDataName = DBConfiguration.Data.DBNAME;
    private final String dbMetaName = DBConfiguration.Meta.DBNAME;

    // Must select a non-tag value, or this will not return anything
    private final String patientPidStr = "SELECT \"time\", \"PID\", \"age\" FROM \"" + DBConfiguration.Meta.PATIENT + "\"";

    public static void main(String[] args) {
        PatientFilterBean pfb = new PatientFilterBean();
        pfb.setGenderFilter("M");
        pfb.setAgeLowerFilter(50);
        pfb.setAgeUpperFilter(55);
        pfb.setArrestLocationFilter(1);
        pfb.setSurvivedFilter(0);

        PatientFilteringService pfs = new PatientFilteringService();
        List<String> s;
        s = pfs.GetAllImportedPid();
        s = pfs.FetchResultPid(pfb);
        System.out.println(s);
    }

    /**
     * Get all imported PIDs
     */
    public List<String> GetAllImportedPid() {
        return InfluxUtil.getAllTables(dbDataName);
    }

    /**
     * Fetch results (Only PID) based on current filters
     *
     * @return PID
     */
    public List<String> FetchResultPid(PatientFilterBean pfb) {
        if (pfb.getNumOfFilters() == 0) return GetAllImportedPid();
        Query q = new Query(this.patientPidStr + pfb.getWhereAndFilters(), dbMetaName);
        return generatePidList(influxDB.query(q));
    }

    private List<String> generatePidList(QueryResult qr) {
        List<QueryResult.Series> qrs = qr.getResults().get(0).getSeries();
        if (qrs == null) return new ArrayList<>(0);

        List<List<Object>> vals = qrs.get(0).getValues();
        List<String> res = new ArrayList<>(vals.size());
        for (List<Object> o : vals) {
            res.add((String) o.get(1));
        }

        return res;
    }

}
