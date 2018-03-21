package edu.pitt.medschool.model.TSData;

/**
 * Header reverse mapping (Info -> col name)
 */
public class HeaderReverseMapping {
    private String type, sid;
    private int sid_count;

    /**
     * Info for reverse mapping, used for data query
     * @param type      Reverse mapping type
     * @param sid       SID (SID prefix 'I10')
     * @param sid_count How many 'I10's do we have
     */
    public HeaderReverseMapping(String type, String sid, int sid_count) {
        this.type = type;
        this.sid = sid;
        this.sid_count = sid_count;
    }

    /**
     * Get columns that we need
     * @return Array (like ['I10_1', 'I10_2', 'I10_3'])
     */
    public String[] getColumnHeaders() {
        String[] res = new String[sid_count];
        for (int i = 1; i <= sid_count; i++) {
            res[i - 1] = sid + "_" + i;
        }
        return res;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }
}
