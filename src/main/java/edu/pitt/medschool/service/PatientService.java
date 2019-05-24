/**
 *
 */
package edu.pitt.medschool.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import edu.pitt.medschool.model.dto.CsvFile;
import edu.pitt.medschool.model.dto.Patient;
import edu.pitt.medschool.model.dto.PatientWithBLOBs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import edu.pitt.medschool.framework.util.MysqlColumnBean;
import edu.pitt.medschool.model.dao.PatientDao;

/**
 * service for returning column information of data
 * 
 * @author Isolachine
 */
@Service
public class PatientService {
    @Autowired
    PatientDao patientDao;

    public List<MysqlColumnBean> getColumnInfo() {
        return patientDao.getColumnInfo();
    }

    public List<String> selecIdByfilter(String condition) {
        return patientDao.selecIdByfilter(condition);
    }

    public List<PatientWithBLOBs> getPatientsFromCsv(String dir){
        List<PatientWithBLOBs> patients = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(dir));
            String line;
            String firstline = reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] info = line.split(",");
                PatientWithBLOBs patient = new PatientWithBLOBs();
                patient.setId(info[0]);
                patient.setAge((info[1]).getBytes()[0]);
                patient.setFemale(info[2]=="1");
                patient.setOohca(-1);
                patients.add(patient);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return patients;
    }

    public int insertPatients(List<PatientWithBLOBs> patients) {
        try {
            int count=0;
            for (PatientWithBLOBs p : patients) {
                count+=patientDao.insertPatinet(p);

            }
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
