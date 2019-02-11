package edu.pitt.medschool.service;


import edu.pitt.medschool.model.PatientTimeLine;
import org.springframework.jdbc.core.RowMapper;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

// Class for reading a row of database
public class PatientRowMapper implements RowMapper<PatientTimeLine> {

    @Override
     public PatientTimeLine mapRow(ResultSet resultSet, int i) throws SQLException {

        String filename = resultSet.getString("filename");
        Date arrestTime = resultSet.getTimestamp("arresttime");
        if (arrestTime == null){
           arrestTime = resultSet.getTimestamp("arrestdate");
       }
        long relativeStartTime = (resultSet.getTimestamp("start_time").getTime() - arrestTime.getTime())/1000;
        long relativeEndTime = (resultSet.getTimestamp("end_time").getTime() - arrestTime.getTime())/1000;
        int len = resultSet.getInt("len");

       PatientTimeLine patientTimeLine =  new PatientTimeLine();
       patientTimeLine.setArrestTime(arrestTime);
       patientTimeLine.setFilename(filename);
       patientTimeLine.setRelativeStartTime(relativeStartTime);
       patientTimeLine.setRelativeEndTime(relativeEndTime);
       patientTimeLine.setLength(len);

       return patientTimeLine;
    }
}
