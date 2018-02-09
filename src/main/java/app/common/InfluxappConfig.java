package app.common;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;

/**
 * Configuration file for InfluxDB connection
 */
public class InfluxappConfig {

    /**
     * Server Address
     */
    public static String IFX_ADDR = "http://127.0.0.1:8086";

    /**
     * Server writable user's name (better to be an admin)
     */
    public static String IFX_USERNAME = "root";

    /**
     * Server writable user's passwd
     */
    public static String IFX_PASSWD = "root";

    /**
     * Server DB name
     */
    public static String IFX_DBNAME = "upmc";

    /**
     * Table name for patients
     */
    public static String IFX_TABLE_PATIENTS="patient";

    /**
     * Table name for files
     */
    public static String IFX_TABLE_FILES="patient";

    /**
     * Prefix for InfluxDB patient data table
     */
    public static String IFX_DATA_PREFIX="data_";
    
    public static InfluxDB INFLUX_DB = InfluxDBFactory.connect(InfluxappConfig.IFX_ADDR, InfluxappConfig.IFX_USERNAME, InfluxappConfig.IFX_PASSWD);

}
