package edu.pitt.medschool.controller.analysis.vo;

import java.util.List;

import edu.pitt.medschool.model.dto.Feature;
import edu.pitt.medschool.service.ColumnService.PredefinedKV;

/**
 * @author Isolachine
 *
 */
public class ElectrodeVO {

    private List<PredefinedKV> predefined;
    private List<Feature> electrodes;

    public List<PredefinedKV> getPredefined() {
        return predefined;
    }

    public void setPredefined(List<PredefinedKV> predefined) {
        this.predefined = predefined;
    }

    public List<Feature> getElectrodes() {
        return electrodes;
    }

    public void setElectrodes(List<Feature> electrodes) {
        this.electrodes = electrodes;
    }
}
