package edu.pitt.medschool.model.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class VersionExample {
    /**
	 * This field was generated by MyBatis Generator. This field corresponds to the database table version
	 * @mbg.generated
	 */
	protected String orderByClause;
	/**
	 * This field was generated by MyBatis Generator. This field corresponds to the database table version
	 * @mbg.generated
	 */
	protected boolean distinct;
	/**
	 * This field was generated by MyBatis Generator. This field corresponds to the database table version
	 * @mbg.generated
	 */
	protected List<Criteria> oredCriteria;

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table version
	 * @mbg.generated
	 */
	public VersionExample() {
		oredCriteria = new ArrayList<Criteria>();
	}

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table version
	 * @mbg.generated
	 */
	public void setOrderByClause(String orderByClause) {
		this.orderByClause = orderByClause;
	}

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table version
	 * @mbg.generated
	 */
	public String getOrderByClause() {
		return orderByClause;
	}

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table version
	 * @mbg.generated
	 */
	public void setDistinct(boolean distinct) {
		this.distinct = distinct;
	}

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table version
	 * @mbg.generated
	 */
	public boolean isDistinct() {
		return distinct;
	}

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table version
	 * @mbg.generated
	 */
	public List<Criteria> getOredCriteria() {
		return oredCriteria;
	}

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table version
	 * @mbg.generated
	 */
	public void or(Criteria criteria) {
		oredCriteria.add(criteria);
	}

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table version
	 * @mbg.generated
	 */
	public Criteria or() {
		Criteria criteria = createCriteriaInternal();
		oredCriteria.add(criteria);
		return criteria;
	}

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table version
	 * @mbg.generated
	 */
	public Criteria createCriteria() {
		Criteria criteria = createCriteriaInternal();
		if (oredCriteria.size() == 0) {
			oredCriteria.add(criteria);
		}
		return criteria;
	}

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table version
	 * @mbg.generated
	 */
	protected Criteria createCriteriaInternal() {
		Criteria criteria = new Criteria();
		return criteria;
	}

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table version
	 * @mbg.generated
	 */
	public void clear() {
		oredCriteria.clear();
		orderByClause = null;
		distinct = false;
	}

	/**
	 * This class was generated by MyBatis Generator. This class corresponds to the database table version
	 * @mbg.generated
	 */
	protected abstract static class GeneratedCriteria {
		protected List<Criterion> criteria;

		protected GeneratedCriteria() {
			super();
			criteria = new ArrayList<Criterion>();
		}

		public boolean isValid() {
			return criteria.size() > 0;
		}

		public List<Criterion> getAllCriteria() {
			return criteria;
		}

		public List<Criterion> getCriteria() {
			return criteria;
		}

		protected void addCriterion(String condition) {
			if (condition == null) {
				throw new RuntimeException("Value for condition cannot be null");
			}
			criteria.add(new Criterion(condition));
		}

		protected void addCriterion(String condition, Object value, String property) {
			if (value == null) {
				throw new RuntimeException("Value for " + property + " cannot be null");
			}
			criteria.add(new Criterion(condition, value));
		}

		protected void addCriterion(String condition, Object value1, Object value2, String property) {
			if (value1 == null || value2 == null) {
				throw new RuntimeException("Between values for " + property + " cannot be null");
			}
			criteria.add(new Criterion(condition, value1, value2));
		}

		public Criteria andVersionIdIsNull() {
			addCriterion("version_id is null");
			return (Criteria) this;
		}

		public Criteria andVersionIdIsNotNull() {
			addCriterion("version_id is not null");
			return (Criteria) this;
		}

		public Criteria andVersionIdEqualTo(Integer value) {
			addCriterion("version_id =", value, "versionId");
			return (Criteria) this;
		}

		public Criteria andVersionIdNotEqualTo(Integer value) {
			addCriterion("version_id <>", value, "versionId");
			return (Criteria) this;
		}

		public Criteria andVersionIdGreaterThan(Integer value) {
			addCriterion("version_id >", value, "versionId");
			return (Criteria) this;
		}

		public Criteria andVersionIdGreaterThanOrEqualTo(Integer value) {
			addCriterion("version_id >=", value, "versionId");
			return (Criteria) this;
		}

		public Criteria andVersionIdLessThan(Integer value) {
			addCriterion("version_id <", value, "versionId");
			return (Criteria) this;
		}

		public Criteria andVersionIdLessThanOrEqualTo(Integer value) {
			addCriterion("version_id <=", value, "versionId");
			return (Criteria) this;
		}

		public Criteria andVersionIdIn(List<Integer> values) {
			addCriterion("version_id in", values, "versionId");
			return (Criteria) this;
		}

		public Criteria andVersionIdNotIn(List<Integer> values) {
			addCriterion("version_id not in", values, "versionId");
			return (Criteria) this;
		}

		public Criteria andVersionIdBetween(Integer value1, Integer value2) {
			addCriterion("version_id between", value1, value2, "versionId");
			return (Criteria) this;
		}

		public Criteria andVersionIdNotBetween(Integer value1, Integer value2) {
			addCriterion("version_id not between", value1, value2, "versionId");
			return (Criteria) this;
		}

		public Criteria andCreateDateIsNull() {
			addCriterion("create_date is null");
			return (Criteria) this;
		}

		public Criteria andCreateDateIsNotNull() {
			addCriterion("create_date is not null");
			return (Criteria) this;
		}

		public Criteria andCreateDateEqualTo(LocalDateTime value) {
			addCriterion("create_date =", value, "createDate");
			return (Criteria) this;
		}

		public Criteria andCreateDateNotEqualTo(LocalDateTime value) {
			addCriterion("create_date <>", value, "createDate");
			return (Criteria) this;
		}

		public Criteria andCreateDateGreaterThan(LocalDateTime value) {
			addCriterion("create_date >", value, "createDate");
			return (Criteria) this;
		}

		public Criteria andCreateDateGreaterThanOrEqualTo(LocalDateTime value) {
			addCriterion("create_date >=", value, "createDate");
			return (Criteria) this;
		}

		public Criteria andCreateDateLessThan(LocalDateTime value) {
			addCriterion("create_date <", value, "createDate");
			return (Criteria) this;
		}

		public Criteria andCreateDateLessThanOrEqualTo(LocalDateTime value) {
			addCriterion("create_date <=", value, "createDate");
			return (Criteria) this;
		}

		public Criteria andCreateDateIn(List<LocalDateTime> values) {
			addCriterion("create_date in", values, "createDate");
			return (Criteria) this;
		}

		public Criteria andCreateDateNotIn(List<LocalDateTime> values) {
			addCriterion("create_date not in", values, "createDate");
			return (Criteria) this;
		}

		public Criteria andCreateDateBetween(LocalDateTime value1, LocalDateTime value2) {
			addCriterion("create_date between", value1, value2, "createDate");
			return (Criteria) this;
		}

		public Criteria andCreateDateNotBetween(LocalDateTime value1, LocalDateTime value2) {
			addCriterion("create_date not between", value1, value2, "createDate");
			return (Criteria) this;
		}

		public Criteria andPatientNumIsNull() {
			addCriterion("patient_num is null");
			return (Criteria) this;
		}

		public Criteria andPatientNumIsNotNull() {
			addCriterion("patient_num is not null");
			return (Criteria) this;
		}

		public Criteria andPatientNumEqualTo(Integer value) {
			addCriterion("patient_num =", value, "patientNum");
			return (Criteria) this;
		}

		public Criteria andPatientNumNotEqualTo(Integer value) {
			addCriterion("patient_num <>", value, "patientNum");
			return (Criteria) this;
		}

		public Criteria andPatientNumGreaterThan(Integer value) {
			addCriterion("patient_num >", value, "patientNum");
			return (Criteria) this;
		}

		public Criteria andPatientNumGreaterThanOrEqualTo(Integer value) {
			addCriterion("patient_num >=", value, "patientNum");
			return (Criteria) this;
		}

		public Criteria andPatientNumLessThan(Integer value) {
			addCriterion("patient_num <", value, "patientNum");
			return (Criteria) this;
		}

		public Criteria andPatientNumLessThanOrEqualTo(Integer value) {
			addCriterion("patient_num <=", value, "patientNum");
			return (Criteria) this;
		}

		public Criteria andPatientNumIn(List<Integer> values) {
			addCriterion("patient_num in", values, "patientNum");
			return (Criteria) this;
		}

		public Criteria andPatientNumNotIn(List<Integer> values) {
			addCriterion("patient_num not in", values, "patientNum");
			return (Criteria) this;
		}

		public Criteria andPatientNumBetween(Integer value1, Integer value2) {
			addCriterion("patient_num between", value1, value2, "patientNum");
			return (Criteria) this;
		}

		public Criteria andPatientNumNotBetween(Integer value1, Integer value2) {
			addCriterion("patient_num not between", value1, value2, "patientNum");
			return (Criteria) this;
		}

		public Criteria andCsvFileNumIsNull() {
			addCriterion("csv_file_num is null");
			return (Criteria) this;
		}

		public Criteria andCsvFileNumIsNotNull() {
			addCriterion("csv_file_num is not null");
			return (Criteria) this;
		}

		public Criteria andCsvFileNumEqualTo(Integer value) {
			addCriterion("csv_file_num =", value, "csvFileNum");
			return (Criteria) this;
		}

		public Criteria andCsvFileNumNotEqualTo(Integer value) {
			addCriterion("csv_file_num <>", value, "csvFileNum");
			return (Criteria) this;
		}

		public Criteria andCsvFileNumGreaterThan(Integer value) {
			addCriterion("csv_file_num >", value, "csvFileNum");
			return (Criteria) this;
		}

		public Criteria andCsvFileNumGreaterThanOrEqualTo(Integer value) {
			addCriterion("csv_file_num >=", value, "csvFileNum");
			return (Criteria) this;
		}

		public Criteria andCsvFileNumLessThan(Integer value) {
			addCriterion("csv_file_num <", value, "csvFileNum");
			return (Criteria) this;
		}

		public Criteria andCsvFileNumLessThanOrEqualTo(Integer value) {
			addCriterion("csv_file_num <=", value, "csvFileNum");
			return (Criteria) this;
		}

		public Criteria andCsvFileNumIn(List<Integer> values) {
			addCriterion("csv_file_num in", values, "csvFileNum");
			return (Criteria) this;
		}

		public Criteria andCsvFileNumNotIn(List<Integer> values) {
			addCriterion("csv_file_num not in", values, "csvFileNum");
			return (Criteria) this;
		}

		public Criteria andCsvFileNumBetween(Integer value1, Integer value2) {
			addCriterion("csv_file_num between", value1, value2, "csvFileNum");
			return (Criteria) this;
		}

		public Criteria andCsvFileNumNotBetween(Integer value1, Integer value2) {
			addCriterion("csv_file_num not between", value1, value2, "csvFileNum");
			return (Criteria) this;
		}

		public Criteria andMedicationNumIsNull() {
			addCriterion("medication_num is null");
			return (Criteria) this;
		}

		public Criteria andMedicationNumIsNotNull() {
			addCriterion("medication_num is not null");
			return (Criteria) this;
		}

		public Criteria andMedicationNumEqualTo(Integer value) {
			addCriterion("medication_num =", value, "medicationNum");
			return (Criteria) this;
		}

		public Criteria andMedicationNumNotEqualTo(Integer value) {
			addCriterion("medication_num <>", value, "medicationNum");
			return (Criteria) this;
		}

		public Criteria andMedicationNumGreaterThan(Integer value) {
			addCriterion("medication_num >", value, "medicationNum");
			return (Criteria) this;
		}

		public Criteria andMedicationNumGreaterThanOrEqualTo(Integer value) {
			addCriterion("medication_num >=", value, "medicationNum");
			return (Criteria) this;
		}

		public Criteria andMedicationNumLessThan(Integer value) {
			addCriterion("medication_num <", value, "medicationNum");
			return (Criteria) this;
		}

		public Criteria andMedicationNumLessThanOrEqualTo(Integer value) {
			addCriterion("medication_num <=", value, "medicationNum");
			return (Criteria) this;
		}

		public Criteria andMedicationNumIn(List<Integer> values) {
			addCriterion("medication_num in", values, "medicationNum");
			return (Criteria) this;
		}

		public Criteria andMedicationNumNotIn(List<Integer> values) {
			addCriterion("medication_num not in", values, "medicationNum");
			return (Criteria) this;
		}

		public Criteria andMedicationNumBetween(Integer value1, Integer value2) {
			addCriterion("medication_num between", value1, value2, "medicationNum");
			return (Criteria) this;
		}

		public Criteria andMedicationNumNotBetween(Integer value1, Integer value2) {
			addCriterion("medication_num not between", value1, value2, "medicationNum");
			return (Criteria) this;
		}

		public Criteria andPatientIncreaseIsNull() {
			addCriterion("patient_increase is null");
			return (Criteria) this;
		}

		public Criteria andPatientIncreaseIsNotNull() {
			addCriterion("patient_increase is not null");
			return (Criteria) this;
		}

		public Criteria andPatientIncreaseEqualTo(Integer value) {
			addCriterion("patient_increase =", value, "patientIncrease");
			return (Criteria) this;
		}

		public Criteria andPatientIncreaseNotEqualTo(Integer value) {
			addCriterion("patient_increase <>", value, "patientIncrease");
			return (Criteria) this;
		}

		public Criteria andPatientIncreaseGreaterThan(Integer value) {
			addCriterion("patient_increase >", value, "patientIncrease");
			return (Criteria) this;
		}

		public Criteria andPatientIncreaseGreaterThanOrEqualTo(Integer value) {
			addCriterion("patient_increase >=", value, "patientIncrease");
			return (Criteria) this;
		}

		public Criteria andPatientIncreaseLessThan(Integer value) {
			addCriterion("patient_increase <", value, "patientIncrease");
			return (Criteria) this;
		}

		public Criteria andPatientIncreaseLessThanOrEqualTo(Integer value) {
			addCriterion("patient_increase <=", value, "patientIncrease");
			return (Criteria) this;
		}

		public Criteria andPatientIncreaseIn(List<Integer> values) {
			addCriterion("patient_increase in", values, "patientIncrease");
			return (Criteria) this;
		}

		public Criteria andPatientIncreaseNotIn(List<Integer> values) {
			addCriterion("patient_increase not in", values, "patientIncrease");
			return (Criteria) this;
		}

		public Criteria andPatientIncreaseBetween(Integer value1, Integer value2) {
			addCriterion("patient_increase between", value1, value2, "patientIncrease");
			return (Criteria) this;
		}

		public Criteria andPatientIncreaseNotBetween(Integer value1, Integer value2) {
			addCriterion("patient_increase not between", value1, value2, "patientIncrease");
			return (Criteria) this;
		}

		public Criteria andMedicationIncreaseIsNull() {
			addCriterion("medication_increase is null");
			return (Criteria) this;
		}

		public Criteria andMedicationIncreaseIsNotNull() {
			addCriterion("medication_increase is not null");
			return (Criteria) this;
		}

		public Criteria andMedicationIncreaseEqualTo(Integer value) {
			addCriterion("medication_increase =", value, "medicationIncrease");
			return (Criteria) this;
		}

		public Criteria andMedicationIncreaseNotEqualTo(Integer value) {
			addCriterion("medication_increase <>", value, "medicationIncrease");
			return (Criteria) this;
		}

		public Criteria andMedicationIncreaseGreaterThan(Integer value) {
			addCriterion("medication_increase >", value, "medicationIncrease");
			return (Criteria) this;
		}

		public Criteria andMedicationIncreaseGreaterThanOrEqualTo(Integer value) {
			addCriterion("medication_increase >=", value, "medicationIncrease");
			return (Criteria) this;
		}

		public Criteria andMedicationIncreaseLessThan(Integer value) {
			addCriterion("medication_increase <", value, "medicationIncrease");
			return (Criteria) this;
		}

		public Criteria andMedicationIncreaseLessThanOrEqualTo(Integer value) {
			addCriterion("medication_increase <=", value, "medicationIncrease");
			return (Criteria) this;
		}

		public Criteria andMedicationIncreaseIn(List<Integer> values) {
			addCriterion("medication_increase in", values, "medicationIncrease");
			return (Criteria) this;
		}

		public Criteria andMedicationIncreaseNotIn(List<Integer> values) {
			addCriterion("medication_increase not in", values, "medicationIncrease");
			return (Criteria) this;
		}

		public Criteria andMedicationIncreaseBetween(Integer value1, Integer value2) {
			addCriterion("medication_increase between", value1, value2, "medicationIncrease");
			return (Criteria) this;
		}

		public Criteria andMedicationIncreaseNotBetween(Integer value1, Integer value2) {
			addCriterion("medication_increase not between", value1, value2, "medicationIncrease");
			return (Criteria) this;
		}

		public Criteria andCsvIncreaseIsNull() {
			addCriterion("csv_increase is null");
			return (Criteria) this;
		}

		public Criteria andCsvIncreaseIsNotNull() {
			addCriterion("csv_increase is not null");
			return (Criteria) this;
		}

		public Criteria andCsvIncreaseEqualTo(Integer value) {
			addCriterion("csv_increase =", value, "csvIncrease");
			return (Criteria) this;
		}

		public Criteria andCsvIncreaseNotEqualTo(Integer value) {
			addCriterion("csv_increase <>", value, "csvIncrease");
			return (Criteria) this;
		}

		public Criteria andCsvIncreaseGreaterThan(Integer value) {
			addCriterion("csv_increase >", value, "csvIncrease");
			return (Criteria) this;
		}

		public Criteria andCsvIncreaseGreaterThanOrEqualTo(Integer value) {
			addCriterion("csv_increase >=", value, "csvIncrease");
			return (Criteria) this;
		}

		public Criteria andCsvIncreaseLessThan(Integer value) {
			addCriterion("csv_increase <", value, "csvIncrease");
			return (Criteria) this;
		}

		public Criteria andCsvIncreaseLessThanOrEqualTo(Integer value) {
			addCriterion("csv_increase <=", value, "csvIncrease");
			return (Criteria) this;
		}

		public Criteria andCsvIncreaseIn(List<Integer> values) {
			addCriterion("csv_increase in", values, "csvIncrease");
			return (Criteria) this;
		}

		public Criteria andCsvIncreaseNotIn(List<Integer> values) {
			addCriterion("csv_increase not in", values, "csvIncrease");
			return (Criteria) this;
		}

		public Criteria andCsvIncreaseBetween(Integer value1, Integer value2) {
			addCriterion("csv_increase between", value1, value2, "csvIncrease");
			return (Criteria) this;
		}

		public Criteria andCsvIncreaseNotBetween(Integer value1, Integer value2) {
			addCriterion("csv_increase not between", value1, value2, "csvIncrease");
			return (Criteria) this;
		}

		public Criteria andCsvDeleteIsNull() {
			addCriterion("csv_delete is null");
			return (Criteria) this;
		}

		public Criteria andCsvDeleteIsNotNull() {
			addCriterion("csv_delete is not null");
			return (Criteria) this;
		}

		public Criteria andCsvDeleteEqualTo(Integer value) {
			addCriterion("csv_delete =", value, "csvDelete");
			return (Criteria) this;
		}

		public Criteria andCsvDeleteNotEqualTo(Integer value) {
			addCriterion("csv_delete <>", value, "csvDelete");
			return (Criteria) this;
		}

		public Criteria andCsvDeleteGreaterThan(Integer value) {
			addCriterion("csv_delete >", value, "csvDelete");
			return (Criteria) this;
		}

		public Criteria andCsvDeleteGreaterThanOrEqualTo(Integer value) {
			addCriterion("csv_delete >=", value, "csvDelete");
			return (Criteria) this;
		}

		public Criteria andCsvDeleteLessThan(Integer value) {
			addCriterion("csv_delete <", value, "csvDelete");
			return (Criteria) this;
		}

		public Criteria andCsvDeleteLessThanOrEqualTo(Integer value) {
			addCriterion("csv_delete <=", value, "csvDelete");
			return (Criteria) this;
		}

		public Criteria andCsvDeleteIn(List<Integer> values) {
			addCriterion("csv_delete in", values, "csvDelete");
			return (Criteria) this;
		}

		public Criteria andCsvDeleteNotIn(List<Integer> values) {
			addCriterion("csv_delete not in", values, "csvDelete");
			return (Criteria) this;
		}

		public Criteria andCsvDeleteBetween(Integer value1, Integer value2) {
			addCriterion("csv_delete between", value1, value2, "csvDelete");
			return (Criteria) this;
		}

		public Criteria andCsvDeleteNotBetween(Integer value1, Integer value2) {
			addCriterion("csv_delete not between", value1, value2, "csvDelete");
			return (Criteria) this;
		}

		public Criteria andCommentIsNull() {
			addCriterion("comment is null");
			return (Criteria) this;
		}

		public Criteria andCommentIsNotNull() {
			addCriterion("comment is not null");
			return (Criteria) this;
		}

		public Criteria andCommentEqualTo(String value) {
			addCriterion("comment =", value, "comment");
			return (Criteria) this;
		}

		public Criteria andCommentNotEqualTo(String value) {
			addCriterion("comment <>", value, "comment");
			return (Criteria) this;
		}

		public Criteria andCommentGreaterThan(String value) {
			addCriterion("comment >", value, "comment");
			return (Criteria) this;
		}

		public Criteria andCommentGreaterThanOrEqualTo(String value) {
			addCriterion("comment >=", value, "comment");
			return (Criteria) this;
		}

		public Criteria andCommentLessThan(String value) {
			addCriterion("comment <", value, "comment");
			return (Criteria) this;
		}

		public Criteria andCommentLessThanOrEqualTo(String value) {
			addCriterion("comment <=", value, "comment");
			return (Criteria) this;
		}

		public Criteria andCommentLike(String value) {
			addCriterion("comment like", value, "comment");
			return (Criteria) this;
		}

		public Criteria andCommentNotLike(String value) {
			addCriterion("comment not like", value, "comment");
			return (Criteria) this;
		}

		public Criteria andCommentIn(List<String> values) {
			addCriterion("comment in", values, "comment");
			return (Criteria) this;
		}

		public Criteria andCommentNotIn(List<String> values) {
			addCriterion("comment not in", values, "comment");
			return (Criteria) this;
		}

		public Criteria andCommentBetween(String value1, String value2) {
			addCriterion("comment between", value1, value2, "comment");
			return (Criteria) this;
		}

		public Criteria andCommentNotBetween(String value1, String value2) {
			addCriterion("comment not between", value1, value2, "comment");
			return (Criteria) this;
		}

		public Criteria andCommentLikeInsensitive(String value) {
			addCriterion("upper(comment) like", value.toUpperCase(), "comment");
			return (Criteria) this;
		}
	}

	/**
	 * This class was generated by MyBatis Generator. This class corresponds to the database table version
	 * @mbg.generated
	 */
	public static class Criterion {
		private String condition;
		private Object value;
		private Object secondValue;
		private boolean noValue;
		private boolean singleValue;
		private boolean betweenValue;
		private boolean listValue;
		private String typeHandler;

		public String getCondition() {
			return condition;
		}

		public Object getValue() {
			return value;
		}

		public Object getSecondValue() {
			return secondValue;
		}

		public boolean isNoValue() {
			return noValue;
		}

		public boolean isSingleValue() {
			return singleValue;
		}

		public boolean isBetweenValue() {
			return betweenValue;
		}

		public boolean isListValue() {
			return listValue;
		}

		public String getTypeHandler() {
			return typeHandler;
		}

		protected Criterion(String condition) {
			super();
			this.condition = condition;
			this.typeHandler = null;
			this.noValue = true;
		}

		protected Criterion(String condition, Object value, String typeHandler) {
			super();
			this.condition = condition;
			this.value = value;
			this.typeHandler = typeHandler;
			if (value instanceof List<?>) {
				this.listValue = true;
			} else {
				this.singleValue = true;
			}
		}

		protected Criterion(String condition, Object value) {
			this(condition, value, null);
		}

		protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
			super();
			this.condition = condition;
			this.value = value;
			this.secondValue = secondValue;
			this.typeHandler = typeHandler;
			this.betweenValue = true;
		}

		protected Criterion(String condition, Object value, Object secondValue) {
			this(condition, value, secondValue, null);
		}
	}

	/**
     * This class was generated by MyBatis Generator.
     * This class corresponds to the database table version
     *
     * @mbg.generated do_not_delete_during_merge
     */
    public static class Criteria extends GeneratedCriteria {

        protected Criteria() {
            super();
        }
    }
}