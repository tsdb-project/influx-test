/**
 * 
 */
package edu.pitt.medschool.model;

import java.util.List;

/**
 * @author Isolachine
 *
 */
public class Column {
    private String category;
    private List<String> column;
    /**
     * @return the category
     */
    public String getCategory() {
        return category;
    }
    /**
     * @param category the category to set
     */
    public void setCategory(String category) {
        this.category = category;
    }
    /**
     * @return the column
     */
    public List<String> getColumn() {
        return column;
    }
    /**
     * @param column the column to set
     */
    public void setColumn(List<String> column) {
        this.column = column;
    }
}
