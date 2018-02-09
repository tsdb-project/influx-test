/**
 *
 */
package app.model;

import app.InfluxappConfig;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;

/**
 * Single patient model
 */
@Measurement(name = InfluxappConfig.IFX_TABLE_PATIENTS)
public class Patient {

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
}
