/**
 * 
 */
package app.bean;

/**
 * @author Isolachine
 *
 */
public class ExceedRequestBodyBean {
    private String column;
    private int threshold;
    private int count;
    /**
     * @return the column
     */
    public String getColumn() {
        return column;
    }
    /**
     * @param column the column to set
     */
    public void setColumn(String column) {
        this.column = column;
    }
    /**
     * @return the threshold
     */
    public int getThreshold() {
        return threshold;
    }
    /**
     * @param threshold the threshold to set
     */
    public void setThreshold(int threshold) {
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
}
