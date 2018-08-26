/**
 *
 */
package edu.pitt.medschool.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import edu.pitt.medschool.framework.util.MysqlColumnBean;
import edu.pitt.medschool.model.dao.PatientDao;

/**
 * service for returning column information of data
 * 
 * @author Isolachine
 */
@Service
public class PatientService {
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    PatientDao patientDao;

    public List<MysqlColumnBean> getColumnInfo() {
        return patientDao.getColumnInfo();
    }
}
