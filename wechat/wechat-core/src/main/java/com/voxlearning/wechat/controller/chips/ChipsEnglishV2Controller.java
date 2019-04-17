package com.voxlearning.wechat.controller.chips;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.ai.api.ChipsStudyArticleService;
import com.voxlearning.utopia.service.ai.api.ChipsUserPageViewLogService;
import com.voxlearning.utopia.service.ai.api.ChipsWechatUserLoader;
import com.voxlearning.utopia.service.ai.constant.PageViewType;
import com.voxlearning.utopia.service.ai.constant.WechatUserType;
import com.voxlearning.utopia.service.ai.data.ChipsWechatUser;
import com.voxlearning.utopia.service.ai.entity.ChipsUserPageViewLog;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.wechat.constants.AuthType;
import com.voxlearning.wechat.context.WxConfig;
import com.voxlearning.wechat.controller.AbstractChipsController;
import com.voxlearning.wechat.support.utils.OAuthUrlGenerator;
import com.voxlearning.wechat.support.utils.StringExtUntil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Optional;

/**
 * Created by Summer on 2018/4/25
 */
@Controller
@RequestMapping(value = "/chipsv2")
public class ChipsEnglishV2Controller extends AbstractChipsController {

    @ImportService(interfaceClass = ChipsWechatUserLoader.class)
    private ChipsWechatUserLoader chipsWechatUserLoader;

    @ImportService(interfaceClass = ChipsUserPageViewLogService.class)
    private ChipsUserPageViewLogService chipsUserPageViewLogService;

    @ImportService(interfaceClass = ChipsStudyArticleService.class)
    private ChipsStudyArticleService chipsStudyArticleService;

    // 学习相关信息流
    @RequestMapping(value = "/center/study_information.vpage", method = RequestMethod.GET)
    public String studyInformation(Model model) {
        String openId = getOpenId();
        String articleId = getRequestString("articleId");
        boolean redirect = Optional.ofNullable(openId)
                .filter(StringUtils::isNotBlank)
                .map(e -> {
                    ChipsWechatUser wechatUser = chipsWechatUserLoader.loadByOpenId(e, WechatUserType.CHIPS_OFFICIAL_ACCOUNTS.name());
                    return wechatUser == null;
                }).orElse(true);
        if (redirect) {
            String param = "articleId=" + articleId;
            String key = StringExtUntil.md5(param);
            persistenceCache(key, param);
            return "redirect:" + OAuthUrlGenerator.generatorUserInfoScopeForChips(AuthType.CHIPS_STUDY_INFORMATION, key);
        }
        User user = currentChipsUser();
        if (user != null) {
            ChipsUserPageViewLog log = new ChipsUserPageViewLog();
            log.setId(ChipsUserPageViewLog.genId(user.getId(), articleId));
            log.setUserId(user.getId());
            log.setUniqueKey(articleId);
            log.setType(PageViewType.STUDY_INFORMATION);
            log.setDisabled(false);
            chipsUserPageViewLogService.upsertChipsUserPageViewLog(log);
        }
        MapMessage message = chipsStudyArticleService.loadArticleForCrm(articleId);
        model.addAttribute("articleId", articleId);

        try {
            WechatType wechatType = WechatType.CHIPS;
            WxConfig wxConfig = new WxConfig(getRequestContext().getFullRequestUrl(), tokenHelper.getJsApiTicket(wechatType));
            initWechatConfigModel(model, wxConfig, wechatType);
            return "/parent/chips/study_information";
        } catch (Exception ex) {
            return redirectWithMsg("生成失败", model);
        }
    }

    @RequestMapping(value = "/center/article_detail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage articleDetail(Model model) {
        String articleId = getRequestString("articleId");
        return chipsStudyArticleService.loadArticleForCrm(articleId);
    }

}
