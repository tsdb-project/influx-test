/**
 * 
 */
package app.model;

import java.time.Instant;

/**
 * @author Isolachine
 *
 */
public class TimeSpan {
    private Instant start;
    private Instant end;
    /**
     * @return the start
     */
    public Instant getStart() {
        return start;
    }
    /**
     * @param start the start to set
     */
    public void setStart(Instant start) {
        this.start = start;
    }
    /**
     * @return the end
     */
    public Instant getEnd() {
        return end;
    }
    /**
     * @param end the end to set
     */
    public void setEnd(Instant end) {
        this.end = end;
    }
    
}
