/**
 *
 */
package edu.pitt.medschool.service;

import java.io.*;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import edu.pitt.medschool.model.dto.Patient;
import edu.pitt.medschool.model.dto.PatientWithBLOBs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.pitt.medschool.framework.util.MysqlColumnBean;
import edu.pitt.medschool.model.dao.PatientDao;

/**
 * service for returning column information of data
 *
 * @author Isolachine
 */
@Service
public class PatientService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    PatientDao patientDao;

    // Maximum Number of patients that includes in one batch
    private static final int PATIENT_BATCH_SIZE = 90;

    public List<MysqlColumnBean> getColumnInfo() {
        return patientDao.getColumnInfo();
    }

    public List<String> selecIdByfilter(String condition) {
        return patientDao.selecIdByfilter(condition);
    }

    public List<Patient> getAllPatientsComments() {
        return patientDao.getAllPatientsComments();
    }

    public PatientWithBLOBs getPatientByPid(String patientId) {
        return patientDao.selectById(patientId);
    }

    public int updatePatientInfo(PatientWithBLOBs patient) {
        int updateResult = 0;
        try {
            updateResult = patientDao.updatePatientInfo(patient);
        } catch (Exception e) {
            logger.debug("PATIENT INFO UPDATE FAILED!");
        }
        return updateResult;
    }

    // Transfer MM/DD/YY HH:MM to yyyy-MM-dd HH:mm:ss
    private String timeToStanderTime(String s) {
        String date = "";
        String time = "";
        if (s.split(" ").length == 1) {
            date = s.split(" ")[0];
            time = "00:00:00";
        } else {
            date = s.split(" ")[0];
            time = s.split(" ")[1];
        }
        String[] date_parts = date.split("/");
        String[] time_parts = time.split(":");
        String year = date_parts[0];
        if (year.length() == 2) {
            year = "20" + year;
        }

        String month = date_parts[1];
        if (month.length() == 1) {
            month = "0" + month;
        }

        String day = date_parts[2];
        if (day.length() == 1) {
            day = "0" + day;
        }

        String hour = time_parts[0];
        if (hour.length() == 1) {
            hour = "0" + hour;
        }

        String minute = time_parts[1];
        if (minute.length() == 1) {
            minute = "0" + minute;
        }
        return year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + "00";
    }

    private String getMethodName(String para) {
        StringBuilder result = new StringBuilder();
        String a[] = para.split("_");
        for (String s : a) {
            if (s.length() > 0) {
                result.append(s.substring(0, 1).toUpperCase());
                result.append(s.substring(1).toLowerCase());
            }
        }
        return "set" + result.toString();
    }

    public void startImportPatients(String dir) {
        try {
            Thread thread = new Thread(() -> {
                try {
                    // create log file to write
                    File patientImportLog = new File(dir.replace(".csv", ".txt"));
                    if (!patientImportLog.exists()) {
                        patientImportLog.createNewFile();
                    }
                    BufferedWriter logWriter = new BufferedWriter(new OutputStreamWriter(
                            new FileOutputStream(patientImportLog, true)));

                    writeLog(logWriter, "Start to Import patients from CSV");

                    // actual method that import the CSV
                    HashMap result = getPatientsFromCsv(logWriter, dir);

                    if (result.get("msg") == "success") {
                        writeLog(logWriter, "Successfully imported " + result.get("num") + " patients, all done");
                    } else {
                        writeLog(logWriter, "Successfully imported " + result.get("num") +
                                " patients, line " + ((int) result.get("num") + 1) + " is wrong");
                    }

                    logWriter.close();
                } catch (Exception Exception) {
                    logger.info("Unable to import new patients!");
                }
            });
            thread.start();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void writeLog(BufferedWriter bw, String log) {
        try {
            bw.write(log + "\n");
            bw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private HashMap getPatientsFromCsv(BufferedWriter logWriter, String dir) {
        List<PatientWithBLOBs> patients = new ArrayList<>();
        int count = 0;
        HashMap<String, Object> result = new HashMap<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(dir));

            // get all the features that we want to use
            String firstLine = reader.readLine();
            String[] features = firstLine.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            HashMap<Integer, String> featureMap = new HashMap<>(features.length);
            for (int i = 0; i < features.length; i++) {
                featureMap.put(i, getMethodName(features[i]));
            }

            /*
             * using reflection to get methods
             * Methods is a Hashtable where key is the method name and value is the method
             * */
            HashMap<String, Method> Methods = new HashMap<>();
            PatientWithBLOBs patientInstance = new PatientWithBLOBs();
            Class patientClass = patientInstance.getClass();
            Method[] existingMethods = patientClass.getMethods();
            String methodName = "";
            for (Method existingMethod : existingMethods) {
                methodName = existingMethod.getName();
                if (methodName.startsWith("set")) {
                    Methods.put(methodName, existingMethod);
                }
            }

            String line = reader.readLine();

            // date format setting
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            // start reading csv
            while ((line != null)) {
                while (patients.size() <= PATIENT_BATCH_SIZE && line != null) {
                    String[] info = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                    String currentId = info[0];
                    PatientWithBLOBs patient = new PatientWithBLOBs();

                    String currentMethodName = "";
                    Method currentMethod = null;

                    for (int i = 0; i < info.length; i++) {
                        if (info[i].isEmpty()) continue;

                        currentMethodName = featureMap.get(i);
                        if (Methods.containsKey(currentMethodName)) {
                            currentMethod = Methods.get(currentMethodName);

                            // handle special cases where value is not directly got from the String
                            Class paramTypes = currentMethod.getParameterTypes()[0];
                            switch (paramTypes.getSimpleName()) {
                                case "Boolean":
                                    currentMethod.invoke(patient, (info[i] == "1"));
                                    break;
                                case "Byte":
                                    currentMethod.invoke(patient, Byte.valueOf(info[i]));
                                    break;
                                case "Integer":
                                    currentMethod.invoke(patient, Integer.valueOf(info[i]));
                                    break;
                                case "LocalDate":
                                    currentMethod.invoke(patient, LocalDate.parse(timeToStanderTime(info[i]), fmt));
                                    break;
                                case "LocalDateTime":
                                    currentMethod.invoke(patient, LocalDateTime.parse(timeToStanderTime(info[i]), fmt));
                                    break;
                                default:
                                    try {
                                        currentMethod.invoke(patient, paramTypes.cast(info[i]));
                                    } catch (IllegalArgumentException argException) {
                                        writeLog(logWriter, "For patient " + currentId + " " + currentMethodName +
                                                " has an illegal input of" + info[i] + ", which is on column " + i);
                                    }
                            }
                        } else {
                            continue;
                        }
                    }


                    // if the arrest time is invalid, set to arrest date
                    if (patient.getArresttime().isEqual(LocalDateTime.parse("1900-01-01 00:00:00", fmt)) ||
                            patient.getArresttime() == null) {
                        patient.setArresttime(LocalDateTime.of(patient.getArrestdate(), LocalTime.of(0, 0, 0)));
                    }

                    writeLog(logWriter, "Patient " + currentId + " imported");
                    patients.add(patient);
                    line = reader.readLine();
                }
                count += insertPatients(patients);
                patients.clear();
            }
            result.put("msg", "success");
            result.put("num", count);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("msg", "fail");
            result.put("num", count);
        }
        return result;
    }

    private int insertPatients(List<PatientWithBLOBs> patients) {
        int count = 0;
        try {
            for (PatientWithBLOBs p : patients) {
                count += patientDao.insertPatinet(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }
}
