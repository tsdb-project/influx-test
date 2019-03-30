package edu.pitt.medschool.model.dto;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class Medication {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column medication.infused_vol
     *
     * @mbg.generated
     */
    private Integer infusedVol;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column medication.id
     *
     * @mbg.generated
     */
    private String id;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column medication.chart_date
     *
     * @mbg.generated
     */
    private Date chartDate;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column medication.drug_name
     *
     * @mbg.generated
     */
    private String drugName;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column medication.dose
     *
     * @mbg.generated
     */
    private Double dose;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column medication.dose_unit
     *
     * @mbg.generated
     */
    private String doseUnit;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column medication.rate
     *
     * @mbg.generated
     */
    private Double rate;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column medication.rate_unit
     *
     * @mbg.generated
     */
    private String rateUnit;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column medication.ordered_as
     *
     * @mbg.generated
     */
    private String orderedAs;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column medication.route
     *
     * @mbg.generated
     */
    private String route;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column medication.status
     *
     * @mbg.generated
     */
    private String status;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column medication.site
     *
     * @mbg.generated
     */
    private String site;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column medication.infused_vol_unit
     *
     * @mbg.generated
     */
    private String infusedVolUnit;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column medication.infuse_ind
     *
     * @mbg.generated
     */
    private Integer infuseInd;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column medication.iv_flag
     *
     * @mbg.generated
     */
    private Integer ivFlag;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column medication.bolus_flag
     *
     * @mbg.generated
     */
    private Integer bolusFlag;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column medication.tdrip_ind
     *
     * @mbg.generated
     */
    private Integer tdripInd;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column medication.infused_vol
     *
     * @return the value of medication.infused_vol
     *
     * @mbg.generated
     */
    public Integer getInfusedVol() {
        return infusedVol;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column medication.infused_vol
     *
     * @param infusedVol the value for medication.infused_vol
     *
     * @mbg.generated
     */
    public void setInfusedVol(Integer infusedVol) {
        this.infusedVol = infusedVol;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column medication.id
     *
     * @return the value of medication.id
     *
     * @mbg.generated
     */
    public String getId() {
        return id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column medication.id
     *
     * @param id the value for medication.id
     *
     * @mbg.generated
     */
    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column medication.chart_date
     *
     * @return the value of medication.chart_date
     *
     * @mbg.generated
     */
    public Date getChartDate() {
        return chartDate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column medication.chart_date
     *
     * @param chartDate the value for medication.chart_date
     *
     * @mbg.generated
     */
    public void setChartDate(Date chartDate) {
        this.chartDate = chartDate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column medication.drug_name
     *
     * @return the value of medication.drug_name
     *
     * @mbg.generated
     */
    public String getDrugName() {
        return drugName;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column medication.drug_name
     *
     * @param drugName the value for medication.drug_name
     *
     * @mbg.generated
     */
    public void setDrugName(String drugName) {
        this.drugName = drugName == null ? null : drugName.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column medication.dose
     *
     * @return the value of medication.dose
     *
     * @mbg.generated
     */
    public Double getDose() {
        return dose;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column medication.dose
     *
     * @param dose the value for medication.dose
     *
     * @mbg.generated
     */
    public void setDose(Double dose) {
        this.dose = dose;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column medication.dose_unit
     *
     * @return the value of medication.dose_unit
     *
     * @mbg.generated
     */
    public String getDoseUnit() {
        return doseUnit;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column medication.dose_unit
     *
     * @param doseUnit the value for medication.dose_unit
     *
     * @mbg.generated
     */
    public void setDoseUnit(String doseUnit) {
        this.doseUnit = doseUnit == null ? null : doseUnit.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column medication.rate
     *
     * @return the value of medication.rate
     *
     * @mbg.generated
     */
    public Double getRate() {
        return rate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column medication.rate
     *
     * @param rate the value for medication.rate
     *
     * @mbg.generated
     */
    public void setRate(Double rate) {
        this.rate = rate;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column medication.rate_unit
     *
     * @return the value of medication.rate_unit
     *
     * @mbg.generated
     */
    public String getRateUnit() {
        return rateUnit;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column medication.rate_unit
     *
     * @param rateUnit the value for medication.rate_unit
     *
     * @mbg.generated
     */
    public void setRateUnit(String rateUnit) {
        this.rateUnit = rateUnit == null ? null : rateUnit.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column medication.ordered_as
     *
     * @return the value of medication.ordered_as
     *
     * @mbg.generated
     */
    public String getOrderedAs() {
        return orderedAs;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column medication.ordered_as
     *
     * @param orderedAs the value for medication.ordered_as
     *
     * @mbg.generated
     */
    public void setOrderedAs(String orderedAs) {
        this.orderedAs = orderedAs == null ? null : orderedAs.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column medication.route
     *
     * @return the value of medication.route
     *
     * @mbg.generated
     */
    public String getRoute() {
        return route;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column medication.route
     *
     * @param route the value for medication.route
     *
     * @mbg.generated
     */
    public void setRoute(String route) {
        this.route = route == null ? null : route.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column medication.status
     *
     * @return the value of medication.status
     *
     * @mbg.generated
     */
    public String getStatus() {
        return status;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column medication.status
     *
     * @param status the value for medication.status
     *
     * @mbg.generated
     */
    public void setStatus(String status) {
        this.status = status == null ? null : status.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column medication.site
     *
     * @return the value of medication.site
     *
     * @mbg.generated
     */
    public String getSite() {
        return site;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column medication.site
     *
     * @param site the value for medication.site
     *
     * @mbg.generated
     */
    public void setSite(String site) {
        this.site = site == null ? null : site.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column medication.infused_vol_unit
     *
     * @return the value of medication.infused_vol_unit
     *
     * @mbg.generated
     */
    public String getInfusedVolUnit() {
        return infusedVolUnit;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column medication.infused_vol_unit
     *
     * @param infusedVolUnit the value for medication.infused_vol_unit
     *
     * @mbg.generated
     */
    public void setInfusedVolUnit(String infusedVolUnit) {
        this.infusedVolUnit = infusedVolUnit == null ? null : infusedVolUnit.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column medication.infuse_ind
     *
     * @return the value of medication.infuse_ind
     *
     * @mbg.generated
     */
    public Integer getInfuseInd() {
        return infuseInd;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column medication.infuse_ind
     *
     * @param infuseInd the value for medication.infuse_ind
     *
     * @mbg.generated
     */
    public void setInfuseInd(Integer infuseInd) {
        this.infuseInd = infuseInd;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column medication.iv_flag
     *
     * @return the value of medication.iv_flag
     *
     * @mbg.generated
     */
    public Integer getIvFlag() {
        return ivFlag;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column medication.iv_flag
     *
     * @param ivFlag the value for medication.iv_flag
     *
     * @mbg.generated
     */
    public void setIvFlag(Integer ivFlag) {
        this.ivFlag = ivFlag;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column medication.bolus_flag
     *
     * @return the value of medication.bolus_flag
     *
     * @mbg.generated
     */
    public Integer getBolusFlag() {
        return bolusFlag;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column medication.bolus_flag
     *
     * @param bolusFlag the value for medication.bolus_flag
     *
     * @mbg.generated
     */
    public void setBolusFlag(Integer bolusFlag) {
        this.bolusFlag = bolusFlag;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column medication.tdrip_ind
     *
     * @return the value of medication.tdrip_ind
     *
     * @mbg.generated
     */
    public Integer getTdripInd() {
        return tdripInd;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column medication.tdrip_ind
     *
     * @param tdripInd the value for medication.tdrip_ind
     *
     * @mbg.generated
     */
    public void setTdripInd(Integer tdripInd) {
        this.tdripInd = tdripInd;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table medication
     *
     * @mbg.generated
     */
    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        Medication other = (Medication) that;
        return (this.getInfusedVol() == null ? other.getInfusedVol() == null : this.getInfusedVol().equals(other.getInfusedVol()))
            && (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getChartDate() == null ? other.getChartDate() == null : this.getChartDate().equals(other.getChartDate()))
            && (this.getDrugName() == null ? other.getDrugName() == null : this.getDrugName().equals(other.getDrugName()))
            && (this.getDose() == null ? other.getDose() == null : this.getDose().equals(other.getDose()))
            && (this.getDoseUnit() == null ? other.getDoseUnit() == null : this.getDoseUnit().equals(other.getDoseUnit()))
            && (this.getRate() == null ? other.getRate() == null : this.getRate().equals(other.getRate()))
            && (this.getRateUnit() == null ? other.getRateUnit() == null : this.getRateUnit().equals(other.getRateUnit()))
            && (this.getOrderedAs() == null ? other.getOrderedAs() == null : this.getOrderedAs().equals(other.getOrderedAs()))
            && (this.getRoute() == null ? other.getRoute() == null : this.getRoute().equals(other.getRoute()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getSite() == null ? other.getSite() == null : this.getSite().equals(other.getSite()))
            && (this.getInfusedVolUnit() == null ? other.getInfusedVolUnit() == null : this.getInfusedVolUnit().equals(other.getInfusedVolUnit()))
            && (this.getInfuseInd() == null ? other.getInfuseInd() == null : this.getInfuseInd().equals(other.getInfuseInd()))
            && (this.getIvFlag() == null ? other.getIvFlag() == null : this.getIvFlag().equals(other.getIvFlag()))
            && (this.getBolusFlag() == null ? other.getBolusFlag() == null : this.getBolusFlag().equals(other.getBolusFlag()))
            && (this.getTdripInd() == null ? other.getTdripInd() == null : this.getTdripInd().equals(other.getTdripInd()));
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table medication
     *
     * @mbg.generated
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getInfusedVol() == null) ? 0 : getInfusedVol().hashCode());
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getChartDate() == null) ? 0 : getChartDate().hashCode());
        result = prime * result + ((getDrugName() == null) ? 0 : getDrugName().hashCode());
        result = prime * result + ((getDose() == null) ? 0 : getDose().hashCode());
        result = prime * result + ((getDoseUnit() == null) ? 0 : getDoseUnit().hashCode());
        result = prime * result + ((getRate() == null) ? 0 : getRate().hashCode());
        result = prime * result + ((getRateUnit() == null) ? 0 : getRateUnit().hashCode());
        result = prime * result + ((getOrderedAs() == null) ? 0 : getOrderedAs().hashCode());
        result = prime * result + ((getRoute() == null) ? 0 : getRoute().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getSite() == null) ? 0 : getSite().hashCode());
        result = prime * result + ((getInfusedVolUnit() == null) ? 0 : getInfusedVolUnit().hashCode());
        result = prime * result + ((getInfuseInd() == null) ? 0 : getInfuseInd().hashCode());
        result = prime * result + ((getIvFlag() == null) ? 0 : getIvFlag().hashCode());
        result = prime * result + ((getBolusFlag() == null) ? 0 : getBolusFlag().hashCode());
        result = prime * result + ((getTdripInd() == null) ? 0 : getTdripInd().hashCode());
        return result;
    }


    // Problem here
    public Instant DatetoInstant(Date date){
        Instant tmp = date.toInstant();
        return tmp.minus(5, ChronoUnit.HOURS);
    }
}