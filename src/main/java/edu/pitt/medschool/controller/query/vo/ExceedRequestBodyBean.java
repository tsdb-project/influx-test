/**
 * 
 */
package edu.pitt.medschool.controller.query.vo;

import java.util.Map;

/**
 * @author Isolachine
 *
 */
public class ExceedRequestBodyBean {
    private String column;
    private double threshold;
    private int count;
    private Map<String, String> meta;

    /**
     * @return the column
     */
    public String getColumn() {
        return column;
    }

    /**
     * @param column
     *            the column to set
     */
    public void setColumn(String column) {
        this.column = column;
    }

    /**
     * @return the threshold
     */
    public double getThreshold() {
        return threshold;
    }

    /**
     * @param threshold
     *            the threshold to set
     */
    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    /**
     * @return the count
     */
    public int getCount() {
        return count;
    }

    /**
     * @param count
     *            the count to set
     */
    public void setCount(int count) {
        this.count = count;
    }

    /**
     * @return the meta
     */
    public Map<String, String> getMeta() {
        return meta;
    }

    /**
     * @param meta
     *            the meta to set
     */
    public void setMeta(Map<String, String> meta) {
        this.meta = meta;
    }
}
