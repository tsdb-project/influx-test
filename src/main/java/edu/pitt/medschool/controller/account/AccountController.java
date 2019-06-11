package edu.pitt.medschool.controller.account;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/user")
public class AccountController {

//    @RequestMapping("/login")
//    public ModelAndView login(ModelAndView modelAndView) {
//        modelAndView.setViewName("user/login");
//        return modelAndView;
//    }
//
//    @RequestMapping("/profile")
//    public ModelAndView userProfile(ModelAndView modelAndView) {
//        modelAndView.setViewName("user/profile");
//        return modelAndView;
//    }

    @RequestMapping("/management")
    public ModelAndView userManagement(ModelAndView modelAndView) {
        modelAndView.addObject("nav", "user");
        modelAndView.addObject("subnav", "");
        modelAndView.setViewName("admin/management");
        return modelAndView;
    }

}
