/**
 * 
 */
package edu.pitt.medschool.controller.analysis.vo;

import java.util.List;

import edu.pitt.medschool.model.dto.DownsampleGroup;

/**
 * @author Isolachine
 *
 */
public class DownsampleGroupVO {
    private Integer queryId;
    private DownsampleGroup group;
    private List<String> columns;
    /**
     * @return the queryId
     */
    public Integer getQueryId() {
        return queryId;
    }
    /**
     * @param queryId the queryId to set
     */
    public void setQueryId(Integer queryId) {
        this.queryId = queryId;
    }
    /**
     * @return the group
     */
    public DownsampleGroup getGroup() {
        return group;
    }
    /**
     * @param group the group to set
     */
    public void setGroup(DownsampleGroup group) {
        this.group = group;
    }
    /**
     * @return the columns
     */
    public List<String> getColumns() {
        return columns;
    }
    /**
     * @param columns the columns to set
     */
    public void setColumns(List<String> columns) {
        this.columns = columns;
    }
}
