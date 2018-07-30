package edu.pitt.medschool.controller.analysis.vo;

import java.util.List;

import edu.pitt.medschool.model.dto.Feature;

/**
 * @author Isolachine
 *
 */
public class ElectrodeVO {

    private List<String> predefined;
    private List<Feature> electrodes;

    public List<String> getPredefined() {
        return predefined;
    }

    public void setPredefined(List<String> predefined) {
        this.predefined = predefined;
    }

    public List<Feature> getElectrodes() {
        return electrodes;
    }

    public void setElectrodes(List<Feature> electrodes) {
        this.electrodes = electrodes;
    }
}
