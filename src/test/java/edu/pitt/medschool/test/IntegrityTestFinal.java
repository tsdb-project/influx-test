package edu.pitt.medschool.test;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.dto.QueryResult.Result;
import org.influxdb.dto.QueryResult.Series;

import com.opencsv.CSVReader;

import edu.pitt.medschool.config.InfluxappConfig;
import okhttp3.OkHttpClient;

class FileInfo {
    private String name;
    private String uuid;
    private String start;
    private String end;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }
}

public class IntegrityTestFinal {

    static InfluxDB idb = generateIdbClient(true);
    static File integrityFile = new File("/Users/Isolachine/Desktop/integrity.csv");
    static File impedenceFile = new File("/Users/Isolachine/Desktop/integrity_impedence.csv");
    static FileWriter writer;

    public static void checkPatientFiles(String pid, FileWriter fw) throws IOException {
        String queryString = String.format("SELECT \"I192_1\" FROM \"%s\" GROUP BY * LIMIT 1;", pid);
        Query query = new Query(queryString, "data");
        QueryResult result = idb.query(query);
        List<FileInfo> fileInfoList = new ArrayList<>();

        if (result.getResults().get(0).getSeries() != null) {
            for (Result e : result.getResults()) {
                for (Series s : e.getSeries()) {
                    for (List<Object> o : s.getValues()) {
                        FileInfo info = new FileInfo();
                        info.setName(s.getTags().get("fileName"));
                        info.setUuid(s.getTags().get("fileUUID"));
                        info.setStart(o.get(0).toString());
                        fileInfoList.add(info);
                    }
                }
            }
        } else {
            System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%");
            System.out.println("nothing");
            System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%");
            fw.write("%%%%%%%%%%%%%%%%%%%%%%%%%\n");
            fw.write("nothing\n");
            fw.write("%%%%%%%%%%%%%%%%%%%%%%%%%\n");
        }

        queryString = String.format("SELECT \"I192_1\" FROM \"%s\" GROUP BY * ORDER BY time DESC LIMIT 1;", pid);
        query = new Query(queryString, "data");
        result = idb.query(query);
        if (result.getResults().get(0).getSeries() != null) {
            for (Result e : result.getResults()) {
                for (Series s : e.getSeries()) {
                    for (List<Object> o : s.getValues()) {
                        for (int i = 0; i < fileInfoList.size(); i++) {
                            if (fileInfoList.get(i).getName().equals(s.getTags().get("fileName"))
                                    && fileInfoList.get(i).getUuid().equals(s.getTags().get("fileUUID"))) {
                                FileInfo info = fileInfoList.get(i);
                                info.setEnd(o.get(0).toString());
                                fileInfoList.set(i, info);
                            }
                        }
                    }
                }
            }
        }

        for (FileInfo fileInfo : fileInfoList) {
            fw.write(
                    fileInfo.getStart() + "," + fileInfo.getEnd() + "," + fileInfo.getUuid() + "," + fileInfo.getName() + "\n");
            // System.out.println(
            // fileInfo.getStart() + "," + fileInfo.getEnd() + "," + fileInfo.getUuid() + "," + fileInfo.getName());
        }
    }

    public static void checkPatientImpedenceFiles() throws IOException {
        FileWriter impedenceWriter = new FileWriter(impedenceFile);
        impedenceWriter.write("time,value,count\n");
        Reader reader = new FileReader(integrityFile);
        CSVReader csvReader = new CSVReader(reader);
        String pid = "";
        while (reader.ready()) {
            String[] fileInfo = csvReader.readNext();
            // if (fileInfo[0].startsWith("PUH-2010-072")) {
            // break;
            // }
            if (!fileInfo[0].startsWith("20")) {
                pid = fileInfo[0];
                impedenceWriter.write("------------\n" + pid + "\n");
            } else {
                impedenceWriter.write(fileInfo[3] + "\n");
                int offset = Integer.parseInt(fileInfo[0].substring(14, 16)) * 60
                        + Integer.parseInt(fileInfo[0].substring(17, 19));
                String queryString = String.format(
                        "SELECT mean(\"I192_1\"), count(\"I192_1\") FROM \"%s\" WHERE time >= '%s' AND time <= '%s' AND "
                                + "fileUUID = '%s' AND fileName = '%s' GROUP BY time(1h, %ss)",
                        pid, fileInfo[0], fileInfo[1], fileInfo[2], fileInfo[3], offset);
                System.out.println(queryString);
                Query query = new Query(queryString, "data");
                QueryResult result = idb.query(query);
                if (result.getResults().get(0).getSeries() != null) {
                    for (Result e : result.getResults()) {
                        for (Series s : e.getSeries()) {
                            for (List<Object> o : s.getValues()) {
                                int i = 0;
                                for (Object value : o) {
                                    i++;
                                    if (i < 3) {
                                        impedenceWriter.write(value + ",");
                                    } else {
                                        impedenceWriter.write(value + "\n");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        csvReader.close();
        impedenceWriter.close();
        // String queryString = String.format("SELECT \"I192_1\" FROM \"%s\" GROUP BY time(10m),* LIMIT 12;", pid);
        // Query query = new Query(queryString, "data");
        // QueryResult result = idb.query(query);
        // List<FileInfo> fileInfoList = new ArrayList<>();
        //
        // if (result.getResults().get(0).getSeries() != null) {
        // for (Result e : result.getResults()) {
        // for (Series s : e.getSeries()) {
        // for (List<Object> o : s.getValues()) {
        // FileInfo info = new FileInfo();
        // info.setName(s.getTags().get("fileName"));
        // info.setUuid(s.getTags().get("fileUUID"));
        // info.setStart(o.get(0).toString());
        // fileInfoList.add(info);
        // }
        // }
        // }
        // } else {
        // System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%");
        // System.out.println("nothing");
        // System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%");
        // fw.write("%%%%%%%%%%%%%%%%%%%%%%%%%\n");
        // fw.write("nothing\n");
        // fw.write("%%%%%%%%%%%%%%%%%%%%%%%%%\n");
        // }
        //
        // queryString = String.format("SELECT \"I192_1\" FROM \"%s\" GROUP BY * ORDER BY time DESC LIMIT 1;", pid);
        // query = new Query(queryString, "data");
        // result = idb.query(query);
        // if (result.getResults().get(0).getSeries() != null) {
        // for (Result e : result.getResults()) {
        // for (Series s : e.getSeries()) {
        // for (List<Object> o : s.getValues()) {
        // for (int i = 0; i < fileInfoList.size(); i++) {
        // if (fileInfoList.get(i).getName().equals(s.getTags().get("fileName"))
        // && fileInfoList.get(i).getUuid().equals(s.getTags().get("fileUUID"))) {
        // FileInfo info = fileInfoList.get(i);
        // info.setEnd(o.get(0).toString());
        // fileInfoList.set(i, info);
        // }
        // }
        // }
        // }
        // }
        // }
        //
        // for (FileInfo fileInfo : fileInfoList) {
        // fw.write(
        // fileInfo.getStart() + "," + fileInfo.getEnd() + "," + fileInfo.getUuid() + "," + fileInfo.getName() + "\n");
        // // System.out.println(
        // // fileInfo.getStart() + "," + fileInfo.getEnd() + "," + fileInfo.getUuid() + "," + fileInfo.getName());
        // }
    }

    public static void main(String[] args) throws IOException {
        checkPatientImpedenceFiles();
        // writer = new FileWriter(integrityFile);
        //
        // String queryString = String.format("Show measurements");
        // Query query = new Query(queryString, "data");
        // QueryResult result = idb.query(query);
        // List<List<Object>> pids = result.getResults().get(0).getSeries().get(0).getValues();
        // int i = 0;
        // for (List<Object> list : pids) {
        // i++;
        // String pid = list.get(0).toString();
        // if (pid.equals("PUH-2015-123")) {
        // System.out.println(pid);
        // writer.write(pid + "\n");
        // checkPatientFiles(pid, writer);
        // }
        // if (i > 5) {
        // // break;
        // }
        // }
        // writer.close();
    }

    private static InfluxDB generateIdbClient(boolean needGzip) {
        InfluxDB idb = InfluxDBFactory.connect(InfluxappConfig.IFX_ADDR, InfluxappConfig.IFX_USERNAME,
                InfluxappConfig.IFX_PASSWD, new OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS)
                        .readTimeout(90, TimeUnit.MINUTES).writeTimeout(120, TimeUnit.SECONDS));
        if (needGzip) {
            idb.enableGzip();
        } else {
            idb.disableGzip();
        }
        return idb;
    }

}