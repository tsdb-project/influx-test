package edu.pitt.medschool.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import edu.pitt.medschool.controller.load.vo.ProgressVO;
import edu.pitt.medschool.model.dao.ImportProgressDao;
import edu.pitt.medschool.model.dto.ImportProgress;

/**
 * Import progress service
 */
@Service
public class ImportProgressService {

    public enum FileProgressStatus {
        STATUS_FINISHED, STATUS_FAIL, STATUS_INPROGRESS
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
     */
    public double GetTaskOverallProgress(String uuid) {
        return ipo.OverallProgress(uuid);
    }

    /**
     * Get progress data for all files within a task UUID
     *
     * @param uuid
     *            Task UUID
     */
    public List<ProgressVO> GetTaskAllFileProgress(String uuid) {
        return ipo.GetTaskDetailProgress(uuid);
    }

    public void _test(boolean loadTestingData) {
        if (loadTestingData) {
            this.uuid = "TESTUUID";
            UpdateFileProgress("1.csv", 100, 10, 5, 5, FileProgressStatus.STATUS_INPROGRESS, null);
            UpdateFileProgress("2.csv", 100, 40, 40, 45, FileProgressStatus.STATUS_FINISHED, null);
            UpdateFileProgress("3.csv", 100, 20, 3, 48, FileProgressStatus.STATUS_INPROGRESS, null);
            UpdateFileProgress("3.csv", 100, 20, 10, 55, FileProgressStatus.STATUS_FAIL, "Wrong file format");
            UpdateFileProgress("1.csv", 100, 10, 6, 56, FileProgressStatus.STATUS_INPROGRESS, null);
            UpdateFileProgress("1.csv", 100, 10, 10, 60, FileProgressStatus.STATUS_FINISHED, null);
            return;
        }
        double s = GetTaskOverallProgress("TESTUUID");
        List<ProgressVO> ss = GetTaskAllFileProgress("TESTUUID");
    }

    private void doInsert(ImportProgress ip) throws Exception {
        ip.setUuid(this.uuid);
        ipo.insert(ip);
    }

    /**
     * Insert file progress to db
     *
     * @param fileName
     *            Current file name
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
    public void UpdateFileProgress(String fileName, long totalFileSize, long fileSize, long processedSize, long totalProcessedSize, FileProgressStatus status, String failReason) {
        double allPercent = 0, currPercent = 0;
        if (totalFileSize != 0)
            allPercent = 1.0 * totalProcessedSize / totalFileSize;
        if (fileSize != 0)
            currPercent = 1.0 * processedSize / fileSize;

        ImportProgress ip = new ImportProgress();

        ip.setStatus(String.valueOf(status));
        ip.setFilename(fileName);
        ip.setAllPercent(allPercent);
        ip.setThisPercent(currPercent);
        ip.setThisFileSize(fileSize);
        ip.setAllFileSize(totalFileSize);

        if (status == FileProgressStatus.STATUS_FAIL) {
            ip.setReason(failReason);
        } else {
            ip.setReason("N/A");
        }

        try {
            doInsert(ip);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
