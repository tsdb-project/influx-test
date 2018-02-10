package app.controller;

import app.model.Patient;
import app.service.PatientService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Get all patient data in the DB
 */
@RestController
@RequestMapping("/apis")
public class PatientGetController {

    private PatientService patientService = new PatientService();

    @RequestMapping(value = "/patients/find", method = RequestMethod.GET)
    public List<Patient> getAllPatientInfo() {
        return patientService.SelectAll();
    }

    @RequestMapping(value = "/patients/{idx}", method = RequestMethod.GET)
    public Patient getOnePatientByIndex(@PathVariable String idx) {
        return patientService.FindById(idx.toUpperCase()).get(0);
    }

    @RequestMapping(value = "/patients/find", method = RequestMethod.POST)
    public List<Patient> getPatientWithCriteria() {
        return patientService.FindByGender("F");
    }

}
