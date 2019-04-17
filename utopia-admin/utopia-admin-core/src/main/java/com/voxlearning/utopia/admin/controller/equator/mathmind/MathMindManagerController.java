package com.voxlearning.utopia.admin.controller.equator.mathmind;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.equator.service.mathmind.api.client.MathMindLoaderClient;
import com.voxlearning.utopia.admin.controller.equator.AbstractEquatorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;

/**
 * @author xiaoying.han
 * @since 2019/2/16
 */
@Controller
@RequestMapping(value = "equator/newwonderland/mathmind")
public class MathMindManagerController extends AbstractEquatorController {

    @Inject
    private MathMindLoaderClient mathMindLoaderClient;

    @RequestMapping(value = "userInfo.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String mathMindUserInfo(Model model) {
        String responseString = "equator/mathmind/index";
        Long studentId = getRequestLong("studentId");
        model.addAttribute("studentId", studentId == 0 ? "" : studentId);
        if (studentId == 0) {
            return responseString;
        }

        MapMessage mapMessage = mathMindLoaderClient.getRemoteReference().fetchMathMindInfo(studentId);
        if (!mapMessage.isSuccess()) {
            model.addAttribute("error", "数据不正确");
            return responseString;
        }
        model.addAllAttributes(mapMessage);
        return responseString;
    }
}
