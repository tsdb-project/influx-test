/**
 * 
 */
package app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Isolachine
 *
 */

@Controller
@RequestMapping("export")
public class ExportController {
    
    @RequestMapping("export")
    public Model index(Model model) {
        model.addAttribute("nav", "export");
        model.addAttribute("subnav", "export_sub");
        return model;
    }
}
