package edu.pitt.medschool.controller.aggregationDB;


import com.fasterxml.jackson.core.JsonProcessingException;
import edu.pitt.medschool.framework.rest.RestfulResponse;
import edu.pitt.medschool.model.dto.AggregationDatabase;
import edu.pitt.medschool.model.dto.AggregationDatabaseWithBLOBs;
import edu.pitt.medschool.service.AggregationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AggregationDBController {

    @Autowired
    AggregationService aggregationService;

    // get all available db with status = success
    @GetMapping("/aggregation/getDBs")
    @ResponseBody
    public RestfulResponse getDBs(){
        RestfulResponse response = new RestfulResponse(1, "");
        response.setData(aggregationService.selectAllAvailableDBs());
        return response;
    }


    // insert a new agg db into aggregation database table
    @GetMapping("/aggregation/newDB")
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

    // check porcess
    @GetMapping("/aggregation/process")
    @ResponseBody
    public RestfulResponse getOngoing(){
        RestfulResponse response = new RestfulResponse(1,"");
        response.setData(aggregationService.selectAllOnGoing());
        return response;
    }



}
