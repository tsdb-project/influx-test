package edu.pitt.medschool.controller.analysis.vo;

import edu.pitt.medschool.framework.util.TimeUtil;
import edu.pitt.medschool.model.dto.MedicalDownsample;

public class MedicalDownsampleEditResponse {
	private MedicalDownsample medicalDownsample;
	private String period;
	private String periodUnit;
    private String beforemedicine;
    private String beforemedicineUnit;
    private String aftermedicine;
    private String aftermedicineUnit;
    private String minBinRow;
    private String minBinRowUnit;
    private int minBin;
    private boolean downsampleFirst;
	
    public MedicalDownsampleEditResponse(MedicalDownsample medicalDownsample) {
    	this.medicalDownsample = medicalDownsample;
    	String[] periodArr =TimeUtil.secondToString(medicalDownsample.getPeriod());
    	this.period = periodArr[0];
    	this.periodUnit = periodArr[1];
    	
    	String[] beforemedicineArr = TimeUtil.secondToString(medicalDownsample.getBeforeMedicine());
    	this.beforemedicine = beforemedicineArr[0];
    	this.beforemedicineUnit = beforemedicineArr[1];
    	
    	String[] aftermedicineArr = TimeUtil.secondToString(medicalDownsample.getAfterMedicine());
    	this.aftermedicine = aftermedicineArr[0];
    	this.aftermedicineUnit = aftermedicineArr[1];
    	
    	this.minBin = medicalDownsample.getMinBin();
    	
    	String[] minBinRowArr = TimeUtil.secondToString(medicalDownsample.getMinBinRow());
        this.setMinBinRow(minBinRowArr[0]);
        this.setMinBinRowUnit(minBinRowArr[1]);

        this.downsampleFirst = medicalDownsample.getDownsampleFirst();
    }

	public MedicalDownsample getMedicalDownsample() {
		return medicalDownsample;
	}

	public void setMedicalDownsample(MedicalDownsample medicalDownsample) {
		this.medicalDownsample = medicalDownsample;
	}

	public String getPeriod() {
		return period;
	}

	public void setPeriod(String period) {
		this.period = period;
	}

	public String getPeriodUnit() {
		return periodUnit;
	}

	public void setPeriodUnit(String periodUnit) {
		this.periodUnit = periodUnit;
	}

	public String getBeforemedicine() {
		return beforemedicine;
	}

	public void setBeforemedicine(String beforemedicine) {
		this.beforemedicine = beforemedicine;
	}

	public String getBeforemedicineUnit() {
		return beforemedicineUnit;
	}

	public void setBeforemedicineUnit(String beforemedicineUnit) {
		this.beforemedicineUnit = beforemedicineUnit;
	}

	public String getAftermedicine() {
		return aftermedicine;
	}

	public void setAftermedicine(String aftermedicine) {
		this.aftermedicine = aftermedicine;
	}

	public String getAftermedicineUnit() {
		return aftermedicineUnit;
	}

	public void setAftermedicineUnit(String aftermedicineUnit) {
		this.aftermedicineUnit = aftermedicineUnit;
	}

	public String getMinBinRow() {
		return minBinRow;
	}

	public void setMinBinRow(String minBinRow) {
		this.minBinRow = minBinRow;
	}

	public String getMinBinRowUnit() {
		return minBinRowUnit;
	}

	public void setMinBinRowUnit(String minBinRowUnit) {
		this.minBinRowUnit = minBinRowUnit;
	}

	public int getMinBin() {
		return minBin;
	}

	public void setMinBin(int minBin) {
		this.minBin = minBin;
	}

	public boolean isDownsampleFirst() {
		return downsampleFirst;
	}

	public void setDownsampleFirst(boolean downsampleFirst) {
		this.downsampleFirst = downsampleFirst;
	}
    
    
    
}
