package edu.pitt.medschool.controller.analysis.vo;

import java.util.List;

import edu.pitt.medschool.model.dto.MedicalDownsample;
import edu.pitt.medschool.model.dto.MedicalDownsampleGroup;

public class MedicalDownsampleVO {
	private MedicalDownsample medicalDownsample;
	private List<MedicalDownsampleGroup> groups;
	
	public MedicalDownsample getMedicalDownsample() {
		return medicalDownsample;
	}
	
	public void setMedicalDownsample(MedicalDownsample medicalDownsample) {
		this.medicalDownsample = medicalDownsample;
	}
	
	public List<MedicalDownsampleGroup> getGroups(){
		return groups;
	}
	
	public void setGroups(List<MedicalDownsampleGroup> groups) {
		this.groups = groups;
	}

}
