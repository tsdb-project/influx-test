package edu.pitt.medschool.model.dao;

import edu.pitt.medschool.model.dto.Downsample;
import edu.pitt.medschool.model.dto.Patient;
import edu.pitt.medschool.model.mapper.PatientMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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

}
