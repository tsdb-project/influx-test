package edu.pitt.medschool.controller.analysis.vo;

import java.util.List;

public class ColumnJSON {
    private String type;
    private List<String> electrodes;
    private List<String> columns;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getElectrodes() {
        return electrodes;
    }

    public void setElectrodes(List<String> electrodes) {
        this.electrodes = electrodes;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }
}
