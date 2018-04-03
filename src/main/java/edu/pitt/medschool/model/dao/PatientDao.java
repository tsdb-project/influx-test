package edu.pitt.medschool.model.dao;

import edu.pitt.medschool.model.dto.Patient;
import edu.pitt.medschool.model.dto.PatientExample;
import edu.pitt.medschool.model.mapper.PatientMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Patient DAO
 */
@Repository
public class PatientDao {
    @Autowired
    PatientMapper patientMapper;

    @Transactional(rollbackFor = Exception.class)
    public int insert(Patient p) throws Exception {
        return patientMapper.insert(p);
    }

    /**
     * @param gender F: Female; M: Male
     * @return List of PIDs
     */
    public List<String> selecIdByGender(String gender) {
        gender = gender.toUpperCase();
        return patientMapper.selectIdByGender(gender.equals("F"));
    }

    /**
     * @param gender F: Female; M: Male
     * @return List of PIDs
     */
    public List<Patient> selectByGender(String gender) {
        PatientExample pe = new PatientExample();
        pe.createCriteria()
                .andFemaleEqualTo(gender.toUpperCase().equals("F"));
        return patientMapper.selectByExample(pe);
    }

    public List<Patient> selectById(String pid) {
        PatientExample pe = new PatientExample();
        pe.createCriteria()
                .andIdEqualTo(pid.toUpperCase());
        return patientMapper.selectByExample(pe);
    }

    public List<Patient> selectByIds(List<String> ids) {
        ids.replaceAll(String::toUpperCase);
        PatientExample pe = new PatientExample();
        pe.createCriteria()
                .andIdIn(ids);
        return patientMapper.selectByExample(pe);
    }

    public List<String> selectIdAll() {
        return patientMapper.selectIdAll();
    }

    /**
     * Age less or equal (<=)
     */
    public List<Patient> selectByAgeLE(byte max) {
        PatientExample pe = new PatientExample();
        pe.createCriteria()
                .andAgeLessThanOrEqualTo(max);
        return patientMapper.selectByExample(pe);
    }

    /**
     * Age greater than (>)
     */
    public List<Patient> selectByAgeG(byte min) {
        PatientExample pe = new PatientExample();
        pe.createCriteria()
                .andAgeGreaterThan(min);
        return patientMapper.selectByExample(pe);
    }

    public List<Patient> selectByAgeRange(byte min, byte max) {
        PatientExample pe = new PatientExample();
        pe.createCriteria()
                .andAgeBetween(min, max);
        return patientMapper.selectByExample(pe);
    }

    /**
     * 'ArrestLocation' filter for patient
     *
     * @param loc 0 for Inside, 1 for outsite, other for unknown
     */
    public List<Patient> selectByArrestLocation(int loc) {
        PatientExample pe = new PatientExample();
        switch (loc) {
            case 0:
                pe.createCriteria().andOohcaEqualTo(false);
                break;
            case 1:
                pe.createCriteria().andOohcaEqualTo(true);
                break;
            default:
                pe.createCriteria().andOohcaIsNull();
                break;
        }
        return patientMapper.selectByExample(pe);
    }

    public List<Patient> selectByCustom(PatientExample pe) {
        return patientMapper.selectByExample(pe);
    }

}
