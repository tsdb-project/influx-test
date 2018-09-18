package edu.pitt.medschool.config;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.springframework.boot.system.ApplicationTemp;

/**
 * Configuration file for InfluxDB connection
 */
public final class InfluxappConfig {

    /**
     * A globally useable InfluxDB Client
     */
    public static final InfluxDB INFLUX_DB = InfluxDBFactory.connect(InfluxappConfig.IFX_ADDR, InfluxappConfig.IFX_USERNAME, InfluxappConfig.IFX_PASSWD);

    /**
     * Available cores on this system
     */
    public static final int AvailableCores = Runtime.getRuntime().availableProcessors();

    /**
     * Server Address
     */
    public static final String IFX_ADDR = "http://127.0.0.1:8086";

    /**
     * Server writable user's name (better to be an admin)
     */
    public static final String IFX_USERNAME = "root";

    /**
     * Server writable user's passwd
     */
    public static final String IFX_PASSWD = "root";

    /**
     * InfluxDB system temp directory
     */
    public static final ApplicationTemp TMP_DIR = new ApplicationTemp();

    /**
     * Bulk insert size (DO NOT CHANGE)
     */
    public static final int PERFORMANCE_INDEX = 1000000;
    public static final int FAILURE_RETRY = 3;

    public static final String OUTPUT_DIRECTORY = "/tsdb/output/";
    public static final String ARCHIVE_DIRECTORY = "archive";

    private InfluxappConfig() {
    }

}
