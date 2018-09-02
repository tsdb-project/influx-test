package edu.pitt.medschool.framework.influxdb;

import org.influxdb.dto.QueryResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dictionary-like result (a table) for a single InfluxDB query series
 */
public class ResultTable {
    private Map<String, List<Object>> dataKV;
    private List<String> dataColumns; // For immutable order
    private int rowCount = 0;
    private int colCount = 0;

    /**
     * Generates an empty result set
     */
    public ResultTable() {
        this.dataKV = new HashMap<>(0);
        this.dataColumns = new ArrayList<>(0);
    }

    public ResultTable(QueryResult.Series r) {
        formDictionary(r);
        this.colCount = this.dataColumns.size();
    }

    /**
     * Line and row starts from 0
     */
    public Object getDataByColAndNum(int colNum, int rowNum) {
        return dataKV.get(dataColumns.get(colNum)).get(rowNum);
    }

    /**
     * Line starts from 0
     */
    public Object getDataByColumnNameAndRow(String columnName, int rowNum) {
        return dataKV.get(columnName).get(rowNum);
    }

    public List<Object> getDatalistByColumnName(String columnName) {
        return dataKV.get(columnName);
    }

    public List<Object> getDatalistByRow(int rowNum) {
        List<Object> res = new ArrayList<>(this.colCount);
        for (int i = 0; i < this.colCount; i++) {
            res.add(dataKV.get(dataColumns.get(i)).get(rowNum));
        }
        return res;
    }

    public Class getDataTypeByColumnName(String columnName) {
        return dataKV.get(columnName).get(0).getClass();
    }

    public int getRowCount() {
        return this.rowCount;
    }

    public int getColCount() {
        return this.colCount;
    }

    /**
     * Extract query result series and form a map
     */
    private void formDictionary(QueryResult.Series resSer) {
        List<String> columnsData = resSer.getColumns();
        int cols = columnsData.size(), rows = resSer.getValues().size();
        this.rowCount = rows;

        // Convert to a dictionary-like structure
        this.dataKV = new HashMap<>(rows);
        this.dataColumns = new ArrayList<>(cols);
        // Fill this by column
        for (int i = 0; i < cols; ++i) {
            String colName = columnsData.get(i);
            List<Object> dataList = new ArrayList<>(rows);
            for (int j = 0; j < rows; j++) {
                Object value = resSer.getValues().get(j).get(i);
                dataList.add(value);
            }
            this.dataColumns.add(colName);
            this.dataKV.put(colName, dataList);
        }
    }
}
