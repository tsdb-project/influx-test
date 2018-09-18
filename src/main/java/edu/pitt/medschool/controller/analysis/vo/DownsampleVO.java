package edu.pitt.medschool.controller.analysis.vo;

import java.util.List;

import edu.pitt.medschool.model.dto.Downsample;
import edu.pitt.medschool.model.dto.DownsampleGroup;

public class DownsampleVO {
    private Downsample downsample;
    private List<DownsampleGroup> groups;

    public Downsample getDownsample() {
        return downsample;
    }

    public void setDownsample(Downsample downsample) {
        this.downsample = downsample;
    }

    public List<DownsampleGroup> getGroups() {
        return groups;
    }

    public void setGroups(List<DownsampleGroup> groups) {
        this.groups = groups;
    }
}
