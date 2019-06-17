package edu.pitt.medschool.controller.VersionControlController;

import edu.pitt.medschool.framework.rest.RestfulResponse;
import edu.pitt.medschool.model.dto.CsvFile;
import edu.pitt.medschool.service.VersionControlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class VersionControlController {
    @Autowired
    VersionControlService versionControlService;
    @RequestMapping("versionControl")
    public ModelAndView toVersionControl(ModelAndView modelAndView){
        modelAndView.addObject("nav", "versionControl");
        modelAndView.addObject("subnav", "");
        modelAndView.setViewName("versionControl/versionControl");
        return modelAndView;
    }

    @RequestMapping("versionControl/getdata")
    @ResponseBody
    public RestfulResponse getAllChanges(){
        RestfulResponse response = new RestfulResponse(1,"success");
        response.setData(versionControlService.getAllChanges());
        return response;
    }

    @RequestMapping(value = "versionControl/cancelDelete", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> cancelChange(@RequestBody(required = true) List<CsvFile> csvFileList) throws Exception {
        Map<String, Object> map = new HashMap<>();
        int cancelResult=1;
        for(CsvFile csvFile:csvFileList){
            cancelResult *= versionControlService.setLog(csvFile,2);
            csvFile.setStatus(0);
            cancelResult*=versionControlService.updateStatus(csvFile);
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
        int confirmResult = 1;
        for(CsvFile csvFile: csvFiles){
            confirmResult*=versionControlService.setLog(csvFile,1);
            csvFile.setStatus(0);
            confirmResult*=versionControlService.updateStatus(csvFile);
        }
        if(confirmResult!=1){
            map.put("res", new RestfulResponse(0, "confirm failed"));
        }else {
            map.put("res", new RestfulResponse(1, "success"));
        }
        return map;
    }
}
