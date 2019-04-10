package edu.pitt.medschool.model.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.pitt.medschool.framework.util.MysqlColumnBean;
import edu.pitt.medschool.model.dto.Patient;
import edu.pitt.medschool.model.dto.PatientExample;
import edu.pitt.medschool.model.dto.PatientWithBLOBs;
import edu.pitt.medschool.model.mapper.PatientMapper;

/**
 * Patient DAO
 */
@Repository
public class PatientDao {
    @Autowired
    PatientMapper patientMapper;

    @Transactional(rollbackFor = Exception.class)
    public int insert(PatientWithBLOBs p) throws Exception {
        return patientMapper.insert(p);
    }

    /**
     * @param condition Where condition for filter
     * @return List of PIDs
     */
    public List<String> selecIdByfilter(String condition) {
        return patientMapper.selecIdByfilter(condition);
    }

    /**
     * @param gender F: Female; M: Male
     * @return List of PIDs
     */
    public List<Patient> selectByGender(String gender) {
        PatientExample pe = new PatientExample();
        pe.createCriteria().andFemaleEqualTo(gender.toUpperCase().equals("F"));
        return patientMapper.selectByExample(pe);
    }

    public List<Patient> selectById(String pid) {
        PatientExample pe = new PatientExample();
        pe.createCriteria().andIdEqualTo(pid.toUpperCase());
        return patientMapper.selectByExample(pe);
    }

    public List<Patient> selectAll() {
        PatientExample pe = new PatientExample();
        return patientMapper.selectByExample(pe);
    }

    public List<Patient> selectByIds(List<String> ids) {
        ids.replaceAll(String::toUpperCase);
        PatientExample pe = new PatientExample();
        pe.createCriteria().andIdIn(ids);
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
        pe.createCriteria().andAgeLessThanOrEqualTo(max);
        return patientMapper.selectByExample(pe);
    }

    /**
     * Age greater than (>)
     */
    public List<Patient> selectByAgeG(byte min) {
        PatientExample pe = new PatientExample();
        pe.createCriteria().andAgeGreaterThan(min);
        return patientMapper.selectByExample(pe);
    }

    public List<Patient> selectByAgeRange(byte min, byte max) {
        PatientExample pe = new PatientExample();
        pe.createCriteria().andAgeBetween(min, max);
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
                pe.createCriteria().andOohcaEqualTo(0);
                break;
            case 1:
                pe.createCriteria().andOohcaEqualTo(1);
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

    public List<String> selectIdByCustom(PatientExample pe) {
        return patientMapper.selectIdByExample(pe);
    }

    public List<MysqlColumnBean> getColumnInfo() {
        return patientMapper.getColumnInfo();
    }

}
