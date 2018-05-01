package edu.pitt.medschool.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import edu.pitt.medschool.controller.load.vo.ActivityVO;
import edu.pitt.medschool.framework.util.Util;
import edu.pitt.medschool.model.dao.ImportProgressDao;
import edu.pitt.medschool.model.dto.ImportLog;
import edu.pitt.medschool.model.dto.ImportProgress;

/**
 * Import progress service
 */
@Service
public class ImportProgressService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public enum FileProgressStatus {
        STATUS_FINISHED, STATUS_FAIL, STATUS_INPROGRESS, STATUS_QUEUED
    }

    @Autowired
    private ImportProgressDao ipo;

    @Value("${machine}")
    private String uuid;

    public String getUUID() {
        return this.uuid;
    }

    /**
     * Get overall progress for a task
     *
     * @param uuid
     *            Task UUID
     * @param batchId 
     */
    public double GetTaskOverallProgress(String uuid, String batchId) {
        return ipo.OverallProgress(uuid, batchId);
    }

    /**
     * Get progress data for all files within a task UUID
     *
     * @param uuid
     *            Task UUID
     * @param batchId 
     */
    public List<ImportProgress> GetTaskAllFileProgress(String uuid, String batchId) {
        return ipo.GetTaskDetailProgress(uuid, batchId);
    }

    private void doInsert(ImportLog importLog) throws Exception {
        importLog.setUuid(this.uuid);
        ipo.insert(importLog);
    }

    /**
     * Insert file progress to db
     *
     * @param fileName
     *            Current file name
     * @param fileName 
     * @param totalFileSize
     *            All files' size
     * @param fileSize
     *            Current file's size
     * @param processedSize
     *            Current processed size
     * @param totalProcessedSize
     *            All processed size
     * @param status
     *            Current file status
     * @param failReason
     *            Reason why failed
     */
    public void UpdateFileProgress(String batchId, String fileName, long totalFileSize, long fileSize, long processedSize, long totalProcessedSize, FileProgressStatus status, String failReason) {
        double allPercent = 0, currPercent = 0;
        if (totalFileSize != 0)
            allPercent = 1.0 * totalProcessedSize / totalFileSize;
        if (fileSize != 0)
            currPercent = 1.0 * processedSize / fileSize;

        ImportLog importLog = new ImportLog();

        importLog.setStatus(String.valueOf(status));
        importLog.setFilename(fileName);
        importLog.setAllPercent(allPercent);
        importLog.setThisPercent(currPercent);
        importLog.setThisFileSize(fileSize);
        importLog.setAllFileSize(totalFileSize);
        importLog.setBatchId(batchId);

        if (status == FileProgressStatus.STATUS_FAIL) {
            importLog.setReason(failReason);
        } else {
            importLog.setReason("N/A");
        }

        try {
            doInsert(importLog);
        } catch (Exception e) {
            logger.error(Util.stackTraceErrorToString(e));
        }
    }

    /**
     * @param uuid 
     * @return
     */
    public List<ActivityVO> getActivityList(String uuid) {
        return ipo.getActivityList(uuid);
    }

}
