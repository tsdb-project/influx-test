package edu.pitt.medschool.service;

import edu.pitt.medschool.bean.PatientFilterBean;
import edu.pitt.medschool.config.DBConfiguration;
import edu.pitt.medschool.config.InfluxappConfig;
import edu.pitt.medschool.framework.util.InfluxUtil;
import edu.pitt.medschool.model.Patient;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.impl.InfluxDBResultMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Filter out patient's PID services
 */
@Service
public class PatientFilteringService {

    private final InfluxDB influxDB = InfluxappConfig.INFLUX_DB;

    private final String dbDataName = DBConfiguration.Data.DBNAME;
    private final String dbMetaName = DBConfiguration.Meta.DBNAME;

    private final InfluxDBResultMapper resultMapper = new InfluxDBResultMapper();

    // Must select a non-tag value, or this will not return anything
    private final String patientPidStr = "SELECT \"time\", \"PID\", \"age\" FROM \"" + DBConfiguration.Meta.PATIENT + "\"";
    private final String patientQueryStr = "SELECT \"time\", \"PID\", \"age\", \"Gender\", \"Survived\", \"ArrestLocation\" FROM \"" + DBConfiguration.Meta.PATIENT + "\"";

    public static void main(String[] args) {
        PatientFilterBean pfb = new PatientFilterBean();
        pfb.setGenderFilter("M");
        // pfb.setAgeLowerFilter(50);
        // pfb.setAgeUpperFilter(55);
        // pfb.setArrestLocationFilter(1);
        // pfb.setSurvivedFilter(0);

        PatientFilteringService pfs = new PatientFilteringService();
        List<String> s;
        s = pfs.GetAllImportedPid();
        s = pfs.FetchResultPid(pfb);
        System.out.println(s);
    }

    /**
     * Select all patients from DB
     *
     * @return List<Patient>
     */
    public List<Patient> FindAll() {
        Query q = new Query(patientQueryStr, dbMetaName);
        return resultMapper.toPOJO(influxDB.query(q), Patient.class);
    }

    /**
     * Find patients by list of ID
     *
     * @param pids List of ID
     * @return Patient Object
     */
    public List<Patient> FindByIds(List<String> pids) {
        if (pids.size() == 0) return new ArrayList<>(0);

        StringBuilder sb = new StringBuilder(patientQueryStr);
        sb.append(" WHERE");
        for (String pid : pids) {
            sb.append(" \"PID\"='");
            sb.append(pid.toUpperCase());
            sb.append("' OR ");
        }
        String fq = sb.toString();
        Query q = new Query(fq.substring(0, fq.length() - 4), dbMetaName);
        return resultMapper.toPOJO(influxDB.query(q), Patient.class);
    }

    /**
     * Select a patient by ID
     *
     * @param pid Patient ID
     * @return Patient Object
     */
    public List<Patient> GetById(String pid) {
        Query query = new Query(patientQueryStr + " WHERE \"PID\" = '" + pid.toUpperCase() + "'", dbMetaName);
        return resultMapper.toPOJO(influxDB.query(query), Patient.class);
    }

    /**
     * Get data for already imported patients
     *
     * @return Patient list
     */
    public List<Patient> GetImportedPatientData() {
        List<String> avalPidList = GetAllImportedPid();
        return this.FindByIds(avalPidList);
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
        if (pfb.getNumOfFilters() == 0)
            return GetAllImportedPid();
        Query q = new Query(this.patientPidStr + pfb.getWhereAndFilters(), dbMetaName);
        return generatePidList(influxDB.query(q));
    }

    /**
     * Fetch results based on current filters
     *
     * @return "Patient" Data
     */
    public List<Patient> FetchResult(PatientFilterBean pfb) {
        if (pfb.getNumOfFilters() == 0) return FindAll();
        Query q = new Query(this.patientQueryStr + pfb.getWhereAndFilters(), dbMetaName);
        return resultMapper.toPOJO(influxDB.query(q), Patient.class);
    }

    private List<String> generatePidList(QueryResult qr) {
        List<QueryResult.Series> qrs = qr.getResults().get(0).getSeries();
        if (qrs == null)
            return new ArrayList<>(0);

        List<List<Object>> vals = qrs.get(0).getValues();
        List<String> res = new ArrayList<>(vals.size());
        for (List<Object> o : vals) {
            res.add((String) o.get(1));
        }

        return res;
    }

}
