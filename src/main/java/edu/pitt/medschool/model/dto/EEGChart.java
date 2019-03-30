package edu.pitt.medschool.model.dto;

public class EEGChart {
    private String patinetID;
    private Integer period;
    private Integer minBin;
    private Integer minBinRow;
    private Boolean downsampleFirst;
    private String downsample;
    private String aggregation;
    private String columns;
    private boolean ar;

    public String getPatinetID() {
        return patinetID;
    }

    public void setPatinetID(String patinetID) {
        this.patinetID = patinetID;
    }

    public Integer getPeriod() {
        return period;
    }

    public void setPeriod(Integer period) {
        this.period = period;
    }

    public Integer getMinBin() {
        return minBin;
    }

    public void setMinBin(Integer minBin) {
        this.minBin = minBin;
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

    public String getDownsample() {
        return downsample;
    }

    public void setDownsample(String downsample) {
        this.downsample = downsample;
    }

    public String getAggregation() {
        return aggregation;
    }

    public void setAggregation(String aggregation) {
        this.aggregation = aggregation;
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
}
