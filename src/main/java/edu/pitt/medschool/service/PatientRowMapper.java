package edu.pitt.medschool.service;


import edu.pitt.medschool.model.ValidateBean;
import org.springframework.jdbc.core.RowMapper;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

// Class for reading a row of database
public class PatientRowMapper implements RowMapper<ValidateBean> {

    @Override
     public ValidateBean mapRow(ResultSet resultSet, int i) throws SQLException {

        String pid = resultSet.getString("pid");
        String filename = resultSet.getString("filename");
        String path = resultSet.getString("path");
        int size = resultSet.getInt("size");
        String uuid = resultSet.getString("uuid");
        Date header_time = resultSet.getTimestamp("header_time");
        Date start_time = resultSet.getTimestamp("start_time");;
        Date end_time = resultSet.getTimestamp("end_time");;
        int length= resultSet.getInt("length");

        ValidateBean validateBean = new ValidateBean();
        validateBean.setPid(pid);
        validateBean.setFilename(filename);
        validateBean.setPath(path);
        validateBean.setSize(size);
        validateBean.setUuid(uuid);
        validateBean.setHeader_time(header_time);
        validateBean.setStart_time(start_time);
        validateBean.setEnd_time(end_time);
        validateBean.setLines(length);

        return validateBean;
    }
}
