package com.voxlearning.wechat.controller.chips;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.ai.api.ChipsActivityService;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.wechat.constants.AuthType;
import com.voxlearning.wechat.context.WxConfig;
import com.voxlearning.wechat.controller.AbstractChipsController;
import com.voxlearning.wechat.support.utils.OAuthUrlGenerator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(value = "/chips/activity")
public class ChipsActivityController extends AbstractChipsController {

    @ImportService(interfaceClass = ChipsActivityService.class)
    private ChipsActivityService chipsActivityService;

    @RequestMapping(value = "waiting_for_you.vpage", method = RequestMethod.GET)
    public String waitingForYou(Model model) {
        User user = currentChipsUser();
        if (user == null) {
            return "redirect:" + OAuthUrlGenerator.generatorUserInfoScopeForChipsLogin(AuthType.CHIPS_ACTIVITY_LEAD, "");
        }
        chipsActivityService.processLeadPageVisit(user.getId());

        WechatType wechatType = WechatType.CHIPS;
        WxConfig wxConfig = new WxConfig(getRequestContext().getFullRequestUrl(), tokenHelper.getJsApiTicket(wechatType));
        initWechatConfigModel(model, wxConfig, wechatType);

        return "/parent/chips/temporary/waiting_for_you";
    }
}
