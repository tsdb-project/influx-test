/**
 * 
 */
package edu.pitt.medschool.controller.export.vo;

/**
 * @author Isolachine
 *
 */
public class ColumnRequestBodyBean {

    private String measure;
    private String electrode;

    /**
     * @return the measure
     */
    public String getMeasure() {
        return measure;
    }

    /**
     * @param measure
     *            the measure to set
     */
    public void setMeasure(String measure) {
        this.measure = measure;
    }

    /**
     * @return the electrode
     */
    public String getElectrode() {
        return electrode;
    }

    /**
     * @param electrode
     *            the electrode to set
     */
    public void setElectrode(String electrode) {
        this.electrode = electrode;
    }
}
