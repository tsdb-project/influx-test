/**
 * 
 */
package edu.pitt.medschool.controller.analysis.vo;

import edu.pitt.medschool.model.dto.DownsampleGroup;

/**
 * @author Isolachine
 *
 */
public class DownsampleGroupVO {
    private Integer queryId;
    private DownsampleGroup group;
    private String columns;

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
    public String getColumns() {
        return columns;
    }

    /**
     * @param columns the columns to set
     */
    public void setColumns(String columns) {
        this.columns = columns;
    }
}
