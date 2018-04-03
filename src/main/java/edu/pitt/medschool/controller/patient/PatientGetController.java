package edu.pitt.medschool.controller.patient;

import edu.pitt.medschool.framework.rest.RestfulResponse;
import edu.pitt.medschool.model.dao.ImportedFileDao;
import edu.pitt.medschool.model.dao.PatientDao;
import edu.pitt.medschool.model.dto.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${machine}")
    private String uuid;

    @Autowired
    PatientDao patientDao;

    @Autowired
    ImportedFileDao importedFileDao;

    @RequestMapping(value = "/patients/find", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getAllPatientInfo() {
        Map<String, Object> map = new HashMap<>();
        //TODO: change front-end! new contents in Patient Obj.
        map.put("data", patientDao.selectByIds(importedFileDao.getAllImportedPid(uuid)));
        RestfulResponse response = new RestfulResponse(1, "success");
        map.put("res", response);

        return map;
    }

    @RequestMapping(value = "/patients/{idx}", method = RequestMethod.GET)
    public Patient getOnePatientByIndex(@PathVariable String idx) {
        List<Patient> p = patientDao.selectById(idx);
        return p.get(0);
    }

    @RequestMapping(value = "/patients/find", method = RequestMethod.POST)
    public List<Patient> getPatientWithCriteria() {
        return patientDao.selectByGender("F");
    }

}
