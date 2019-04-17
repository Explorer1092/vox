package com.voxlearning.washington.controller.connect;

import com.voxlearning.washington.support.AbstractController;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;

/**
 * 中央电教馆云平台对接处理
 * Created by Alex on 15-3-10.
 */
@Controller
@RequestMapping("/")
@Slf4j
@NoArgsConstructor
@Deprecated
public class CneduController extends AbstractController {

    @Inject private SsoConnectorFactory ssoConnectorFactory;

    @RequestMapping(value = "cnedulogin/index.vpage", method = RequestMethod.GET)
    public String gotoHomePage(Model model) {
        String ticket = getRequestString("ticket");
        return "redirect:/ssologin/cnedu.vpage?token=" + ticket;
    }

}
