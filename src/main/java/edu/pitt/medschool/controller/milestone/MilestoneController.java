package edu.pitt.medschool.controller.milestone;

import edu.pitt.medschool.config.DBConfiguration;
import edu.pitt.medschool.framework.rest.RestfulResponse;
import edu.pitt.medschool.model.dto.Milestone;
import edu.pitt.medschool.service.MilestoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import retrofit2.http.GET;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class MilestoneController {
    @Autowired
    MilestoneService milestoneService;

    @RequestMapping("milestone")
    public String toMillstone(){
        return "milestone/milestone";
    }

    @RequestMapping(value = "milestone/getallMilestones", method = RequestMethod.GET)
    @ResponseBody
    public RestfulResponse getAllMilestones(Model model){
        List<Milestone> milestones = new ArrayList<>();
        RestfulResponse response = new RestfulResponse(1,"success");
        milestones = milestoneService.getAllMileStones();
        response.setData(milestones);
        return response;
    }

    @RequestMapping(value = "milestone/unlock", method = RequestMethod.POST)
    @ResponseBody
    public RestfulResponse unlock(@RequestBody Map<String,Object> param){
        RestfulResponse response;
        String publishtime = param.get("publishtime").toString();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime ldt = LocalDateTime.parse(publishtime,fmt);
        if(milestoneService.checklock() && milestoneService.unlock(ldt) == 1){
            response = new RestfulResponse(1,"success");
        }else {
            response = new RestfulResponse(0,"failed");
        }
        return response;
    }

    @RequestMapping(value = "milestone/publish", method = RequestMethod.POST)
    @ResponseBody
    public RestfulResponse publish(@RequestBody Map<String,Object> param){
        RestfulResponse response;
        String publishtime = param.get("publishtime").toString();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime ldt = LocalDateTime.parse(publishtime,fmt);
        if(!milestoneService.checklock() && milestoneService.publish(ldt) == 1){
            response = new RestfulResponse(1,"success");
        }else {
            response = new RestfulResponse(0,"failed");
        }
        return response;
    }
}
