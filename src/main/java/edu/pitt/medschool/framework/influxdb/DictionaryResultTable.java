package edu.pitt.medschool.framework.influxdb;

import org.influxdb.dto.QueryResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dictionary-like result (a table), safe returns
 */
public class DictionaryResultTable extends ResultTable {

    private Map<String, List<Object>> dataKV;

    /**
     * Generate an empty result set
     */
    public DictionaryResultTable() {
        genEmptyData();
    }

    public DictionaryResultTable(QueryResult.Series r) {
        if (r == null) {
            this.genEmptyData();
            return;
        }
        formDictionary(r);
        super.colCount = super.dataColumns.size();
    }

    @Override
    public Object getDataByColAndRow(int colNum, int rowNum) {
        return this.dataKV.get(super.dataColumns.get(colNum)).get(rowNum);
    }

    @Override
    public Object getDataByColumnNameAndRow(String columnName, int rowNum) {
        return this.dataKV.get(columnName).get(rowNum);
    }

    @Override
    public List<Object> getDatalistByColumnName(String columnName) {
        return this.dataKV.get(columnName);
    }

    @Override
    public List<Object> getDatalistByRow(int rowNum) {
        List<Object> res = new ArrayList<>(super.colCount);
        for (int i = 0; i < super.colCount; i++) {
            res.add(this.dataKV.get(super.dataColumns.get(i)).get(rowNum));
        }
        return res;
    }

    public Class getDataTypeByColumnName(String columnName) {
        return this.dataKV.get(columnName).get(0).getClass();
    }

    /**
     * Extract query result series and form a map
     */
    private void formDictionary(QueryResult.Series resSer) {
        List<String> columnsData = resSer.getColumns();
        int cols = columnsData.size(), rows = resSer.getValues().size();
        super.rowCount = rows;

        // Convert to a dictionary-like structure
        this.dataKV = new HashMap<>(rows);
        super.dataColumns = new ArrayList<>(cols);
        // Fill this by column
        for (int i = 0; i < cols; ++i) {
            String colName = columnsData.get(i);
            List<Object> dataList = new ArrayList<>(rows);
            for (int j = 0; j < rows; j++) {
                Object value = resSer.getValues().get(j).get(i);
                dataList.add(value);
            }
            super.dataColumns.add(colName);
            this.dataKV.put(colName, dataList);
        }
    }

    private void genEmptyData() {
        this.dataKV = new HashMap<>(0);
        super.dataColumns = new ArrayList<>(0);
    }

}
