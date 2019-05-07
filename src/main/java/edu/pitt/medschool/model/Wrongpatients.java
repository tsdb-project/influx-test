package edu.pitt.medschool.model;

public class Wrongpatients {
    private String pid;
    private boolean isoverlap;
    private boolean isabscent;
    private boolean notsame;

    public Wrongpatients(){
        this.isabscent = false;
        this.isoverlap = false;
        this.notsame = false;
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

    public boolean isIsabscent() {
        return isabscent;
    }

    public void setIsabscent(boolean isabsenct) {
        this.isabscent = isabsenct;
    }

    public boolean isNotsame() {
        return notsame;
    }

    public void setNotsame(boolean issame) {
        this.notsame = issame;
    }
}
