/**
 *
 */
package app.common;

/**
 * @author Isolachine
 */
public class Measurement {

    /**
     * Table name for patients
     */
    public static final String PATIENTS = "Patients";
    /**
     * Table name for files
     */
    public static final String FILES = "Files";

    /**
     * Prefix for InfluxDB patient data table
     */
    public static final String DATA_PREFIX = "data_";

    /**
     * File import progress data table
     */
    public static final String SYS_FILE_IMPORT_PROGRESS = "file_impprgs";
}
