package edu.pitt.medschool.algorithm;

import edu.pitt.medschool.controller.analysis.vo.DownsampleGroupVO;

/**
 * Queries for doing the downsample-aggregation query
 * Refactor from AnalysisService.exportToFile
 */
public class ExportQueries {

    /**
     * Downsample group data
     */
    private DownsampleGroupVO dsgVo;

    private String pid;

    /**
     * Initial this class
     *
     * @param p Patient ID
     * @param v DownsampleGroupVO
     */
    public ExportQueries(String p, DownsampleGroupVO v) {
        this.dsgVo = v;
        this.pid = p;
    }

    @Override
    /**
     * Get the final assembled queries
     */
    public String toString() {
        return super.toString();
    }
}
