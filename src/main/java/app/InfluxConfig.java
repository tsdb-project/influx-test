package app;

/**
 * Configuration file for InfluxDB connection
 */
public class InfluxConfig {

    /**
     * Server Address
     */
    public static String ADDR = "http://192.168.254.129:8086";

    /**
     * Server writable user's name (better to be an admin)
     */
    public static String USERNAME = "admin";

    /**
     * Server writable user's passwd
     */
    public static String PASSWD = "1QaZ2WsX";

    /**
     * Server DB name
     */
    public static String DBNAME = "limit_test";

}
