package app.common;

/**
 * DB Names and measurement names
 */
public class DBConfiguration {

    /**
     * Measurements in data table
     */
    public class Data {
        public static final String DBNAME = "upmc";

        /**
         * Table name for patients
         */
        public static final String PATIENTS = "Patients";

        /**
         * Prefix for InfluxDB patient data table
         */
        public static final String DATA_PREFIX = "data_";
    }

    /**
     * Measurements in meta table
     */
    public class Meta {
        public static final String DBNAME = "tsdbmeta";

        /**
         * Meta data for patients
         */
        public static final String PATIENT_META = "Patients_metadata";
    }

    /**
     * Measurements in application table
     */
    public class App {
        public static final String DBNAME = "tsdbapp";

        /**
         * Table name for files
         */
        public static final String FILES = "File";


        /**
         * File import progress data table
         */
        public static final String SYS_FILE_IMPORT_PROGRESS = "file_impprgs";
    }

}
