package app.common;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;

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
    public static final String IFX_USERNAME = "admin";

    /**
     * Server writable user's passwd
     */
    public static final String IFX_PASSWD = "1QaZ2WsX";

    /**
     * Server DB name
     */
    public static final String IFX_DBNAME = "upmc";

    /**
     * Table name for patients
     */
    public static final String IFX_TABLE_PATIENTS="Patients";

    /**
     * Table name for files
     */
    public static final String IFX_TABLE_FILES="Files";

    /**
     * Prefix for InfluxDB patient data table
     */
    public static final String IFX_DATA_PREFIX="data_";
    
    
    public static InfluxDB INFLUX_DB = InfluxDBFactory.connect(InfluxappConfig.IFX_ADDR, InfluxappConfig.IFX_USERNAME, InfluxappConfig.IFX_PASSWD);

}
