package edu.pitt.medschool.model.dto;

import java.time.LocalDateTime;

public class AggregationDatabase {

	/**
	 * This field was generated by MyBatis Generator. This field corresponds to the database column aggregation_database.id
	 * @mbg.generated
	 */
	private Integer id;
	/**
	 * This field was generated by MyBatis Generator. This field corresponds to the database column aggregation_database.db_name
	 * @mbg.generated
	 */
	private String dbName;
	/**
	 * This field was generated by MyBatis Generator. This field corresponds to the database column aggregation_database.version
	 * @mbg.generated
	 */
	private Integer version;
	/**
	 * This field was generated by MyBatis Generator. This field corresponds to the database column aggregation_database.aggregate_time
	 * @mbg.generated
	 */
	private Integer aggregateTime;
	/**
	 * This field was generated by MyBatis Generator. This field corresponds to the database column aggregation_database.create_time
	 * @mbg.generated
	 */
	private LocalDateTime createTime;
	/**
	 * This field was generated by MyBatis Generator. This field corresponds to the database column aggregation_database.mean
	 * @mbg.generated
	 */
	private Boolean mean;
	/**
	 * This field was generated by MyBatis Generator. This field corresponds to the database column aggregation_database.max
	 * @mbg.generated
	 */
	private Boolean max;
	/**
	 * This field was generated by MyBatis Generator. This field corresponds to the database column aggregation_database.min
	 * @mbg.generated
	 */
	private Boolean min;
	/**
	 * This field was generated by MyBatis Generator. This field corresponds to the database column aggregation_database.sd
	 * @mbg.generated
	 */
	private Boolean sd;
	/**
	 * This field was generated by MyBatis Generator. This field corresponds to the database column aggregation_database.median
	 * @mbg.generated
	 */
	private Boolean median;
	/**
	 * This field was generated by MyBatis Generator. This field corresponds to the database column aggregation_database.q1
	 * @mbg.generated
	 */
	private Boolean q1;
	/**
	 * This field was generated by MyBatis Generator. This field corresponds to the database column aggregation_database.q3
	 * @mbg.generated
	 */
	private Boolean q3;
	/**
	 * This field was generated by MyBatis Generator. This field corresponds to the database column aggregation_database.sum
	 * @mbg.generated
	 */
	private Boolean sum;
	/**
	 * This field was generated by MyBatis Generator. This field corresponds to the database column aggregation_database.status
	 * @mbg.generated
	 */
	private String status;
	/**
	 * This field was generated by MyBatis Generator. This field corresponds to the database column aggregation_database.total
	 * @mbg.generated
	 */
	private Integer total;
	/**
	 * This field was generated by MyBatis Generator. This field corresponds to the database column aggregation_database.finished
	 * @mbg.generated
	 */
	private Integer finished;
	/**
	 * This field was generated by MyBatis Generator. This field corresponds to the database column aggregation_database.auto_update
	 * @mbg.generated
	 */
	private Boolean autoUpdate;
	/**
	 * This field was generated by MyBatis Generator. This field corresponds to the database column aggregation_database.arType
	 * @mbg.generated
	 */
	private Boolean artype;
	/**
	 * This field was generated by MyBatis Generator. This field corresponds to the database column aggregation_database.from_db
	 * @mbg.generated
	 */
	private String fromDb;
	/**
	 * This field was generated by MyBatis Generator. This field corresponds to the database column aggregation_database.time_cost
	 * @mbg.generated
	 */
	private String timeCost;
	/**
	 * This field was generated by MyBatis Generator. This field corresponds to the database column aggregation_database.threads
	 * @mbg.generated
	 */
	private Integer threads;
	/**
	 * This field was generated by MyBatis Generator. This field corresponds to the database column aggregation_database.parts
	 * @mbg.generated
	 */
	private Integer parts;
	/**
	 * This field was generated by MyBatis Generator. This field corresponds to the database column aggregation_database.nday
	 * @mbg.generated
	 */
	private Integer nday;

	/**
	 * This method was generated by MyBatis Generator. This method returns the value of the database column aggregation_database.id
	 * @return  the value of aggregation_database.id
	 * @mbg.generated
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * This method was generated by MyBatis Generator. This method sets the value of the database column aggregation_database.id
	 * @param id  the value for aggregation_database.id
	 * @mbg.generated
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * This method was generated by MyBatis Generator. This method returns the value of the database column aggregation_database.db_name
	 * @return  the value of aggregation_database.db_name
	 * @mbg.generated
	 */
	public String getDbName() {
		return dbName;
	}

	/**
	 * This method was generated by MyBatis Generator. This method sets the value of the database column aggregation_database.db_name
	 * @param dbName  the value for aggregation_database.db_name
	 * @mbg.generated
	 */
	public void setDbName(String dbName) {
		this.dbName = dbName == null ? null : dbName.trim();
	}

	/**
	 * This method was generated by MyBatis Generator. This method returns the value of the database column aggregation_database.version
	 * @return  the value of aggregation_database.version
	 * @mbg.generated
	 */
	public Integer getVersion() {
		return version;
	}

	/**
	 * This method was generated by MyBatis Generator. This method sets the value of the database column aggregation_database.version
	 * @param version  the value for aggregation_database.version
	 * @mbg.generated
	 */
	public void setVersion(Integer version) {
		this.version = version;
	}

	/**
	 * This method was generated by MyBatis Generator. This method returns the value of the database column aggregation_database.aggregate_time
	 * @return  the value of aggregation_database.aggregate_time
	 * @mbg.generated
	 */
	public Integer getAggregateTime() {
		return aggregateTime;
	}

	/**
	 * This method was generated by MyBatis Generator. This method sets the value of the database column aggregation_database.aggregate_time
	 * @param aggregateTime  the value for aggregation_database.aggregate_time
	 * @mbg.generated
	 */
	public void setAggregateTime(Integer aggregateTime) {
		this.aggregateTime = aggregateTime;
	}

	/**
	 * This method was generated by MyBatis Generator. This method returns the value of the database column aggregation_database.create_time
	 * @return  the value of aggregation_database.create_time
	 * @mbg.generated
	 */
	public LocalDateTime getCreateTime() {
		return createTime;
	}

	/**
	 * This method was generated by MyBatis Generator. This method sets the value of the database column aggregation_database.create_time
	 * @param createTime  the value for aggregation_database.create_time
	 * @mbg.generated
	 */
	public void setCreateTime(LocalDateTime createTime) {
		this.createTime = createTime;
	}

	/**
	 * This method was generated by MyBatis Generator. This method returns the value of the database column aggregation_database.mean
	 * @return  the value of aggregation_database.mean
	 * @mbg.generated
	 */
	public Boolean getMean() {
		return mean;
	}

	/**
	 * This method was generated by MyBatis Generator. This method sets the value of the database column aggregation_database.mean
	 * @param mean  the value for aggregation_database.mean
	 * @mbg.generated
	 */
	public void setMean(Boolean mean) {
		this.mean = mean;
	}

	/**
	 * This method was generated by MyBatis Generator. This method returns the value of the database column aggregation_database.max
	 * @return  the value of aggregation_database.max
	 * @mbg.generated
	 */
	public Boolean getMax() {
		return max;
	}

	/**
	 * This method was generated by MyBatis Generator. This method sets the value of the database column aggregation_database.max
	 * @param max  the value for aggregation_database.max
	 * @mbg.generated
	 */
	public void setMax(Boolean max) {
		this.max = max;
	}

	/**
	 * This method was generated by MyBatis Generator. This method returns the value of the database column aggregation_database.min
	 * @return  the value of aggregation_database.min
	 * @mbg.generated
	 */
	public Boolean getMin() {
		return min;
	}

	/**
	 * This method was generated by MyBatis Generator. This method sets the value of the database column aggregation_database.min
	 * @param min  the value for aggregation_database.min
	 * @mbg.generated
	 */
	public void setMin(Boolean min) {
		this.min = min;
	}

	/**
	 * This method was generated by MyBatis Generator. This method returns the value of the database column aggregation_database.sd
	 * @return  the value of aggregation_database.sd
	 * @mbg.generated
	 */
	public Boolean getSd() {
		return sd;
	}

	/**
	 * This method was generated by MyBatis Generator. This method sets the value of the database column aggregation_database.sd
	 * @param sd  the value for aggregation_database.sd
	 * @mbg.generated
	 */
	public void setSd(Boolean sd) {
		this.sd = sd;
	}

	/**
	 * This method was generated by MyBatis Generator. This method returns the value of the database column aggregation_database.median
	 * @return  the value of aggregation_database.median
	 * @mbg.generated
	 */
	public Boolean getMedian() {
		return median;
	}

	/**
	 * This method was generated by MyBatis Generator. This method sets the value of the database column aggregation_database.median
	 * @param median  the value for aggregation_database.median
	 * @mbg.generated
	 */
	public void setMedian(Boolean median) {
		this.median = median;
	}

	/**
	 * This method was generated by MyBatis Generator. This method returns the value of the database column aggregation_database.q1
	 * @return  the value of aggregation_database.q1
	 * @mbg.generated
	 */
	public Boolean getQ1() {
		return q1;
	}

	/**
	 * This method was generated by MyBatis Generator. This method sets the value of the database column aggregation_database.q1
	 * @param q1  the value for aggregation_database.q1
	 * @mbg.generated
	 */
	public void setQ1(Boolean q1) {
		this.q1 = q1;
	}

	/**
	 * This method was generated by MyBatis Generator. This method returns the value of the database column aggregation_database.q3
	 * @return  the value of aggregation_database.q3
	 * @mbg.generated
	 */
	public Boolean getQ3() {
		return q3;
	}

	/**
	 * This method was generated by MyBatis Generator. This method sets the value of the database column aggregation_database.q3
	 * @param q3  the value for aggregation_database.q3
	 * @mbg.generated
	 */
	public void setQ3(Boolean q3) {
		this.q3 = q3;
	}

	/**
	 * This method was generated by MyBatis Generator. This method returns the value of the database column aggregation_database.sum
	 * @return  the value of aggregation_database.sum
	 * @mbg.generated
	 */
	public Boolean getSum() {
		return sum;
	}

	/**
	 * This method was generated by MyBatis Generator. This method sets the value of the database column aggregation_database.sum
	 * @param sum  the value for aggregation_database.sum
	 * @mbg.generated
	 */
	public void setSum(Boolean sum) {
		this.sum = sum;
	}

	/**
	 * This method was generated by MyBatis Generator. This method returns the value of the database column aggregation_database.status
	 * @return  the value of aggregation_database.status
	 * @mbg.generated
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * This method was generated by MyBatis Generator. This method sets the value of the database column aggregation_database.status
	 * @param status  the value for aggregation_database.status
	 * @mbg.generated
	 */
	public void setStatus(String status) {
		this.status = status == null ? null : status.trim();
	}

	/**
	 * This method was generated by MyBatis Generator. This method returns the value of the database column aggregation_database.total
	 * @return  the value of aggregation_database.total
	 * @mbg.generated
	 */
	public Integer getTotal() {
		return total;
	}

	/**
	 * This method was generated by MyBatis Generator. This method sets the value of the database column aggregation_database.total
	 * @param total  the value for aggregation_database.total
	 * @mbg.generated
	 */
	public void setTotal(Integer total) {
		this.total = total;
	}

	/**
	 * This method was generated by MyBatis Generator. This method returns the value of the database column aggregation_database.finished
	 * @return  the value of aggregation_database.finished
	 * @mbg.generated
	 */
	public Integer getFinished() {
		return finished;
	}

	/**
	 * This method was generated by MyBatis Generator. This method sets the value of the database column aggregation_database.finished
	 * @param finished  the value for aggregation_database.finished
	 * @mbg.generated
	 */
	public void setFinished(Integer finished) {
		this.finished = finished;
	}

	/**
	 * This method was generated by MyBatis Generator. This method returns the value of the database column aggregation_database.auto_update
	 * @return  the value of aggregation_database.auto_update
	 * @mbg.generated
	 */
	public Boolean getAutoUpdate() {
		return autoUpdate;
	}

	/**
	 * This method was generated by MyBatis Generator. This method sets the value of the database column aggregation_database.auto_update
	 * @param autoUpdate  the value for aggregation_database.auto_update
	 * @mbg.generated
	 */
	public void setAutoUpdate(Boolean autoUpdate) {
		this.autoUpdate = autoUpdate;
	}

	/**
	 * This method was generated by MyBatis Generator. This method returns the value of the database column aggregation_database.arType
	 * @return  the value of aggregation_database.arType
	 * @mbg.generated
	 */
	public Boolean getArtype() {
		return artype;
	}

	/**
	 * This method was generated by MyBatis Generator. This method sets the value of the database column aggregation_database.arType
	 * @param artype  the value for aggregation_database.arType
	 * @mbg.generated
	 */
	public void setArtype(Boolean artype) {
		this.artype = artype;
	}

	/**
	 * This method was generated by MyBatis Generator. This method returns the value of the database column aggregation_database.from_db
	 * @return  the value of aggregation_database.from_db
	 * @mbg.generated
	 */
	public String getFromDb() {
		return fromDb;
	}

	/**
	 * This method was generated by MyBatis Generator. This method sets the value of the database column aggregation_database.from_db
	 * @param fromDb  the value for aggregation_database.from_db
	 * @mbg.generated
	 */
	public void setFromDb(String fromDb) {
		this.fromDb = fromDb == null ? null : fromDb.trim();
	}

	/**
	 * This method was generated by MyBatis Generator. This method returns the value of the database column aggregation_database.time_cost
	 * @return  the value of aggregation_database.time_cost
	 * @mbg.generated
	 */
	public String getTimeCost() {
		return timeCost;
	}

	/**
	 * This method was generated by MyBatis Generator. This method sets the value of the database column aggregation_database.time_cost
	 * @param timeCost  the value for aggregation_database.time_cost
	 * @mbg.generated
	 */
	public void setTimeCost(String timeCost) {
		this.timeCost = timeCost == null ? null : timeCost.trim();
	}

	/**
	 * This method was generated by MyBatis Generator. This method returns the value of the database column aggregation_database.threads
	 * @return  the value of aggregation_database.threads
	 * @mbg.generated
	 */
	public Integer getThreads() {
		return threads;
	}

	/**
	 * This method was generated by MyBatis Generator. This method sets the value of the database column aggregation_database.threads
	 * @param threads  the value for aggregation_database.threads
	 * @mbg.generated
	 */
	public void setThreads(Integer threads) {
		this.threads = threads;
	}

	/**
	 * This method was generated by MyBatis Generator. This method returns the value of the database column aggregation_database.parts
	 * @return  the value of aggregation_database.parts
	 * @mbg.generated
	 */
	public Integer getParts() {
		return parts;
	}

	/**
	 * This method was generated by MyBatis Generator. This method sets the value of the database column aggregation_database.parts
	 * @param parts  the value for aggregation_database.parts
	 * @mbg.generated
	 */
	public void setParts(Integer parts) {
		this.parts = parts;
	}

	/**
	 * This method was generated by MyBatis Generator. This method returns the value of the database column aggregation_database.nday
	 * @return  the value of aggregation_database.nday
	 * @mbg.generated
	 */
	public Integer getNday() {
		return nday;
	}

	/**
	 * This method was generated by MyBatis Generator. This method sets the value of the database column aggregation_database.nday
	 * @param nday  the value for aggregation_database.nday
	 * @mbg.generated
	 */
	public void setNday(Integer nday) {
		this.nday = nday;
	}

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table aggregation_database
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
		AggregationDatabase other = (AggregationDatabase) that;
		return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
				&& (this.getDbName() == null ? other.getDbName() == null : this.getDbName().equals(other.getDbName()))
				&& (this.getVersion() == null ? other.getVersion() == null
						: this.getVersion().equals(other.getVersion()))
				&& (this.getAggregateTime() == null ? other.getAggregateTime() == null
						: this.getAggregateTime().equals(other.getAggregateTime()))
				&& (this.getCreateTime() == null ? other.getCreateTime() == null
						: this.getCreateTime().equals(other.getCreateTime()))
				&& (this.getMean() == null ? other.getMean() == null : this.getMean().equals(other.getMean()))
				&& (this.getMax() == null ? other.getMax() == null : this.getMax().equals(other.getMax()))
				&& (this.getMin() == null ? other.getMin() == null : this.getMin().equals(other.getMin()))
				&& (this.getSd() == null ? other.getSd() == null : this.getSd().equals(other.getSd()))
				&& (this.getMedian() == null ? other.getMedian() == null : this.getMedian().equals(other.getMedian()))
				&& (this.getQ1() == null ? other.getQ1() == null : this.getQ1().equals(other.getQ1()))
				&& (this.getQ3() == null ? other.getQ3() == null : this.getQ3().equals(other.getQ3()))
				&& (this.getSum() == null ? other.getSum() == null : this.getSum().equals(other.getSum()))
				&& (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
				&& (this.getTotal() == null ? other.getTotal() == null : this.getTotal().equals(other.getTotal()))
				&& (this.getFinished() == null ? other.getFinished() == null
						: this.getFinished().equals(other.getFinished()))
				&& (this.getAutoUpdate() == null ? other.getAutoUpdate() == null
						: this.getAutoUpdate().equals(other.getAutoUpdate()))
				&& (this.getArtype() == null ? other.getArtype() == null : this.getArtype().equals(other.getArtype()))
				&& (this.getFromDb() == null ? other.getFromDb() == null : this.getFromDb().equals(other.getFromDb()))
				&& (this.getTimeCost() == null ? other.getTimeCost() == null
						: this.getTimeCost().equals(other.getTimeCost()))
				&& (this.getThreads() == null ? other.getThreads() == null
						: this.getThreads().equals(other.getThreads()))
				&& (this.getParts() == null ? other.getParts() == null : this.getParts().equals(other.getParts()))
				&& (this.getNday() == null ? other.getNday() == null : this.getNday().equals(other.getNday()));
	}

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table aggregation_database
	 * @mbg.generated
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
		result = prime * result + ((getDbName() == null) ? 0 : getDbName().hashCode());
		result = prime * result + ((getVersion() == null) ? 0 : getVersion().hashCode());
		result = prime * result + ((getAggregateTime() == null) ? 0 : getAggregateTime().hashCode());
		result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
		result = prime * result + ((getMean() == null) ? 0 : getMean().hashCode());
		result = prime * result + ((getMax() == null) ? 0 : getMax().hashCode());
		result = prime * result + ((getMin() == null) ? 0 : getMin().hashCode());
		result = prime * result + ((getSd() == null) ? 0 : getSd().hashCode());
		result = prime * result + ((getMedian() == null) ? 0 : getMedian().hashCode());
		result = prime * result + ((getQ1() == null) ? 0 : getQ1().hashCode());
		result = prime * result + ((getQ3() == null) ? 0 : getQ3().hashCode());
		result = prime * result + ((getSum() == null) ? 0 : getSum().hashCode());
		result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
		result = prime * result + ((getTotal() == null) ? 0 : getTotal().hashCode());
		result = prime * result + ((getFinished() == null) ? 0 : getFinished().hashCode());
		result = prime * result + ((getAutoUpdate() == null) ? 0 : getAutoUpdate().hashCode());
		result = prime * result + ((getArtype() == null) ? 0 : getArtype().hashCode());
		result = prime * result + ((getFromDb() == null) ? 0 : getFromDb().hashCode());
		result = prime * result + ((getTimeCost() == null) ? 0 : getTimeCost().hashCode());
		result = prime * result + ((getThreads() == null) ? 0 : getThreads().hashCode());
		result = prime * result + ((getParts() == null) ? 0 : getParts().hashCode());
		result = prime * result + ((getNday() == null) ? 0 : getNday().hashCode());
		return result;
	}
}