package edu.pitt.medschool.deprecated;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.opencsv.CSVReader;

import edu.pitt.medschool.framework.util.TimeUtil;
import edu.pitt.medschool.model.dao.PatientDao;
import edu.pitt.medschool.model.dto.PatientWithBLOBs;

/**
 * Importing patient metadata into DB Based on mail at 02/21/2018
 * 
 * @deprecated Unchecked Time zone problems and this file not necessary
 */
@Service
@Deprecated
public class ImportMetadataService {

    /**
     * Status code for this service
     */
    public enum StatusCode {
    ALL_GOOD, FILE_IO_ERROR, FILE_NOT_FOUND, FILE_DATE_ERROR, FILE_FORMAT_ERROR
    }

    @Autowired
    private PatientDao pdo;

    /**
     * CSV Header processing
     */
    private void processHeader(String[] hdr) {
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
                return -1;
            } else {
                return obj;
            }
        }

        switch (i) {
            case 0:
                // PID
                obj = val.trim().toUpperCase();
                break;
            case 2:
                // Gender
                obj = val.equals("1") ? "F" : "M";
                break;
            case 3:
                // ArrestLocation
                obj = val.equals("1") ? "Outside" : "Inside";
                break;
            case 1:
                obj = Integer.parseInt(val);
                break;
            case 175:
                // Survived
                obj = val.equals("1") ? "Y" : "N";
                break;
            case 11: // Arrest date
                obj = TimeUtil.dateTimeFormatToDate(val, "yyyy-MM-dd", null).toString();
                break;
            case 12: // Arrest time
                obj = TimeUtil.dateTimeFormatToDate(val, "yyyy-MM-dd kk:mm", null).toString();
                break;
            case 30: // Arrive date
                obj = TimeUtil.dateTimeFormatToDate(val, "yyyy-MM-dd kk:mm", null).toString();
                break;
            case 174: // date_fol_com
                obj = TimeUtil.dateTimeFormatToDate(val, "yyyy-MM-dd", null).toString();
                break;
            case 176: // dischargedate
                obj = TimeUtil.dateTimeFormatToDate(val, "yyyy-MM-dd", null).toString();
                break;
            case 177: // deathdate
                obj = TimeUtil.dateTimeFormatToDate(val, "yyyy-MM-dd", null).toString();
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

                PatientWithBLOBs p = new PatientWithBLOBs();
                // TODO: Only support PID/Age/Gender now
                for (int i = 0; i < 3; i++) {
                    Object obj = genObjFromLine(i, values[i]);
                    switch (i) {
                        case 0:
                            p.setId((String) obj);
                            break;
                        case 1:
                            p.setAge((Byte) obj);
                            break;
                        case 2:
                            p.setFemale(obj.equals("F"));
                            break;
                        default:
                            continue;
                    }
                }
                pdo.insert(p);
            }

        } catch (IOException ioe) {
            ioe.printStackTrace();
            return StatusCode.FILE_IO_ERROR;
        } catch (Exception re) {
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
            flag = processFile(fr, TimeUtil.dateTimeFormatToInstant(fileTime, "yyyy-MM-dd_kkmm", null));
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
