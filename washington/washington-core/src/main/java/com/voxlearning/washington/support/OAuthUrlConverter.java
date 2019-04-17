package com.voxlearning.washington.support;

import com.voxlearning.alps.core.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.net.URLEncoder;

/**
 * @author malong
 * @since 2018/05/19
 */
@Slf4j
public class OAuthUrlConverter {
    @Deprecated
    public static String convert(String appId, String authUrl, String returnUrl, String state) {
        return convert(appId, authUrl, returnUrl, state, "snsapi_base");
    }

    public static String convertOAuthUrlForBase(String appId, String authUrl, String returnUrl, String state) {
        return convert(appId, authUrl, returnUrl, state, "snsapi_base");
    }

    public static String convertOAuthUrlForUserInfo(String appId, String authUrl, String returnUrl, String state) {
        return convert(appId, authUrl, returnUrl, state, "snsapi_userinfo");
    }

    public static String convert(String appId, String authUrl, String returnUrl, String state, String scope) {
        String redirectUrl;

        try {
            if (!authUrl.startsWith("http://") && !authUrl.startsWith("https://")) {
                redirectUrl = "https://" + authUrl;
            } else {
                redirectUrl = authUrl;
            }

            if (StringUtils.isNotBlank(returnUrl)) {
                redirectUrl += "?returnUrl=" + URLEncoder.encode(returnUrl, "UTF-8");
            }
            redirectUrl = URLEncoder.encode(redirectUrl, "UTF-8");

            return "https://open.weixin.qq.com/connect/oauth2/authorize?appid="
                    + appId + "&redirect_uri=" + redirectUrl +
                    "&response_type=code&scope=" + scope + "&state=" + state + "#wechat_redirect";
        } catch (Exception ex) {
            log.error("Generate redirect url failed", ex);
            return "";
        }
    }
}
