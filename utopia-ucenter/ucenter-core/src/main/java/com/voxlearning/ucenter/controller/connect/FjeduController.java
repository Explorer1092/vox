package com.voxlearning.ucenter.controller.connect;

import com.voxlearning.ucenter.support.controller.AbstractWebController;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 福建教育云平台对接处理
 * Created by Alex on 15-3-10.
 */
@Controller
@RequestMapping("/")
@Slf4j
@NoArgsConstructor
public class FjeduController extends AbstractWebController {

    @RequestMapping(value = "fjedulogin/smartclazz.vpage", method = RequestMethod.GET)
    public String gotoSmartClazz(Model model) {
        String ticket = getRequestString("ticket");
        String returnUrl = ProductConfig.getMainSiteBaseUrl() + "/teacher/smartclazz/list.vpage";
        return "redirect:/ssologin/fjedu.vpage?returnUrl=" + returnUrl + "&token=" + ticket;
    }

    @RequestMapping(value = "fjedulogin/homework.vpage", method = RequestMethod.GET)
    public String gotoHomework(Model model) {
        String ticket = getRequestString("ticket");
        String returnUrl = ProductConfig.getMainSiteBaseUrl() + "/teacher/homework/batchassignhomework.vpage";
        return "redirect:/ssologin/fjedu.vpage?returnUrl=" + returnUrl + "&token=" + ticket;
    }

    @RequestMapping(value = "fjedulogin/exam.vpage", method = RequestMethod.GET)
    public String gotoExam(Model model) {
        String ticket = getRequestString("ticket");
        String returnUrl = ProductConfig.getMainSiteBaseUrl() + "/teacher/clazzwork/batch.vpage";
        return "redirect:/ssologin/fjedu.vpage?returnUrl=" + returnUrl + "&token=" + ticket;
    }

    @RequestMapping(value = "fjedulogin/student.vpage", method = RequestMethod.GET)
    public String gotoStudnetIndex(Model model) {
        String ticket = getRequestString("ticket");
        return "redirect:/ssologin/fjedu.vpage?token=" + ticket;
    }

    @RequestMapping(value = "fjedulogin/index.vpage", method = RequestMethod.GET)
    public String gotoHomePage(Model model) {
        String ticket = getRequestString("ticket");
        return "redirect:/ssologin/fjedu.vpage?token=" + ticket;
    }

}

