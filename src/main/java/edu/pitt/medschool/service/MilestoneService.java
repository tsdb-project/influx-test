package edu.pitt.medschool.service;

import edu.pitt.medschool.model.dao.CsvFileDao;
import edu.pitt.medschool.model.dao.MilestoneDao;
import edu.pitt.medschool.model.dao.PatientDao;
import edu.pitt.medschool.model.dto.Milestone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Service
public class MilestoneService {
    @Autowired
    MilestoneDao milestoneDao;
    CsvFileDao csvFileDao;
    PatientDao patientDao;

    public List<Milestone> getAllMileStones(){
        return milestoneDao.getAllMilestones();
    }

    public int unlock(LocalDateTime date){
        Milestone milestone = new Milestone();
        milestone.setDate(date);
        milestone.setNumcsv(csvFileDao.getTotal());
        milestone.setNumpatients(patientDao.getTotal());
        milestone.setType((byte)0);
        return milestoneDao.insert(milestone);
    }
    public int publish(LocalDateTime date){
        Milestone milestone = new Milestone();
        milestone.setDate(date);
        milestone.setNumcsv(csvFileDao.getTotal());
        milestone.setNumpatients(patientDao.getTotal());
        milestone.setType((byte)1);
        return milestoneDao.insert(milestone);
    }

    public Boolean checklock(){
        if(milestoneDao.getlatest() == 0){
            System.out.println("not locked");
            return false;
        }else {
            return true;
        }
    }
}
