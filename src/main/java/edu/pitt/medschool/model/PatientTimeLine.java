package edu.pitt.medschool.model;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatientTimeLine {
    private String pid;
    private String filename;
    private String filetype;
    private LocalDateTime arrestTime;
    private long relativeStartTime;
    private long relativeEndTime;
    private int length;
    private String uuid;
    private boolean resolved;
    private String comment;

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

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

    public LocalDateTime getArrestTime() {
        return arrestTime;
    }

    public void setArrestTime(LocalDateTime arrestTime) {
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
    
    public String getUuid() {
        return uuid;
    }
    
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getLength() { return length;}

    public void setLength(int length) { this.length = length; }

    public boolean isResolved() { return resolved; }

    public void setResolved(boolean resolved) { this.resolved = resolved; }

    public String getComment() { return comment; }

    public void setComment(String comment) { this.comment = comment; }

    public Integer getDocumentNo(){
        String doc = this.filename;
        Pattern pattern = Pattern.compile("([ -_][0]*[1-9]+[0]*(noar|ar).csv)");
        Matcher matcher = pattern.matcher(doc);
        if(matcher.find()){
            Pattern pattern1 = Pattern.compile("(\\d+)");
            Matcher matcher1 = pattern1.matcher(matcher.group(1));
            matcher1.find();
            return Integer.parseInt(matcher1.group(1));
        }else{
            return -1;
        }
    }
}
