package app.model;

/**
 * Non-data restful status
 */
public class RestStatus {

    private int statusCode;
    private String info;

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public int getStatusCode() {

        return statusCode;
    }

    public String getInfo() {
        return info;
    }
}
