package app.service;

import java.util.List;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.impl.InfluxDBResultMapper;
import org.springframework.stereotype.Service;

import app.common.InfluxappConfig;
import app.model.CSVFile;


/**
 * Csv file service
 */
@Service
public class CsvFileService {

    private static final InfluxDB influxDB = InfluxDBFactory.connect(InfluxappConfig.IFX_ADDR, InfluxappConfig.IFX_USERNAME, InfluxappConfig.IFX_PASSWD);
    private static final InfluxDBResultMapper resultMapper = new InfluxDBResultMapper();

    private static final String fileQueryStr = "SELECT * FROM " + InfluxappConfig.IFX_TABLE_FILES;

    //TODO: AR state judge!

    /**
     * Find latest UUID for one patient
     *
     * @param pid PID
     * @return File UUID
     */
    public String FindLatestFileForPatient(String pid) {
        Query q = new Query(fileQueryStr + " WHERE \"pid\" = '" + pid.toUpperCase() + "' ORDER BY time DESC LIMIT 1", InfluxappConfig.IFX_DBNAME);
        return resultMapper.toPOJO(influxDB.query(q), CSVFile.class).get(0).getFile_uuid();
    }

    /**
     * Find oldest UUID for one patient
     *
     * @param pid PID
     * @return File UUID
     */
    public String FindEarliestFileForPatient(String pid) {
        Query q = new Query(fileQueryStr + " WHERE \"pid\" = '" + pid.toUpperCase() + "' ORDER BY time ASC LIMIT 1", InfluxappConfig.IFX_DBNAME);
        return resultMapper.toPOJO(influxDB.query(q), CSVFile.class).get(0).getFile_uuid();
    }

    /**
     * File all files for one patient
     *
     * @param pid PID
     * @return List of files
     */
    public List<CSVFile> FindFilesForPatient(String pid) {
        Query q = new Query(fileQueryStr + " WHERE \"pid\" = '" + pid.toUpperCase() + "'", InfluxappConfig.IFX_DBNAME);
        return resultMapper.toPOJO(influxDB.query(q), CSVFile.class);
    }

    /**
     * Find all imported files
     *
     * @return List of files
     */
    public List<CSVFile> FindAllFiles() {
        Query q = new Query(fileQueryStr, InfluxappConfig.IFX_DBNAME);
        return resultMapper.toPOJO(influxDB.query(q), CSVFile.class);
    }

    public static void main(String[] args) {
        CsvFileService cfs = new CsvFileService();
        List<CSVFile> f = cfs.FindFilesForPatient("puh-2010-080");
        String a = cfs.FindEarliestFileForPatient("puh-2010-080");
        String b = cfs.FindLatestFileForPatient("puh-2010-080");
    }
}
