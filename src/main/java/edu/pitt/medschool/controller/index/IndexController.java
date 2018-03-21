/**
 * 
 */
package edu.pitt.medschool.controller.index;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Isolachine
 *
 */

@Controller
public class IndexController {
    @RequestMapping("template/template")
    public Model template(Model model) {
        return model;
    }
    
    @RequestMapping("index/home")
    public Model index(Model model) {
        model.addAttribute("nav", "home");
        model.addAttribute("subnav", "");
        return model;
    }
    
    @RequestMapping("/")
    public String home(Model model) {
        model.addAttribute("nav", "home");
        model.addAttribute("subnav", "");
        return "redirect:/index/home";
    }

}
