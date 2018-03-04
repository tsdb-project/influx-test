/**
 *
 */
package app.model;

import app.common.DBConfiguration;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

import java.time.Instant;

/**
 * Single patient model
 */
@Deprecated
@Measurement(name = DBConfiguration.Meta.PATIENT)
public class Patient {

    @Column(name = "time")
    private Instant imported_time;

    @Column(name = "PID")
    private String pid;

    @Column(name = "age")
    private Double age;

    @Column(name = "Gender")
    private String gender;

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public Double getAge() {
        return age;
    }

    public void setAge(Double age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Instant getImported_time() {
        return imported_time;
    }

    public void setImported_time(Instant imported_time) {
        this.imported_time = imported_time;
    }
}
