package app.model;

import java.time.Instant;

import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

/**
 * Single CSV file modle
 */
@Measurement(name = app.common.Measurement.FILES)
public class CSVFile {

    @Column(name = "time")
    private Instant record_time;

    @Column(name = "name")
    private String file_name;

    @Column(name = "pid")
    private String patient_id;

    @Column(name = "uuid")
    private String file_uuid;

    @Column(name = "size")
    private long file_size;

    @Column(name = "isAR")
    private boolean isAr;

    public boolean isAr() {
        return isAr;
    }

    public Instant getRecord_time() {
        return record_time;
    }

    public String getFile_name() {
        return file_name;
    }

    public String getPatient_id() {
        return patient_id;
    }

    public String getFile_uuid() {
        return file_uuid;
    }

    public long getFile_size() {
        return file_size;
    }

}
