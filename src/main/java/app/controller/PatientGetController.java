package app.controller;

import app.model.Patient;
import app.service.PatientFilteringService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Get all patient data in the DB
 */
@RestController
@RequestMapping("/apis")
public class PatientGetController {

    private PatientFilteringService patientFilteringService = new PatientFilteringService();

    @RequestMapping(value = "/patients/find", method = RequestMethod.GET)
    public List<Patient> getAllPatientInfo() {
        return patientFilteringService.SelectAll();
    }

    @RequestMapping(value = "/patients/{idx}", method = RequestMethod.GET)
    public Patient getOnePatientByIndex(@PathVariable String idx) {
        return patientFilteringService.FindById(idx.toUpperCase()).get(0);
    }

    @RequestMapping(value = "/patients/find", method = RequestMethod.POST)
    public List<Patient> getPatientWithCriteria() {
        return patientFilteringService.FindByGender("F");
    }

}
