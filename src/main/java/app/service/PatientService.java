package app.service;

import java.util.List;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.impl.InfluxDBResultMapper;
import org.springframework.stereotype.Service;

import app.InfluxappConfig;
import app.model.Patient;

/**
 * Patient related services
 */
@Service
public class PatientService {

    private static final InfluxDB influxDB = InfluxDBFactory.connect(InfluxappConfig.IFX_ADDR, InfluxappConfig.IFX_USERNAME, InfluxappConfig.IFX_PASSWD);
    private static final InfluxDBResultMapper resultMapper = new InfluxDBResultMapper();

    private static final String patientQueryStr = "SELECT * FROM " + InfluxappConfig.IFX_TABLE_PATIENTS;

    /**
     * Select all patients from DB
     *
     * @return List<Patient>
     */
    public List<Patient> SelectAll() {
        Query query = new Query(patientQueryStr, InfluxappConfig.IFX_DBNAME);
        return resultMapper.toPOJO(influxDB.query(query), Patient.class);
    }

    /**
     * Select a patient by ID
     *
     * @param pid Patient ID
     * @return Patient Object
     */
    public List<Patient> SelectById(String pid) {
        Query query = new Query(patientQueryStr + " WHERE pid = '" + pid.toUpperCase() + "'", InfluxappConfig.IFX_DBNAME);
        return resultMapper.toPOJO(influxDB.query(query), Patient.class);
    }

    /**
     * Select a patient by Gender
     *
     * @param gnd (M)ale or (F)emale
     * @return Lists of patients
     */
    public List<Patient> SelectByGender(String gnd) {
        Query query = new Query(patientQueryStr + " WHERE gender = '" + gnd.toUpperCase() + "'", InfluxappConfig.IFX_DBNAME);
        return resultMapper.toPOJO(influxDB.query(query), Patient.class);
    }

    /**
     * Select patients with age
     *
     * @param lower Inclusive lower
     * @param upper Non-inclusive upper
     * @return Lists of patients
     */
    public List<Patient> SelectByAge(int lower, int upper) {
        Query query = new Query(patientQueryStr + " WHERE age >= " + lower + " AND age < " + upper, InfluxappConfig.IFX_DBNAME);
        return resultMapper.toPOJO(influxDB.query(query), Patient.class);
    }

    /**
     * Select patients with age
     *
     * @param upper Inclusive upper
     * @return Lists of patients
     */
    public List<Patient> SelectByAgeUpperbound(int upper) {
        Query query = new Query(patientQueryStr + " WHERE age <= " + upper, InfluxappConfig.IFX_DBNAME);
        return resultMapper.toPOJO(influxDB.query(query), Patient.class);
    }

    /**
     * Select patients with age
     *
     * @param lower Inclusive lower
     * @return Lists of patients
     */
    public List<Patient> SelectByAgeLowerbound(int lower) {
        Query query = new Query(patientQueryStr + " WHERE age >= " + lower, InfluxappConfig.IFX_DBNAME);
        return resultMapper.toPOJO(influxDB.query(query), Patient.class);
    }

    public static void main(String[] args) {
        PatientService patientService = new PatientService();
        List<Patient> s = patientService.SelectAll();
        s = patientService.SelectByGender("M");
        s = patientService.SelectById("pu-2010-083");
        s = patientService.SelectByAgeLowerbound(23);
        s = patientService.SelectByAgeUpperbound(50);
        s = patientService.SelectByAge(20, 24);
        System.out.println();
    }
}
