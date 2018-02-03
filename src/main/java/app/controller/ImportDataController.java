/**
 * 
 */
package app.controller;

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
@RequestMapping("data")
public class ImportDataController {
    @RequestMapping("import")
    @ResponseBody
    public Model importData(Model model) {
        return model;
    }
    
    @RequestMapping("alerts")
    @ResponseBody
    public Model importD(Model model) {
        return model;
    }

}
