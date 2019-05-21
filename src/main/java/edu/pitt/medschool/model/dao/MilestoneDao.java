package edu.pitt.medschool.model.dao;


import edu.pitt.medschool.model.dto.Milestone;
import edu.pitt.medschool.model.mapper.MilestoneMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MilestoneDao {
    @Autowired
    MilestoneMapper milestoneMapper;

    public List<Milestone> getAllMilestones(){
        return milestoneMapper.selectAll();
    }

    public int insert(Milestone milestone){
        return milestoneMapper.insert(milestone);
    }

    public byte getlatest(){
        System.out.println(milestoneMapper.getlatest());
        return milestoneMapper.getlatest();
    }
}
