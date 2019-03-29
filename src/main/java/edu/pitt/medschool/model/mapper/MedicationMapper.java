package edu.pitt.medschool.model.mapper;

import edu.pitt.medschool.model.dto.Medication;
import edu.pitt.medschool.model.dto.MedicationExample;

import java.util.List;

import org.apache.ibatis.annotations.*;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.JdbcType;

@Mapper
public interface MedicationMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table medication
     *
     * @mbg.generated
     */
    @SelectProvider(type=MedicationSqlProvider.class, method="countByExample")
    long countByExample(MedicationExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table medication
     *
     * @mbg.generated
     */
    @DeleteProvider(type=MedicationSqlProvider.class, method="deleteByExample")
    int deleteByExample(MedicationExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table medication
     *
     * @mbg.generated
     */
    @Insert({
        "insert into medication (infused_vol, id, ",
        "chart_date, drug_name, ",
        "dose, dose_unit, rate, ",
        "rate_unit, ordered_as, ",
        "route, status, site, ",
        "infused_vol_unit, infuse_ind, ",
        "iv_flag, bolus_flag, ",
        "tdrip_ind)",
        "values (#{infusedVol,jdbcType=INTEGER}, #{id,jdbcType=VARCHAR}, ",
        "#{chartDate,jdbcType=TIMESTAMP}, #{drugName,jdbcType=VARCHAR}, ",
        "#{dose,jdbcType=DOUBLE}, #{doseUnit,jdbcType=VARCHAR}, #{rate,jdbcType=DOUBLE}, ",
        "#{rateUnit,jdbcType=VARCHAR}, #{orderedAs,jdbcType=VARCHAR}, ",
        "#{route,jdbcType=VARCHAR}, #{status,jdbcType=VARCHAR}, #{site,jdbcType=VARCHAR}, ",
        "#{infusedVolUnit,jdbcType=VARCHAR}, #{infuseInd,jdbcType=INTEGER}, ",
        "#{ivFlag,jdbcType=INTEGER}, #{bolusFlag,jdbcType=INTEGER}, ",
        "#{tdripInd,jdbcType=INTEGER})"
    })
    int insert(Medication record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table medication
     *
     * @mbg.generated
     */
    @InsertProvider(type=MedicationSqlProvider.class, method="insertSelective")
    int insertSelective(Medication record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table medication
     *
     * @mbg.generated
     */
    @SelectProvider(type=MedicationSqlProvider.class, method="selectByExample")
    @Results({
        @Result(column="infused_vol", property="infusedVol", jdbcType=JdbcType.INTEGER),
        @Result(column="id", property="id", jdbcType=JdbcType.VARCHAR),
        @Result(column="chart_date", property="chartDate", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="drug_name", property="drugName", jdbcType=JdbcType.VARCHAR),
        @Result(column="dose", property="dose", jdbcType=JdbcType.DOUBLE),
        @Result(column="dose_unit", property="doseUnit", jdbcType=JdbcType.VARCHAR),
        @Result(column="rate", property="rate", jdbcType=JdbcType.DOUBLE),
        @Result(column="rate_unit", property="rateUnit", jdbcType=JdbcType.VARCHAR),
        @Result(column="ordered_as", property="orderedAs", jdbcType=JdbcType.VARCHAR),
        @Result(column="route", property="route", jdbcType=JdbcType.VARCHAR),
        @Result(column="status", property="status", jdbcType=JdbcType.VARCHAR),
        @Result(column="site", property="site", jdbcType=JdbcType.VARCHAR),
        @Result(column="infused_vol_unit", property="infusedVolUnit", jdbcType=JdbcType.VARCHAR),
        @Result(column="infuse_ind", property="infuseInd", jdbcType=JdbcType.INTEGER),
        @Result(column="iv_flag", property="ivFlag", jdbcType=JdbcType.INTEGER),
        @Result(column="bolus_flag", property="bolusFlag", jdbcType=JdbcType.INTEGER),
        @Result(column="tdrip_ind", property="tdripInd", jdbcType=JdbcType.INTEGER)
    })
    List<Medication> selectByExampleWithRowbounds(MedicationExample example, RowBounds rowBounds);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table medication
     *
     * @mbg.generated
     */
    @SelectProvider(type=MedicationSqlProvider.class, method="selectByExample")
    @Results({
        @Result(column="infused_vol", property="infusedVol", jdbcType=JdbcType.INTEGER),
        @Result(column="id", property="id", jdbcType=JdbcType.VARCHAR),
        @Result(column="chart_date", property="chartDate", jdbcType=JdbcType.TIMESTAMP),
        @Result(column="drug_name", property="drugName", jdbcType=JdbcType.VARCHAR),
        @Result(column="dose", property="dose", jdbcType=JdbcType.DOUBLE),
        @Result(column="dose_unit", property="doseUnit", jdbcType=JdbcType.VARCHAR),
        @Result(column="rate", property="rate", jdbcType=JdbcType.DOUBLE),
        @Result(column="rate_unit", property="rateUnit", jdbcType=JdbcType.VARCHAR),
        @Result(column="ordered_as", property="orderedAs", jdbcType=JdbcType.VARCHAR),
        @Result(column="route", property="route", jdbcType=JdbcType.VARCHAR),
        @Result(column="status", property="status", jdbcType=JdbcType.VARCHAR),
        @Result(column="site", property="site", jdbcType=JdbcType.VARCHAR),
        @Result(column="infused_vol_unit", property="infusedVolUnit", jdbcType=JdbcType.VARCHAR),
        @Result(column="infuse_ind", property="infuseInd", jdbcType=JdbcType.INTEGER),
        @Result(column="iv_flag", property="ivFlag", jdbcType=JdbcType.INTEGER),
        @Result(column="bolus_flag", property="bolusFlag", jdbcType=JdbcType.INTEGER),
        @Result(column="tdrip_ind", property="tdripInd", jdbcType=JdbcType.INTEGER)
    })
    List<Medication> selectByExample(MedicationExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table medication
     *
     * @mbg.generated
     */
    @UpdateProvider(type=MedicationSqlProvider.class, method="updateByExampleSelective")
    int updateByExampleSelective(@Param("record") Medication record, @Param("example") MedicationExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table medication
     *
     * @mbg.generated
     */
    @UpdateProvider(type=MedicationSqlProvider.class, method="updateByExample")
    int updateByExample(@Param("record") Medication record, @Param("example") MedicationExample example);

    /**
     * This method is used for get all the medication information
     * of the patients in the csv_file table
     */
    @Select({
            "select m.infused_vol as infusedVol, m.id as id ,m.chart_date as chartDate ,m.drug_name as drugName,",
            "m.dose as dose, m.dose_unit as doseUnit, m.rate as rate, m.rate_unit as rateUnit, m.ordered_as as orderedAs,",
            "m.route as route, m.status as status, m.site as site, m.infused_vol_unit as infusedVolUnit, m.infuse_ind as infuseInd,",
            "m.iv_flag as ivFlag, m.bolus_flag as bolusFlag, m.tdrip_ind as tdripInd ",
            "from medication m , csv_file c",
            "where c.pid = m.id and c.machine= '${machine}'"
    })
    List<Medication> getAllMedInfo (@Param("machine") String machine);

    @Select({
            "select m.infused_vol as infusedVol, m.id as id ,m.chart_date as chartDate ,m.drug_name as drugName,",
            "m.dose as dose, m.dose_unit as doseUnit, m.rate as rate, m.rate_unit as rateUnit, m.ordered_as as orderedAs,",
            "m.route as route, m.status as status, m.site as site, m.infused_vol_unit as infusedVolUnit, m.infuse_ind as infuseInd,",
            "m.iv_flag as ivFlag, m.bolus_flag as bolusFlag, m.tdrip_ind as tdripInd ",
            "from medication m , csv_file c",
            "where c.pid = m.id and m.id = '${patientId}' and c.machine= '${machine}'"
    })
    List<Medication> getMedInfoById (@Param("machine") String machine,@Param("patientId") String patientId);

    @Select({
            "SELECT distinct drug_name FROM upmc.medication"
    })
    List<String> getAllMedicine();

    @Select({
            "SELECT m.bolus_flag as bolusFlag, m.chart_date as chartDate, m.dose as dose, m.dose_unit as doseUnit, m.drug_name as drugName, m.id as id, m.infused_vol as infusedVol, m.infused_vol_unit as infusedVolUnit, m.infuse_ind as infuseInd, m.iv_flag as ivFlag, m.ordered_as as orderedAs, m.rate as rate, m.rate_unit as rateUnit, m.route as route, m.site as site, m.status as status, m.tdrip_ind as tdripInd FROM upmc.medication m",
            "WHERE drug_name='${drugName}' and m.id IN (${ids})" +
                    "order by m.id, m.chart_date;"
    })
    List<Medication> selectAllbyMedication(@Param("drugName") String drugName, @Param("ids") String ids);

    @Select({
            "SELECT id FROM upmc.medication where drug_name='${drugName}'"
    })
    List<String> selectPatientsbyMedications(@Param("drugName") String drugName);
}