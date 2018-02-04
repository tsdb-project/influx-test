/**
 * 
 */
package app.service;

import java.util.ArrayList;
import java.util.List;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.springframework.stereotype.Service;

import app.InfluxConfig;
import app.model.Patient;
import app.util.Util;

/**
 * @author Isolachine
 *
 */
@Service
public class PatientService {
    private InfluxDB influxDB = InfluxDBFactory.connect(InfluxConfig.ADDR, InfluxConfig.USERNAME, InfluxConfig.PASSWD);
    private String tableName = "patient";
    
    public List<Patient> selectAll() {
        Query query = new Query("select \"id\",\"first_name\",\"last_name\",\"birth_date\" from " + tableName, InfluxConfig.DBNAME);
        QueryResult results = influxDB.query(query);
        List<Patient> patients = new ArrayList<>();
        for (List<Object> result : results.getResults().get(0).getSeries().get(0).getValues()) {
            Patient patient = new Patient();
            patient.setId(result.get(1).toString());
            patient.setFirstName(result.get(2).toString());
            patient.setLastName(result.get(3).toString());
            patient.setBirthDate(Util.timestampToUTCDate(Long.valueOf(result.get(4).toString())));
            patient.setAge(Util.timestampToAge(Long.valueOf(result.get(4).toString())));
            patients.add(patient);
        }
        return patients;
    }
    
    public static void main(String[] args) {
        PatientService patientService = new PatientService();
        System.out.println(patientService.selectAll().get(0).getId());
    }
}
