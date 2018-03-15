package app.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import app.bean.PatientFilterBean;
import app.bean.Response;
import app.model.Patient;
import app.service.PatientMetadataService;

/**
 * Get all patient data in the DB
 */
@RestController
@RequestMapping("/apis")
public class PatientGetController {

    @Autowired
    PatientMetadataService patientMetadataService = new PatientMetadataService();

    @RequestMapping(value = "/patients/find", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getAllPatientInfo() {
        Map<String, Object> map = new HashMap<>();
        map.put("data", patientMetadataService.GetImportedPatientData());
        Response response = new Response(1, "success");
        map.put("res", response);

        return map;
    }

    @RequestMapping(value = "/patients/{idx}", method = RequestMethod.GET)
    public Patient getOnePatientByIndex(@PathVariable String idx) {
        return patientMetadataService.GetById(idx.toUpperCase()).get(0);
    }

    @RequestMapping(value = "/patients/find", method = RequestMethod.POST)
    public List<Patient> getPatientWithCriteria() {
        PatientFilterBean pfb = new PatientFilterBean();
        pfb.setGenderFilter("F");
        return patientMetadataService.FetchResult(pfb);
    }

}
