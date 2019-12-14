package edu.pitt.medschool.model;

import java.util.List;

import org.influxdb.InfluxDB;
import org.junit.Ignore;
import org.junit.Test;

import edu.pitt.medschool.framework.influxdb.InfluxUtil;
import edu.pitt.medschool.model.dao.AnalysisUtil;

public class AnalysisTest {

    @Test
    @Ignore
    public void main() {
        InfluxDB i = InfluxUtil.generateIdbClient(true);
        @SuppressWarnings("unused")
        List<DataTimeSpanBean> tmp;

        tmp = AnalysisUtil.getPatientAllDataSpan("data", i, null, "PUH-2010-141","");
        tmp = AnalysisUtil.getPatientAllDataSpan("data", i, null, "PUH-2010-127","");
    }
}
