package app.model;

import java.time.Instant;
import java.util.List;

/**
 * Container for single query result
 */
public class QueryResultBean implements java.io.Serializable {

    private String queryNickname;

    private String problemPid;

    private List<Instant> occurTime;

    private int occurTimes;

    private boolean isAR;

    public QueryResultBean() {
    }

    public String getQueryNickname() {
        return queryNickname;
    }

    public void setQueryNickname(String queryNickname) {
        this.queryNickname = queryNickname;
    }

    public String getProblemPid() {
        return problemPid;
    }

    public void setProblemPid(String problemPid) {
        this.problemPid = problemPid;
    }

    public List<Instant> getOccurTime() {
        return occurTime;
    }

    public void setOccurTime(List<Instant> occurTime) {
        this.occurTime = occurTime;
    }

    public int getOccurTimes() {
        return occurTimes;
    }

    public void setOccurTimes(int occurTimes) {
        this.occurTimes = occurTimes;
    }

    public boolean isAR() {
        return isAR;
    }

    public void setAR(boolean AR) {
        isAR = AR;
    }
}
