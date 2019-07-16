package edu.pitt.medschool.model.dao;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private Logger logger = LoggerFactory.getLogger(this.getClass());

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

    public List<Patient> getAllPatientsComments() {
        PatientExample pe = new PatientExample();
        pe.createCriteria().andCommentIsNotNull();
        return patientMapper.selectByExample(pe);
    }

    @Transactional(rollbackFor = Exception.class)
    public int updatePatientInfo(PatientWithBLOBs patient) throws Exception  {
        PatientExample pe = new PatientExample();
        pe.createCriteria().andIdEqualTo(patient.getId().toUpperCase());

        int changeCommentResult = patientMapper.updateByExampleWithBLOBs(patient,pe);
        try {
            if (changeCommentResult == 0) {
                throw new Exception();
            }
        } catch (Exception e) {
            logger.error("CANNOT FIND THIS PATIENT!");
            throw e;
        }
        return changeCommentResult;
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

    public PatientWithBLOBs selectById(String pid) {
        return patientMapper.selectByPrimaryKey(pid.toUpperCase());
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

    public Patient selectByPatientId(String patientId) {
        return patientMapper.selectByPrimaryKey(patientId);
    }

    public int insertPatinet(PatientWithBLOBs p){
        try{
            PatientExample patientExample = new PatientExample();
            PatientExample.Criteria criteria = patientExample.createCriteria();
            criteria.andIdEqualTo(p.getId());
            if(patientMapper.updateByExampleSelective(p,patientExample)==0){
                patientMapper.insert(p);
            };
            return 1;
        }catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }

    public Long getTotal(){
        PatientExample example = new PatientExample();
        PatientExample.Criteria criteria = example.createCriteria();
        return patientMapper.countByExample(example);
    }

    public int publishNewData(int currentVersion) {
        PatientExample example = new PatientExample();
        PatientExample.Criteria criteria = example.createCriteria();
        criteria.andVersionEqualTo(0);
        PatientWithBLOBs patient = new PatientWithBLOBs();
        patient.setVersion(currentVersion);
        return patientMapper.updateByExampleSelective(patient,example);
    }

    public Long getPatientIncrease(int currentVersion) {
        PatientExample example = new PatientExample();
        PatientExample.Criteria criteria = example.createCriteria();
        criteria.andVersionEqualTo(currentVersion);
        return patientMapper.countByExample(example);
    }

    public Long getPatientNumber(int currentVersion) {
        PatientExample example = new PatientExample();
        PatientExample.Criteria criteria = example.createCriteria();
        criteria.andVersionLessThanOrEqualTo(currentVersion);
        return patientMapper.countByExample(example);
    }

    public Long getPuhPatientNumber(int currentVersion) {
        PatientExample example = new PatientExample();
        PatientExample.Criteria criteria = example.createCriteria();
        criteria.andVersionLessThanOrEqualTo(currentVersion);
        criteria.andIdLike("PUH%");
        return patientMapper.countByExample(example);
    }

    public Long getUabPatientNumber(int currentVersion) {
        PatientExample example = new PatientExample();
        PatientExample.Criteria criteria = example.createCriteria();
        criteria.andVersionLessThanOrEqualTo(currentVersion);
        criteria.andIdLike("UAB%");
        return patientMapper.countByExample(example);
    }

    public Long getTbiPatientNumber(int currentVersion) {
        PatientExample example = new PatientExample();
        PatientExample.Criteria criteria = example.createCriteria();
        criteria.andVersionLessThanOrEqualTo(currentVersion);
        criteria.andIdLike("TBI%");
        return patientMapper.countByExample(example);
    }
}
