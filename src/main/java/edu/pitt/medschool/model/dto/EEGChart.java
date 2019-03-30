package edu.pitt.medschool.model.dto;

public class EEGChart {
    private String patientID;
    private Integer period;
    private Integer minBinRow;
    private Boolean downsampleFirst;
    private String columns;
    private boolean ar;
    private String downsampleMethod;
    private String aggregationMethod;

    public String getPatientID() {
        return patientID;
    }

    public void setPatientID(String patientID) {
        this.patientID = patientID;
    }

    public Integer getPeriod() {
        return period;
    }

    public void setPeriod(Integer period) {
        this.period = period;
    }

    public Integer getMinBinRow() {
        return minBinRow;
    }

    public void setMinBinRow(Integer minBinRow) {
        this.minBinRow = minBinRow;
    }

    public Boolean getDownsampleFirst() {
        return downsampleFirst;
    }

    public void setDownsampleFirst(Boolean downsampleFirst) {
        this.downsampleFirst = downsampleFirst;
    }

    public String getColumns() {
        return columns;
    }

    public void setColumns(String columns) {
        this.columns = columns;
    }

    public boolean isAr() {
        return ar;
    }

    public void setAr(boolean ar) {
        this.ar = ar;
    }

    public String getDownsampleMethod() {
        return downsampleMethod;
    }

    public void setDownsampleMethod(String downsampleMethod) {
        this.downsampleMethod = downsampleMethod;
    }

    public String getAggregationMethod() {
        return aggregationMethod;
    }

    public void setAggregationMethod(String aggregationMethod) {
        this.aggregationMethod = aggregationMethod;
    }
}
