/**
 *
 */
package edu.pitt.medschool.controller.load;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    @Autowired
    TemplateService templateService;
    @Autowired
    TimeShiftService timeShiftService;
//    @Autowired
//    ConvertToCsvServies convertToCsvServies;

    @RequestMapping("data/import")
    @ResponseBody
    public Model importData(Model model) {
        model.addAttribute("nav", "data");
        model.addAttribute("subnav", "import");
        return model;
    }

    @RequestMapping("data/updateMetadata")
    @ResponseBody
    public Model updateMetadataPage(Model model) {
        model.addAttribute("nav", "data");
        model.addAttribute("subnav", "updateMetadata");
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

        List<FileBean> files = Util.filesInFolder(directory,"csv");
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

    // analysis existing patients to add header time and column count
    @RequestMapping(value = "api/data/validate")
    @ResponseBody
    public Map<String,Object> analysis(@RequestBody(required = false) SearchFileVO dir, String dirString, Model model){
        Map<String, Object> map = new HashMap<>();
        map.put("msg","success");
        String[] allfiles = new String[dir.getFiles().size()];
        for(int i=0;i<allfiles.length;i++){
            allfiles[i] = dir.getFiles().get(i);
        }
        templateService.AddArrayFiles(allfiles);
        return map;
    }

//    @RequestMapping(value = "api/data/ImportErd")
//    @ResponseBody
//    public Map<String, Object> covertToCsv(@RequestBody(required = false) SearchFileVO dir, String dirString, Model model) {
//        Map<String, Object> map = new HashMap<>();
//        map.put("msg","success");
//        String[] allAR = new String[dir.getFiles().size()];
//        for (int i = 0; i < allAR.length; i++) {
//            allAR[i] = dir.getFiles().get(i);
//        }
//        convertToCsvServies.covertAndSend(allAR);
//        return map;
//    }

    @RequestMapping(value = "api/data/timeDrift")
    @ResponseBody
    public RestfulResponse timeDrift() {
        RestfulResponse response = new RestfulResponse(1,"success");
        timeShiftService.fixTimeDrift();
        return response;
    }


    // import patients
    @RequestMapping(value = "api/data/importPatients")
    @ResponseBody
    public Map<String,Object> ImportPatinets(@RequestParam(name = "dir") String dirString, Model model)
        throws Exception{
        Map map = new HashMap<>();
        map = patientService.getPatientsFromCsv(dirString);
        System.out.println("**********************************Import finished**********************************");
        return map;
    }

    @RequestMapping(value = "/api/data/activity/list")
    @ResponseBody
    public RestfulResponse activityList() {
        RestfulResponse response = new RestfulResponse(1,"success");
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

//    @RequestMapping(value = "/apis/patient/getDeletedFiles")
//    @ResponseBody
//    public RestfulResponse getDeletedFiles(@RequestParam(name = "pid", required = true) String pid) throws Exception{
//        RestfulResponse response = new RestfulResponse(1, "success");
//        response.setData(rawDataService.getDeletedFilesByPid(pid));
//        return response;
//    }


    @DeleteMapping(value = "/apis/file")
    @ResponseBody
    public Map<String, Object> deletePatientDataByFiles(@RequestBody(required = true) List<CsvFile> csvFiles) throws Exception {
        Map<String, Object> map = new HashMap<>();
        if(importProgressService.isImporting()){
            map.put("res",new RestfulResponse(2,"Importing"));
            return map;
        }
        int deleteResult =1;
        for(CsvFile csvFile : csvFiles){
            if(csvFile.getStatus()==1){
                    deleteResult=versionControlService.setLog(csvFile,"Commit_Start")*rawDataService.deletePatientDataByFile(csvFile)*versionControlService.setLog(csvFile,"Commit_Finished");
            }else {
                    deleteResult=versionControlService.setLog(csvFile,"Cancel_Start")*rawDataService.deletePatientDataByFile(csvFile)*versionControlService.setLog(csvFile,"Cancel_Finished");
            }
        }
        RestfulResponse response;
        if( deleteResult !=0 ){
            map.put("res",new RestfulResponse(1, "success"));
        }else{
            map.put("res",new RestfulResponse(0,  "fail"));
        }
        return map;
    }

    @PostMapping(value = "/apis/pseudoDeleteFile")
    @ResponseBody
    public RestfulResponse pseudoDeleteFile(@RequestBody(required = true) CsvFile file) throws Exception {
        file.setStatus(1);
        int deleteResult=1;
            deleteResult=versionControlService.setLog(file,"Pending") * rawDataService.pseudoDeleteFile(file);
        RestfulResponse response;
        if( deleteResult == 1 ){
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
