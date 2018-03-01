package app.service.util;

import app.common.InfluxappConfig;
import app.common.Measurement;
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
public class ImportProgressService {

    public enum FileProgressStatus {
        STATUS_FINISHED, STATUS_FAIL, STATUS_INPROGRESS
    }

    private static final InfluxDB prgsLookupIdb = InfluxDBFactory.connect(InfluxappConfig.IFX_ADDR, InfluxappConfig.IFX_USERNAME, InfluxappConfig.IFX_PASSWD);

    private final InfluxDB sysMiscIdb = InfluxDBFactory.connect(InfluxappConfig.IFX_ADDR, InfluxappConfig.IFX_USERNAME, InfluxappConfig.IFX_PASSWD);
    private static final String dbName = InfluxappConfig.SYSTEM_DBNAME;
    public static final String importProgressRpName = "importProgressRetentionPolicy";
    private String uuid;

    /**
     * Get overall progress for a task
     *
     * @param uuid Task UUID
     */
    public static double GetTaskOverallProgress(String uuid) {
        Query q = new Query("SELECT \"AllPercent\" AS C FROM " + Measurement.SYS_FILE_IMPORT_PROGRESS
                + " WHERE status != '" + FileProgressStatus.STATUS_FAIL
                + "' AND tid = '" + uuid + "' ORDER BY time DESC LIMIT 1;", dbName);
        Map<String, List<Object>> result = InfluxUtil.QueryResultToKV(prgsLookupIdb.query(q));
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
                "SELECT \"filename\", MAX(\"CurrentPercent\") AS progress, \"status\" FROM "
                        + Measurement.SYS_FILE_IMPORT_PROGRESS
                        + " WHERE tid = '" + uuid + "' GROUP BY \"filename\";", dbName);
        return InfluxUtil.QueryResultToKV(prgsLookupIdb.query(q));
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
     */
    public void UpdateFileProgress(String fileName, long totalFileSize, long fileSize,
                                   long processedSize, long totalProcessedSize,
                                   FileProgressStatus status) {
        Point.Builder pnt = Point.measurement(Measurement.SYS_FILE_IMPORT_PROGRESS)
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .tag("status", String.valueOf(status))
                .tag("filename", fileName)
                .addField("AllPercent", 1.0 * totalProcessedSize / totalFileSize)
                .addField("CurrentPercent", 1.0 * processedSize / fileSize)
                .addField("fileAllSz", fileSize)
                .addField("allFilesSz", totalFileSize);
        doInsert(pnt);
    }

    private void doInsert(Point.Builder p) {
        p.tag("tid", uuid);
        sysMiscIdb.write(p.build());
    }

    /**
     * Init progress service
     */
    public ImportProgressService(String uuid) {
        Objects.requireNonNull(uuid);
        sysMiscIdb.createDatabase(dbName);
        this.uuid = uuid;
        // Keep data for 7 days
        sysMiscIdb.createRetentionPolicy(importProgressRpName, dbName, "7d", 2, true);
        sysMiscIdb.dropRetentionPolicy("autogen", dbName);
        // Init this class for writing
        sysMiscIdb.setDatabase(dbName);
        sysMiscIdb.setRetentionPolicy(importProgressRpName);
    }

    public static void main(String[] args) {
        ImportProgressService ips = new ImportProgressService("TESTUUID");
        boolean loadTestingData = false;
        if (loadTestingData) {
            ips.UpdateFileProgress("1.csv", 100, 10, 5, 5, FileProgressStatus.STATUS_INPROGRESS);
            ips.UpdateFileProgress("2.csv", 100, 40, 40, 45, FileProgressStatus.STATUS_FINISHED);
            ips.UpdateFileProgress("3.csv", 100, 20, 3, 48, FileProgressStatus.STATUS_INPROGRESS);
            ips.UpdateFileProgress("3.csv", 100, 20, 10, 55, FileProgressStatus.STATUS_FAIL);
            ips.UpdateFileProgress("1.csv", 100, 10, 6, 56, FileProgressStatus.STATUS_INPROGRESS);
            ips.UpdateFileProgress("1.csv", 100, 10, 10, 60, FileProgressStatus.STATUS_FINISHED);
        }
        double s = GetTaskOverallProgress("TESTUUID");
        Map<String, List<Object>> ss = GetTaskAllFileProgress("TESTUUID");
    }

}
