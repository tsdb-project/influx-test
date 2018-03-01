/**
 * 
 */
package app.model;

import java.time.Instant;
import java.util.List;

/**
 * @author Isolachine
 *
 */
public class RawData {

    private Instant time;

    private List<Double> values;
    
    private List<String> columnNames;

    /**
     * @return the time
     */
    public Instant getTime() {
        return time;
    }

    /**
     * @param time the time to set
     */
    public void setTime(Instant time) {
        this.time = time;
    }

    /**
     * @return the values
     */
    public List<Double> getValues() {
        return values;
    }

    /**
     * @param values the values to set
     */
    public void setValues(List<Double> values) {
        this.values = values;
    }

    /**
     * @return the columnNames
     */
    public List<String> getColumnNames() {
        return columnNames;
    }

    /**
     * @param columnNames the columnNames to set
     */
    public void setColumnNames(List<String> columnNames) {
        this.columnNames = columnNames;
    }

}
