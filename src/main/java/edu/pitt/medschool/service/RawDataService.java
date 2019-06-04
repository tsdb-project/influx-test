package edu.pitt.medschool.service;

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.influxdb.InfluxDB;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.pitt.medschool.config.DBConfiguration;
import edu.pitt.medschool.controller.load.vo.CsvFileVO;
import edu.pitt.medschool.framework.influxdb.InfluxUtil;
import edu.pitt.medschool.framework.influxdb.ResultTable;
import edu.pitt.medschool.framework.util.TimeUtil;
import edu.pitt.medschool.model.TSData.RawData;
import edu.pitt.medschool.model.dao.CsvFileDao;
import edu.pitt.medschool.model.dao.ImportedFileDao;
import edu.pitt.medschool.model.dao.PatientDao;
import edu.pitt.medschool.model.dto.CsvFile;
import edu.pitt.medschool.model.dto.Patient;

/**
 * @author Isolachine
 */
@Service
public class RawDataService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    CsvFileDao csvFileDao;
    @Autowired
    ImportedFileDao importedFileDao;
    @Autowired
    PatientDao patientDao;

    private final String dbDataName = DBConfiguration.Data.DBNAME;

    /**
     * Return the first available data for a patient
     *
     * @return Time
     */
    public Instant GetFirstAvailData(String pid, boolean hasAr) {
        String qT = "SELECT \"time\",\"Time\" FROM \"%s\" WHERE \"arType\"='%s' ORDER BY \"time\" ASC LIMIT 1";
        return availDataTimeQ(qT, pid.toUpperCase(), hasAr);
    }

    /**
     * Return the last available data for a patient
     *
     * @return Time
     */
    public Instant GetLastAvailData(String pid, boolean hasAr) {
        String qT = "SELECT \"time\",\"Time\" FROM \"%s\" WHERE \"arType\"='%s' ORDER BY \"time\" DESC LIMIT 1";
        return availDataTimeQ(qT, pid.toUpperCase(), hasAr);
    }

    public List<RawData> selectAllRawDataInColumns(String patientTable, List<String> columnNames) throws ParseException {
        String columns = String.join(", ", columnNames);
        String queryString = "Select " + columns + " from \"" + patientTable + "\"";
        Query q = new Query(queryString, dbDataName);
        InfluxDB influxDB = InfluxUtil.generateIdbClient(true);
        QueryResult result = influxDB.query(q);

        List<RawData> data = new ArrayList<>();
        if (!result.hasError() && !result.getResults().get(0).hasError()) {
            for (List<Object> res : result.getResults().get(0).getSeries().get(0).getValues()) {
                RawData aRow = new RawData();
                aRow.setTime(Instant.ofEpochMilli(
                        TimeUtil.dateTimeFormatToTimestamp(res.get(0).toString(), "yyyy-MM-dd'T'HH:mm:ss'Z'", null)));
                aRow.setColumnNames(columnNames);
                List<Double> values = new ArrayList<>();
                for (int i = 1; i < res.size(); i++) {
                    values.add(Double.valueOf(res.get(i).toString()));
                }
                aRow.setValues(values);

                data.add(aRow);
            }

        }
        influxDB.close();
        return data;
    }

    private Instant availDataTimeQ(String qT, String pid, boolean hasAr) {
        InfluxDB influxDB = InfluxUtil.generateIdbClient(true);
        ResultTable[] res = InfluxUtil.justQueryData(influxDB, true, String.format(qT, pid, hasAr ? "ar" : "noar"));
        influxDB.close();

        // Table does not exist
        if (res.length == 0)
            return null;
        return Instant.parse(res[0].getDataByColAndRow(0, 0).toString());
    }

    @SuppressWarnings("unused")
    public static void main(String[] args) throws ParseException {
        RawDataService rawDataService = new RawDataService();

        Instant a;
        a = rawDataService.GetFirstAvailData("PUH-2010-087", true);
        a = rawDataService.GetLastAvailData("PUH-2010-087", true);

        List<String> list = new ArrayList<>();
        list.add("I1_1");
        list.add("I1_2");
        List<RawData> news = rawDataService.selectAllRawDataInColumns("PUH-2010-014", list);

        for (int i = 0; i < news.size(); i++) {
            System.out.print(news.get(i).getTime().getEpochSecond());
            System.out.println(" :  " + news.get(i).getValues().get(0));
        }
    }

    public List<CsvFileVO> selectPatientFilesByPatientId(String patientId) {
        List<CsvFile> list = csvFileDao.selectByPatientId(patientId);

        List<CsvFileVO> ar = new ArrayList<>();
        List<CsvFileVO> noar = new ArrayList<>();

        for (CsvFile csvFile : list) {
            if (csvFile.getAr()) {
                ar.add(new CsvFileVO(csvFile));
            } else {
                noar.add(new CsvFileVO(csvFile));
            }
        }

        // Has a valid counterpart?
        // Also deals with multiple counterparts!
        for (CsvFileVO arVO : ar) {
            for (CsvFileVO noarVO : noar) {
                CsvFile arFile = arVO.getCsvFile();
                CsvFile noarFile = noarVO.getCsvFile();
                if (arFile.getStartTime().isEqual(noarFile.getStartTime()) && arFile.getEndTime().isEqual(noarFile.getEndTime())
                        && arFile.getLength().equals(noarFile.getLength())) {
                    arVO.getCounterpart().add(noarFile);
                    noarVO.getCounterpart().add(arFile);
                }
            }
        }

        Comparator<CsvFileVO> comparator = new Comparator<CsvFileVO>() {
            @Override
            public int compare(CsvFileVO o1, CsvFileVO o2) {
                int start = o1.getCsvFile().getStartTime().compareTo(o2.getCsvFile().getStartTime());
                if (start == 0) {
                    return o1.getCsvFile().getEndTime().compareTo(o2.getCsvFile().getEndTime());
                } else {
                    return start;
                }
            }
        };

        Collections.sort(ar, comparator);
        Collections.sort(noar, comparator);

        Patient patientMeta = patientDao.selectByPatientId(patientId);

        LocalDateTime arrestTime;
        if (patientMeta.getArrestdate() == null) {
            arrestTime = null;
        } else {
            arrestTime = patientMeta.getArresttime() == null ? patientMeta.getArrestdate().atStartOfDay()
                    : patientMeta.getArresttime();
            ZoneId zoneId = ZoneId.of("America/New_York");
            arrestTime = arrestTime.atZone(zoneId).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
        }

        for (int i = 0; i < ar.size(); i++) {
            Duration gap;
            if (i == 0) {
                if (arrestTime == null) {
                    continue;
                }
                gap = Duration.between(arrestTime, ar.get(i).getCsvFile().getStartTime());
            } else {
                gap = Duration.between(ar.get(i - 1).getCsvFile().getEndTime(), ar.get(i).getCsvFile().getStartTime());
            }
            if (gap.isNegative()) {
                ar.get(i).setGap("-" + DurationFormatUtils.formatDuration(-gap.toMillis(), "HH:mm:ss", true));
            } else {
                ar.get(i).setGap(DurationFormatUtils.formatDuration(gap.toMillis(), "HH:mm:ss", true));
            }
        }

        for (int i = 0; i < noar.size(); i++) {
            Duration gap;
            if (i == 0) {
                if (arrestTime == null) {
                    continue;
                }
                gap = Duration.between(arrestTime, noar.get(i).getCsvFile().getStartTime());
            } else {
                gap = Duration.between(noar.get(i - 1).getCsvFile().getEndTime(), noar.get(i).getCsvFile().getStartTime());
            }
            if (gap.isNegative()) {
                noar.get(i).setGap("-" + DurationFormatUtils.formatDuration(-gap.toMillis(), "HH:mm:ss", true));
            } else {
                noar.get(i).setGap(DurationFormatUtils.formatDuration(gap.toMillis(), "HH:mm:ss", true));
            }
        }
        ar.addAll(noar);
        return ar;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<String> getDeletedFilesByPid(String pid) throws Exception {
        List<String> resolveResult = new ArrayList<>();
        try {
            resolveResult = csvFileDao.selectDeletedFilesByPatientId(pid);
        }catch (Exception e){
            logger.debug("RETREVING DELETED FILES FAILED!");
        }
        return resolveResult;
    }

    @Transactional(rollbackFor = Exception.class)
    public int resolveAllFilesByPid(String pid) throws Exception {
        int resolveResult = 0;
        try {
            resolveResult = csvFileDao.resolveAllFilesByPid(pid);
        }catch (Exception e){
            logger.debug("RESOLVE DATA FAILED!");
        }
        return resolveResult;
    }

    @Transactional(rollbackFor = Exception.class)
    public int resolveFileByFile(CsvFile file) throws Exception {
        int resolveResult = 0;
        try {
            resolveResult = csvFileDao.resolveFileByFile(file);
        }catch (Exception e){
            logger.debug("RESOLVE DATA FAILED!");
        }
        return resolveResult;
    }

    @Transactional(rollbackFor = Exception.class)
    public int changeComment(CsvFile file) throws Exception {
        int changeCommentResult = 0;
        try {
            changeCommentResult = csvFileDao.changeComment(file);
        }catch (Exception e){
            logger.debug("CHANGE COMMENT FAILED!");
        }
        return changeCommentResult;
    }

    @Transactional(rollbackFor = Exception.class)
    public int deletePatientDataByFile(CsvFile file) throws Exception {
        Map<String, String> tags = new HashMap<>();
        tags.put("fileName", file.getFilename().replace(".csv", ""));

//      delete from influxDB
        boolean deleteInfluxDataResult = false;
        deleteInfluxDataResult = InfluxUtil.deleteDataByTagValues(file.getPid(), tags);


        int deleteResult = 0;
        int first = 0;
        int second= 0;
        if (deleteInfluxDataResult) {
            System.out.println("delete from influx success");
            first = importedFileDao.deletePatientDataByFile(file);
            second = csvFileDao.deletePatientDataByFile(file);
            deleteResult = first*second;
        }
        try {
            if (deleteResult == 0) {
                System.out.println(first);
                System.out.println(second);
                throw new Exception();
            }
        } catch (Exception e) {
            logger.debug("DELETE DATA FAILED!");
        }
        return deleteResult;
    }

    @Transactional(rollbackFor = Exception.class)
    public int pseudoDeleteFile(CsvFile file) throws Exception {
        int deleteResult = 0;
        try {
            deleteResult = csvFileDao.pseudoDeleteFile(file);
        } catch (Exception e) {
            logger.debug("DELETE DATA FAILED!");
        }
        return deleteResult;
    }

}
