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
@RequestMapping("template")
public class TestController {
    @RequestMapping("template")
    @ResponseBody
    public Model template(Model model) {
        return model;
    }
}
