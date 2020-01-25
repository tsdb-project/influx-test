package edu.pitt.medschool.controller.aggregationDB;


import com.fasterxml.jackson.core.JsonProcessingException;
import edu.pitt.medschool.framework.rest.RestfulResponse;
import edu.pitt.medschool.model.dto.AggregationDatabase;
import edu.pitt.medschool.model.dto.AggregationDatabaseWithBLOBs;
import edu.pitt.medschool.model.dto.AggregationDb;
import edu.pitt.medschool.service.AggregationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
    
    
    // get useful db refer to period, origin, and duration
    @GetMapping("/getUsefulDBs")
    @ResponseBody
    public RestfulResponse getUsefulDBs(@RequestParam String period, @RequestParam String origin, @RequestParam String duration, 
    		@RequestParam String max, @RequestParam String min, @RequestParam String mean, @RequestParam String median,
    		@RequestParam String std, @RequestParam String fq, @RequestParam String tq, @RequestParam String sum){
        RestfulResponse response = new RestfulResponse(1, "");
        Integer period0 = Integer.valueOf(period);
        Integer origin0 = Integer.valueOf(origin);
        Integer duration0 = Integer.valueOf(duration);
        response.setData(aggregationService.selectAllUsefulDBs(period0, origin0, duration0, max, min, mean, median, std, fq, tq, sum));
        return response;
    }


    // todo old way, delete later
    // insert a new agg db into aggregation database table
//    @PostMapping("/newDB")
//    @ResponseBody
//    public RestfulResponse exportQuery(@RequestBody(required = true) AggregationDatabaseWithBLOBs job, RestfulResponse response) {
//        if (aggregationService.completeJobAndInsert(job)) {
////            int jobid = 0;
////            if(job.getId()==null){
////                String dbname = aggregationService.getDbName(job);
////                jobid = aggregationService.getJobId(dbname);
////            }else {
////                jobid = job.getId();
////            }
//            if (aggregationService.addOneAggregationJob(job.getId())) {
//                response.setCode(1);
//                response.setMsg("Successfully added job.");
//            } else {
//                response.setCode(2);
//                response.setMsg("Failed to add job into queue.");
//            }
//        } else {
//            response.setCode(0);
//            response.setMsg("Database error!");
//        }
//        return response;
//    }

    // new way to create db aggregation
    @PostMapping("/createDB")
    @ResponseBody
    public RestfulResponse createDB(@RequestBody(required = true) AggregationDb job, RestfulResponse response){
        if(aggregationService.insertNewDB(job)){
            if(aggregationService.addOneAggregationJob(job.getId())){
                response.setCode(1);
                response.setMsg("Successfully added job.");
            }else {
                response.setCode(2);
                response.setMsg("Failed to add job into queue.");
            }
        }else {
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

    @PostMapping("/checkIntegrity")
    @ResponseBody
    public RestfulResponse checkIntegrity(@RequestBody(required = true) AggregationDatabaseWithBLOBs job){
        System.out.println(job.getDbName());
        RestfulResponse response = new RestfulResponse(1,"");
        aggregationService.checkIntegrity(job);
        return response;
    }

}
