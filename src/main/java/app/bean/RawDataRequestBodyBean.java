/**
 * 
 */
package app.bean;

import java.util.List;

/**
 * @author Isolachine
 *
 */
public class RawDataRequestBodyBean {
    private String tableName;
    
    private List<String> columnNames;

    /**
     * @return the tableName
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * @param tableName the tableName to set
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
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
