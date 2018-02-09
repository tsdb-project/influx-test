/**
 *
 */
package app.model;

import app.InfluxappConfig;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

import java.time.Instant;

/**
 * Single patient model
 */
@Measurement(name = InfluxappConfig.IFX_TABLE_PATIENTS)
public class Patient {

    @Column(name="time")
    private Instant imported_time;

    @Column(name = "pid")
    private String pid;

    @Column(name = "age")
    private Double age;

    @Column(name = "gender")
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
