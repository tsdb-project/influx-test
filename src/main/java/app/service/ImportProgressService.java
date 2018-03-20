package app.service;

import app.config.DBConfiguration;
import app.config.InfluxappConfig;
import app.util.InfluxUtil;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Import progress service
 */
//TODO: Operate in MySQL
public class ImportProgressService {

    public enum FileProgressStatus {
        STATUS_FINISHED, STATUS_FAIL, STATUS_INPROGRESS
    }

    private final InfluxDB sysMiscIdb = InfluxDBFactory.connect(InfluxappConfig.IFX_ADDR, InfluxappConfig.IFX_USERNAME, InfluxappConfig.IFX_PASSWD);
    private static final String dbName = DBConfiguration.Sys.DBNAME;
    private String uuid;

    /**
     * Init progress service
     */
    public ImportProgressService(String uuid) {
        Objects.requireNonNull(uuid);
        sysMiscIdb.createDatabase(dbName);
        this.uuid = uuid;
        // Init this class for writing
        sysMiscIdb.setDatabase(dbName);
    }

    /**
     * Get overall progress for a task
     *
     * @param uuid Task UUID
     */
    public static double GetTaskOverallProgress(String uuid) {
        Query q = new Query("SELECT \"AllPercent\" AS C FROM " + DBConfiguration.Sys.SYS_FILE_IMPORT_PROGRESS
                + " WHERE status != '" + FileProgressStatus.STATUS_FAIL
                + "' AND tid = '" + uuid + "' ORDER BY time DESC LIMIT 1;", dbName);
        Map<String, List<Object>> result = InfluxUtil.QueryResultToKV(InfluxappConfig.INFLUX_DB.query(q));
        // No data or not started yet
        if (result.size() == 0) return 0;
        return (double) result.get("C").get(0);
    }

    /**
     * Get progress data for all files within a task UUID
     *
     * @param uuid Task UUID
     */
    public static Map<String, List<Object>> GetTaskAllFileProgress(String uuid) {
        Query q = new Query(
                "SELECT \"filename\", MAX(\"CurrentPercent\") AS progress, \"status\", \"Reason\" FROM "
                        + DBConfiguration.Sys.SYS_FILE_IMPORT_PROGRESS
                        + " WHERE tid = '" + uuid + "' GROUP BY \"filename\";", dbName);
        //TODO: Convert to List<Progress Object>, result in rows, not columns!!!
        return InfluxUtil.QueryResultToKV(InfluxappConfig.INFLUX_DB.query(q));
    }

    public static void main(String[] args) {
        ImportProgressService ips = new ImportProgressService("TESTUUID");
        boolean loadTestingData = false;
        if (loadTestingData) {
            ips.UpdateFileProgress("1.csv", 100, 10, 5, 5, FileProgressStatus.STATUS_INPROGRESS, null);
            ips.UpdateFileProgress("2.csv", 100, 40, 40, 45, FileProgressStatus.STATUS_FINISHED, null);
            ips.UpdateFileProgress("3.csv", 100, 20, 3, 48, FileProgressStatus.STATUS_INPROGRESS, null);
            ips.UpdateFileProgress("3.csv", 100, 20, 10, 55, FileProgressStatus.STATUS_FAIL, "Wrong file format");
            ips.UpdateFileProgress("1.csv", 100, 10, 6, 56, FileProgressStatus.STATUS_INPROGRESS, null);
            ips.UpdateFileProgress("1.csv", 100, 10, 10, 60, FileProgressStatus.STATUS_FINISHED, null);
        }
        double s = GetTaskOverallProgress("TESTUUID");
        Map<String, List<Object>> ss = GetTaskAllFileProgress("TESTUUID");
        s = GetTaskOverallProgress("f1cabc42-c359-4c7f-885b-bfa35fe64611");
        ss = GetTaskAllFileProgress("f1cabc42-c359-4c7f-885b-bfa35fe64611");
    }

    private void doInsert(Point.Builder p) {
        p.tag("tid", this.uuid);
        sysMiscIdb.write(p.build());
    }

    /**
     * Insert file progress to db
     *
     * @param fileName           Current file name
     * @param totalFileSize      All files' size
     * @param fileSize           Current file's size
     * @param processedSize      Current processed size
     * @param totalProcessedSize All processed size
     * @param status             Current file status
     * @param failReason         Reason why failed
     */
    public void UpdateFileProgress(String fileName, long totalFileSize, long fileSize,
                                   long processedSize, long totalProcessedSize,
                                   FileProgressStatus status, String failReason) {
        double allPercent = 0, currPercent = 0;
        if (totalFileSize != 0) allPercent = 1.0 * totalProcessedSize / totalFileSize;
        if (fileSize != 0) currPercent = 1.0 * processedSize / fileSize;

        Point.Builder pnt = Point.measurement(DBConfiguration.Sys.SYS_FILE_IMPORT_PROGRESS)
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .tag("status", String.valueOf(status))
                .tag("filename", fileName)
                .addField("AllPercent", allPercent)
                .addField("CurrentPercent", currPercent)
                .addField("fileAllSz", fileSize)
                .addField("allFilesSz", totalFileSize);
        if (status == FileProgressStatus.STATUS_FAIL) {
            pnt.addField("Reason", failReason);
        } else {
            pnt.addField("Reason", "N/A");
        }
        doInsert(pnt);
    }

}
