package edu.pitt.medschool.controller.patient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.pitt.medschool.model.dto.PatientWithBLOBs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import edu.pitt.medschool.framework.rest.RestfulResponse;
import edu.pitt.medschool.framework.util.MysqlColumnBean;
import edu.pitt.medschool.model.dao.ImportedFileDao;
import edu.pitt.medschool.model.dao.PatientDao;
import edu.pitt.medschool.model.dto.Patient;
import edu.pitt.medschool.service.PatientService;

/**
 * Get all patient data in the DB
 */
@RestController
@RequestMapping("/apis")
public class PatientController {

    @Value("${machine}")
    private String uuid;

    @Autowired
    PatientService patientService;
    @Autowired
    PatientDao patientDao;

    @Autowired
    ImportedFileDao importedFileDao;

    @RequestMapping(value = "/patients/find", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getAllPatientInfo() {
        Map<String, Object> map = new HashMap<>();
        // TODO: change front-end! new contents in Patient Obj.
        List<String> pids = importedFileDao.getAllImportedPid(uuid);
        if (pids.isEmpty()) {
            map.put("data", new ArrayList<>());
        } else {
            map.put("data", patientDao.selectByIds(pids));
        }
        RestfulResponse response = new RestfulResponse(1, "success");
        map.put("res", response);

        return map;
    }

    @RequestMapping(value = "/patients/{pid}", method = RequestMethod.GET)
    public PatientWithBLOBs getOnePatientByPid(@PathVariable String pid) {
        return patientService.getPatientByPid(pid);
    }

    @RequestMapping(value = "/patients/find", method = RequestMethod.POST)
    public RestfulResponse getPatientWithCriteria() {
        List<String> pids = importedFileDao.getAllImportedPid(uuid);
        RestfulResponse response = new RestfulResponse(1, "success");
        response.setData(patientDao.selectByIds(pids));
        return response;
    }

    @RequestMapping(value = "/patients/columns", method = RequestMethod.GET)
    public List<MysqlColumnBean> getColumnInfo() {
        return patientService.getColumnInfo();
    }

    @PostMapping(value = "/patient/updatePatientInfo")
    @ResponseBody
    public RestfulResponse updatePatientInfo(@RequestBody(required = true) PatientWithBLOBs patient) throws Exception{
        RestfulResponse response = new RestfulResponse(1, "success");
        response.setData(patientService.updatePatientInfo(patient));
        return response;
    }
}
