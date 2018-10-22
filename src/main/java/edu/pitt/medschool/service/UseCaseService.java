package edu.pitt.medschool.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.opencsv.CSVWriter;

import edu.pitt.medschool.config.DBConfiguration;
import edu.pitt.medschool.config.InfluxappConfig;
import edu.pitt.medschool.framework.util.Util;
import edu.pitt.medschool.model.dao.DownsampleDao;
import edu.pitt.medschool.model.dao.DownsampleGroupDao;
import edu.pitt.medschool.model.dao.ImportedFileDao;
import edu.pitt.medschool.model.dao.PatientDao;
import okhttp3.OkHttpClient;

/**
 * Export functions
 */
@Service
public class UseCaseService {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Value("${machine}")
    private String uuid;

    @Value("${load}")
    private double loadFactor;

    private final static String dbName = DBConfiguration.Data.DBNAME;
    private final static String DIRECTORY = "/Volumes/CSV Export3/tsdb/output";

    @Autowired
    DownsampleDao downsampleDao;
    @Autowired
    DownsampleGroupDao downsampleGroupDao;
    @Autowired
    ColumnService columnService;

    /*
     * Be able to restrict the epochs for which data are exported (e.g. specify to export up to the first 36 hours of available
     * data, but truncate data thereafter). Be able to specify which columns are exported (e.g. I10_*, I10_2 only, all data,
     * etc) Be able to export down sampled data (e.g. hourly mean, median, variance, etc)
     */
    @Autowired
    PatientDao patientDao;
    @Autowired
    ImportedFileDao importedFileDao;

    private InfluxDB influxDB = InfluxDBFactory.connect(InfluxappConfig.IFX_ADDR, InfluxappConfig.IFX_USERNAME,
            InfluxappConfig.IFX_PASSWD, new OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(1200, TimeUnit.SECONDS).writeTimeout(120, TimeUnit.SECONDS));
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private BlockingQueue<String> idQueue = new LinkedBlockingQueue<>();

    public String useCaseTwo() throws IOException {
        File dir = generateOutputDir("UC2");
        if (dir == null)
            return "Failed to create directory";

        // Get Patient List by uuid
        List<String> patientIDs = importedFileDao.selectAllImportedPidOnMachine(uuid);
        idQueue = new LinkedBlockingQueue<>(patientIDs);

        CSVWriter writer = new CSVWriter(new FileWriter(dir.getAbsolutePath() + "/output.csv"));
        String[] cols = new String[] { "ID", "Data Status", "aEEG1", "aEEG2", "aEEG3", "aEEG4", "aEEG5", "aEEG6", "aEEG7",
                "aEEG8", "aEEG9", "aEEG10", "aEEG11", "aEEG12", "aEEG13", "aEEG14", "aEEG15", "aEEG16", "aEEG17", "aEEG18",
                "aEEG19", "aEEG20", "aEEG21", "aEEG22", "aEEG23", "aEEG24", "aEEG25", "aEEG26", "aEEG27", "aEEG28", "aEEG29",
                "aEEG30", "aEEG31", "aEEG32", "aEEG33", "aEEG34", "aEEG35", "aEEG36", "aEEG37", "aEEG38", "aEEG39", "aEEG40",
                "aEEG41", "aEEG42", "aEEG43", "aEEG44", "aEEG45", "aEEG46", "aEEG47", "aEEG48" };
        writer.writeNext(cols);

        int paraCount = determineParaNumber();
        ExecutorService scheduler = generateNewThreadPool(paraCount);
        Runnable queryTask = () -> {
            String patientId;
            while ((patientId = idQueue.poll()) != null) {
                try {
                    String template = "select median(avg) as MEDIAN from (select"
                            + "(\"I64_3\" + \"I65_3\" + \"I66_3\" + \"I67_3\" + \"I68_3\" + \"I69_3\" + "
                            + "\"I70_3\" + \"I71_3\" + \"I72_3\" + \"I73_3\" + \"I74_3\" + \"I75_3\" + "
                            + "\"I76_3\" + \"I77_3\" + \"I78_3\" + \"I79_3\" + \"I80_3\" + \"I81_3\") / 18 as avg "
                            + "from \"%s\" where arType = 'ar' and time >= '%s' and time < '%s' + 48h) where time >= '%s' and time < '%s' + 48h "
                            + "and avg > 2 group by time(1h, %ss) fill(0)";

                    String countTemplate = "select count(avg) as COUNT from (select"
                            + "(\"I64_3\" + \"I65_3\" + \"I66_3\" + \"I67_3\" + \"I68_3\" + \"I69_3\" + "
                            + "\"I70_3\" + \"I71_3\" + \"I72_3\" + \"I73_3\" + \"I74_3\" + \"I75_3\" + "
                            + "\"I76_3\" + \"I77_3\" + \"I78_3\" + \"I79_3\" + \"I80_3\" + \"I81_3\") / 18 as avg "
                            + "from \"%s\" where arType = 'ar' and time >= '%s' and time < '%s' + 48h) "
                            + "where time >= '%s' and time < '%s' + 48h group by time(1h, %ss) fill(0)";

                    String firstRecordTimeQuery = "select \"I3_1\" from \"" + patientId + "\" where arType = 'ar' limit 1";
                    QueryResult recordResult = influxDB.query(new Query(firstRecordTimeQuery, dbName));
                    String firstRecordTime = recordResult.getResults().get(0).getSeries().get(0).getValues().get(0).get(0)
                            .toString();

                    int offset = Integer.valueOf(firstRecordTime.substring(14, 16)) * 60
                            + Integer.valueOf(firstRecordTime.substring(17, 19));

                    String queryString = String.format(template, patientId, firstRecordTime, firstRecordTime, firstRecordTime,
                            firstRecordTime, offset);

                    String countQueryString = String.format(countTemplate, patientId, firstRecordTime, firstRecordTime,
                            firstRecordTime, firstRecordTime, offset);

                    logger.debug(patientId + " :\n" + queryString);
                    logger.debug(patientId + " :\n" + countQueryString);

                    Query query = new Query(queryString, dbName);
                    Query countQuery = new Query(countQueryString, dbName);

                    QueryResult result = influxDB.query(query);
                    QueryResult countResult = influxDB.query(countQuery);

                    logger.debug(patientId + " :\n" + result.toString());
                    logger.debug(patientId + " :\n" + countResult.toString());

                    String[] row = new String[2 + 48];
                    row[0] = patientId;

                    List<List<Object>> res = result.getResults().get(0).getSeries().get(0).getValues();
                    List<List<Object>> countRes = countResult.getResults().get(0).getSeries().get(0).getValues();
                    logger.debug(patientId + " : " + String.valueOf(res.size()));

                    int validCount = 0;
                    for (int i = 0; i < 48; i++) {
                        if (res.size() > i) {
                            List<Object> vals = res.get(i);
                            List<Object> countVals = countRes.get(i);
                            System.out.println(vals.toString());
                            if (Double.valueOf(countVals.get(1).toString()).intValue() < 900
                                    && Double.valueOf(countVals.get(1).toString()).intValue() > 0) {
                                row[2 + i] = "Insuff. Data";
                            } else if (Double.valueOf(countVals.get(1).toString()).intValue() == 0) {
                                row[2 + i] = "N/A";
                            } else {
                                validCount++;
                                row[2 + i] = vals.get(1).toString();
                            }
                        } else {
                            row[2 + i] = "";
                        }
                    }
                    if (validCount >= 6) {
                        row[1] = "Valid";
                    } else {
                        row[1] = "Invalid: bin count < 6";
                    }
                    writer.writeNext(row);
                } catch (Exception e) {
                    logger.error(patientId + " : " + Util.stackTraceErrorToString(e));
                    idQueue.offer(patientId);
                }
            }
        };

        for (int i = 0; i < paraCount; ++i) {
            scheduler.submit(queryTask);
        }
        scheduler.shutdown();
        try {
            scheduler.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
            writer.close();
            return "Success!";
        } catch (InterruptedException e) {
            logger.error(Util.stackTraceErrorToString(e));
            writer.close();
            return "Failed with:" + Util.stackTraceErrorToString(e);
        }
    }

    /**
     * Generate an object for output directory class
     */
    private File generateOutputDir(String purpose) {
        String rfc3339 = "_(" + LocalDateTime.now().toString() + ")";
        // Workaround for Windows name restriction
        String path = DIRECTORY + purpose + rfc3339.replace(':', '.');
        File outputDir = new File(path);
        boolean dirCreationSuccess = true;

        if (!outputDir.exists()) {
            String err = "Failed to create 'Results' dir. ";
            try {
                if (!outputDir.mkdirs()) {
                    dirCreationSuccess = false;
                }
            } catch (SecurityException se) {
                err += se.getLocalizedMessage();
                dirCreationSuccess = false;
            }
            // Use a flag for flexible work flow
            if (!dirCreationSuccess) {
                logger.error(err);
                return null;
            }
            return outputDir;
        }
        // If the directory already exists (bad for us), we should consider this operation failed
        return null;
    }

    /**
     * How parallel should we go?
     */
    private int determineParaNumber() {
        int paraCount = (int) Math.round(loadFactor * InfluxappConfig.AvailableCores);
        return paraCount > 0 ? paraCount : 1;
    }

    /**
     * Generate a new fixed threadpool (when new request comes)
     */
    private ExecutorService generateNewThreadPool(int i) {
        return Executors.newFixedThreadPool(i);
    }

    public String useCaseAkoglu() throws IOException {
        final int TOTAL_COLUMNS = 3206;
        final int CUTOFF = 18000;

        List<Col> list = jdbcTemplate.query("SELECT f.SID, f.SID_Count FROM feature f WHERE f.electrode NOT LIKE '%Av17%'",
                new BeanPropertyRowMapper<Col>(Col.class));
        StringBuffer sb = new StringBuffer();
        String[] cols = new String[TOTAL_COLUMNS + 2];
        cols[0] = "ID";
        cols[1] = "time";
        int colCount = 2;
        for (Col col : list) {
            for (int i = 1; i <= col.getSID_Count(); i++) {
                String colName = col.getSID() + '_' + i;
                cols[colCount++] = colName;
                sb.append("mean(").append(colName).append(") AS ").append(colName).append(", ");
            }
        }
        String allColumns = sb.substring(0, sb.length() - 2);

        File dir = generateOutputDir("UC_Akoglu");
        if (dir == null)
            return "Failed to create directory";

        // Get Patient List by uuid
        List<String> patientIDs = importedFileDao.selectAllImportedPidOnMachine(uuid);
        idQueue = new LinkedBlockingQueue<>(patientIDs);

        CSVWriter writer = new CSVWriter(new FileWriter(dir.getAbsolutePath() + "/output.csv"));
        writer.writeNext(cols);

        int paraCount = determineParaNumber();
        ExecutorService scheduler = generateNewThreadPool(paraCount);
        Runnable queryTask = () -> {
            String patientId;
            while ((patientId = idQueue.poll()) != null) {
                try {
                    String template = "SELECT %s FROM \"%s\" WHERE time >= '%s' and time < '%s' "
                            + "and arType = 'ar' GROUP BY time(10s, %ss) fill(none)";
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
                    formatter.setTimeZone(TimeZone.getTimeZone("UTC"));

                    CSVWriter singleWriter = new CSVWriter(new FileWriter(dir.getAbsolutePath() + "/" + patientId + ".csv"));
                    singleWriter.writeNext(cols);

                    String firstRecordTimeQuery = "select \"I3_1\" from \"" + patientId + "\" where arType = 'ar' limit 1";
                    String lastRecordTimeQuery = "select \"I3_1\" from \"" + patientId
                            + "\" where arType = 'ar' order by time desc limit 1";
                    QueryResult firstRecordResult = influxDB.query(new Query(firstRecordTimeQuery, dbName));
                    String firstRecordTime = firstRecordResult.getResults().get(0).getSeries().get(0).getValues().get(0).get(0)
                            .toString();
                    QueryResult lastRecordResult = influxDB.query(new Query(lastRecordTimeQuery, dbName));
                    String lastRecordTime = lastRecordResult.getResults().get(0).getSeries().get(0).getValues().get(0).get(0)
                            .toString();

                    String intermediateTime = firstRecordTime;
                    while (Instant.parse(lastRecordTime).getEpochSecond() > Instant.parse(intermediateTime).getEpochSecond()) {
                        Instant intermediateInstant = Instant.parse(intermediateTime);
                        String startingTime = intermediateTime;
                        intermediateTime = formatter
                                .format(Date.from(Instant.ofEpochSecond(intermediateInstant.getEpochSecond() + CUTOFF)));

                        int offset = Integer.valueOf(firstRecordTime.substring(18, 19));

                        String queryString = String.format(template, allColumns, patientId, startingTime, intermediateTime,
                                offset);

                        logger.debug(patientId + " :\n" + queryString.substring(79919));

                        Query query = new Query(queryString, dbName);

                        QueryResult result = influxDB.query(query);

                        String[] row = new String[TOTAL_COLUMNS + 2];
                        row[0] = patientId;

                        List<List<Object>> res = result.getResults().get(0).getSeries().get(0).getValues();
                        logger.debug(patientId + " : " + String.valueOf(res.size()));

                        for (int i = 0; i < res.size(); i++) {
                            List<Object> rowData = res.get(i);
                            for (int j = 0; j < TOTAL_COLUMNS + 1; j++) {
                                Object data = rowData.get(j);
                                if (data == null) {
                                    row[j + 1] = "N/A";
                                } else {
                                    row[j + 1] = data.toString();
                                }
                            }
                            writer.writeNext(row);
                            singleWriter.writeNext(row);
                        }
                    }
                    singleWriter.close();
                } catch (Exception e) {
                    logger.error(patientId + " : " + Util.stackTraceErrorToString(e));
                    idQueue.offer(patientId);
                }
            }
        };

        for (int i = 0; i < paraCount; ++i) {
            scheduler.submit(queryTask);
        }
        scheduler.shutdown();
        try {
            scheduler.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
            writer.close();
            return "Success!";
        } catch (InterruptedException e) {
            logger.error(Util.stackTraceErrorToString(e));
            writer.close();
            return "Failed with:" + Util.stackTraceErrorToString(e);
        }
    }

}
