package edu.pitt.medschool.bean;

import java.util.LinkedList;
import java.util.List;

/**
 * Bean for patient filter
 */
//TODO: Probably using Map<>?
public class PatientFilterBean {

    private List<String> filters = new LinkedList<>();

    /**
     * set 'Survived' filter for patient
     *
     * @param status 1 for Yes, 0 for No, 2 for unknown, other for don't care
     */
    public void setSurvivedFilter(int status) {
        String flag;
        switch (status) {
            case 0:
                flag = "N";
                break;
            case 1:
                flag = "Y";
                break;
            case 2:
                flag = "N/A";
                break;
            default:
                return;
        }
        filters.add(String.format("\"Survived\"='%s'", flag));
    }

    /**
     * set 'ArrestLocation' filter for patient
     *
     * @param status 0 for Inside, 1 for outsite, 2 for unknown, other for don't care
     */
    public void setArrestLocationFilter(int status) {
        String flag;
        switch (status) {
            case 0:
                flag = "Inside";
                break;
            case 1:
                flag = "Outside";
                break;
            case 2:
                flag = "N/A";
                break;
            default:
                return;
        }
        filters.add(String.format("\"ArrestLocation\"='%s'", flag));
    }

    /**
     * set 'Gender' filter for patient
     *
     * @param gnd M or F
     */
    public void setGenderFilter(String gnd) {
        String tgt = String.format("\"Gender\"='%s'", gnd.toUpperCase());
        filters.add(tgt);
    }

    /**
     * set 'Age' filter for patient
     *
     * @param lower Inclusive lower
     */
    public void setAgeLowerFilter(int lower) {
        String tgt = String.format("\"age\">=%d", lower);
        filters.add(tgt);
    }

    /**
     * set 'Age' filter for patient
     *
     * @param upper Inclusive upper
     */
    public void setAgeUpperFilter(int upper) {
        String tgt = String.format("\"age\"<=%d", upper);
        filters.add(tgt);
    }

    /**
     * Customn filter (e.g. duration, prid_scene...)
     *
     * @param key          Filter key name
     * @param value        Filter value
     * @param operator     e.g. (>= > <= < =)
     * @param numericValue Value is number or String
     */
    public void setCustomFilter(String key, String value, String operator, boolean numericValue) {
        String formatStr;
        if (numericValue)
            formatStr = "\"%s\"%s%s";
        else
            formatStr = "\"%s\"%s'%s'";
        String tgt = String.format(formatStr, key, operator, value);
        filters.add(tgt);
    }

    /**
     * Get accumulated filters
     */
    public String getWhereAndFilters() {
        if (filters.size() == 0) return "";
        return " WHERE " + String.join(" AND ", filters);
    }

    /**
     * How many filters do we have
     */
    public int getNumOfFilters() {
        return filters.size();
    }

}
