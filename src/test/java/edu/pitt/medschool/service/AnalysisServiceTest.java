/**
 * 
 */
package edu.pitt.medschool.service;

import static org.junit.Assert.assertEquals;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import edu.pitt.medschool.model.dto.Downsample;
import edu.pitt.medschool.service.AnalysisService;

/**
 * @author Isolachine
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class AnalysisServiceTest {
    @Autowired
    AnalysisService analysisService;

    @Test
    public void test() {
        Downsample query = analysisService.selectByPrimaryKey(4);
        assertEquals("03/27/2018 10:38:12", DateFormatUtils.format(query.getCreateTime(), "MM/dd/yyyy HH:mm:ss"));
    }

}
