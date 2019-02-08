package edu.pitt.medschool.model;

import java.util.Date;

public class PatientTimeLine {
    private String filename;
    private Date arrestTime;
    private long relevantStartTime;
    private long relevantEndTime;


    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Date getArrestTime() {
        return arrestTime;
    }

    public void setArrestTime(Date arrestTime) {
        this.arrestTime = arrestTime;
    }

    public long getRelevantStartTime() {
        return relevantStartTime;
    }

    public void setRelevantStartTime(long relevantStartTime) {
        this.relevantStartTime = relevantStartTime;
    }

    public long getRelevantEndTime() {
        return relevantEndTime;
    }

    public void setRelevantEndTime(long relevantEndTime) {
        this.relevantEndTime = relevantEndTime;
    }

}
