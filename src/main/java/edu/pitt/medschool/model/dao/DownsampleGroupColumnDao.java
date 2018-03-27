/**
 * 
 */
package edu.pitt.medschool.model.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import edu.pitt.medschool.model.dto.DownsampleGroupColumn;
import edu.pitt.medschool.model.mapper.DownsampleGroupColumnMapper;

/**
 * @author Isolachine
 *
 */
@Repository
public class DownsampleGroupColumnDao {
    @Autowired
    DownsampleGroupColumnMapper downsampleGroupColumnMapper;
    
    public int insert(DownsampleGroupColumn column) throws Exception {
        return downsampleGroupColumnMapper.insertSelective(column);
    }
}
