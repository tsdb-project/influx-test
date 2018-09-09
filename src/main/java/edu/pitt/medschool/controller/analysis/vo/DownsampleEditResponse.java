package edu.pitt.medschool.controller.analysis.vo;

import edu.pitt.medschool.framework.util.TimeUtil;
import edu.pitt.medschool.model.dto.Downsample;

/**
 * @author Isolachine
 */
public class DownsampleEditResponse {
    private Downsample downsample;
    private String period;
    private String periodUnit;
    private String duration;
    private String durationUnit;
    private String origin;
    private String originUnit;
    private int minTotalBinThreshold;
    private int minEveryBinThershold;
    private boolean isDownsampleFirst;

    public DownsampleEditResponse(Downsample downsample) {
        this.downsample = downsample;
        String[] periodArr = TimeUtil.secondToString(downsample.getPeriod());
        this.period = periodArr[0];
        this.periodUnit = periodArr[1];

        String[] durationArr = TimeUtil.secondToString(downsample.getDuration());
        this.duration = durationArr[0];
        this.durationUnit = durationArr[1];

        String[] originArr = TimeUtil.secondToString(downsample.getOrigin());
        this.origin = originArr[0];
        this.originUnit = originArr[1];

        this.minEveryBinThershold = downsample.getMinEveryBinThershold();
        this.minTotalBinThreshold = downsample.getMinTotalBinThreshold();
        this.isDownsampleFirst = downsample.getIsDownsampleFirst();
    }

    public boolean getIsDownsampleFirst() {
        return isDownsampleFirst;
    }

    public void setIsDownsampleFirst(boolean downsampleFirst) {
        isDownsampleFirst = downsampleFirst;
    }

    public int getMinTotalBinThreshold() {
        return minTotalBinThreshold;
    }

    public void setMinTotalBinThreshold(int minTotalBinThreshold) {
        this.minTotalBinThreshold = minTotalBinThreshold;
    }

    public int getMinEveryBinThershold() {
        return minEveryBinThershold;
    }

    public void setMinEveryBinThershold(int minEveryBinThershold) {
        this.minEveryBinThershold = minEveryBinThershold;
    }

    /**
     * @return the downsample
     */
    public Downsample getDownsample() {
        return downsample;
    }

    /**
     * @param downsample the downsample to set
     */
    public void setDownsample(Downsample downsample) {
        this.downsample = downsample;
    }

    /**
     * @return the period
     */
    public String getPeriod() {
        return period;
    }

    /**
     * @param period the period to set
     */
    public void setPeriod(String period) {
        this.period = period;
    }

    /**
     * @return the periodUnit
     */
    public String getPeriodUnit() {
        return periodUnit;
    }

    /**
     * @param periodUnit the periodUnit to set
     */
    public void setPeriodUnit(String periodUnit) {
        this.periodUnit = periodUnit;
    }

    /**
     * @return the duration
     */
    public String getDuration() {
        return duration;
    }

    /**
     * @param duration the duration to set
     */
    public void setDuration(String duration) {
        this.duration = duration;
    }

    /**
     * @return the durationUnit
     */
    public String getDurationUnit() {
        return durationUnit;
    }

    /**
     * @param durationUnit the durationUnit to set
     */
    public void setDurationUnit(String durationUnit) {
        this.durationUnit = durationUnit;
    }

    /**
     * @return the origin
     */
    public String getOrigin() {
        return origin;
    }

    /**
     * @param origin the origin to set
     */
    public void setOrigin(String origin) {
        this.origin = origin;
    }

    /**
     * @return the originUnit
     */
    public String getOriginUnit() {
        return originUnit;
    }

    /**
     * @param originUnit the originUnit to set
     */
    public void setOriginUnit(String originUnit) {
        this.originUnit = originUnit;
    }
}
