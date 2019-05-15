package edu.pitt.medschool.model;

import edu.pitt.medschool.config.DBConfiguration;

import java.util.Date;
import java.util.regex.Pattern;

public class PatientTimeLine {
    private String pid;
    private String filename;
    private String filetype;
    private Date arrestTime;
    private long relativeStartTime;
    private long relativeEndTime;
    private int length;
    private String uuid;

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
    
    public String getUuid() {
        return uuid;
    }
    
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getLength() { return length;}

    public void setLength(int length) { this.length = length; }

    public Integer getDocumentNo(){
        String doc = this.filename;
        int i = 0;
        while (i<doc.length() && doc.charAt(i) !='a' && doc.charAt(i) !='n'){
            i++;
        }
        int j =i-1;
        while (j>0 && Character.isDigit(doc.charAt(j))){
            j--;
        }
        if(Pattern.matches("[0-9]*",doc.substring(j+1,i)) && j!=i-1) {
            //System.out.println(Integer.parseInt(doc.substring(j+1, i)));
            return Integer.parseInt(doc.substring(j+1, i));
        }else {
            return -1;
        }
    }

}
