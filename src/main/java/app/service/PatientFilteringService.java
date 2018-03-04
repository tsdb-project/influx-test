package app.service;

import app.common.DBConfiguration;
import app.common.InfluxappConfig;
import app.model.Patient;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.impl.InfluxDBResultMapper;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

/**
 * Patient meta data services
 */
@Service
public class PatientFilteringService {

    private final InfluxDB influxDB = InfluxDBFactory.connect(InfluxappConfig.IFX_ADDR, InfluxappConfig.IFX_USERNAME, InfluxappConfig.IFX_PASSWD);
    private final InfluxDBResultMapper resultMapper = new InfluxDBResultMapper();

    private final String dbName = DBConfiguration.Meta.DBNAME;
    private final String patientQueryStr = "SELECT \"time\", \"PID\", \"age\", \"Gender\", \"Survived\", \"ArrestLocation\" FROM \"" + DBConfiguration.Meta.PATIENT + "\"";

    private List<String> filters = new LinkedList<>();

    public static void main(String[] args) {
        PatientFilteringService pfs = new PatientFilteringService();
        List<Patient> s = null;
//        s = patientFilteringService.FindAll();
//        s = patientFilteringService.FindById("pu-2010-083");
        s = pfs.FindById("puh-2010-074");
        pfs.AddGenderFilter("M");
        pfs.AddAgeLowerFilter(50);
        pfs.AddAgeUpperFilter(55);
        pfs.AddArrestLocationFilter(1);
        pfs.AddSurvivedFilter(0);
        s = pfs.FetchResult();
    }

    /**
     * Select all patients from DB
     *
     * @return List<Patient>
     */
    public List<Patient> FindAll() {
        Query query = new Query(patientQueryStr, dbName);
        return resultMapper.toPOJO(influxDB.query(query), Patient.class);
    }

    /**
     * Select a patient by ID
     *
     * @param pid Patient ID
     * @return Patient Object
     */
    public List<Patient> FindById(String pid) {
        Query query = new Query(patientQueryStr + " WHERE \"PID\" = '" + pid.toUpperCase() + "'", dbName);
        return resultMapper.toPOJO(influxDB.query(query), Patient.class);
    }

    /**
     * Add 'Survived' filter for patient
     *
     * @param status 1 for Yes, 0 for No, 2 for unknown, other for don't care
     */
    public void AddSurvivedFilter(int status) {
        String flag;
        switch (status) {
            case 0:
                flag = "N";
                break;
            case 1:
                flag = "Y";
                break;
            case 2:
                flag = "N/A";
                break;
            default:
                return;
        }
        filters.add(String.format("\"Survived\"='%s'", flag));
    }

    /**
     * Add 'ArrestLocation' filter for patient
     *
     * @param status 0 for Inside, 1 for outsite, 2 for unknown, other for don't care
     */
    public void AddArrestLocationFilter(int status) {
        String flag;
        switch (status) {
            case 0:
                flag = "Inside";
                break;
            case 1:
                flag = "Outside";
                break;
            case 2:
                flag = "N/A";
                break;
            default:
                return;
        }
        filters.add(String.format("\"ArrestLocation\"='%s'", flag));
    }

    /**
     * Add 'Gender' filter for patient
     *
     * @param gnd M or F
     */
    public void AddGenderFilter(String gnd) {
        String tgt = String.format("\"Gender\"='%s'", gnd.toUpperCase());
        filters.add(tgt);
    }

    /**
     * Add 'Age' filter for patient
     *
     * @param lower Inclusive lower
     */
    public void AddAgeLowerFilter(int lower) {
        String tgt = String.format("\"age\">=%d", lower);
        filters.add(tgt);
    }

    /**
     * Add 'Age' filter for patient
     *
     * @param upper Inclusive upper
     */
    public void AddAgeUpperFilter(int upper) {
        String tgt = String.format("\"age\"<=%d", upper);
        filters.add(tgt);
    }

    /**
     * Clear all active filters
     */
    public void ClearFilters() {
        filters.clear();
    }

    /**
     * Fetch results based on current filters
     *
     * @return "Patient" Data
     */
    public List<Patient> FetchResult() {
        if (filters.size() == 0) return FindAll();

        String fullQuery = patientQueryStr + whereFiltersGenerator();
        Query q = new Query(fullQuery, dbName);
        return resultMapper.toPOJO(influxDB.query(q), Patient.class);
    }

    /**
     * How many filters we have now?
     */
    public int NumOfFilters() {
        return filters.size();
    }

    private String whereFiltersGenerator() {
        if (filters.size() == 0) return "";

        StringBuilder sb = new StringBuilder(" WHERE 1=1");
        for (String afil : filters) {
            sb.append(" AND ");
            sb.append(afil);
        }
        return sb.toString();
    }

}
