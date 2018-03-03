package app.controller;

import app.bean.Response;
import app.model.Patient;
import app.service.PatientFilteringService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Get all patient data in the DB
 */
@RestController
@RequestMapping("/apis")
public class PatientGetController {

    private PatientFilteringService patientFilteringService = new PatientFilteringService();

    @RequestMapping(value = "/patients/find", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getAllPatientInfo() {
        Map<String, Object> map = new HashMap<>();
        map.put("data", patientFilteringService.SelectAll());
        Response response = new Response(1, "success");
        map.put("res", response);
        
        return map;
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
