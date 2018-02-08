package app;

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
    public static String IFX_USERNAME = "admin";

    /**
     * Server writable user's passwd
     */
    public static String IFX_PASSWD = "1QaZ2WsX";

    /**
     * Server DB name
     */
    public static String IFX_DBNAME = "upmc";

    /**
     * Table name for patients
     */
    public static String IFX_TABLE_PATIENTS="Patients";

    /**
     * Table name for files
     */
    public static String IFX_TABLE_FILES="Files";

    /**
     * Prefix for InfluxDB patient data table
     */
    public static String IFX_DATA_PREFIX="data_";

}
