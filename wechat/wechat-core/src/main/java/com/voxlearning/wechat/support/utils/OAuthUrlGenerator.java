package com.voxlearning.wechat.support.utils;

import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.wechat.constants.AuthType;
import com.voxlearning.wechat.support.WechatConfig;
import lombok.extern.slf4j.Slf4j;

import java.net.URLEncoder;

/**
 * @author Xin Xin
 * @since 11/8/15
 */
@Slf4j
public class OAuthUrlGenerator {
    //消息体中直接跳转web页面的链接需要做身份认证取得帐号openId
    public static final String generator(String state) {
        try {
            return "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + ProductConfig.get(WechatType.PARENT.getAppId()) + "&redirect_uri=" + URLEncoder.encode(WechatConfig.getBaseSiteUrl(), "UTF-8") + "%2fparent_auth.vpage&response_type=code&scope=snsapi_base&state=" + state + "#wechat_redirect";
        } catch (Exception ex) {
            throw new RuntimeException("Generate wechat oauth link failed");
        }
    }

    public static final String generatorForChips(String state) {
        try {
            return "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + ProductConfig.get(WechatType.CHIPS.getAppId()) + "&redirect_uri=" + URLEncoder.encode(WechatConfig.getBaseSiteUrl() + "/chips_open.vpage", "UTF-8") + "&response_type=code&scope=snsapi_base&state=" + state + "#wechat_redirect";
        } catch (Exception ex) {
            throw new RuntimeException("Generate wechat oauth link failed");
        }
    }

    public static final String generatorLoginCenterUrlForChips() {
       return generatorAuthUrlForChips(AuthType.CHIPS_CENTER);
    }

    public static final String generatorAuthUrlForChips(AuthType authType) {
        try {
            return "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + ProductConfig.get(WechatType.CHIPS.getAppId()) + "&redirect_uri=" + URLEncoder.encode(WechatConfig.getBaseSiteUrl() + "/chips_auth.vpage", "UTF-8") + "&response_type=code&scope=snsapi_base&state=" + authType.getType() + "#wechat_redirect";
        } catch (Exception ex) {
            log.error("Generate wechat oauth link failed", ex);
            throw new RuntimeException("Generate wechat oauth link failed");
        }
    }

    public static final String generatorUserInfoScopeForChips(AuthType authType, String paramKey) {
        try {
            return "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + ProductConfig.get(WechatType.CHIPS.getAppId()) + "&redirect_uri=" + URLEncoder.encode(WechatConfig.getBaseSiteUrl() + "/chips_userauth.vpage", "UTF-8") + "&response_type=code&scope=snsapi_userinfo&state=" + authType.getType() + "_" + paramKey + "#wechat_redirect";
        } catch (Exception ex) {
            log.error("Generate wechat oauth link failed", ex);
            throw new RuntimeException("Generate wechat oauth link failed");
        }
    }

    public static final String generatorUserInfoScopeForChipsLogin(AuthType authType, String paramKey) {
        try {
            return "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + ProductConfig.get(WechatType.CHIPS.getAppId()) + "&redirect_uri=" + URLEncoder.encode(WechatConfig.getBaseSiteUrl() + "/chips_userlogin.vpage", "UTF-8") + "&response_type=code&scope=snsapi_userinfo&state=" + authType.getType() + "_" + paramKey + "#wechat_redirect";
        } catch (Exception ex) {
            log.error("Generate wechat oauth link failed", ex);
            throw new RuntimeException("Generate wechat oauth link failed");
        }
    }
}
