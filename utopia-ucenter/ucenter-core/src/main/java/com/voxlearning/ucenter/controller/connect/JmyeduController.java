package com.voxlearning.ucenter.controller.connect;

import com.voxlearning.ucenter.support.controller.AbstractWebController;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/")
@Slf4j
@NoArgsConstructor
public class JmyeduController extends AbstractWebController {

    @RequestMapping(value = "jmyedulogin/index.vpage", method = RequestMethod.GET)
    public String gotoHomePage(Model model) {
        String ticket = getRequestString("ticket");
        return "redirect:/ssologin/jmyedu.vpage?token=" + ticket;
    }

}
