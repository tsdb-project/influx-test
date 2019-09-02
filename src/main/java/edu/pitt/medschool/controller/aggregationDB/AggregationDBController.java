package edu.pitt.medschool.controller.aggregationDB;


import edu.pitt.medschool.service.AggregationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AggregationDBController {

    @Autowired
    AggregationService aggregationService;

    @RequestMapping("/aggDatabase")
    public Model aggDatabasePage(Model model) {
        model.addAttribute("nav", "management");
        model.addAttribute("subnav", "aggDatabase");
        return model;
    }

}
