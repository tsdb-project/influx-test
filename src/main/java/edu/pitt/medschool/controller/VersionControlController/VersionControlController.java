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

import java.util.HashMap;
import java.util.Map;

@Controller
public class VersionControlController {
    @Autowired
    VersionControlService versionControlService;
    @RequestMapping("versionControl")
    public String toVersionControl(){
        return "versionControl/versionControl";
    }

    @RequestMapping("versionControl/getdata")
    @ResponseBody
    public RestfulResponse getAllChanges(){
        RestfulResponse response = new RestfulResponse(1,"success");
        response.setData(versionControlService.getAllChanges());
        return response;
    }

    @RequestMapping(value = "versionControl/cancel", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> cancelChange(@RequestBody(required = true) Integer id) throws Exception {
        Map<String, Object> map = new HashMap<>();
        CsvFile csvFile = versionControlService.getElementByID(id);
        if(versionControlService.setLog(csvFile,2)!=1){
            map.put("res", new RestfulResponse(0, "cancel failed"));
        };
        if(csvFile.getStatus()==1){
            csvFile.setStatus(0);
            if (versionControlService.updateStatus(csvFile) == 1) {
                map.put("res", new RestfulResponse(1, "success"));
            } else {
                map.put("res", new RestfulResponse(0, "cancel failed"));
            }
        }else{
            if (versionControlService.deleteRecord(id) == 1) {
                map.put("res", new RestfulResponse(1, "success"));
            } else {
                map.put("res", new RestfulResponse(0, "cancel failed"));
            }
        }


        return map;
    }

    @RequestMapping(value = "versionControl/confirm",method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> confirmChange(@RequestBody(required = true) Integer id) throws Exception {
        Map<String, Object> map = new HashMap<>();
        CsvFile csvFile = versionControlService.getElementByID(id);
        if(versionControlService.setLog(csvFile,1)!=1){
            map.put("res", new RestfulResponse(0, "confirm failed"));
        };
        if(csvFile.getStatus()==1){
            if (versionControlService.deleteRecord(id) == 1) {
                map.put("res", new RestfulResponse(1, "success"));
            } else {
                map.put("res", new RestfulResponse(0, "confirm failed"));
            }
        }else{
            csvFile.setStatus(0);
            if (versionControlService.updateStatus(csvFile) == 1) {
                map.put("res", new RestfulResponse(1, "success"));
            } else {
                map.put("res", new RestfulResponse(0, "confirm failed"));
            }
        }

        return map;
    }
}
