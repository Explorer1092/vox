package com.voxlearning.wechat.controller.chips;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.wechat.context.WxConfig;
import com.voxlearning.wechat.controller.AbstractChipsController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Slf4j
@Controller
@RequestMapping(value = "/chips/be")
public class ChipsAdPageController extends AbstractChipsController {

    @RequestMapping(value = "short/choice.vpage", method = RequestMethod.GET)
    public String learningDuration(Model model) {
        WechatType wechatType = WechatType.CHIPS;
        WxConfig wxConfig = new WxConfig(getRequestContext().getFullRequestUrl(), tokenHelper.getJsApiTicket(wechatType));
        initWechatConfigModel(model, wxConfig, wechatType);

        String refer = getRequestParameter("refer", "");
        String chanel = getRequestParameter("channel", "");
        model.addAttribute("refer", refer);
        model.addAttribute("channel", chanel);
        model.addAttribute("type", getRequestString("type"));
        model.addAttribute("inviter", getRequestLong("inviter"));
        return "/parent/chips/learning_duration";
    }

    @RequestMapping(value = "short/fc.vpage", method = RequestMethod.GET)
    public String fcShortPage(Model model) {

        WechatType wechatType = WechatType.CHIPS;
        WxConfig wxConfig = new WxConfig(getRequestContext().getFullRequestUrl(), tokenHelper.getJsApiTicket(wechatType));
        initWechatConfigModel(model, wxConfig, wechatType);

        String refer = getRequestParameter("orderReferer", "330358");
        String rel = getRequestParameter("rel", "fc_app");
        model.addAttribute("refer", refer);
        model.addAttribute("channel", rel);
        model.addAttribute("type", "fc");
        return "/parent/chips/learning_duration";
    }

    @RequestMapping(value = "short/introduction.vpage", method = RequestMethod.GET)
    public String introductionPage(Model model) {
        WechatType wechatType = WechatType.CHIPS;
        WxConfig wxConfig = new WxConfig(getRequestContext().getFullRequestUrl(), tokenHelper.getJsApiTicket(wechatType));
        initWechatConfigModel(model, wxConfig, wechatType);
        String url = ProductConfig.getMainSiteBaseUrl() + "/view/mobile/parent/parent_ai/be/s_2";
        String queryString = getRequestContext().getRequest().getQueryString();
        if (StringUtils.isNotBlank(queryString)) {
            return "redirect:" + url + "?" + queryString;
        }
        return "redirect:" + url;
        //return "/parent/chips/ground_be_advanced";
    }

    @RequestMapping(value = "short/travel.vpage", method = RequestMethod.GET)
    public String travelPage(Model model) {
        WechatType wechatType = WechatType.CHIPS;
        WxConfig wxConfig = new WxConfig(getRequestContext().getFullRequestUrl(), tokenHelper.getJsApiTicket(wechatType));
        initWechatConfigModel(model, wxConfig, wechatType);
        return "/parent/chips/ground_be_travel";
    }
}
