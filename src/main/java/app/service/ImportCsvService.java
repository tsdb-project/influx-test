package app.service;


import app.common.InfluxappConfig;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Auto-parallel importing CSV data
 */
@Service
public class ImportCsvService {

    private final String taskUUID = UUID.randomUUID().toString();



    /**
     * Get UUID for this task
     *
     * @return
     */
    public String GetUUID() {
        return this.taskUUID;
    }

    public static void main(String[] args) {

    }

}
