package com.voxlearning.ucenter.controller.connect.controller;

import com.voxlearning.ucenter.support.controller.AbstractWebController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author maofeng.lu
 * @since 2017/8/7.19:41
 */
@Controller
@RequestMapping("/sso")
public class SsoLogoutController extends AbstractWebController {

    @RequestMapping(value = "logout.vpage", method = RequestMethod.GET)
    public String logout() {
        getWebRequestContext().cleanupAuthenticationStates();
        return "sso/logout";
    }
}
