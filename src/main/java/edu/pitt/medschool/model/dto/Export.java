package edu.pitt.medschool.model.dto;

import java.util.Date;

public class Export {

    /**
     * This field was generated by MyBatis Generator. This field corresponds to the database column export.id
     * @mbg.generated
     */
    private Integer id;
    /**
     * This field was generated by MyBatis Generator. This field corresponds to the database column export.query_id
     * @mbg.generated
     */
    private Integer queryId;
    /**
     * This field was generated by MyBatis Generator. This field corresponds to the database column export.ar
     * @mbg.generated
     */
    private Boolean ar;
    /**
     * This field was generated by MyBatis Generator. This field corresponds to the database column export.layout
     * @mbg.generated
     */
    private Boolean layout;
    /**
     * This field was generated by MyBatis Generator. This field corresponds to the database column export.finished
     * @mbg.generated
     */
    private Boolean finished;
    /**
     * This field was generated by MyBatis Generator. This field corresponds to the database column export.machine
     * @mbg.generated
     */
    private String machine;
    /**
     * This field was generated by MyBatis Generator. This field corresponds to the database column export.db_version
     * @mbg.generated
     */
    private String dbVersion;
    /**
     * This field was generated by MyBatis Generator. This field corresponds to the database column export.create_time
     * @mbg.generated
     */
    private Date createTime;
    /**
     * This field was generated by MyBatis Generator. This field corresponds to the database column export.update_time
     * @mbg.generated
     */
    private Date updateTime;

    /**
     * This method was generated by MyBatis Generator. This method returns the value of the database column export.id
     * @return  the value of export.id
     * @mbg.generated
     */
    public Integer getId() {
        return id;
    }

    /**
     * This method was generated by MyBatis Generator. This method sets the value of the database column export.id
     * @param id  the value for export.id
     * @mbg.generated
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * This method was generated by MyBatis Generator. This method returns the value of the database column export.query_id
     * @return  the value of export.query_id
     * @mbg.generated
     */
    public Integer getQueryId() {
        return queryId;
    }

    /**
     * This method was generated by MyBatis Generator. This method sets the value of the database column export.query_id
     * @param queryId  the value for export.query_id
     * @mbg.generated
     */
    public void setQueryId(Integer queryId) {
        this.queryId = queryId;
    }

    /**
     * This method was generated by MyBatis Generator. This method returns the value of the database column export.ar
     * @return  the value of export.ar
     * @mbg.generated
     */
    public Boolean getAr() {
        return ar;
    }

    /**
     * This method was generated by MyBatis Generator. This method sets the value of the database column export.ar
     * @param ar  the value for export.ar
     * @mbg.generated
     */
    public void setAr(Boolean ar) {
        this.ar = ar;
    }

    /**
     * This method was generated by MyBatis Generator. This method returns the value of the database column export.layout
     * @return  the value of export.layout
     * @mbg.generated
     */
    public Boolean getLayout() {
        return layout;
    }

    /**
     * This method was generated by MyBatis Generator. This method sets the value of the database column export.layout
     * @param layout  the value for export.layout
     * @mbg.generated
     */
    public void setLayout(Boolean layout) {
        this.layout = layout;
    }

    /**
     * This method was generated by MyBatis Generator. This method returns the value of the database column export.finished
     * @return  the value of export.finished
     * @mbg.generated
     */
    public Boolean getFinished() {
        return finished;
    }

    /**
     * This method was generated by MyBatis Generator. This method sets the value of the database column export.finished
     * @param finished  the value for export.finished
     * @mbg.generated
     */
    public void setFinished(Boolean finished) {
        this.finished = finished;
    }

    /**
     * This method was generated by MyBatis Generator. This method returns the value of the database column export.machine
     * @return  the value of export.machine
     * @mbg.generated
     */
    public String getMachine() {
        return machine;
    }

    /**
     * This method was generated by MyBatis Generator. This method sets the value of the database column export.machine
     * @param machine  the value for export.machine
     * @mbg.generated
     */
    public void setMachine(String machine) {
        this.machine = machine == null ? null : machine.trim();
    }

    /**
     * This method was generated by MyBatis Generator. This method returns the value of the database column export.db_version
     * @return  the value of export.db_version
     * @mbg.generated
     */
    public String getDbVersion() {
        return dbVersion;
    }

    /**
     * This method was generated by MyBatis Generator. This method sets the value of the database column export.db_version
     * @param dbVersion  the value for export.db_version
     * @mbg.generated
     */
    public void setDbVersion(String dbVersion) {
        this.dbVersion = dbVersion == null ? null : dbVersion.trim();
    }

    /**
     * This method was generated by MyBatis Generator. This method returns the value of the database column export.create_time
     * @return  the value of export.create_time
     * @mbg.generated
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * This method was generated by MyBatis Generator. This method sets the value of the database column export.create_time
     * @param createTime  the value for export.create_time
     * @mbg.generated
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * This method was generated by MyBatis Generator. This method returns the value of the database column export.update_time
     * @return  the value of export.update_time
     * @mbg.generated
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * This method was generated by MyBatis Generator. This method sets the value of the database column export.update_time
     * @param updateTime  the value for export.update_time
     * @mbg.generated
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * This method was generated by MyBatis Generator. This method corresponds to the database table export
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
        Export other = (Export) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
                && (this.getQueryId() == null ? other.getQueryId() == null : this.getQueryId().equals(other.getQueryId()))
                && (this.getAr() == null ? other.getAr() == null : this.getAr().equals(other.getAr()))
                && (this.getLayout() == null ? other.getLayout() == null : this.getLayout().equals(other.getLayout()))
                && (this.getFinished() == null ? other.getFinished() == null : this.getFinished().equals(other.getFinished()))
                && (this.getMachine() == null ? other.getMachine() == null : this.getMachine().equals(other.getMachine()))
                && (this.getDbVersion() == null ? other.getDbVersion() == null : this.getDbVersion().equals(other.getDbVersion()))
                && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
                && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
    }

    /**
     * This method was generated by MyBatis Generator. This method corresponds to the database table export
     * @mbg.generated
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getQueryId() == null) ? 0 : getQueryId().hashCode());
        result = prime * result + ((getAr() == null) ? 0 : getAr().hashCode());
        result = prime * result + ((getLayout() == null) ? 0 : getLayout().hashCode());
        result = prime * result + ((getFinished() == null) ? 0 : getFinished().hashCode());
        result = prime * result + ((getMachine() == null) ? 0 : getMachine().hashCode());
        result = prime * result + ((getDbVersion() == null) ? 0 : getDbVersion().hashCode());
        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
        return result;
    }
}