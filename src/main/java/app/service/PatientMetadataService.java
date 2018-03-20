package app.service;

import app.bean.PatientFilterBean;
import app.config.DBConfiguration;
import app.config.InfluxappConfig;
import app.model.Patient;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Query;
import org.influxdb.impl.InfluxDBResultMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Get metadata for patients
 */
@Service
public class PatientMetadataService {

    private final InfluxDB influxDB = InfluxappConfig.INFLUX_DB;
    private final String dbDataName = DBConfiguration.Data.DBNAME;
    private final String dbMetaName = DBConfiguration.Meta.DBNAME;

    private final InfluxDBResultMapper resultMapper = new InfluxDBResultMapper();
    private final PatientFilteringService pfs = new PatientFilteringService();

    private final String patientQueryStr = "SELECT \"time\", \"PID\", \"age\", \"Gender\", \"Survived\", \"ArrestLocation\" FROM \"" + DBConfiguration.Meta.PATIENT + "\"";

    public static void main(String[] args) {
        PatientFilterBean pfb = new PatientFilterBean();
        pfb.setGenderFilter("M");
        pfb.setAgeLowerFilter(50);
        pfb.setAgeUpperFilter(55);
        pfb.setArrestLocationFilter(1);
        pfb.setSurvivedFilter(0);

        PatientMetadataService pms = new PatientMetadataService();
        List<Patient> t;
        t = pms.GetImportedPatientData();
        t = pms.FetchResult(pfb);
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
     * Select all patients from DB
     *
     * @return List<Patient>
     */
    public List<Patient> FindAll() {
        Query q = new Query(patientQueryStr, dbMetaName);
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
        List<String> avalPidList = pfs.GetAllImportedPid();
        return this.FindByIds(avalPidList);
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

}
