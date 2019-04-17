package com.voxlearning.washington.controller.oauth;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.washington.cache.WechatCacheManager;
import com.voxlearning.washington.constant.AuthType;
import com.voxlearning.washington.mapper.wechat.WechatOAuthUserInfo;
import com.voxlearning.washington.support.OAuthUrlConverter;
import com.voxlearning.washington.support.wechat.WechatInfoProvider;
import org.slf4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * 微信认证相关, 参考 parent 工程 com.voxlearning.andromeda.controller.OAuthController
 */
@Controller
@RequestMapping(value = "/wechat/")
public class OAuthController extends AbstractMobileTeacherOauthController {

    private static final Logger log = LoggerFactory.getLogger(OAuthController.class);

    @RequestMapping(value = "/auth.vpage", method = RequestMethod.GET)
    public String auth(Model model) {
        String code = getRequestString("code");
        String state = getRequestString("state");
        //第三方跳转链接
        String returnUrl = getRequestString("returnUrl");
        String openId = null;
        try {
            AuthType authType = AuthType.of(state);
            if (null == authType) {
                model.addAttribute(ERROR_MESSAGE, "未知类型的回调");
                return "/common/mobileerrorinfo";
            }

            openId = getOpenIdByCode(code);
            if (null == openId) {
                model.addAttribute(ERROR_MESSAGE, "获取openId失败");
                return "/common/mobileerrorinfo";
            }

            return "redirect:" + authenticate(authType, openId, returnUrl, ProductConfig.getMainSiteUcenterLoginUrl());
        } catch (Exception ex) {
            log.error("wechat auth error, openId:{}, code:{}, state:{}", openId, code, state, ex);

            model.addAttribute(ERROR_MESSAGE, "认证授权失败，系统异常");
            return "/common/mobileerrorinfo";
        }
    }

    /**
     * 微信帐号授权步骤一：
     * 检查是否已授权获取用户信息
     */
    @RequestMapping(value = "/oauth_redirect.vpage", method = RequestMethod.GET)
    public String authRedirect() throws UnsupportedEncodingException {
        setCorsHeaders();

        String returnUrl = getRequestString("returnUrl");
        if (StringUtils.isBlank(returnUrl)) {
            return "redirect:/";
        }
        returnUrl = URLDecoder.decode(returnUrl, "UTF-8");

        String oauthUrl = OAuthUrlConverter.convertOAuthUrlForUserInfo(WechatInfoProvider.INSTANCE.appId(), ProductConfig.getMainSiteBaseUrl() + "/wechat/auth.vpage", "/wechat/oauth_redirect.vpage?returnUrl=" + returnUrl, "NLG");
        String openId = getRequestContext().getAuthenticatedOpenId();
        if (StringUtils.isBlank(openId)) {
            //没有openId需要重新发起授权流程
            return "redirect:" + oauthUrl;
        }

        //已经查过微信帐号信息了，直接跳走
        WechatOAuthUserInfo userInfo = WechatCacheManager.INSTANCE.getOAuthUserInfo(openId);
        if (null != userInfo) {
            return "redirect:" + returnUrl;
        }

        String refreshToken = WechatCacheManager.INSTANCE.getOAuthRefreshToken(openId);
        if (StringUtils.isNotBlank(refreshToken)) {
            //refresh_token存在，可以取用户微信帐号信息
            return "redirect:" + returnUrl;
        }

        //没有refresh_token，需要重新发起授权
        return "redirect:" + oauthUrl;
    }

    /**
     * 只获取 openId ,如果有跳回到 returnUrl 如果没有跳到微信认证后再跳回 returnUrl
     */
    @RequestMapping(value = "/base_oauth_redirect.vpage", method = RequestMethod.GET)
    public String baseOauthRedirect() throws UnsupportedEncodingException {
        setCorsHeaders();

        String returnUrl = getRequestString("returnUrl");
        if (StringUtils.isBlank(returnUrl)) {
            return "redirect:/";
        }
        returnUrl = URLDecoder.decode(returnUrl, "UTF-8");

        String oauthUrl = OAuthUrlConverter.convertOAuthUrlForBase(WechatInfoProvider.INSTANCE.appId(), ProductConfig.getMainSiteBaseUrl() + "/wechat/auth.vpage", "/wechat/base_oauth_redirect.vpage?returnUrl=" + returnUrl, "NLG");
        String openId = getRequestContext().getAuthenticatedOpenId();
        if (StringUtils.isBlank(openId)) {
            return "redirect:" + oauthUrl;
        }
        return "redirect:" + returnUrl;
    }

    /**
     * 微信帐号授权步骤二：
     * 查询用户微信帐号信息
     */
    @RequestMapping(value = "/oauth_userinfo.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage oauthUserInfo() {
        setCorsHeaders();

        String openId = getRequestContext().getAuthenticatedOpenId();
        if (StringUtils.isBlank(openId)) {
            return MapMessage.errorMessage("openId未知");
        }

        WechatOAuthUserInfo userInfo = WechatCacheManager.INSTANCE.getOAuthUserInfo(openId);
        if (null != userInfo) {
            return MapMessage.successMessage().add("info", userInfo);
        }

        WechatOAuthUserInfo info = getWechatUserInfoByAccessToken(openId);
        if (null != info) {
            return MapMessage.successMessage().add("info", info);
        }

        info = getWechatUserInfoByRefreshToken(openId);
        if (null != info) {
            return MapMessage.successMessage().add("info", info);
        }

        return MapMessage.errorMessage("查询微信帐号信息失败");
    }
}
