package edu.pitt.medschool.test;

import edu.pitt.medschool.algorithm.AnalysisUtil;
import edu.pitt.medschool.config.InfluxappConfig;
import org.influxdb.InfluxDB;

public class AlgoAnalysisTest {
    public static void main(String... args) {
        InfluxDB i = InfluxappConfig.INFLUX_DB;

        AnalysisUtil.getPatientAllDataSpan(i, null, "PUH-2010-141");
        AnalysisUtil.getPatientAllDataSpan(i, null, "PUH-2010-127");
    }
}
