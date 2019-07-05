package edu.pitt.medschool.model.dto;

import java.util.Date;
import java.time.LocalDateTime;

public class MedicalDownsample {

	/**
	 * This field was generated by MyBatis Generator. This field corresponds to the database column medical_downsample.id
	 * @mbg.generated
	 */
	private Integer id;
	/**
	 * This field was generated by MyBatis Generator. This field corresponds to the database column medical_downsample.alias
	 * @mbg.generated
	 */
	private String alias;
	/**
	 * This field was generated by MyBatis Generator. This field corresponds to the database column medical_downsample.medicine
	 * @mbg.generated
	 */
	private String medicine;
	/**
	 * This field was generated by MyBatis Generator. This field corresponds to the database column medical_downsample.before_medicine
	 * @mbg.generated
	 */
	private Integer beforeMedicine;
	/**
	 * This field was generated by MyBatis Generator. This field corresponds to the database column medical_downsample.after_medicine
	 * @mbg.generated
	 */
	private Integer afterMedicine;
	/**
	 * This field was generated by MyBatis Generator. This field corresponds to the database column medical_downsample.period
	 * @mbg.generated
	 */
	private Integer period;
	/**
	 * This field was generated by MyBatis Generator. This field corresponds to the database column medical_downsample.min_bin
	 * @mbg.generated
	 */
	private Integer minBin;
	/**
	 * This field was generated by MyBatis Generator. This field corresponds to the database column medical_downsample.min_bin_row
	 * @mbg.generated
	 */
	private Integer minBinRow;
	/**
	 * This field was generated by MyBatis Generator. This field corresponds to the database column medical_downsample.downsample_first
	 * @mbg.generated
	 */
	private Boolean downsampleFirst;
	/**
	 * This field was generated by MyBatis Generator. This field corresponds to the database column medical_downsample.create_time
	 * @mbg.generated
	 */
	private LocalDateTime createTime;
	/**
	 * This field was generated by MyBatis Generator. This field corresponds to the database column medical_downsample.update_time
	 * @mbg.generated
	 */
	private LocalDateTime updateTime;
	/**
	 * This field was generated by MyBatis Generator. This field corresponds to the database column medical_downsample.deleted
	 * @mbg.generated
	 */
	private Boolean deleted;
	/**
	 * This field was generated by MyBatis Generator. This field corresponds to the database column medical_downsample.data_before_medicine
	 * @mbg.generated
	 */
	private Boolean dataBeforeMedicine;
	/**
	 * This field was generated by MyBatis Generator. This field corresponds to the database column medical_downsample.only_first
	 * @mbg.generated
	 */
	private Boolean onlyFirst;

	/**
	 * This method was generated by MyBatis Generator. This method returns the value of the database column medical_downsample.id
	 * @return  the value of medical_downsample.id
	 * @mbg.generated
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * This method was generated by MyBatis Generator. This method sets the value of the database column medical_downsample.id
	 * @param id  the value for medical_downsample.id
	 * @mbg.generated
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * This method was generated by MyBatis Generator. This method returns the value of the database column medical_downsample.alias
	 * @return  the value of medical_downsample.alias
	 * @mbg.generated
	 */
	public String getAlias() {
		return alias;
	}

	/**
	 * This method was generated by MyBatis Generator. This method sets the value of the database column medical_downsample.alias
	 * @param alias  the value for medical_downsample.alias
	 * @mbg.generated
	 */
	public void setAlias(String alias) {
		this.alias = alias == null ? null : alias.trim();
	}

	/**
	 * This method was generated by MyBatis Generator. This method returns the value of the database column medical_downsample.medicine
	 * @return  the value of medical_downsample.medicine
	 * @mbg.generated
	 */
	public String getMedicine() {
		return medicine;
	}

	/**
	 * This method was generated by MyBatis Generator. This method sets the value of the database column medical_downsample.medicine
	 * @param medicine  the value for medical_downsample.medicine
	 * @mbg.generated
	 */
	public void setMedicine(String medicine) {
		this.medicine = medicine == null ? null : medicine.trim();
	}

	/**
	 * This method was generated by MyBatis Generator. This method returns the value of the database column medical_downsample.before_medicine
	 * @return  the value of medical_downsample.before_medicine
	 * @mbg.generated
	 */
	public Integer getBeforeMedicine() {
		return beforeMedicine;
	}

	/**
	 * This method was generated by MyBatis Generator. This method sets the value of the database column medical_downsample.before_medicine
	 * @param beforeMedicine  the value for medical_downsample.before_medicine
	 * @mbg.generated
	 */
	public void setBeforeMedicine(Integer beforeMedicine) {
		this.beforeMedicine = beforeMedicine;
	}

	/**
	 * This method was generated by MyBatis Generator. This method returns the value of the database column medical_downsample.after_medicine
	 * @return  the value of medical_downsample.after_medicine
	 * @mbg.generated
	 */
	public Integer getAfterMedicine() {
		return afterMedicine;
	}

	/**
	 * This method was generated by MyBatis Generator. This method sets the value of the database column medical_downsample.after_medicine
	 * @param afterMedicine  the value for medical_downsample.after_medicine
	 * @mbg.generated
	 */
	public void setAfterMedicine(Integer afterMedicine) {
		this.afterMedicine = afterMedicine;
	}

	/**
	 * This method was generated by MyBatis Generator. This method returns the value of the database column medical_downsample.period
	 * @return  the value of medical_downsample.period
	 * @mbg.generated
	 */
	public Integer getPeriod() {
		return period;
	}

	/**
	 * This method was generated by MyBatis Generator. This method sets the value of the database column medical_downsample.period
	 * @param period  the value for medical_downsample.period
	 * @mbg.generated
	 */
	public void setPeriod(Integer period) {
		this.period = period;
	}

	/**
	 * This method was generated by MyBatis Generator. This method returns the value of the database column medical_downsample.min_bin
	 * @return  the value of medical_downsample.min_bin
	 * @mbg.generated
	 */
	public Integer getMinBin() {
		return minBin;
	}

	/**
	 * This method was generated by MyBatis Generator. This method sets the value of the database column medical_downsample.min_bin
	 * @param minBin  the value for medical_downsample.min_bin
	 * @mbg.generated
	 */
	public void setMinBin(Integer minBin) {
		this.minBin = minBin;
	}

	/**
	 * This method was generated by MyBatis Generator. This method returns the value of the database column medical_downsample.min_bin_row
	 * @return  the value of medical_downsample.min_bin_row
	 * @mbg.generated
	 */
	public Integer getMinBinRow() {
		return minBinRow;
	}

	/**
	 * This method was generated by MyBatis Generator. This method sets the value of the database column medical_downsample.min_bin_row
	 * @param minBinRow  the value for medical_downsample.min_bin_row
	 * @mbg.generated
	 */
	public void setMinBinRow(Integer minBinRow) {
		this.minBinRow = minBinRow;
	}

	/**
	 * This method was generated by MyBatis Generator. This method returns the value of the database column medical_downsample.downsample_first
	 * @return  the value of medical_downsample.downsample_first
	 * @mbg.generated
	 */
	public Boolean getDownsampleFirst() {
		return downsampleFirst;
	}

	/**
	 * This method was generated by MyBatis Generator. This method sets the value of the database column medical_downsample.downsample_first
	 * @param downsampleFirst  the value for medical_downsample.downsample_first
	 * @mbg.generated
	 */
	public void setDownsampleFirst(Boolean downsampleFirst) {
		this.downsampleFirst = downsampleFirst;
	}

	/**
	 * This method was generated by MyBatis Generator. This method returns the value of the database column medical_downsample.create_time
	 * @return  the value of medical_downsample.create_time
	 * @mbg.generated
	 */
	public LocalDateTime getCreateTime() {
		return createTime;
	}

	/**
	 * This method was generated by MyBatis Generator. This method sets the value of the database column medical_downsample.create_time
	 * @param createTime  the value for medical_downsample.create_time
	 * @mbg.generated
	 */
	public void setCreateTime(LocalDateTime createTime) {
		this.createTime = createTime;
	}

	/**
	 * This method was generated by MyBatis Generator. This method returns the value of the database column medical_downsample.update_time
	 * @return  the value of medical_downsample.update_time
	 * @mbg.generated
	 */
	public LocalDateTime getUpdateTime() {
		return updateTime;
	}

	/**
	 * This method was generated by MyBatis Generator. This method sets the value of the database column medical_downsample.update_time
	 * @param updateTime  the value for medical_downsample.update_time
	 * @mbg.generated
	 */
	public void setUpdateTime(LocalDateTime updateTime) {
		this.updateTime = updateTime;
	}

	/**
	 * This method was generated by MyBatis Generator. This method returns the value of the database column medical_downsample.deleted
	 * @return  the value of medical_downsample.deleted
	 * @mbg.generated
	 */
	public Boolean getDeleted() {
		return deleted;
	}

	/**
	 * This method was generated by MyBatis Generator. This method sets the value of the database column medical_downsample.deleted
	 * @param deleted  the value for medical_downsample.deleted
	 * @mbg.generated
	 */
	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	/**
	 * This method was generated by MyBatis Generator. This method returns the value of the database column medical_downsample.data_before_medicine
	 * @return  the value of medical_downsample.data_before_medicine
	 * @mbg.generated
	 */
	public Boolean getDataBeforeMedicine() {
		return dataBeforeMedicine;
	}

	/**
	 * This method was generated by MyBatis Generator. This method sets the value of the database column medical_downsample.data_before_medicine
	 * @param dataBeforeMedicine  the value for medical_downsample.data_before_medicine
	 * @mbg.generated
	 */
	public void setDataBeforeMedicine(Boolean dataBeforeMedicine) {
		this.dataBeforeMedicine = dataBeforeMedicine;
	}

	/**
	 * This method was generated by MyBatis Generator. This method returns the value of the database column medical_downsample.only_first
	 * @return  the value of medical_downsample.only_first
	 * @mbg.generated
	 */
	public Boolean getOnlyFirst() {
		return onlyFirst;
	}

	/**
	 * This method was generated by MyBatis Generator. This method sets the value of the database column medical_downsample.only_first
	 * @param onlyFirst  the value for medical_downsample.only_first
	 * @mbg.generated
	 */
	public void setOnlyFirst(Boolean onlyFirst) {
		this.onlyFirst = onlyFirst;
	}

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table medical_downsample
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
		MedicalDownsample other = (MedicalDownsample) that;
		return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
				&& (this.getAlias() == null ? other.getAlias() == null : this.getAlias().equals(other.getAlias()))
				&& (this.getMedicine() == null ? other.getMedicine() == null
						: this.getMedicine().equals(other.getMedicine()))
				&& (this.getBeforeMedicine() == null ? other.getBeforeMedicine() == null
						: this.getBeforeMedicine().equals(other.getBeforeMedicine()))
				&& (this.getAfterMedicine() == null ? other.getAfterMedicine() == null
						: this.getAfterMedicine().equals(other.getAfterMedicine()))
				&& (this.getPeriod() == null ? other.getPeriod() == null : this.getPeriod().equals(other.getPeriod()))
				&& (this.getMinBin() == null ? other.getMinBin() == null : this.getMinBin().equals(other.getMinBin()))
				&& (this.getMinBinRow() == null ? other.getMinBinRow() == null
						: this.getMinBinRow().equals(other.getMinBinRow()))
				&& (this.getDownsampleFirst() == null ? other.getDownsampleFirst() == null
						: this.getDownsampleFirst().equals(other.getDownsampleFirst()))
				&& (this.getCreateTime() == null ? other.getCreateTime() == null
						: this.getCreateTime().equals(other.getCreateTime()))
				&& (this.getUpdateTime() == null ? other.getUpdateTime() == null
						: this.getUpdateTime().equals(other.getUpdateTime()))
				&& (this.getDeleted() == null ? other.getDeleted() == null
						: this.getDeleted().equals(other.getDeleted()))
				&& (this.getDataBeforeMedicine() == null ? other.getDataBeforeMedicine() == null
						: this.getDataBeforeMedicine().equals(other.getDataBeforeMedicine()))
				&& (this.getOnlyFirst() == null ? other.getOnlyFirst() == null
						: this.getOnlyFirst().equals(other.getOnlyFirst()));
	}

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table medical_downsample
	 * @mbg.generated
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
		result = prime * result + ((getAlias() == null) ? 0 : getAlias().hashCode());
		result = prime * result + ((getMedicine() == null) ? 0 : getMedicine().hashCode());
		result = prime * result + ((getBeforeMedicine() == null) ? 0 : getBeforeMedicine().hashCode());
		result = prime * result + ((getAfterMedicine() == null) ? 0 : getAfterMedicine().hashCode());
		result = prime * result + ((getPeriod() == null) ? 0 : getPeriod().hashCode());
		result = prime * result + ((getMinBin() == null) ? 0 : getMinBin().hashCode());
		result = prime * result + ((getMinBinRow() == null) ? 0 : getMinBinRow().hashCode());
		result = prime * result + ((getDownsampleFirst() == null) ? 0 : getDownsampleFirst().hashCode());
		result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
		result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
		result = prime * result + ((getDeleted() == null) ? 0 : getDeleted().hashCode());
		result = prime * result + ((getDataBeforeMedicine() == null) ? 0 : getDataBeforeMedicine().hashCode());
		result = prime * result + ((getOnlyFirst() == null) ? 0 : getOnlyFirst().hashCode());
		return result;
	}
}