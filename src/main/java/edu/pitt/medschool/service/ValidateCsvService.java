package edu.pitt.medschool.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import edu.pitt.medschool.model.WrongPatientsNum;
import edu.pitt.medschool.model.dto.CsvFileExample;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import edu.pitt.medschool.framework.util.TimeUtil;
import edu.pitt.medschool.model.PatientTimeLine;
import edu.pitt.medschool.model.Wrongpatients;
import edu.pitt.medschool.model.dao.CsvFileDao;
import edu.pitt.medschool.model.dto.CsvFile;
import edu.pitt.medschool.model.dto.GraphFilter;

@Service
public class ValidateCsvService {

    @Value("${machine}")
    private String machineId;
    private final CsvFileDao csvFileDao;

    public ValidateCsvService(CsvFileDao csvFileDao) {
        this.csvFileDao = csvFileDao;
    }

    private Date strToDate(String str) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        Date date = null;
        try {
            date = format.parse(str);
        } catch (ParseException pe) {
            pe.printStackTrace();
        }
        return date;
    }

    private String processFirstLineInCSV(String fLine, String pid) {
        if (!fLine.toUpperCase().contains(pid))
            throw new RuntimeException("Wrong PID in filename!");
        if (fLine.length() < 50)
            throw new RuntimeException("File UUID misformat!");
        if (!fLine.substring(fLine.length() - 40, fLine.length() - 4)
                .matches("([\\w\\d]){8}-([\\w\\d]){4}-([\\w\\d]){4}-([\\w\\d]){4}-([\\w\\d]){12}"))
            throw new RuntimeException("File does not have a valid UUID!");
        return fLine.substring(fLine.length() - 40, fLine.length() - 4);
    }

    public CsvFile getHeaderTime(String dir, String filename){
        CsvFile validateBean = new CsvFile();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(dir));
            String firstline = reader.readLine();
            for (int i = 0; i < 3; i++) {
                reader.readLine();
            }
            String header_time = reader.readLine().split(",")[1];
            header_time = header_time + " " + reader.readLine().split(",")[1];
            String pid;
            // PUH-20xx_xxx
            // UAB-010_xx
            // TBI-1001_xxx
            if (filename.startsWith("PUH-")) {
                pid = filename.substring(0, 12).trim().toUpperCase();

            } else if (filename.startsWith("UAB")) {
                pid = filename.substring(0, 7).trim().toUpperCase();

            } else {
                pid = filename.substring(0, 8).trim().toUpperCase();

            }

            File file = new File(dir);
            for (int j = 0; j < 2; j++) {
                reader.readLine();
            }
            String[] firstdata = reader.readLine().split(",");
            validateBean.setWidth(firstdata.length);

            reader.close();

            ZoneId zoneId = ZoneId.of("America/New_York");

            validateBean.setFilename(file.getName());
            validateBean.setPid(pid);
            validateBean.setUuid(processFirstLineInCSV(firstline, validateBean.getPid()));
            validateBean.setHeaderTime(LocalDateTime.ofInstant(strToDate(header_time).toInstant(), zoneId));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return validateBean;

    }

    public CsvFile analyzeCsv(String dir,String filename) {
        CsvFile validateBean = new CsvFile();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(dir));
            String line;
            String firstline = reader.readLine();
            for (int i = 0; i < 3; i++) {
                reader.readLine();
            }
            String header_time = reader.readLine().split(",")[1];
            header_time = header_time + " " + reader.readLine().split(",")[1];
            String pid;
            String fn_laterpart;
            // PUH-20xx_xxx
            // UAB-010_xx
            // TBI-1001_xxx
            if (filename.startsWith("PUH-")) {
                pid = filename.substring(0, 12).trim().toUpperCase();
                fn_laterpart = filename.substring(12).toLowerCase();
            } else if (filename.startsWith("UAB")) {
                pid = filename.substring(0, 7).trim().toUpperCase();
                fn_laterpart = filename.substring(7).toLowerCase();
            } else {
                pid = filename.substring(0, 8).trim().toUpperCase();
                fn_laterpart = filename.substring(8).toLowerCase();
            }
            // Ar or NoAr
            if (fn_laterpart.contains("noar")) {
                validateBean.setAr(false);
            } else if (fn_laterpart.contains("ar")) {
                validateBean.setAr(true);
            }
            File file = new File(dir);
            int count = 1;
            for (int j = 0; j < 2; j++) {
                reader.readLine();
            }
            String[] firstdata = reader.readLine().split(",");
            Double start_time = Double.valueOf(firstdata[0]);
            validateBean.setWidth(firstdata.length);
            String end_time = null;
            while ((line = reader.readLine()) != null) {
                end_time = line;
                count += 1;
            }
            reader.close();

            ZoneId zoneId = ZoneId.of("America/New_York");

            validateBean.setSize(file.length());
            validateBean.setPath(dir);
            validateBean.setFilename(file.getName());
            validateBean.setLength(count);
            LocalDateTime start = LocalDateTime
                    .ofInstant(TimeUtil.serialTimeToDate(start_time, TimeUtil.nycTimeZone).toInstant(), zoneId);
            validateBean.setStartTime(start);
            LocalDateTime end = LocalDateTime.ofInstant(
                    TimeUtil.serialTimeToDate(Double.valueOf(end_time.split(",")[0]), TimeUtil.nycTimeZone).toInstant(),
                    zoneId);
            validateBean.setEndTime(end);
            validateBean.setPid(pid);
            validateBean.setUuid(processFirstLineInCSV(firstline, validateBean.getPid()));
            validateBean.setHeaderTime(LocalDateTime.ofInstant(strToDate(header_time).toInstant(), zoneId));
            validateBean.setMachine(machineId);
            validateBean.setDensity((count*1.0)/(Duration.between(start,end).getSeconds()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return validateBean;
    }

    public int insertCsvFile(CsvFile csvFile) throws Exception {
        return csvFileDao.insert(csvFile);
    }

    public List<PatientTimeLine> getPatientTimeLines(String machine) {
        return csvFileDao.getPatientTimeLines(machine);
    }

    public String getFilteredtPatientTimeLines(String machine, GraphFilter filter) throws Exception {
        throw new NumberFormatException();
    }

    static class TimelineCompare implements Comparator<long[]> {
        @Override
        public int compare(long[] o1, long[] o2) {
            if (o1[0] > o2[0]) {
                return 1;
            } else if (o1[0] < o2[0]) {
                return -1;
            } else {
                if (o1[1] > o2[1]) {
                    return 1;
                } else {
                    return -1;
                }
            }
        }
    }

    public ArrayList<Wrongpatients> getWrongPatients(List<PatientTimeLine> patientTimeLines) {
        ArrayList<Wrongpatients> wrongpatients = new ArrayList<>();
        HashMap<String, HashMap<String, List<Integer>>> documents = new HashMap<>();
        HashMap<String, HashMap<String, List<long[]>>> timelines = new HashMap<>();
        // create hashmap for patients
        for (PatientTimeLine p : patientTimeLines) {
            if (timelines.containsKey(p.getPid()) && timelines.get(p.getPid()).containsKey(p.getFiletype())) {
                HashMap<String, List<long[]>> innermap = timelines.get(p.getPid());
                List<long[]> timeline = innermap.get(p.getFiletype());
                long[] ts = { p.getRelativeStartTime(), p.getRelativeEndTime(), p.getLength() };
                timeline.add(ts);
                innermap.put(p.getFiletype(), timeline);
                timelines.put(p.getPid(), innermap);

            } else if (timelines.containsKey(p.getPid()) && !timelines.get(p.getPid()).containsKey(p.getFiletype())) {
                List<long[]> tmplist = new ArrayList<>();
                HashMap<String, List<long[]>> innermap = timelines.get(p.getPid());
                long[] ts = { p.getRelativeStartTime(), p.getRelativeEndTime(), p.getLength() };
                tmplist.add(ts);
                innermap.put(p.getFiletype(), tmplist);
                timelines.put(p.getPid(), innermap);

            } else {
                List<long[]> tmplist = new ArrayList<>();
                HashMap<String, List<long[]>> innermap = new HashMap<>();
                long[] ts = { p.getRelativeStartTime(), p.getRelativeEndTime(), p.getLength() };
                tmplist.add(ts);
                innermap.put(p.getFiletype(), tmplist);
                timelines.put(p.getPid(), innermap);
            }

            // create hashmap for document number
            if (documents.containsKey(p.getPid()) && documents.get(p.getPid()).containsKey(p.getFiletype())) {
                HashMap<String, List<Integer>> innermap = documents.get(p.getPid());
                List<Integer> documentNo = innermap.get(p.getFiletype());
                documentNo.add(p.getDocumentNo());
                innermap.put(p.getFiletype(), documentNo);
                documents.put(p.getPid(), innermap);

            } else if (documents.containsKey(p.getPid()) && !documents.get(p.getPid()).containsKey(p.getFiletype())) {
                List<Integer> tmplist = new ArrayList<>();
                HashMap<String, List<Integer>> innermap = documents.get(p.getPid());
                tmplist.add(p.getDocumentNo());
                innermap.put(p.getFiletype(), tmplist);
                documents.put(p.getPid(), innermap);

            } else {
                List<Integer> tmplist = new ArrayList<>();
                HashMap<String, List<Integer>> innermap = new HashMap<>();
                tmplist.add(p.getDocumentNo());
                innermap.put(p.getFiletype(), tmplist);
                documents.put(p.getPid(), innermap);
            }
        }

        // detect wrong patients
        for (HashMap.Entry<String, HashMap<String, List<long[]>>> patinet : timelines.entrySet()) {
            // System.out.println(patinet.getKey());
            Wrongpatients wrongpatient = new Wrongpatients();
            wrongpatient.setPid(patinet.getKey());
            List<long[]> ar = patinet.getValue().get("ar");
            List<long[]> noar = patinet.getValue().get("noar");
            // System.out.println(noar.size());
            if (ar != null) {
                ar.sort(new TimelineCompare());
                for (int i = 0; i < ar.size() - 1; i++) {
                    if (ar.get(i)[1] > ar.get(i + 1)[0]) {
                        wrongpatient.setIsoverlap(true);
                    }
                }
            }
            if (noar != null) {
                noar.sort(new TimelineCompare());
                for (int i = 0; i < noar.size() - 1; i++) {
                    if (noar.get(i)[1] > noar.get(i + 1)[0]) {
                        wrongpatient.setIsoverlap(true);
                    }
                }
            }
            if(ar!=null && noar!=null){
                for (long[] longs : ar) {
                    boolean diff_time = true;
                    for (long[] longs1 : noar) {
                        if (longs[0] == longs1[0] && longs[1] == longs1[1] && longs[2] == longs1[2]) {
                            diff_time = false;
                        }
                    }
                    wrongpatient.setDiffTime(diff_time);
                }
            }


            List<Integer> ar_num = documents.get(patinet.getKey()).get("ar");
            List<Integer> noar_num = documents.get(patinet.getKey()).get("noar");
            if (ar_num == null) {
                ar_num = new ArrayList<>();
            }
            if (noar_num == null) {
                noar_num = new ArrayList<>();
            }
            Collections.sort(ar_num);
            Collections.sort(noar_num);
            List<Integer> miss_ar = new ArrayList<>();
            List<Integer> miss_noar = new ArrayList<>();
            int i = 0;
            int j = 0;
            while (i < ar_num.size() && ar_num.get(i) == -1) {
                wrongpatient.setWrongname(true);
                i++;
            }
            while (j < noar_num.size() && noar_num.get(j) == -1) {
                wrongpatient.setWrongname(true);
                j++;
            }
            while (i < ar_num.size() && j < noar_num.size()) {
                if (ar_num.get(i) == noar_num.get(j)) {
                    i++;
                    j++;
                } else if (ar_num.get(i) > noar_num.get(j)) {
                    miss_ar.add(noar_num.get(j));
                    j++;
                } else {
                    miss_noar.add(ar_num.get(i));
                    i++;
                }
            }
            if (i < ar_num.size()) {
                miss_noar.addAll(ar_num.subList(i, ar_num.size()));
            }
            if (j < noar_num.size()) {
                miss_ar.addAll(noar_num.subList(j, noar_num.size()));
            }

            wrongpatient.setAr_miss(miss_ar);
            wrongpatient.setNoar_miss(miss_noar);

            if (!wrongpatient.getAr_miss().isEmpty() || wrongpatient.isIsoverlap() || !wrongpatient.getNoar_miss().isEmpty()
                    || wrongpatient.isWrongname() || wrongpatient.isDiffTime()) {
                wrongpatients.add(wrongpatient);
            }

        }
        return wrongpatients;
    }

    public WrongPatientsNum getWrongPatientsNum(List<PatientTimeLine> patientTimeLines){
        WrongPatientsNum wrongPatientsNum = new WrongPatientsNum();
        ArrayList<Wrongpatients> wrongpatients = getWrongPatients(patientTimeLines);
        int overlap = 0;
        int missAr=0;
        int missNoar = 0;
        int wrongName = 0;
        int diffTime = 0;
        for(Wrongpatients w:wrongpatients){
            if(w.isWrongname()){
                wrongName++;
            }
            if(!w.getNoar_miss().isEmpty()){
                missNoar++;
            }
            if(!w.getAr_miss().isEmpty()){
                missAr++;
            }
            if(w.isIsoverlap()){
                overlap++;
            }
            if(w.isDiffTime()){
                diffTime++;
            }
        }
        wrongPatientsNum.setMissAr(missAr);
        wrongPatientsNum.setMissNoar(missNoar);
        wrongPatientsNum.setOverlap(overlap);
        wrongPatientsNum.setWrongName(wrongName);
        wrongPatientsNum.setDiffTime(diffTime);
        return wrongPatientsNum;
    }

    public int addCsvFileHearderWidth(CsvFile csvFile){
        return csvFileDao.addCsvFileHearderWidth(csvFile);
    }
}
