package edu.pitt.medschool.model.mapper;

import edu.pitt.medschool.model.dto.Accounts;
import edu.pitt.medschool.model.dto.AccountsExample;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.UpdateProvider;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.JdbcType;

@Mapper
public interface AccountsMapper {

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table upmc..accounts
	 * @mbg.generated
	 */
	@SelectProvider(type = AccountsSqlProvider.class, method = "countByExample")
	long countByExample(AccountsExample example);

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table upmc..accounts
	 * @mbg.generated
	 */
	@DeleteProvider(type = AccountsSqlProvider.class, method = "deleteByExample")
	int deleteByExample(AccountsExample example);

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table upmc..accounts
	 * @mbg.generated
	 */
	@Delete({ "delete from accounts", "where id = #{id,jdbcType=INTEGER}" })
	int deleteByPrimaryKey(Integer id);

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table upmc..accounts
	 * @mbg.generated
	 */
	@Insert({ "insert into accounts (id, username, ", "email, password, ", "role, first_name, ",
			"last_name, enable, last_update, ", "create_time, database_version)",
			"values (#{id,jdbcType=INTEGER}, #{username,jdbcType=VARCHAR}, ",
			"#{email,jdbcType=VARCHAR}, #{password,jdbcType=VARCHAR}, ",
			"#{role,jdbcType=VARCHAR}, #{firstName,jdbcType=VARCHAR}, ",
			"#{lastName,jdbcType=VARCHAR}, #{enable,jdbcType=BIT}, #{lastUpdate,jdbcType=TIMESTAMP}, ",
			"#{createTime,jdbcType=TIMESTAMP}, #{databaseVersion,jdbcType=INTEGER})" })
	int insert(Accounts record);

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table upmc..accounts
	 * @mbg.generated
	 */
	@InsertProvider(type = AccountsSqlProvider.class, method = "insertSelective")
	int insertSelective(Accounts record);

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table upmc..accounts
	 * @mbg.generated
	 */
	@SelectProvider(type = AccountsSqlProvider.class, method = "selectByExample")
	@Results({ @Result(column = "id", property = "id", jdbcType = JdbcType.INTEGER, id = true),
			@Result(column = "username", property = "username", jdbcType = JdbcType.VARCHAR),
			@Result(column = "email", property = "email", jdbcType = JdbcType.VARCHAR),
			@Result(column = "password", property = "password", jdbcType = JdbcType.VARCHAR),
			@Result(column = "role", property = "role", jdbcType = JdbcType.VARCHAR),
			@Result(column = "first_name", property = "firstName", jdbcType = JdbcType.VARCHAR),
			@Result(column = "last_name", property = "lastName", jdbcType = JdbcType.VARCHAR),
			@Result(column = "enable", property = "enable", jdbcType = JdbcType.BIT),
			@Result(column = "last_update", property = "lastUpdate", jdbcType = JdbcType.TIMESTAMP),
			@Result(column = "create_time", property = "createTime", jdbcType = JdbcType.TIMESTAMP),
			@Result(column = "database_version", property = "databaseVersion", jdbcType = JdbcType.INTEGER) })
	List<Accounts> selectByExampleWithRowbounds(AccountsExample example, RowBounds rowBounds);

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table upmc..accounts
	 * @mbg.generated
	 */
	@SelectProvider(type = AccountsSqlProvider.class, method = "selectByExample")
	@Results({ @Result(column = "id", property = "id", jdbcType = JdbcType.INTEGER, id = true),
			@Result(column = "username", property = "username", jdbcType = JdbcType.VARCHAR),
			@Result(column = "email", property = "email", jdbcType = JdbcType.VARCHAR),
			@Result(column = "password", property = "password", jdbcType = JdbcType.VARCHAR),
			@Result(column = "role", property = "role", jdbcType = JdbcType.VARCHAR),
			@Result(column = "first_name", property = "firstName", jdbcType = JdbcType.VARCHAR),
			@Result(column = "last_name", property = "lastName", jdbcType = JdbcType.VARCHAR),
			@Result(column = "enable", property = "enable", jdbcType = JdbcType.BIT),
			@Result(column = "last_update", property = "lastUpdate", jdbcType = JdbcType.TIMESTAMP),
			@Result(column = "create_time", property = "createTime", jdbcType = JdbcType.TIMESTAMP),
			@Result(column = "database_version", property = "databaseVersion", jdbcType = JdbcType.INTEGER) })
	List<Accounts> selectByExample(AccountsExample example);

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table upmc..accounts
	 * @mbg.generated
	 */
	@Select({ "select", "id, username, email, password, role, first_name, last_name, enable, last_update, ",
			"create_time, database_version", "from accounts", "where id = #{id,jdbcType=INTEGER}" })
	@Results({ @Result(column = "id", property = "id", jdbcType = JdbcType.INTEGER, id = true),
			@Result(column = "username", property = "username", jdbcType = JdbcType.VARCHAR),
			@Result(column = "email", property = "email", jdbcType = JdbcType.VARCHAR),
			@Result(column = "password", property = "password", jdbcType = JdbcType.VARCHAR),
			@Result(column = "role", property = "role", jdbcType = JdbcType.VARCHAR),
			@Result(column = "first_name", property = "firstName", jdbcType = JdbcType.VARCHAR),
			@Result(column = "last_name", property = "lastName", jdbcType = JdbcType.VARCHAR),
			@Result(column = "enable", property = "enable", jdbcType = JdbcType.BIT),
			@Result(column = "last_update", property = "lastUpdate", jdbcType = JdbcType.TIMESTAMP),
			@Result(column = "create_time", property = "createTime", jdbcType = JdbcType.TIMESTAMP),
			@Result(column = "database_version", property = "databaseVersion", jdbcType = JdbcType.INTEGER) })
	Accounts selectByPrimaryKey(Integer id);

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table upmc..accounts
	 * @mbg.generated
	 */
	@UpdateProvider(type = AccountsSqlProvider.class, method = "updateByExampleSelective")
	int updateByExampleSelective(@Param("record") Accounts record, @Param("example") AccountsExample example);

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table upmc..accounts
	 * @mbg.generated
	 */
	@UpdateProvider(type = AccountsSqlProvider.class, method = "updateByExample")
	int updateByExample(@Param("record") Accounts record, @Param("example") AccountsExample example);

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table upmc..accounts
	 * @mbg.generated
	 */
	@UpdateProvider(type = AccountsSqlProvider.class, method = "updateByPrimaryKeySelective")
	int updateByPrimaryKeySelective(Accounts record);

	/**
	 * This method was generated by MyBatis Generator. This method corresponds to the database table upmc..accounts
	 * @mbg.generated
	 */
	@Update({ "update accounts", "set username = #{username,jdbcType=VARCHAR},", "email = #{email,jdbcType=VARCHAR},",
			"password = #{password,jdbcType=VARCHAR},", "role = #{role,jdbcType=VARCHAR},",
			"first_name = #{firstName,jdbcType=VARCHAR},", "last_name = #{lastName,jdbcType=VARCHAR},",
			"enable = #{enable,jdbcType=BIT},", "last_update = #{lastUpdate,jdbcType=TIMESTAMP},",
			"create_time = #{createTime,jdbcType=TIMESTAMP},", "database_version = #{databaseVersion,jdbcType=INTEGER}",
			"where id = #{id,jdbcType=INTEGER}" })
	int updateByPrimaryKey(Accounts record);

	@Select({ "select", "id, username, email, password, role, first_name, last_name, enable, last_update, ",
			"create_time", "from accounts"})
	@Results({ @Result(column = "id", property = "id", jdbcType = JdbcType.INTEGER, id = true),
			@Result(column = "username", property = "username", jdbcType = JdbcType.VARCHAR),
			@Result(column = "email", property = "email", jdbcType = JdbcType.VARCHAR),
			@Result(column = "password", property = "password", jdbcType = JdbcType.VARCHAR),
			@Result(column = "role", property = "role", jdbcType = JdbcType.VARCHAR),
			@Result(column = "first_name", property = "firstName", jdbcType = JdbcType.VARCHAR),
			@Result(column = "last_name", property = "lastName", jdbcType = JdbcType.VARCHAR),
			@Result(column = "enable", property = "enable", jdbcType = JdbcType.BIT),
			@Result(column = "last_update", property = "lastUpdate", jdbcType = JdbcType.TIMESTAMP),
			@Result(column = "create_time", property = "createTime", jdbcType = JdbcType.TIMESTAMP) })
	List<Accounts> selectAll();
}