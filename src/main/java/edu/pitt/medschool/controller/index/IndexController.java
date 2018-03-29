/**
 * 
 */
package edu.pitt.medschool.controller.index;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

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

    @RequestMapping("/")
    public ModelAndView index(ModelAndView modelAndView) {
        modelAndView.addObject("nav", "home");
        modelAndView.addObject("subnav", "");
        modelAndView.setViewName("index/home");
        return modelAndView;
    }

}
