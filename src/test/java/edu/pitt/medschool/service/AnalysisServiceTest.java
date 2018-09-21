package edu.pitt.medschool.service;

import edu.pitt.medschool.model.dto.Downsample;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AnalysisServiceTest {
    @Autowired
    AnalysisService analysisService;

    @Test
    public void testGet() {
        Downsample query = analysisService.selectByPrimaryKey(26);
        assertEquals("07/02/2018 19:21:48", DateFormatUtils.format(query.getCreateTime(), "MM/dd/yyyy HH:mm:ss"));
        assertEquals("09/17/2018", DateFormatUtils.format(query.getUpdateTime(), "MM/dd/yyyy"));
        assertEquals((int) query.getPeriod(), 3600);
        assertEquals((int) query.getDuration(), 172800);
        assertFalse(query.getDownsampleFirst());
    }
    
    @Test
    public void testParseAggregationGroupColumnsString() throws IOException {
        String columnsJson = "{\"type\":\"Asymmetry EASI/REASI\",\"columns\":[\"Relative Index (REASI), 1 - 5 Hz\",\"Relative Index (REASI), 6 - 14 Hz\"],\"electrodes\":[\"Posterior\"]}";
        List<String> list = analysisService.parseAggregationGroupColumnsString(columnsJson);
        assertEquals(list.get(0), "I163_1");
        assertEquals(list.get(1), "I168_1");
    }

}
