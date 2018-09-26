package edu.pitt.medschool.model;

import edu.pitt.medschool.config.InfluxappConfig;
import edu.pitt.medschool.model.dao.AnalysisUtil;
import org.influxdb.InfluxDB;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

public class AnalysisTest {

    @Test
    @Ignore
    public void main() {
        InfluxDB i = InfluxappConfig.INFLUX_DB;
        @SuppressWarnings("unused")
        List<DataTimeSpanBean> tmp;

        tmp = AnalysisUtil.getPatientAllDataSpan(i, null, "PUH-2010-141");
        tmp = AnalysisUtil.getPatientAllDataSpan(i, null, "PUH-2010-127");
    }
}
