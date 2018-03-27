package edu.pitt.medschool.service;

import com.opencsv.CSVReader;
import edu.pitt.medschool.config.DBConfiguration;
import edu.pitt.medschool.config.InfluxappConfig;
import edu.pitt.medschool.framework.util.Util;
import org.springframework.stereotype.Service;

import java.io.*;
import java.text.ParseException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Importing patient metadata into DB Based on mail at 02/21/2018
 */
//TODO: Switch to MySQL storage
@Service
public class ImportMetadataService {

    /**
     * Status code for this service
     */
    public enum StatusCode {
        ALL_GOOD, FILE_IO_ERROR, FILE_NOT_FOUND, FILE_DATE_ERROR, FILE_FORMAT_ERROR
    }

    /**
     * CSV Header processing
     */
    private void processHeader(String[] hdr) {
        // TODO: Need more soild vaildation
        if (hdr.length != 209)
            throw new RuntimeException();
    }

    /**
     * Line data: Could be: util.Date, String, Double or int
     *
     * @return An object
     */
    private Object genObjFromLine(int i, String val) throws ParseException {
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
                obj = Util.dateTimeFormatToDate(val, "yyyy-MM-dd", null).toString();
                break;
            case 12: // Arrest time
                obj = Util.dateTimeFormatToDate(val, "yyyy-MM-dd kk:mm", null).toString();
                break;
            case 30: // Arrive date
                obj = Util.dateTimeFormatToDate(val, "yyyy-MM-dd kk:mm", null).toString();
                break;
            case 174: // date_fol_com
                obj = Util.dateTimeFormatToDate(val, "yyyy-MM-dd", null).toString();
                break;
            case 176: // dischargedate
                obj = Util.dateTimeFormatToDate(val, "yyyy-MM-dd", null).toString();
                break;
            case 177: // deathdate
                obj = Util.dateTimeFormatToDate(val, "yyyy-MM-dd", null).toString();
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
     * @param fReader  Filereader obj
     * @param fileTime File time
     */
    private StatusCode processFile(BufferedReader fReader, Instant fileTime) {

        try (CSVReader csvReader = new CSVReader(fReader)) {
            // CSV header
            String[] columnNames = csvReader.readNext();
            processHeader(columnNames);
            int columnCount = columnNames.length;

            String[] values;

            // Import every line
            while ((values = csvReader.readNext()) != null) {
                if (columnCount != values.length)
                    throw new RuntimeException(); // Line mismatch

                for (int i = 0; i < columnCount; i++) {
                    Object obj = genObjFromLine(i, values[i);
                    if (obj == null) {
                        continue;
                    }
                }

            }

        } catch (IOException ioe) {
            ioe.printStackTrace();
            return StatusCode.FILE_IO_ERROR;
        } catch (RuntimeException re) {
            re.printStackTrace();
            return StatusCode.FILE_FORMAT_ERROR;
        }

        return StatusCode.ALL_GOOD;
    }

    /**
     * Invoke importing a PCADatabase file
     *
     * @param path File path
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
        ImportMetadataService ipms = new ImportMetadataService();
        long startTime = System.currentTimeMillis();
        StatusCode c = ipms.DoImport("E:\\UPMC\\TSDB\\PCASDatabase_DATA_2018-02-21_0905.csv");
        // StatusCode c = ipms.DoImport("/tsdb/meta/PCASDatabase_DATA_2018-02-21_0905.csv");
        long endTime = System.currentTimeMillis();
        if (c == StatusCode.ALL_GOOD)
            System.out.println("Import time: " + String.format("%.2f", (endTime - startTime) / 60000.0) + " min\n");
    }

}
