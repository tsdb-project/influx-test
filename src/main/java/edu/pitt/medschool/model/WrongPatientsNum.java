package edu.pitt.medschool.model;

public class WrongPatientsNum {
    private int overlap;
    private int missAr;
    private int missNoar;
    private int wrongName;

    public int getOverlap() {
        return overlap;
    }

    public void setOverlap(int overlap) {
        this.overlap = overlap;
    }

    public int getMissAr() {
        return missAr;
    }

    public void setMissAr(int missAr) {
        this.missAr = missAr;
    }

    public int getMissNoar() {
        return missNoar;
    }

    public void setMissNoar(int missNoar) {
        this.missNoar = missNoar;
    }

    public int getWrongName() {
        return wrongName;
    }

    public void setWrongName(int wrongName) {
        this.wrongName = wrongName;
    }
}