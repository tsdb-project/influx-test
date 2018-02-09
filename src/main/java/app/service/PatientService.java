package app.service;

import java.util.ArrayList;
import java.util.List;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.springframework.stereotype.Service;

import app.common.InfluxappConfig;
import app.model.Patient;
import app.util.Util;

/**
 * Patient related services
 */
@Service
public class PatientService {

    private InfluxDB influxDB = InfluxDBFactory.connect(InfluxappConfig.IFX_ADDR, InfluxappConfig.IFX_USERNAME, InfluxappConfig.IFX_PASSWD);
    private String tableName = InfluxappConfig.IFX_TABLE_PATIENTS;

    public List<Patient> selectAll() {
        Query query = new Query("SELECT pid, age, gender FROM " + tableName, InfluxappConfig.IFX_DBNAME);
        QueryResult results = influxDB.query(query);
        return queryResultToPatientList(results);
    }

    public Patient selectById(String pid) {
        Query query = new Query("SELECT pid, age, gender FROM " + tableName +
                " WHERE pid = '" + pid.toUpperCase() + "'", InfluxappConfig.IFX_DBNAME);
        QueryResult results = influxDB.query(query);
        if (results.getResults().get(0).getSeries() == null)
            return null;
        return queryResultToPatient(results.getResults().get(0).getSeries().get(0).getValues().get(0));
    }

    private Patient queryResultToPatient(List<Object> result) {
        Patient tmP = new Patient();
        tmP.setPid(result.get(1).toString());
        tmP.setAge((Double) result.get(2));
        tmP.setGender(result.get(3).toString());
        return tmP;
    }

    /**
     * DB result to List for json serialize
     */
    private List<Patient> queryResultToPatientList(QueryResult results) {
        List<Patient> patients = new ArrayList<>();
        for (List<Object> result : results.getResults().get(0).getSeries().get(0).getValues()) {
            patients.add(queryResultToPatient(result));
        }
        return patients;
    }

    public static void main(String[] args) {
        PatientService patientService = new PatientService();
        System.out.println(patientService.selectById("puh-2010-093"));
    }
}
