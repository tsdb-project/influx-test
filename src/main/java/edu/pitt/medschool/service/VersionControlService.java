package edu.pitt.medschool.service;

import edu.pitt.medschool.model.dao.*;
import edu.pitt.medschool.model.dto.CsvFile;
import edu.pitt.medschool.model.dto.CsvLog;
import edu.pitt.medschool.model.mapper.CsvLogMapper;
import edu.pitt.medschool.model.dto.Version;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class VersionControlService {
    private final CsvFileDao csvFileDao;
    private final CsvLogDao csvLogDao;
    private final VersionDao versionDao;
    private final MedicationDao medicationDao;
    private final PatientDao patientDao;

    public VersionControlService(CsvFileDao csvFileDao, CsvLogDao csvLogDao, VersionDao versionDao, PatientDao patientDao, MedicationDao medicationDao) {
        this.csvFileDao = csvFileDao;
        this.csvLogDao = csvLogDao;
        this.versionDao = versionDao;
        this.patientDao =patientDao;
        this.medicationDao = medicationDao;
    }

    public List<CsvFile> getAllChanges(){
        return csvFileDao.getAllchanges();
    }

    public CsvFile getElementByID(Integer id){
        return csvFileDao.getElementByID(id);
    }

    public int updateStatus(CsvFile csvFile){

        return csvFileDao.updateStatus(csvFile);
    }


    public int setLog(CsvFile csvFile, String status){
        List<CsvLog> csvLogs = csvLogDao.selectByFileNameActivity(csvFile,status);
        if(csvLogs.isEmpty()){
            CsvLog csvLog = new CsvLog();
            ZoneId america = ZoneId.of("America/New_York");
            LocalDateTime americaDateTime = LocalDateTime.now(america);
            csvLog.setActivity(status);
            csvLog.setEndTime(csvFile.getEndTime());
            csvLog.setStatus(csvFile.getStatus());
            csvLog.setFilename(csvFile.getFilename());
            csvLog.setStatTime(csvFile.getStartTime());
            csvLog.setTimestamp(americaDateTime);
            csvLog.setComment(csvFile.getComment());
            return csvLogDao.addLog(csvLog);
        }else{
            // maybe need to think about it
            return 1;
        }
    }

    // add new version into the version table
    private void setNewVersion(int currentVersion,Long deleted,Long imported){
        Version version = new Version();
        version.setVersionId(currentVersion);
        version.setCreateDate(LocalDateTime.now(ZoneId.of("America/New_York")));
        version.setCsvDelete(deleted);
        version.setCsvFileNum(csvFileDao.getCsvNumber(currentVersion));
        version.setCsvIncrease(imported);
        version.setMedicationIncrease(medicationDao.getMedicationIncrease(currentVersion));
        version.setMedicationNum(medicationDao.getMedicationNumber(currentVersion));
        version.setPatientIncrease(patientDao.getPatientIncrease(currentVersion));
        version.setPatientNum(patientDao.getPatientNumber(currentVersion));
        version.setPuhPatients(patientDao.getPuhPatientNumber(currentVersion));
        version.setUabPatients(patientDao.getUabPatientNumber(currentVersion));
        version.setTbiPatients(patientDao.getTbiPatientNumber(currentVersion));
        version.setPatientsWithCsv(csvFileDao.getPatientNumber(currentVersion));
        version.setTotalLength(csvFileDao.getTotalLength(currentVersion));
        version.setDbSize(csvFileDao.getDbSize());
        versionDao.setNewVersion(version);
    }

    // give new data the current version number
    private void publishNewData(int currentVersion){
        csvFileDao.publishNewData(currentVersion);
        patientDao.publishNewData(currentVersion);
        medicationDao.publishNewData(currentVersion);
    }

    // publish new version
    public int publishNewVersion(int wrongPatients, int unhandled){
        int currentVersion = versionDao.getLatestVersion()+1;
        Long deleted = csvFileDao.getCsvDelete(currentVersion);
        Long imports = csvFileDao.getCsvIncrease(0);
        if(wrongPatients !=0){
            System.out.println("There are still errors in database, cannot publish");
            return -1;
        }else if (unhandled !=0){
            System.out.println("There are still unhandled changes in database, cannot publish");
            return -2;
        }else if(deleted==0 && imports==0){
            System.out.println("There is no change from last version");
            return -3;
        }else {
            // set current version to new data

            System.out.println("Current publish version: "+currentVersion);
            publishNewData(currentVersion);
            // insert this version to version table
            setNewVersion(currentVersion,deleted,imports);
            return 1;
        }
    }

    public List<Version> getAllVersions() {
        return versionDao.getAllVersions();
    }

    public Version getLastVersion(){
        int id = versionDao.getLatestVersion();
        return  versionDao.selectById(id);
    }

    //get one version
    public Version selectById(int id){
        return versionDao.selectById(id);
    }

    public int setComment(Version version) {
        return versionDao.setComment(version);
    }
}
