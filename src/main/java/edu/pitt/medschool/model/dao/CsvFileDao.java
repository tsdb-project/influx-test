package edu.pitt.medschool.model.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import edu.pitt.medschool.model.PatientTimeLine;
import edu.pitt.medschool.model.dto.CsvFile;
import edu.pitt.medschool.model.dto.Downsample;
import edu.pitt.medschool.model.dto.TimeLine;
import edu.pitt.medschool.model.mapper.CsvFileMapper;

@Repository
public class CsvFileDao {
	@Autowired
	CsvFileMapper csvFileMapper;
	
	@Transactional(rollbackFor = Exception.class)
    public int insert(CsvFile csvFile) throws Exception {
        return csvFileMapper.insertSelective(csvFile);
    }
	
	@SuppressWarnings("null")
	public ArrayList<PatientTimeLine> getPatientTimeLines(String machine){
		List<TimeLine> array = csvFileMapper.getPatientTimeLines(machine);
		ArrayList<PatientTimeLine> patientTimeLines = new ArrayList<PatientTimeLine>();
		for (int i = 0; i < array.size(); i++) {
//			System.out.println(array.get(i).getStart_time());
//			System.out.println(array.get(i).getEnd_time());
			patientTimeLines.add(array.get(i).toPatientTimeLine());
		}
		return patientTimeLines;
	}
	
}
