package app.common;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.springframework.boot.ApplicationTemp;

/**
 * Configuration file for InfluxDB connection
 */
public final class InfluxappConfig {

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
     * Server DB name
     */
    public static final String IFX_DBNAME = "upmc";

    /**
     * InfluxDB system temp directory
     */
    public static final ApplicationTemp TMP_DIR = new ApplicationTemp();

    /**
     * A globally useable InfluxDB Client
     */
    public static InfluxDB INFLUX_DB = InfluxDBFactory.connect(InfluxappConfig.IFX_ADDR, InfluxappConfig.IFX_USERNAME, InfluxappConfig.IFX_PASSWD);

}
