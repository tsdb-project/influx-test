package edu.pitt.medschool.controller.load.vo;

import java.util.ArrayList;
import java.util.List;

import edu.pitt.medschool.model.dto.CsvFile;

public class CsvFileVO {
    private CsvFile csvFile;
    private List<CsvFile> counterpart;
    private String gap;

    public CsvFileVO(CsvFile csvFile) {
        this.csvFile = csvFile;
        this.counterpart = new ArrayList<>();
        this.gap = "N/A";
    }

    public CsvFile getCsvFile() {
        return csvFile;
    }

    public void setCsvFile(CsvFile csvFile) {
        this.csvFile = csvFile;
    }

    public List<CsvFile> getCounterpart() {
        return counterpart;
    }

    public void setCounterpart(List<CsvFile> counterpart) {
        this.counterpart = counterpart;
    }

    public String getGap() {
        return gap;
    }

    public void setGap(String gap) {
        this.gap = gap;
    }
}
