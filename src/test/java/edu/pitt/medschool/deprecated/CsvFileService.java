package edu.pitt.medschool.deprecated;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.impl.InfluxDBResultMapper;
import org.springframework.stereotype.Service;

import edu.pitt.medschool.config.DBConfiguration;
import edu.pitt.medschool.config.InfluxappConfig;
import edu.pitt.medschool.model.TSData.CSVFile;

import java.util.List;


/**
 * Csv file service
 */
@Service
@Deprecated
public class CsvFileService {

    private static final InfluxDB influxDB = InfluxDBFactory.connect(InfluxappConfig.IFX_ADDR, InfluxappConfig.IFX_USERNAME, InfluxappConfig.IFX_PASSWD);
    private static final InfluxDBResultMapper resultMapper = new InfluxDBResultMapper();

    private static final String fileQueryStr = "SELECT * FROM " + DBConfiguration.Sys.FILE;
    private final String dbName = DBConfiguration.Sys.DBNAME;

    //TODO: AR state judge!

    /**
     * Find latest UUID for one patient
     *
     * @param pid PID
     * @return File UUID
     */
    public String FindLatestFileForPatient(String pid) {
        Query q = new Query(fileQueryStr + " WHERE \"pid\" = '" + pid.toUpperCase() + "' ORDER BY time DESC LIMIT 1", dbName);
        return resultMapper.toPOJO(influxDB.query(q), CSVFile.class).get(0).getFile_uuid();
    }

    /**
     * Find oldest UUID for one patient
     *
     * @param pid PID
     * @return File UUID
     */
    public String FindEarliestFileForPatient(String pid) {
        Query q = new Query(fileQueryStr + " WHERE \"pid\" = '" + pid.toUpperCase() + "' ORDER BY time ASC LIMIT 1", dbName);
        return resultMapper.toPOJO(influxDB.query(q), CSVFile.class).get(0).getFile_uuid();
    }

    /**
     * File all files for one patient
     *
     * @param pid PID
     * @return List of files
     */
    public List<CSVFile> FindFilesForPatient(String pid) {
        Query q = new Query(fileQueryStr + " WHERE \"pid\" = '" + pid.toUpperCase() + "'", dbName);
        return resultMapper.toPOJO(influxDB.query(q), CSVFile.class);
    }

    /**
     * Find all imported files
     *
     * @return List of files
     */
    public List<CSVFile> FindAllFiles() {
        Query q = new Query(fileQueryStr, dbName);
        return resultMapper.toPOJO(influxDB.query(q), CSVFile.class);
    }

    public static void main(String[] args) {
        CsvFileService cfs = new CsvFileService();
        List<CSVFile> f = cfs.FindFilesForPatient("puh-2010-080");
        String a = cfs.FindEarliestFileForPatient("puh-2010-080");
        String b = cfs.FindLatestFileForPatient("puh-2010-080");
    }
}
