/**
 *
 */
package app.common;

/**
 * @author Isolachine
 */
public class Measurement {

    /**
     * Meta data for patients
     */
    public static final String PATIENT_META = "Patients_metadata";

    /**
     * Table name for patients
     */
    public static final String PATIENTS = "Patients";

    /**
     * Table name for files
     */
    public static final String FILES = "File";

    /**
     * Prefix for InfluxDB patient data table
     */
    public static final String DATA_PREFIX = "data_";

    /**
     * File import progress data table
     */
    public static final String SYS_FILE_IMPORT_PROGRESS = "file_impprgs";
}
