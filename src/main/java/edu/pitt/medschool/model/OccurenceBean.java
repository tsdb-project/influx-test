/**
 * 
 */
package edu.pitt.medschool.model;

import edu.pitt.medschool.model.dto.Patient;

/**
 * @author Isolachine
 *
 */
public class OccurenceBean {
    private Patient patient;
    private String occurence;

    /**
     * @return the occurence
     */
    public String getOccurence() {
        return occurence;
    }

    /**
     * @param occurence the occurence to set
     */
    public void setOccurence(String occurence) {
        this.occurence = occurence;
    }

    /**
     * @return the patient
     */
    public Patient getPatient() {
        return patient;
    }

    /**
     * @param patient the patient to set
     */
    public void setPatient(Patient patient) {
        this.patient = patient;
    }
}
