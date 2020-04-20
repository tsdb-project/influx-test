package edu.pitt.medschool.controller.account;

import edu.pitt.medschool.config.DBConfiguration;
import edu.pitt.medschool.framework.rest.RestfulResponse;
import edu.pitt.medschool.model.Email;
import edu.pitt.medschool.model.dto.Accounts;
import edu.pitt.medschool.model.dto.Downsample;
import edu.pitt.medschool.model.dto.Version;
import edu.pitt.medschool.service.UsersService;
import edu.pitt.medschool.service.VersionControlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class AccountController {

    @Autowired
    UsersService usersService;

    @Autowired
    VersionControlService versionControlService;

//    @RequestMapping("/login")
//    public ModelAndView login(ModelAndView modelAndView) {
//        modelAndView.setViewName("user/login");
//        return modelAndView;
//    }
//
    @RequestMapping("/profile/{username}")
    public ModelAndView userProfile(@PathVariable String username, ModelAndView modelAndView) {
        Accounts userVO = usersService.selectByUserName(username).get(0);
        List<Version> versionList = versionControlService.getAllVersions();
        modelAndView.addObject("userInfo",userVO);
        modelAndView.addObject("versions",versionList);
        modelAndView.setViewName("user/profile");
        return modelAndView;
    }

    @RequestMapping("/management")
    public ModelAndView userManagement(ModelAndView modelAndView) {
        modelAndView.addObject("nav", "user");
        modelAndView.addObject("subnav", "");
        modelAndView.setViewName("admin/management");
        return modelAndView;
    }

    @GetMapping(value = { "/user", "/user/{id}" })
    @ResponseBody
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
    @ResponseBody
    public RestfulResponse createOrUpdateUser(@RequestBody(required = true) Accounts userVO) {
        int updatedRow;
        if (userVO.getId() == null) {
            if(! usersService.selectByUserName(userVO.getUsername()).isEmpty()){
                return new RestfulResponse(0, "failed");
            }else {
                updatedRow = usersService.insertUser(userVO);
            }
        } else {
            updatedRow = usersService.updateUser(userVO);
        }
        return new RestfulResponse(1, "success",updatedRow);
    }

    @PatchMapping("/reset_password/{id}")
    @ResponseBody
    public RestfulResponse resetPassword(@PathVariable Integer id) {
        int res = usersService.resetPassword(id);
        return new RestfulResponse(1, "success",res);
    }

    @PostMapping("/change_password")
    @ResponseBody
    public RestfulResponse changePassword(@RequestBody(required = true) Accounts userVO ) {
        int res = usersService.changePassword(userVO.getId(),userVO.getPassword());
        return new RestfulResponse(1, "success",res);
    }

    @PatchMapping("/toggle_enable/{id}")
    @ResponseBody
    public RestfulResponse toggleEnableUser(@PathVariable Integer id, @RequestBody(required = true) Boolean enable) {
        int res = usersService.toggleEnabled(id, enable);
        return new RestfulResponse(1, "success",res);
    }

    @RequestMapping("/getUserByName/{username}")
    @ResponseBody
    public RestfulResponse getUserByName(@PathVariable String username){
        Accounts accounts = usersService.selectByUserName(username).get(0);
        return new RestfulResponse(1,"success",accounts);
    }

    @RequestMapping("/email")
    public ModelAndView userFeedBack(ModelAndView modelAndView) {
//        modelAndView.addObject("nav", "user");
//        modelAndView.addObject("subnav", "");
        modelAndView.setViewName("user/email");
        return modelAndView;
    }

    @RequestMapping(value = "/sendEmail", method = RequestMethod.POST)
    @ResponseBody
    public RestfulResponse sendEmail(@RequestBody(required = true) Email email) throws Exception {
        if(usersService.sendEmailMessage(email.getEmailAddress(),email.getContent())){
            System.out.println("true");
            return new RestfulResponse(1,"success");
        }else {
            return new RestfulResponse(0,"fail");
        }
    }

}
