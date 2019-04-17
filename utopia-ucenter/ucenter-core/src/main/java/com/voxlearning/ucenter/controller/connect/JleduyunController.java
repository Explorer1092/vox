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
public class JleduyunController extends AbstractWebController {

  @RequestMapping(value = "jledupslogin/index.vpage", method = RequestMethod.GET)
  public String gotoPrimaryHomePage(Model model) {
    String ticket = getRequestString("ticket");
    return "redirect:/ssologin/jledups.vpage?token=" + ticket;
  }

  @RequestMapping(value = "jledumslogin/index.vpage", method = RequestMethod.GET)
  public String gotoSecondaryHomePage(Model model) {
    String ticket = getRequestString("ticket");
    return "redirect:/ssologin/jledums.vpage?token=" + ticket;
  }
}
