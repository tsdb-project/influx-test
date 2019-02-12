package edu.pitt.medschool.model;

import java.util.Date;

public class PatientTimeLine {
    private String filename;
    private String filetype;
    private Date arrestTime;
    private long relativeStartTime;
    private long relativeEndTime;
    private int length;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFiletype() {
        return filetype;
    }

    public void setFiletype(String filetype) {
        this.filetype = filetype;
    }

    public Date getArrestTime() {
        return arrestTime;
    }

    public void setArrestTime(Date arrestTime) {
        this.arrestTime = arrestTime;
    }

    public long getRelativeStartTime() {
        return relativeStartTime;
    }

    public void setRelativeStartTime(long relevantStartTime) {
        this.relativeStartTime = relevantStartTime;
    }

    public long getRelativeEndTime() {
        return relativeEndTime;
    }

    public void setRelativeEndTime(long relevantEndTime) {
        this.relativeEndTime = relevantEndTime;
    }

    public int getLength() { return length;}

    public void setLength(int length) { this.length = length; }

}
