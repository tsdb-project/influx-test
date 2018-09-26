package edu.pitt.medschool.model;

import edu.pitt.medschool.framework.influxdb.InfluxUtil;
import edu.pitt.medschool.model.dao.AnalysisUtil;
import org.influxdb.InfluxDB;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

public class analysisTest {

    @Test
    @Ignore
    public void main() {
        InfluxDB i = InfluxUtil.generateIdbClient(true, false);
        List<DataTimeSpanBean> tmp;

        tmp = AnalysisUtil.getPatientAllDataSpan(i, null, "PUH-2010-141");
        tmp = AnalysisUtil.getPatientAllDataSpan(i, null, "PUH-2010-127");
    }
}
