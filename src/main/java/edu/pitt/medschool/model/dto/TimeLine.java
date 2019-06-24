package edu.pitt.medschool.model.dto;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.regex.Pattern;

import edu.pitt.medschool.model.PatientTimeLine;

public class TimeLine {
    private String pid;
    private String filename;
    private LocalDateTime start_time;
    private LocalDateTime end_time;
    private LocalDate arrestdate;
    private int len;
    private LocalDateTime arresttime;
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

    public LocalDateTime getStart_time() {
        return start_time;
    }

    public void setStart_time(LocalDateTime start_time) {
        this.start_time = start_time;
    }

    public LocalDateTime getEnd_time() {
        return end_time;
    }

    public void setEnd_time(LocalDateTime end_time) {
        this.end_time = end_time;
    }

    public LocalDate getArrestdate() {
        return arrestdate;
    }

    public void setArrestdate(LocalDate arrestdate) {
        this.arrestdate = arrestdate;
    }

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }

    public LocalDateTime getArresttime() {
        return arresttime;
    }

    public void setArresttime(LocalDateTime arresttime) {
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

        LocalDateTime arrestTime;
        if (this.getArresttime() != null) {
            arrestTime = this.getArresttime().atZone(ZoneId.of("America/New_York")).toLocalDateTime();
        }else {
            arrestTime = this.getArrestdate().atStartOfDay().atZone(ZoneId.of("America/New_York")).toLocalDateTime();
        }

        LocalDateTime startTime = this.getStart_time().atZone(ZoneId.of("UTC")).withZoneSameInstant(ZoneId.of("America/New_York")).toLocalDateTime();
        LocalDateTime endTime = this.getEnd_time().atZone(ZoneId.of("UTC")).withZoneSameInstant(ZoneId.of("America/New_York")).toLocalDateTime();
        long relativeStartTime = Duration.between(arrestTime,startTime).toMillis()/1000;
        long relativeEndTime = Duration.between(arrestTime,endTime).toMillis()/1000;

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
