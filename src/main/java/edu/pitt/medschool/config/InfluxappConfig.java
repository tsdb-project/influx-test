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

    public static final String REMOTE_SSH_HOST = "bridges.psc.edu";
    public static final String SSH_PRIVATEKEY_PATH = "./mykey.key";
    public static final byte[] BRIDGE_LOGIN_PUBKEY_RSA = java.util.Base64.getDecoder().decode("AAAAB3NzaC1yc2EAAAADAQABAAABAQDNjMeomUtyr02dN2c/qwplfMTqzVsoMut2gv8tMLdnQ/y9V4f5n9gqIAVMLk4NH7ri6CrJZnDBZbBQLq9QwLuEisYRDEYFaq5hyOnVp8OTN24LgPwhWwZlIeyUpGHyswGdqN267mLctndbFNyueR+Ci7ri4/l8uXb4XChMxfunwieOZGr/bVn2zfiHa4mowpsySgX/XiAUrxujbWqwZdBbTEI/BqM0wAdIKFxlcGg4BEikUvEcLO3Wyuuh5p3A+O1AJpGZzCnr54oB+rrzxzg1i0SHQZC5dVFOyj39M0kVTQK8shqe5HMVSgGxErVEr1ZXPB9FkNYYkqlRKqZUNFgr");

    private InfluxappConfig() {
    }

}
