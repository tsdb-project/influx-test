package edu.pitt.medschool.model.dao;


import java.util.List;

import edu.pitt.medschool.framework.util.Util;
import edu.pitt.medschool.model.mapper.MedicationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import edu.pitt.medschool.model.dto.Medication;

@Repository
public class MedicationDao {
    @Autowired
    MedicationMapper medicationMapper;

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Transactional(rollbackFor = Exception.class)
    public int insert(Medication medication){
        return medicationMapper.insertSelective(medication);
    }

    public List<Medication> getAllMedInfo(String machine){
        return medicationMapper.getAllMedInfo(machine);
    }

    public List<Medication> getMedInfoById (String machine, String id){
        return medicationMapper.getMedInfoById(machine,id);
    }

    public List<String> selectAllMedication(){
        List<String> list = medicationMapper.getAllMedicine();
        return list;
    }

    public List<Medication> selectAllbyMedications(String drugName, List<String> patientIDs){
        String patients = Util.wrapAndConcatStringList("'", ", ", patientIDs);
        logger.info(patients);
        List<Medication> list = medicationMapper.selectAllbyMedication(drugName,patients);
        return list;
    }

    public List<String> selectPatientbyMedications(String drugName){
        List<String> list = medicationMapper.selectPatientsbyMedications(drugName);
        return list;
    }
}
