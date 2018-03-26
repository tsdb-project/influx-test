package edu.pitt.medschool.model.dto;

public class DownsampleGroupColumn {

    /**
     * This field was generated by MyBatis Generator. This field corresponds to the database column downsample_group_column.id
     * @mbg.generated
     */
    private Integer id;
    /**
     * This field was generated by MyBatis Generator. This field corresponds to the database column downsample_group_column.query_group_id
     * @mbg.generated
     */
    private Integer queryGroupId;
    /**
     * This field was generated by MyBatis Generator. This field corresponds to the database column downsample_group_column.column
     * @mbg.generated
     */
    private String column;

    /**
     * This method was generated by MyBatis Generator. This method returns the value of the database column downsample_group_column.id
     * @return  the value of downsample_group_column.id
     * @mbg.generated
     */
    public Integer getId() {
        return id;
    }

    /**
     * This method was generated by MyBatis Generator. This method sets the value of the database column downsample_group_column.id
     * @param id  the value for downsample_group_column.id
     * @mbg.generated
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * This method was generated by MyBatis Generator. This method returns the value of the database column downsample_group_column.query_group_id
     * @return  the value of downsample_group_column.query_group_id
     * @mbg.generated
     */
    public Integer getQueryGroupId() {
        return queryGroupId;
    }

    /**
     * This method was generated by MyBatis Generator. This method sets the value of the database column downsample_group_column.query_group_id
     * @param queryGroupId  the value for downsample_group_column.query_group_id
     * @mbg.generated
     */
    public void setQueryGroupId(Integer queryGroupId) {
        this.queryGroupId = queryGroupId;
    }

    /**
     * This method was generated by MyBatis Generator. This method returns the value of the database column downsample_group_column.column
     * @return  the value of downsample_group_column.column
     * @mbg.generated
     */
    public String getColumn() {
        return column;
    }

    /**
     * This method was generated by MyBatis Generator. This method sets the value of the database column downsample_group_column.column
     * @param column  the value for downsample_group_column.column
     * @mbg.generated
     */
    public void setColumn(String column) {
        this.column = column == null ? null : column.trim();
    }

    /**
     * This method was generated by MyBatis Generator. This method corresponds to the database table downsample_group_column
     * @mbg.generated
     */
    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        DownsampleGroupColumn other = (DownsampleGroupColumn) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId())) && (this.getQueryGroupId() == null ? other.getQueryGroupId() == null : this.getQueryGroupId().equals(other.getQueryGroupId())) && (this.getColumn() == null ? other.getColumn() == null : this.getColumn().equals(other.getColumn()));
    }

    /**
     * This method was generated by MyBatis Generator. This method corresponds to the database table downsample_group_column
     * @mbg.generated
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getQueryGroupId() == null) ? 0 : getQueryGroupId().hashCode());
        result = prime * result + ((getColumn() == null) ? 0 : getColumn().hashCode());
        return result;
    }
}