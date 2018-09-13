package edu.pitt.medschool.test;

import edu.pitt.medschool.config.InfluxappConfig;
import edu.pitt.medschool.model.DataTimeSpanBean;
import edu.pitt.medschool.model.dao.AnalysisUtil;
import org.influxdb.InfluxDB;

import java.util.List;

public class AlgoAnalysisTest {
    @SuppressWarnings("unused")
    public static void main(String... args) {
        InfluxDB i = InfluxappConfig.INFLUX_DB;
        List<DataTimeSpanBean> tmp;

        tmp = AnalysisUtil.getPatientAllDataSpan(i, null, "PUH-2010-141");
        tmp = AnalysisUtil.getPatientAllDataSpan(i, null, "PUH-2010-127");
    }
}
