package edu.pitt.medschool.controller.aggregationDB;


import com.fasterxml.jackson.core.JsonProcessingException;
import edu.pitt.medschool.framework.rest.RestfulResponse;
import edu.pitt.medschool.model.dto.AggregationDatabase;
import edu.pitt.medschool.model.dto.AggregationDatabaseWithBLOBs;
import edu.pitt.medschool.service.AggregationService;
import edu.pitt.medschool.service.ColumnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import retrofit2.http.POST;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/aggregation")
public class AggregationDBController {

    @Autowired
    AggregationService aggregationService;

//    @Autowired
//    ColumnService columnService;


    @RequestMapping("/aggDatabase")
    @ResponseBody
    public Model aggDatabasePage(Model model) {
        model.addAttribute("nav", "management");
        model.addAttribute("subnav", "aggDatabase");
//        List<String> trends = columnService.selectAllMeasures();
        List<String> trends = aggregationService.getColumns();
        List<AggregationDatabase> availableDatabases = aggregationService.selectAllAvailableDBs();
        List<String> DBNames = new ArrayList<>();
        for (AggregationDatabase database: availableDatabases){
            DBNames.add(database.getDbName());
        }
        model.addAttribute("measures", trends);
        model.addAttribute("databases",DBNames);
        return model;
    }


    // get all available db with status = success
    @GetMapping("/getDBs")
    @ResponseBody
    public RestfulResponse getDBs(){
        RestfulResponse response = new RestfulResponse(1, "");
        response.setData(aggregationService.selectAllAvailableDBs());
        return response;
    }


    // insert a new agg db into aggregation database table
    @PostMapping("/newDB")
    @ResponseBody
    public RestfulResponse exportQuery(@RequestBody(required = true) AggregationDatabaseWithBLOBs job, RestfulResponse response) {
        if (aggregationService.completeJobAndInsert(job)) {
            if (aggregationService.addOneAggregationJob(job.getId())) {
                response.setCode(1);
                response.setMsg("Successfully added job.");
            } else {
                response.setCode(2);
                response.setMsg("Failed to add job into queue.");
            }
        } else {
            response.setCode(0);
            response.setMsg("Database error!");
        }
        return response;
    }

    // check process
    @GetMapping("/process")
    @ResponseBody
    public RestfulResponse getOngoing(){
        RestfulResponse response = new RestfulResponse(1,"");
        response.setData(aggregationService.selectAllOnGoing());
        return response;
    }

    @GetMapping("/checkIntegrity")
    @ResponseBody
    public RestfulResponse checkIntegrity(){
        RestfulResponse response = new RestfulResponse(1,"");
        aggregationService.checkIntegrity();
        return response;
    }

}
