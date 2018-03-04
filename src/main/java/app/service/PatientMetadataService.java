package app.service;

import app.common.DBConfiguration;
import app.common.InfluxappConfig;
import app.model.Patient;
import app.util.InfluxUtil;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Get some metadata for a patient
 */
@Service
public class PatientMetadataService {

    private final InfluxDB influxDB = InfluxDBFactory.connect(InfluxappConfig.IFX_ADDR, InfluxappConfig.IFX_USERNAME, InfluxappConfig.IFX_PASSWD);
    private final String dbName = DBConfiguration.Data.DBNAME;
    private Patient target;

    private Instant availDataTimeQ(String qT, boolean hasAr) {
        String table = target.getPid().toUpperCase();

        Query q = new Query(String.format(qT, table, hasAr ? "ar" : "noar"), dbName);
        Map<String, List<Object>> res = InfluxUtil.QueryResultToKV(influxDB.query(q));

        // Table does not exist
        if (res.size() == 0) return null;

        return Instant.parse(res.get("time").get(0).toString());
    }

    /**
     * Set a patient for this class
     *
     * @param pid Patient ID
     * @return Found this PID or not
     */
    public boolean SetPatient(String pid) {
        List<Patient> ps = new PatientFilteringService().FindById(pid);
        if (ps.size() < 1) return false;
        target = ps.get(0);
        return true;
    }

    /**
     * Return the first available data for a patient
     *
     * @return Time
     */
    public Instant GetFirstAvailData(boolean hasAr) {
        if (target == null) return null;

        String qT = "SELECT \"time\",\"Time\" FROM \"%s\" WHERE \"arType\"='%s' ORDER BY \"time\" ASC LIMIT 1";
        return availDataTimeQ(qT, hasAr);
    }

    /**
     * Return the last available data for a patient
     *
     * @return Time
     */
    public Instant GetLastAvailData(boolean hasAr) {
        if (target == null) return null;

        String qT = "SELECT \"time\",\"Time\" FROM \"%s\" WHERE \"arType\"='%s' ORDER BY \"time\" DESC LIMIT 1";
        return availDataTimeQ(qT, hasAr);
    }

    public static void main(String[] args) {
        PatientMetadataService pms = new PatientMetadataService();
        Instant a;
        if (pms.SetPatient("PUH-2010-087")) {
            a = pms.GetFirstAvailData(true);
            a = pms.GetLastAvailData(true);
            System.out.println();
        }
    }

}
