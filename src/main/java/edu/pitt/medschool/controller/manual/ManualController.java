package edu.pitt.medschool.controller.manual;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Isolachine
 */

@Controller
public class ManualController {
    @RequestMapping("manual")
    public ModelAndView index(ModelAndView modelAndView) {
        modelAndView.addObject("nav", "manual");
        modelAndView.addObject("subnav", "manualIndex");
        modelAndView.setViewName("manual/manual");
        return modelAndView;
    }

    @RequestMapping("manual/EEGDataCodebook")
    public ModelAndView EEGDataCodebook(ModelAndView modelAndView) {
        modelAndView.addObject("nav", "manual");
        modelAndView.addObject("subnav", "EEGDataCodebook");
        modelAndView.setViewName("manual/EEGDataCodebook");
        return modelAndView;
    }
}
