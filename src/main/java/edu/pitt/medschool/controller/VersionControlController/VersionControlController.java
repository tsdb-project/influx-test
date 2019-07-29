package edu.pitt.medschool.controller.VersionControlController;

import edu.pitt.medschool.framework.rest.RestfulResponse;
import edu.pitt.medschool.model.dto.CsvFile;
import edu.pitt.medschool.model.dto.Version;
import edu.pitt.medschool.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class VersionControlController {
    @Autowired
    VersionControlService versionControlService;
    @Autowired
    ValidateCsvService validateCsvService;
    @Autowired
    RawDataService rawDataService;
    @Autowired
    ImportProgressService importProgressService;
    @RequestMapping("versionControl")
    public ModelAndView toVersionControl(ModelAndView modelAndView){
        modelAndView.addObject("nav", "versionControl");
        modelAndView.addObject("subnav", "");
        modelAndView.setViewName("versionControl/versionControl");
        return modelAndView;
    }

    @RequestMapping("confirmChanges")
    public ModelAndView toConfirmChanges(ModelAndView modelAndView){
        modelAndView.addObject("nav", "management");
        modelAndView.addObject("subnav", "confirmChanges");
        modelAndView.setViewName("versionControl/confirmChanges");
        return modelAndView;
    }


    @RequestMapping("versionControl/getdata")
    @ResponseBody
    public RestfulResponse getAllChanges(){
        RestfulResponse response = new RestfulResponse(1,"success");
        response.setData(versionControlService.getAllChanges());
        return response;
    }

    @RequestMapping("versionControl/getAllVersion")
    @ResponseBody
    public RestfulResponse getAllVersions(){
        RestfulResponse response = new RestfulResponse(1,"success");
        response.setData(versionControlService.getAllVersions());
        return response;
    }

    @RequestMapping("versionControl/getLastVersion")
    @ResponseBody
    public RestfulResponse getLastVersion(){
        RestfulResponse response = new RestfulResponse(1,"success");
        response.setData(versionControlService.getLastVersion());
        return response;
    }

    @RequestMapping(value = "versionControl/cancelDelete", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> cancelChange(@RequestBody(required = true) List<CsvFile> csvFileList) throws Exception {
        Map<String, Object> map = new HashMap<>();
        if(importProgressService.isImporting()){
            map.put("res",new RestfulResponse(2,"Importing"));
            return map;
        }
        int cancelResult=1;
        for(CsvFile csvFile:csvFileList){
            cancelResult *= versionControlService.setLog(csvFile,"Cancel_Start");
            csvFile.setStatus(0);
            cancelResult*=versionControlService.updateStatus(csvFile);
            cancelResult*=versionControlService.setLog(csvFile,"Cancel_Finished");
        }
        if(cancelResult!=1){
            map.put("res", new RestfulResponse(0, "cancel failed"));
        }else{
            map.put("res", new RestfulResponse(1, "success"));
        }
        return map;
    }

    @RequestMapping(value = "versionControl/confirmImport",method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> confirmChange(@RequestBody(required = true) List<CsvFile> csvFiles) throws Exception {
        Map<String, Object> map = new HashMap<>();
        if(importProgressService.isImporting()){
            map.put("res",new RestfulResponse(2,"Importing"));
            return map;
        }
        int confirmResult = 1;
        for(CsvFile csvFile: csvFiles){
            confirmResult*=versionControlService.setLog(csvFile,"Commit_Start");
            csvFile.setStatus(0);
            confirmResult*=versionControlService.updateStatus(csvFile);
            confirmResult*=versionControlService.setLog(csvFile,"Commit_Finished");
        }
        if(confirmResult!=1){
            map.put("res", new RestfulResponse(0, "confirm failed"));
        }else {
            map.put("res", new RestfulResponse(1, "success"));
        }
        return map;
    }

    @RequestMapping(value = "versionControl/publish",method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> publishVersion() throws  Exception{
        Map<String,Object> map = new HashMap<>();
        int wrongPatients = validateCsvService.getWrongPatients(validateCsvService.getPatientTimeLines("realpsc")).size();
        int unhandled = versionControlService.getAllChanges().size();
        int result = versionControlService.publishNewVersion(wrongPatients,unhandled);
        if(result==-1){
            map.put("res",new RestfulResponse(0,"There are still errors in database, cannot publish"));
        }else if(result==-2){
            map.put("res",new RestfulResponse(0,"There are still unhandled changes in database, cannot publish"));
        }else if(result==-3){
            map.put("res",new RestfulResponse(0,"There is no change from last version"));
        }else {
            map.put("res",new RestfulResponse(1,"success"));
        }
        return map;
    }

    @GetMapping("/versionControl/getOneVersion/{id}")
    @ResponseBody
    public RestfulResponse getOneVersion(@PathVariable Integer id) {
        Version version = versionControlService.selectById(id);
        return new RestfulResponse(1, "success",version);
    }

    @PutMapping("/versionControl/setComment")
    @ResponseBody
    public RestfulResponse createOrUpdateUser(@RequestBody(required = true) Version version) {
        versionControlService.setComment(version);
        return new RestfulResponse(1, "success");
    }
}
