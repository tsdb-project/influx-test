/**
 * 
 */
package app.controller;

import app.service.ColumnService;
import app.service.PatientFilteringService;
import app.service.QueriesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Isolachine
 *
 */

@Controller
@RestController
public class IndexController {
    @Autowired
    QueriesService queriesService;
    @Autowired
    ColumnService columnService;
    @Autowired
    PatientFilteringService patientFilteringService;

    @RequestMapping("index/home")
    @ResponseBody
    public Model index(Model model) {
        model.addAttribute("nav", "home");
        model.addAttribute("subnav", "");
        return model;
    }

}
