package app.config;

/**
 * DB Names and measurement names
 */
public class DBConfiguration {

    public class RelationalData {
        public static final String DBNAME = "upmc";

        /**
         * Feature <---> Column mapping
         */
        public static final String HEADERMAPING = "feature_mapping_tmp";
    }

    /**
     * Measurements in data table
     */
    public class Data {
        public static final String DBNAME = "data";

        /**
         * Table name for patients
         */
        public static final String PATIENT = "Patient";
    }

    /**
     * Measurements in meta table
     */
    public class Meta {
        public static final String DBNAME = "meta";

        /**
         * Meta data for patients
         */
        public static final String PATIENT = "Patient";
    }

    /**
     * Measurements in application table
     */
    public class Sys {
        public static final String DBNAME = "sys";

        /**
         * Table name for files
         */
        public static final String FILE = "File";


        /**
         * File import progress data table
         */
        public static final String SYS_FILE_IMPORT_PROGRESS = "ImportProgress";
    }

}
