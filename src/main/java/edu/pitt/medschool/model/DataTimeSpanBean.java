package edu.pitt.medschool.model;

import java.time.Instant;

/**
 * Data time span (period)
 * Original data should be once per second
 */
public class DataTimeSpanBean implements java.io.Serializable {

    private static final long serialVersionUID = -6688161009706456563L;

    private long delta;
    private String pid;
    private String fileUuid;
    private Instant start;
    private Instant end;
    private ArStatus arStat;
    private long effectiveDataCount;
    private double effectiveDataPerSecond;

    public double getEffectiveDataPerSecond() {
        return effectiveDataPerSecond;
    }

    public void setEffectiveDataPerSecond(double effectiveDataPerSecond) {
        this.effectiveDataPerSecond = effectiveDataPerSecond;
    }

    public long getEffectiveDataCount() {
        return effectiveDataCount;
    }

    public void setEffectiveDataCount(long effectiveDataCount) {
        this.effectiveDataCount = effectiveDataCount;
    }

    public ArStatus getArStat() {
        return arStat;
    }

    public void setArStat(ArStatus arStat) {
        this.arStat = arStat;
    }

    public long getDelta() {
        return delta;
    }

    public void setDelta(long delta) {
        this.delta = delta;
    }

    public enum ArStatus {ArOnly, NoArOnly, Both}

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getFileUuid() {
        return fileUuid;
    }

    public void setFileUuid(String fileUuid) {
        this.fileUuid = fileUuid;
    }

    public Instant getStart() {
        return start;
    }

    public void setStart(Instant start) {
        this.start = start;
    }

    public Instant getEnd() {
        return end;
    }

    public void setEnd(Instant end) {
        this.end = end;
    }
}
