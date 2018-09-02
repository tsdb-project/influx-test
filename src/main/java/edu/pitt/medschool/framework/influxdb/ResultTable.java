package edu.pitt.medschool.framework.influxdb;

import org.influxdb.dto.QueryResult;

import java.util.List;

/**
 * Common abstract class of result table, for a single InfluxDB query series
 * Born for the analysis sub-system
 */
public abstract class ResultTable {

    List<String> dataColumns; // For immutable order
    int rowCount = 0;
    int colCount = 0;

    /**
     * Get the column name by giving index
     */
    public int getColumnIndexByName(String name) {
        return dataColumns.indexOf(name);
    }

    /**
     * Get the index of given column name
     */
    public String getColumnNameWithIndex(int colIdx) {
        return dataColumns.get(colIdx);
    }

    /**
     * Get data by column# and row#
     * # Starts from 0
     */
    public abstract Object getDataByColAndRow(int colNum, int rowNum);

    /**
     * Get data by column name and row#
     * # Starts from 0
     */
    public abstract Object getDataByColumnNameAndRow(String columnName, int rowNum);

    /**
     * Get data list with the column name
     */
    public abstract List<Object> getDatalistByColumnName(String columnName);

    /**
     * Get data list with the row number
     */
    public abstract List<Object> getDatalistByRow(int rowNum);

    /**
     * Total row count in this series
     */
    public int getRowCount() {
        return this.rowCount;
    }

    /**
     * Total column count in this series
     */
    public int getColCount() {
        return this.colCount;
    }

    void extractResultColRowNumber(QueryResult.Series sr) {
        this.rowCount = sr.getValues().size();
        this.colCount = sr.getColumns().size();
    }

}
