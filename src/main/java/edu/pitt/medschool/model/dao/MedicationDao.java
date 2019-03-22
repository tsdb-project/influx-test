package edu.pitt.medschool.model.dao;

import java.util.ArrayList;
import java.util.List;

import edu.pitt.medschool.model.mapper.MedicationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.pitt.medschool.model.dto.Medication;
import edu.pitt.medschool.model.mapper.PatientMapper;

@Repository
public class MedicationDao {
    @Autowired
    MedicationMapper medicationMapper;

    @Transactional(rollbackFor = Exception.class)
    public int insert(Medication medication) throws Exception {
        return medicationMapper.insertSelective(medication);
    }


    public ArrayList<Medication> getAllMedInfo(String machine){
        List<Medication> array = medicationMapper.getAllMedInfo(machine);
        ArrayList<Medication> allMedInfo = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            allMedInfo.add(array.get(i));
        }
        return allMedInfo;
    }

    public ArrayList<Medication> getMedInfoById (String machine, String id){
        List<Medication> array = medicationMapper.getMedInfoById(machine,id);
        ArrayList<Medication> MedInfoById = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            MedInfoById.add(array.get(i));
        }
        return MedInfoById;
    }
}
