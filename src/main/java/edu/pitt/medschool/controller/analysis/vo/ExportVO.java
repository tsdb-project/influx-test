package edu.pitt.medschool.controller.analysis.vo;

import java.util.Date;

public class ExportVO {
    private Integer id;
    private Boolean ar;
    private Date createTime;
    private String alias;
    private String machine;
    private Boolean finished;
    private Integer finishedPatient;
    private Integer allPatient;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean getAr() {
        return ar;
    }

    public void setAr(Boolean ar) {
        this.ar = ar;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getMachine() {
        return machine;
    }

    public void setMachine(String machine) {
        this.machine = machine;
    }

    public Boolean getFinished() {
        return finished;
    }

    public void setFinished(Boolean finished) {
        this.finished = finished;
    }

    public Integer getFinishedPatient() {
        return finishedPatient;
    }

    public void setFinishedPatient(Integer finishedPatient) {
        this.finishedPatient = finishedPatient;
    }

    public Integer getAllPatient() {
        return allPatient;
    }

    public void setAllPatient(Integer allPatient) {
        this.allPatient = allPatient;
    }
}
