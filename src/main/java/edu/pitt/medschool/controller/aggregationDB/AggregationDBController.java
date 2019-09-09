package edu.pitt.medschool.controller.aggregationDB;


import edu.pitt.medschool.framework.rest.RestfulResponse;
import edu.pitt.medschool.service.AggregationService;
import edu.pitt.medschool.service.ColumnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

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
        model.addAttribute("measures", trends);
        return model;
    }

    @GetMapping("/getDBs")
    @ResponseBody
    public RestfulResponse getDBs(){
        RestfulResponse response = new RestfulResponse(1, "");
       // response.setData(aggregationService.selectAllAvailableDBs());
        return response;
    }
}
