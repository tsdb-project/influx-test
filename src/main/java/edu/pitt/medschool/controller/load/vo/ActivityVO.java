/**
 * 
 */
package edu.pitt.medschool.controller.load.vo;

/**
 * @author Isolachine
 *
 */
public class ActivityVO {
    private String batchId;
    private String endTime;
    private String startTime;
    private String elapsedTime;
    private String size;
    private int fileCount;
    private int finishedCount;
    private int failCount;
    private int inProgressCount;
    private int queuedCount;
    private boolean finished;
    /**
     * @return the batchId
     */
    public String getBatchId() {
        return batchId;
    }
    /**
     * @param batchId the batchId to set
     */
    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }
    /**
     * @return the endTime
     */
    public String getEndTime() {
        return endTime;
    }
    /**
     * @param endTime the endTime to set
     */
    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
    /**
     * @return the startTime
     */
    public String getStartTime() {
        return startTime;
    }
    /**
     * @param startTime the startTime to set
     */
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
    /**
     * @return the elapsedTime
     */
    public String getElapsedTime() {
        return elapsedTime;
    }
    /**
     * @param elapsedTime the elapsedTime to set
     */
    public void setElapsedTime(String elapsedTime) {
        this.elapsedTime = elapsedTime;
    }
    /**
     * @return the size
     */
    public String getSize() {
        return size;
    }
    /**
     * @param size the size to set
     */
    public void setSize(String size) {
        this.size = size;
    }
    /**
     * @return the fileCount
     */
    public int getFileCount() {
        return fileCount;
    }
    /**
     * @param fileCount the fileCount to set
     */
    public void setFileCount(int fileCount) {
        this.fileCount = fileCount;
    }
    /**
     * @return the finishedCount
     */
    public int getFinishedCount() {
        return finishedCount;
    }
    /**
     * @param finishedCount the finishedCount to set
     */
    public void setFinishedCount(int finishedCount) {
        this.finishedCount = finishedCount;
    }
    /**
     * @return the failCount
     */
    public int getFailCount() {
        return failCount;
    }
    /**
     * @param failCount the failCount to set
     */
    public void setFailCount(int failCount) {
        this.failCount = failCount;
    }
    /**
     * @return the inProgressCount
     */
    public int getInProgressCount() {
        return inProgressCount;
    }
    /**
     * @param inProgressCount the inProgressCount to set
     */
    public void setInProgressCount(int inProgressCount) {
        this.inProgressCount = inProgressCount;
    }
    /**
     * @return the queuedCount
     */
    public int getQueuedCount() {
        return queuedCount;
    }
    /**
     * @param queuedCount the queuedCount to set
     */
    public void setQueuedCount(int queuedCount) {
        this.queuedCount = queuedCount;
    }
    /**
     * @return the finished
     */
    public boolean isFinished() {
        return finished;
    }
    /**
     * @param finished the finished to set
     */
    public void setFinished(boolean finished) {
        this.finished = finished;
    }
}
