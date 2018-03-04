package app.service;

import app.common.DBConfiguration;
import app.common.InfluxappConfig;
import app.util.Util;
import com.opencsv.CSVReader;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.springframework.stereotype.Service;

import java.io.*;
import java.text.ParseException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Importing patient metadata into InfluxDB Based on mail at 02/21/2018
 */
@Service
public class MetadataService {

    /**
     * Status code for this service
     */
    public enum StatusCode {
        ALL_GOOD, FILE_IO_ERROR, FILE_NOT_FOUND, FILE_DATE_ERROR, FILE_FORMAT_ERROR
    }

    private final static String tableName = DBConfiguration.Meta.PATIENT;
    private final InfluxDB influxDB = InfluxDBFactory.connect(InfluxappConfig.IFX_ADDR, InfluxappConfig.IFX_USERNAME, InfluxappConfig.IFX_PASSWD);

    /**
     * CSV Header processing
     */
    private void processHeader(String[] hdr) {
        // TODO: Need more soild vaildation
        if (hdr.length != 209)
            throw new RuntimeException();
    }

    private Object genObjFromLine(int i, String val, Map<String, String> tags) throws ParseException {
        Object obj = "N/A";
        // Some fields are empty
        if (val.isEmpty()) {
            if (i == 1) {
                // Special work-around for age
                return -1.0;
            } else {
                return obj;
            }
        }

        switch (i) {
        case 0:
            tags.put("PID", val.trim().toUpperCase());
            return null;
        case 2:
            tags.put("Gender", val.equals("1") ? "F" : "M");
            return null;
        case 3:
            tags.put("ArrestLocation", val.equals("1") ? "Outside" : "Inside");
            return null;
        case 1:
            obj = Double.parseDouble(val);
            break;
        case 175:
            tags.put("Survived", val.equals("1") ? "Y" : "N");
            return null;
        case 11: // Arrest date
            obj = Util.dateTimeFormatToInstant(val, "yyyy-MM-dd", null).toString();
            break;
        case 12: // Arrest time
            obj = Util.dateTimeFormatToInstant(val, "yyyy-MM-dd kk:mm", null).toString();
            break;
        case 30: // Arrive date
            obj = Util.dateTimeFormatToInstant(val, "yyyy-MM-dd kk:mm", null).toString();
            break;
        case 174: // date_fol_com
            obj = Util.dateTimeFormatToInstant(val, "yyyy-MM-dd", null).toString();
            break;
        case 176: // dischargedate
            obj = Util.dateTimeFormatToInstant(val, "yyyy-MM-dd", null).toString();
            break;
        case 177: // deathdate
            obj = Util.dateTimeFormatToInstant(val, "yyyy-MM-dd", null).toString();
            break;
        default:
            obj = val;
            break;
        }

        return obj;
    }

    /**
     * Process file for importing
     *
     * @param fReader
     *            Filereader obj
     * @param fileTime
     *            File time
     */
    private StatusCode processFile(BufferedReader fReader, Instant fileTime) {
        String dbName = DBConfiguration.Meta.DBNAME;
        influxDB.query(new Query("create database " + dbName, dbName));

        // Batch records perp
        BatchPoints records = BatchPoints.database(dbName).consistency(InfluxDB.ConsistencyLevel.ALL).build();

        try (CSVReader csvReader = new CSVReader(fReader)) {
            // CSV header
            String[] columnNames = csvReader.readNext();
            processHeader(columnNames);
            int columnCount = columnNames.length;
            int bulkInsertMax = InfluxappConfig.PERFORMANCE_INDEX / columnCount;

            String[] values;
            int batchCnt = 0;

            // Import every line
            while ((values = csvReader.readNext()) != null) {
                if (columnCount != values.length)
                    throw new RuntimeException(); // Line mismatch
                Map<String, Object> lineKVMap = new HashMap<>();
                Map<String, String> tags = new HashMap<>();

                for (int i = 0; i < columnCount; i++) {
                    Object obj = genObjFromLine(i, values[i], tags);
                    if (obj == null) {
                        // Lines that are tags
                        continue;
                    }
                    lineKVMap.put(columnNames[i], obj);
                }

                Point record = Point.measurement(tableName).time(fileTime.toEpochMilli(), TimeUnit.MILLISECONDS).tag(tags).fields(lineKVMap).build();
                records.point(record);
                batchCnt++;

                if (batchCnt > bulkInsertMax) {
                    influxDB.write(records);
                    records = BatchPoints.database(dbName).consistency(InfluxDB.ConsistencyLevel.ALL).build();
                    batchCnt = 0;
                }
            }
            // Last write
            if (batchCnt != 0) {
                influxDB.write(records);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return StatusCode.FILE_IO_ERROR;
        } catch (RuntimeException re) {
            re.printStackTrace();
            return StatusCode.FILE_FORMAT_ERROR;
        } catch (ParseException pe) {
            pe.printStackTrace();
            return StatusCode.FILE_FORMAT_ERROR;
        }

        return StatusCode.ALL_GOOD;
    }

    /**
     * Invoke importing a PCADatabase file
     *
     * @param path
     *            File path
     * @return Status
     */
    public StatusCode DoImport(String path) {
        File fileI = new File(path);
        StatusCode flag;

        String fileName = fileI.getName();
        // Fully spilt, e.g. 2018-02-21_0905
        String fileTime = fileName.trim().substring(18, fileName.length() - 4);

        BufferedReader fr = null;
        try {
            // Main import progress
            fr = new BufferedReader(new FileReader(fileI));
            flag = processFile(fr, Util.dateTimeFormatToInstant(fileTime, "yyyy-MM-dd_kkmm", null));
            // Below are error processing
        } catch (ParseException e) {
            flag = StatusCode.FILE_DATE_ERROR;
        } catch (FileNotFoundException e) {
            flag = StatusCode.FILE_NOT_FOUND;
        } finally {
            try {
                if (fr != null) {
                    fr.close();
                }
            } catch (IOException e) {
                flag = StatusCode.FILE_IO_ERROR;
            }
        }

        return flag;
    }

    public static void main(String[] args) {
        MetadataService ipms = new MetadataService();
        long startTime = System.currentTimeMillis();
        StatusCode c = ipms.DoImport("E:\\UPMC\\TSDB\\PCASDatabase_DATA_2018-02-21_0905.csv");
        // StatusCode c = ipms.DoImport("/tsdb/meta/PCASDatabase_DATA_2018-02-21_0905.csv");
        long endTime = System.currentTimeMillis();
        if (c == StatusCode.ALL_GOOD)
            System.out.println("Import time: " + String.format("%.2f", (endTime - startTime) / 60000.0) + " min\n");
    }

}
