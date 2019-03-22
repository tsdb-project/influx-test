package edu.pitt.medschool.service;

import edu.pitt.medschool.model.dao.MedicationDao;
import edu.pitt.medschool.model.dto.Medication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Service
public class PatientMedInfoService {
    @Value("${machine}")
    private String machineId;
    private final MedicationDao medicationDao;


    public PatientMedInfoService(MedicationDao medicationDao){this.medicationDao = medicationDao;}

    public List<Medication> getAllMedInfo(String machine) {
        return medicationDao.getAllMedInfo(machine);
    }

    public List<Medication> getMedInfoById(String machine, String id) { return medicationDao.getMedInfoById(machine,id);}
}
