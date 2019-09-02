package edu.pitt.medschool.controller.aggregationDB;


import edu.pitt.medschool.service.AggregationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AggregationDBController {

    @Autowired
    AggregationService aggregationService;

}
