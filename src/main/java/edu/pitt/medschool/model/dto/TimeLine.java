package edu.pitt.medschool.model.dto;

import java.sql.Timestamp;
import java.util.regex.Pattern;

import edu.pitt.medschool.model.PatientTimeLine;

public class TimeLine {
    private String pid;
    private String filename;
    private Timestamp start_time;
    private java.sql.Timestamp end_time;
    private Timestamp arrestdate;
    private int len;
    private java.sql.Timestamp arresttime;
    private String uuid;
    private boolean resolved;
    private String comment;

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) { this.pid = pid; }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public java.sql.Timestamp getStart_time() {
        return start_time;
    }

    public void setStart_time(Timestamp start_time) {
        this.start_time = start_time;
    }

    public Timestamp getEnd_time() {
        return end_time;
    }

    public void setEnd_time(Timestamp end_time) {
        this.end_time = end_time;
    }

    public Timestamp getArrestdate() {
        return arrestdate;
    }

    public void setArrestdate(Timestamp arrestdate) {
        this.arrestdate = arrestdate;
    }

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }

    public Timestamp getArresttime() {
        return arresttime;
    }

    public void setArresttime(Timestamp arresttime) {
        this.arresttime = arresttime;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public boolean isResolved() { return resolved; }

    public void setResolved(boolean resolved) { this.resolved = resolved; }

    public String getComment() { return comment; }

    public void setComment(String comment) { this.comment = comment; }

    public PatientTimeLine toPatientTimeLine() {
        // TODO Auto-generated method stub
        PatientTimeLine patientTimeLine = new PatientTimeLine();
        String filename = this.getFilename();
        Timestamp arrestTime = this.getArrestdate();
        if (this.getArresttime() != null) {
            arrestTime = this.getArresttime();
        }
        long relativeStartTime = (this.getStart_time().getTime() - arrestTime.getTime()) / 1000;
        long relativeEndTime = (this.getEnd_time().getTime() - arrestTime.getTime()) / 1000;
        int len = this.getLen();
        String uuid = this.getUuid();
        String pid = this.getPid();
        Boolean resolved = this.isResolved();
        String comment = this.getComment();
       patientTimeLine.setArrestTime(arrestTime);
       patientTimeLine.setPid(pid);
       patientTimeLine.setFilename(filename);
       if (Pattern.matches("(?i).*noar.*",filename)) patientTimeLine.setFiletype("noar");
       else patientTimeLine.setFiletype("ar");
       patientTimeLine.setRelativeStartTime(relativeStartTime);
       patientTimeLine.setRelativeEndTime(relativeEndTime);
       patientTimeLine.setLength(len);
       patientTimeLine.setFilename(filename);
       patientTimeLine.setArrestTime(arrestTime);
       patientTimeLine.setUuid(uuid);
       patientTimeLine.setResolved(resolved);
       patientTimeLine.setComment(comment);
		return patientTimeLine;
	}
}
