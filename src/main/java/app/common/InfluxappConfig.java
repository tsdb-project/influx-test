package app.common;

import app.Environment;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.springframework.boot.system.ApplicationTemp;

/**
 * Configuration file for InfluxDB connection
 */
public final class InfluxappConfig {

    /**
     * Server Address
     */
    public static final String IFX_ADDR = Environment.DEV_MACHINE.equals("quz3") ? "http://127.0.0.1:8086" : "http://192.168.149.129:8086";

    /**
     * Server writable user's name (better to be an admin)
     */
    public static final String IFX_USERNAME = Environment.DEV_MACHINE.equals("quz3") ? "root" : "admin";

    /**
     * Server writable user's passwd
     */
    public static final String IFX_PASSWD = Environment.DEV_MACHINE.equals("quz3") ? "root" : "1QaZ2WsX";

    /**
     * InfluxDB system temp directory
     */
    public static final ApplicationTemp TMP_DIR = new ApplicationTemp();

    /**
     * Bulk insert size (DO NOT CHANGE)
     */
    public static final int PERFORMANCE_INDEX = 1000000;

    /**
     * A globally useable InfluxDB Client
     */
    public static InfluxDB INFLUX_DB = InfluxDBFactory.connect(InfluxappConfig.IFX_ADDR, InfluxappConfig.IFX_USERNAME, InfluxappConfig.IFX_PASSWD);

}
