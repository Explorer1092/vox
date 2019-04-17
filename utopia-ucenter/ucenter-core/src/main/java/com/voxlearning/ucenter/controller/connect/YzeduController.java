package com.voxlearning.ucenter.controller.connect;

import com.voxlearning.ucenter.support.controller.AbstractWebController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 扬州电教馆JSE教育平台SSO对接
 *
 * @author Jia HuanYin
 * @since 2015/6/16
 */
@Controller
@RequestMapping("/yzedulogin")
public class YzeduController extends AbstractWebController {

    private static final String SSO_URI = "/ssologin/yzedu.vpage?token=";

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String parseCode() {
        String code = getRequestString("code");
        return "redirect:" + SSO_URI + code;
    }
}