/**
 *
 */
package edu.pitt.medschool.controller.load;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import edu.pitt.medschool.model.dto.PatientWithBLOBs;
import edu.pitt.medschool.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import edu.pitt.medschool.controller.load.vo.SearchFileVO;
import edu.pitt.medschool.framework.rest.RestfulResponse;
import edu.pitt.medschool.framework.util.FileBean;
import edu.pitt.medschool.framework.util.Util;
import edu.pitt.medschool.model.dto.CsvFile;
import edu.pitt.medschool.model.dto.ImportProgress;

/**
 * @author Isolachine
 */
@RestController
public class DataController {

    @Autowired
    ImportCsvService importCsvService;
    @Autowired
    ImportProgressService importProgressService;
    @Autowired
    PatientService patientService;
    @Autowired
    ValidateCsvService validateCsvService;
    @Autowired
    RawDataService rawDataService;
    @Autowired
    VersionControlService versionControlService;

    @RequestMapping("data/import")
    @ResponseBody
    public Model importData(Model model) {
        model.addAttribute("nav", "data");
        model.addAttribute("subnav", "import");
        return model;
    }

    @RequestMapping("data/status")
    @ResponseBody
    public Model dataStatus(Model model) {
        model.addAttribute("nav", "data");
        model.addAttribute("subnav", "status");
        return model;
    }

    @RequestMapping("data/patients")
    @ResponseBody
    public ModelAndView patients(ModelAndView model) {
        model.addObject("nav", "data");
        model.addObject("subnav", "patients");
        model.addObject("columns", patientService.getColumnInfo());
        return model;
    }

    @RequestMapping("data/patient/{id}")
    @ResponseBody
    public ModelAndView patientFiles(ModelAndView model, @PathVariable(value = "id", required = true) String patientId) {
        model.setViewName("data/patient");
        model.addObject("nav", "data");
        model.addObject("subnav", "patients");
        model.addObject("patientId", patientId);
        return model;
    }

    @RequestMapping("data/activity")
    @ResponseBody
    public Model dataActivity(Model model) {
        model.addAttribute("nav", "data");
        model.addAttribute("subnav", "activity");
        return model;
    }

    @RequestMapping(value = "data/searchfile")
    @ResponseBody
    public Map<String, Object> searchfile(@RequestBody(required = false) SearchFileVO dir,
            @RequestParam(value = "dir", required = false, defaultValue = "") String dirString, Model model) {
        Map<String, Object> map = new HashMap<>();

        String directory;
        if (dir != null) {
            directory = dir.getDir();
        } else {
            directory = dirString;
        }

        List<FileBean> files = Util.filesInFolder(directory);
        map.put("data", files);

        if (files == null) {
            map.put("res", new RestfulResponse(1, "no such directory"));
        } else {
            if (files.size() > 0) {
                map.put("res", new RestfulResponse(1, "success"));
            } else {
                map.put("res", new RestfulResponse(0, "empty folder"));
            }
        }
        if (!directory.endsWith("/")) {
            directory += "/";
        }
        map.put("dir", directory);
        return map;
    }

    @RequestMapping(value = "api/data/import")
    @ResponseBody
    public Map<String, Object> importDir(@RequestBody(required = false) SearchFileVO dir, String dirString, Model model) {
        Map<String, Object> map = new HashMap<>();
            map.put("msg","success");
            String[] allAR = new String[dir.getFiles().size()];

            for (int i = 0; i < allAR.length; i++) {
                allAR[i] = dir.getFiles().get(i);
            }

            importCsvService.AddArrayFiles(allAR);

        return map;
    }

    // new part for data validation
    // do not use, add to import function
    @RequestMapping(value = "api/data/validate")
    @ResponseBody
    public Map<String, Object> dataValidate(@RequestBody(required = false) SearchFileVO dir, String dirString, Model model)
            throws Exception {
        Map<String, Object> map = new HashMap<>();
            map.put("msg","success");
            for (int i = 0; i < dir.getFiles().size(); i++) {
                CsvFile csvFile = validateCsvService.analyzeCsv(dir.getFiles().get(i));
                validateCsvService.insertCsvFile(csvFile);
            }
            System.out.println(
                    "***********************************************Analyze finished*****************************************");

        return map;
    }

    // import patients
    @RequestMapping(value = "api/data/importPatients")
    @ResponseBody
    public Map<String,Object> ImportPatinets(@RequestBody(required = false) SearchFileVO dir, String dirString, Model model)
        throws Exception{
        Map<String,Object> map = new HashMap<>();
        int count = 0;
            map.put("msg","success");
            for (int i = 0; i<dir.getFiles().size();i++){
                List<PatientWithBLOBs> patients = patientService.getPatientsFromCsv(dir.getFiles().get(i));
                count+=patientService.insertPatients(patients);

            }
            System.out.println("**********************************Import finished**********************************");
            map.put("num",count);

        return map;
    }

    @RequestMapping(value = "/api/data/activity/list")
    @ResponseBody
    public RestfulResponse activityList(RestfulResponse response) {
        response.setData(importProgressService.getActivityList(importCsvService.GetUUID()));
        return response;
    }

    @GetMapping(value = "/apis/patient/files")
    @ResponseBody
    public RestfulResponse patientCsvFiles(@RequestParam(name = "pid", required = false, defaultValue = "") String patientId) {
        RestfulResponse response = new RestfulResponse(1, "success");
        response.setData(rawDataService.selectPatientFilesByPatientId(patientId));
        return response;
    }

    @RequestMapping(value = "/apis/patient/getAllPatientsComments")
    @ResponseBody
    public RestfulResponse getAllPatientsComments() throws Exception{
        RestfulResponse response = new RestfulResponse(1, "success");
        response.setData(patientService.getAllPatientsComments());
        return response;
    }

    @RequestMapping(value = "/apis/patient/changePatientComment")
    @ResponseBody
    public RestfulResponse changePatientComment(@RequestParam(name = "pid") String patientId,@RequestParam(name = "comment", required = false,defaultValue = "") String comment) throws Exception{
        RestfulResponse response = new RestfulResponse(1, "success");
        response.setData(patientService.changePatientComment(patientId,comment));
        return response;
    }

    @RequestMapping(value = "/apis/patient/resolveAllFiles")
    @ResponseBody
    public RestfulResponse resolveAllFile(@RequestParam(name = "pid", required = true) String patientId) throws Exception{
        RestfulResponse response = new RestfulResponse(1, "success");
        response.setData(rawDataService.resolveAllFilesByPid(patientId));
        return response;
    }

    @PostMapping(value = "/apis/patient/resolveFiles")
    @ResponseBody
    public RestfulResponse resolveFile(@RequestBody(required = true) CsvFile file) throws Exception{
        RestfulResponse response = new RestfulResponse(1, "success");
        response.setData(rawDataService.resolveFileByFile(file));
        return response;
    }

    @PostMapping(value = "/apis/patient/changeComment")
    @ResponseBody
    public RestfulResponse changeComment(@RequestBody(required = true) CsvFile file) throws Exception{
        RestfulResponse response = new RestfulResponse(1, "success");
        response.setData(rawDataService.changeComment(file));
        return response;
    }

    @RequestMapping(value = "/apis/patient/getDeletedFiles")
    @ResponseBody
    public RestfulResponse getDeletedFiles(@RequestParam(name = "pid", required = true) String pid) throws Exception{
        RestfulResponse response = new RestfulResponse(1, "success");
        response.setData(rawDataService.getDeletedFilesByPid(pid));
        return response;
    }


    @DeleteMapping(value = "/apis/file")
    @ResponseBody
    public RestfulResponse deletePatientDataByFiles(@RequestBody(required = true) CsvFile file) throws Exception {
        int deleteResult=-1;
        if(file.getStatus()==1){
            deleteResult = versionControlService.setLog(file,1) * rawDataService.deletePatientDataByFile(file);

        }else {
            deleteResult = versionControlService.setLog(file,2) * rawDataService.deletePatientDataByFile(file);
        }
        RestfulResponse response;
        if( deleteResult == 0 ){
            response = new RestfulResponse(1, "success");
        }else{
            response = new RestfulResponse(0, "delete failed");
        }
        return response;
    }

    @PostMapping(value = "/apis/pseudoDeleteFile")
    @ResponseBody
    public RestfulResponse pseudoDeleteFile(@RequestBody(required = true) CsvFile file) throws Exception {
        file.setStatus(1);
        int deleteResult = versionControlService.setLog(file,0) * rawDataService.pseudoDeleteFile(file);
        RestfulResponse response;
        if( deleteResult == 0 ){
            response = new RestfulResponse(1, "success");
        }else{
            response = new RestfulResponse(0, "delete failed");
        }
        return response;
    }

    @RequestMapping(value = "api/data/progress")
    @ResponseBody
    public Map<String, Object> importProgress(@RequestParam(value = "file", required = false, defaultValue = "") String file,
            Model model) {
        Map<String, Object> map = new HashMap<>();

        String uuid = importCsvService.GetUUID();
        String batchId = importCsvService.getBatchId();
        List<ImportProgress> list = importProgressService.GetTaskAllFileProgress(uuid, batchId);
        map.put("progress", list);
        map.put("total", importProgressService.GetTaskOverallProgress(uuid, batchId));
        return map;
    }
}
