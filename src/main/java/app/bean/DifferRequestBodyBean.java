/**
 * 
 */
package app.bean;

/**
 * @author Isolachine
 *
 */
public class DifferRequestBodyBean {
    private String columnA;
    private String columnB;
    private double threshold;
    private int count;
    /**
     * @return the threshold
     */
    public double getThreshold() {
        return threshold;
    }
    /**
     * @param threshold the threshold to set
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
     * @param count the count to set
     */
    public void setCount(int count) {
        this.count = count;
    }
    /**
     * @return the columnA
     */
    public String getColumnA() {
        return columnA;
    }
    /**
     * @param columnA the columnA to set
     */
    public void setColumnA(String columnA) {
        this.columnA = columnA;
    }
    /**
     * @return the columnB
     */
    public String getColumnB() {
        return columnB;
    }
    /**
     * @param columnB the columnB to set
     */
    public void setColumnB(String columnB) {
        this.columnB = columnB;
    }
}
