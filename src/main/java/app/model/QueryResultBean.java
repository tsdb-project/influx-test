package app.model;

import java.util.List;

/**
 * Container for single query result
 */
public class QueryResultBean implements java.io.Serializable {

    private static final long serialVersionUID = -6110153689820276538L;

    private String queryNickname;

    private Patient interestPatient;

    private List<TimeSpan> occurTime;

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

    public List<TimeSpan> getOccurTime() {
        return occurTime;
    }

    public void setOccurTime(List<TimeSpan> occurTime) {
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

    public Patient getInterestPatient() {
        return interestPatient;
    }

    public void setInterestPatient(Patient interestPatient) {
        this.interestPatient = interestPatient;
    }
}
