package edu.pitt.medschool.controller.patient;

import edu.pitt.medschool.bean.PatientFilterBean;
import edu.pitt.medschool.framework.rest.RestfulResponse;
import edu.pitt.medschool.model.Patient;
import edu.pitt.medschool.service.PatientFilteringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Get all patient data in the DB
 */
@RestController
@RequestMapping("/apis")
public class PatientGetController {
    @Autowired
    PatientFilteringService patientFilteringService;

    @RequestMapping(value = "/patients/find", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getAllPatientInfo() {
        Map<String, Object> map = new HashMap<>();
        map.put("data", patientFilteringService.GetImportedPatientData());
        RestfulResponse response = new RestfulResponse(1, "success");
        map.put("res", response);

        return map;
    }

    @RequestMapping(value = "/patients/{idx}", method = RequestMethod.GET)
    public Patient getOnePatientByIndex(@PathVariable String idx) {
        return patientFilteringService.GetById(idx.toUpperCase()).get(0);
    }

    @RequestMapping(value = "/patients/find", method = RequestMethod.POST)
    public List<Patient> getPatientWithCriteria() {
        PatientFilterBean pfb = new PatientFilterBean();
        pfb.setGenderFilter("F");
        return patientFilteringService.FetchResult(pfb);
    }

}
