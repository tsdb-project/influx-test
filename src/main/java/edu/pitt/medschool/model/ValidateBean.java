package edu.pitt.medschool.model;

import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
public class ValidateBean {
    private String pid;
    private String filename;
    private String path;
    private int size;
    private String uuid;
    private Date header_time;
    private Date start_time;
    private Date end_time;
    private int lines;

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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Date getHeader_time() {
        return header_time;
    }

    public void setHeader_time(Date header_time) {
        this.header_time = header_time;
    }

    public Date getStart_time() {
        return start_time;
    }

    public void setStart_time(Date start_time) {
        this.start_time = start_time;
    }

    public Date getEnd_time() {
        return end_time;
    }

    public void setEnd_time(Date end_time) {
        this.end_time = end_time;
    }

    public int getLines() {
        return lines;
    }

    public void setLines(int lines) {
        this.lines = lines;
    }
}
