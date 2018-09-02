package edu.pitt.medschool.test;

import edu.pitt.medschool.algorithm.Analysis;
import edu.pitt.medschool.config.InfluxappConfig;
import org.influxdb.InfluxDB;

public class AlgoAnalysisTest {
    public static void main(String... args) {
        InfluxDB i = InfluxappConfig.INFLUX_DB;

        Analysis.getPatientAllDataSpan(i, null, "PUH-2010-141");
        Analysis.getPatientAllDataSpan(i, null, "PUH-2010-127");
    }
}
