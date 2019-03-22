package edu.pitt.medschool.model.dto;

public class MedicalQuery {
	private Medication medication;
	private Downsample downsample;
	
	
	public Medication getMedication() {
		return medication;
	}
	public void setMedication(Medication medication) {
		this.medication = medication;
	}
	public Downsample getDownsample() {
		return downsample;
	}
	public void setDownsample(Downsample downsample) {
		this.downsample = downsample;
	}
	
	

}
