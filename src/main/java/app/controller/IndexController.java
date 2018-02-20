/**
 * 
 */
package app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import app.service.ColumnService;
import app.service.PatientService;
import app.service.QueriesService;

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
    PatientService patientService;

    @RequestMapping("index/home")
    @ResponseBody
    public Model index(Model model) {
        model.addAttribute("nav", "home");
        model.addAttribute("subnav", "");
        return model;
    }

}
