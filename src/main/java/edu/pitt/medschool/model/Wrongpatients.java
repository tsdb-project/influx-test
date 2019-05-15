package edu.pitt.medschool.model;

import java.util.List;

public class Wrongpatients {
    private String pid;
    private boolean isoverlap;
    private List<Integer> ar_miss;
    private List<Integer> noar_miss;
    private boolean wrongname;
    public Wrongpatients(){
        this.isoverlap = false;
        this.wrongname = false;
    }
    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public boolean isIsoverlap() {
        return isoverlap;
    }

    public void setIsoverlap(boolean isoverlap) {
        this.isoverlap = isoverlap;
    }

    public boolean isWrongname() { return wrongname; }

    public void setWrongname(boolean wrongname) {
        this.wrongname = wrongname;
    }

    public List<Integer> getAr_miss() {
        return ar_miss;
    }

    public void setAr_miss(List<Integer> ar_miss) {
        this.ar_miss = ar_miss;
    }

    public List<Integer> getNoar_miss() {
        return noar_miss;
    }

    public void setNoar_miss(List<Integer> noar_miss) {
        this.noar_miss = noar_miss;
    }
}
