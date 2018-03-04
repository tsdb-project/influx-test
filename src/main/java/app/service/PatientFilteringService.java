package app.service;

import app.common.DBConfiguration;
import app.common.InfluxappConfig;
import app.model.Patient;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.impl.InfluxDBResultMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Patient meta data services
 * (Needs rewrite)
 */
@Service
@Deprecated
public class PatientFilteringService {

    private final InfluxDB influxDB = InfluxDBFactory.connect(InfluxappConfig.IFX_ADDR, InfluxappConfig.IFX_USERNAME, InfluxappConfig.IFX_PASSWD);
    private final InfluxDBResultMapper resultMapper = new InfluxDBResultMapper();

    private final String dbName = DBConfiguration.Meta.DBNAME;
    private final String patientQueryStr = "select \"time\", \"PID\", \"age\", \"Gender\" from " + DBConfiguration.Meta.PATIENT;

    public static void main(String[] args) {
        PatientFilteringService patientFilteringService = new PatientFilteringService();
        List<Patient> s = patientFilteringService.SelectAll();
        s = patientFilteringService.FindByGender("M");
        s = patientFilteringService.FindById("puh-2010-083");
        s = patientFilteringService.FindByAgeLowerbound(23);
        s = patientFilteringService.FindByAgeUpperbound(50);
        s = patientFilteringService.FindByAge(10, 50);
        System.out.println();
    }

    /**
     * Select all patients from DB
     *
     * @return List<Patient>
     */
    public List<Patient> SelectAll() {
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
        Query query = new Query(patientQueryStr + " WHERE PID = '" + pid.toUpperCase() + "'", dbName);
        return resultMapper.toPOJO(influxDB.query(query), Patient.class);
    }

    /**
     * Select a patient by Gender
     *
     * @param gnd (M)ale or (F)emale
     * @return Lists of patients
     */
    public List<Patient> FindByGender(String gnd) {
        Query query = new Query(patientQueryStr + " WHERE Gender = '" + gnd.toUpperCase() + "'", dbName);
        return resultMapper.toPOJO(influxDB.query(query), Patient.class);
    }

    /**
     * Select patients with age
     *
     * @param lower Inclusive lower
     * @param upper Non-inclusive upper
     * @return Lists of patients
     */
    public List<Patient> FindByAge(int lower, int upper) {
        Query query = new Query(patientQueryStr + " WHERE age >= " + lower + " AND age < " + upper, dbName);
        return resultMapper.toPOJO(influxDB.query(query), Patient.class);
    }

    /**
     * Select patients with age
     *
     * @param upper Inclusive upper
     * @return Lists of patients
     */
    public List<Patient> FindByAgeUpperbound(int upper) {
        Query query = new Query(patientQueryStr + " WHERE age <= " + upper, dbName);
        return resultMapper.toPOJO(influxDB.query(query), Patient.class);
    }

    /**
     * Select patients with age
     *
     * @param lower Inclusive lower
     * @return Lists of patients
     */
    public List<Patient> FindByAgeLowerbound(int lower) {
        Query query = new Query(patientQueryStr + " WHERE age >= " + lower, dbName);
        return resultMapper.toPOJO(influxDB.query(query), Patient.class);
    }
}
