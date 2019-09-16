package edu.pitt.medschool.controller.aggregationDB;


import edu.pitt.medschool.framework.rest.RestfulResponse;
import edu.pitt.medschool.service.AggregationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AggregationDBController {

    @Autowired
    AggregationService aggregationService;

    @GetMapping("/aggregation/getDBs")
    @ResponseBody
    public RestfulResponse getDBs(){
        RestfulResponse response = new RestfulResponse(1, "");
       // response.setData(aggregationService.selectAllAvailableDBs());
        return response;
    }

}
