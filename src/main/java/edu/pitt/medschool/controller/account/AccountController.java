package edu.pitt.medschool.controller.account;

import edu.pitt.medschool.framework.rest.RestfulResponse;
import edu.pitt.medschool.model.dto.Accounts;
import edu.pitt.medschool.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class AccountController {

    @Autowired
    UsersService usersService;

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

    @GetMapping(value = { "/user", "/user/{id}" })
    public RestfulResponse userList(@PathVariable Optional<Integer> id) {
        if (id.isPresent()) {
            Accounts userVO = usersService.selectById(id.get());
            return new RestfulResponse(1, "success",userVO);
        } else {
            List<Accounts> list = usersService.selectAll();
            return new RestfulResponse(1, "success",list);
        }
    }

    @PutMapping("/user")
    public RestfulResponse createOrUpdateUser(@RequestBody(required = true) Accounts userVO) {
        int updatedRow;
        if (userVO.getId() == null) {
            updatedRow = usersService.insertUser(userVO);
        } else {
            updatedRow = usersService.updateUser(userVO);
        }
        return new RestfulResponse(1, "success",updatedRow);
    }

    @PatchMapping("/reset_password/{id}")
    public RestfulResponse resetPassword(@PathVariable Integer id) {
        int res = usersService.resetPassword(id);
        return new RestfulResponse(1, "success",res);
    }

    @PatchMapping("/toggle_enable/{id}")
    public RestfulResponse toggleEnableUser(@PathVariable Integer id, @RequestBody(required = true) Boolean enable) {
        int res = usersService.toggleEnabled(id, enable);
        return new RestfulResponse(1, "success",res);
    }


}
