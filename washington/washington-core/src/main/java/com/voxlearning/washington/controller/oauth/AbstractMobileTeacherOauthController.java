package com.voxlearning.washington.controller.oauth;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.spi.core.HttpClientType;
import com.voxlearning.washington.cache.WechatCacheManager;
import com.voxlearning.washington.constant.AuthType;
import com.voxlearning.washington.controller.mobile.teacher.AbstractMobileTeacherController;
import com.voxlearning.washington.mapper.wechat.WechatOAuthUserInfo;
import com.voxlearning.washington.support.wechat.WechatInfoProvider;
import org.slf4j.Logger;

import java.util.Map;

public class AbstractMobileTeacherOauthController extends AbstractMobileTeacherController {

    private static final Logger log = LoggerFactory.getLogger(AbstractMobileTeacherOauthController.class);

    private static final String OAUTH_GET_OPENID_URL = "https://api.weixin.qq.com/sns/oauth2/access_token?appid={}&secret={}&code={}&grant_type=authorization_code";
    private static final String OAUTH_GET_USER_INFO_URL = "https://api.weixin.qq.com/sns/userinfo?access_token={}&openid={}&lang=zh_CN";
    private static final String OAUTH_REFRESH_ACCESS_TOKEN_URL = "https://api.weixin.qq.com/sns/oauth2/refresh_token?appid={}&grant_type=refresh_token&refresh_token={}";

    private static final String SCOPE = "scope";
    private static final String OPEN_ID = "openid";
    private static final String ERROR_CODE = "errcode";
    static final String ERROR_MESSAGE = "errmsg";
    private static final String IGNORE_ERROR_CODE = "40029";
    private static final String SNSAPI_USERINFO = "snsapi_userinfo";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String REFRESH_TOKEN = "refresh_token";

    String getOpenIdByCode(String code) {
        String url = generateUrlFetchOpenId(code);

        AlpsHttpResponse response = HttpRequestExecutor.instance(HttpClientType.POOLING).get(url).execute();
        if (null == response.getResponseString()) {
            logger.warn("Get openId by code failed, response nothing.");
            return null;
        }

        Map<String, Object> result = JsonUtils.fromJson(response.getResponseString());
        if (MapUtils.isNotEmpty(result)) {
            if (!result.keySet().contains(ERROR_CODE)) {
                String openId = result.get(OPEN_ID).toString();
                if (SNSAPI_USERINFO.equals(result.get(SCOPE)) && result.containsKey(ACCESS_TOKEN)) {
                    //是获取帐号信息的授权
                    String accessToken = result.get(ACCESS_TOKEN).toString();
                    String refreshToken = result.get(REFRESH_TOKEN).toString();

                    WechatOAuthUserInfo wechatOAuthUserInfo = getWechatUserInfo(accessToken, openId);
                    WechatCacheManager.INSTANCE.setOAuthUserInfo(openId, wechatOAuthUserInfo);
                    WechatCacheManager.INSTANCE.setOAuthAccessToken(openId, accessToken);
                    WechatCacheManager.INSTANCE.setOAuthRefreshToken(openId, refreshToken);
                }

                return openId;
            }

            if (result.keySet().contains(ERROR_CODE) && IGNORE_ERROR_CODE.equals(result.get(ERROR_CODE).toString())) {
                //微信有bug,菜单点击后会发出两次请求,第一次请求会被微信终止进程,导致第二次接收到的是已经接收过一次的,此时code已经失效
                return null;
            }
        }

        logger.error("Get openId by code failed,code:{},response:{}", code, response.getResponseString());
        return null;
    }

    String generateUrlFetchOpenId(String code) {
        return StringUtils.formatMessage(OAUTH_GET_OPENID_URL, WechatInfoProvider.INSTANCE.appId(), WechatInfoProvider.INSTANCE.secret(), code);
    }

    String authenticate(AuthType type, String openId, String returnUrl, String loginUrl) {
        getRequestContext().setAuthenticatedOpenId(openId, 24 * 60 * 60);

        //加一个cookie给h5用于标记页面是在公众号里打开的
        getRequestContext().getCookieManager().setCookieTLD("is_official_account", "1", 30 * 60);

        //getRequestContext().cleanupAuthenticationStates();

        if (type.getNeedLogin()) {
            //跳转到业务页面之前必须有登录状态
            return loginUrl;
        } else {
            //跳转到业务页面之前，不必有登录状态，在业务页面做登录态检验
            getRequestContext().getCookieManager().setCookieTLD("st_nlg", "1", 30 * 60);
            return returnUrl;
        }
    }

    public String redirect(AuthType authType, String returnUrl) {
        String url;
        if (StringUtils.isNotBlank(returnUrl)) {
            url = returnUrl;
        } else {
            switch (authType) {
                default:
                    url = "/";
                    break;
            }
        }
        return appendQueryString(url);
    }

    String appendQueryString(String url) {
        String queryString = getRequest().getQueryString();
        if (StringUtils.isBlank(queryString)) {
            return url;
        }

        if (url.contains("?")) {
            url = url + "&" + queryString;
        } else {
            url = url + "?" + queryString;
        }
        return url;
    }


    /**
     * 最大努力急迫的拉取微信用户的详细信息
     */
    protected WechatOAuthUserInfo getWechatOauthUserInfo(String openId) {
        WechatOAuthUserInfo userInfo = WechatCacheManager.INSTANCE.getOAuthUserInfo(openId);
        if (userInfo == null) {
            userInfo = getWechatUserInfoByAccessToken(openId);
            if (userInfo == null) {
                userInfo = getWechatUserInfoByRefreshToken(openId);
            }
        }
        return userInfo;
    }

    WechatOAuthUserInfo getWechatUserInfoByAccessToken(String openId) {
        String accessToken = WechatCacheManager.INSTANCE.getOAuthAccessToken(openId);
        if (StringUtils.isBlank(accessToken)) {
            return null;
        }

        return getWechatUserInfo(accessToken, openId);
    }

    WechatOAuthUserInfo getWechatUserInfoByRefreshToken(String openId) {
        String refreshToken = WechatCacheManager.INSTANCE.getOAuthRefreshToken(openId);
        if (StringUtils.isBlank(refreshToken)) {
            return null;
        }

        String url = StringUtils.formatMessage(OAUTH_REFRESH_ACCESS_TOKEN_URL, WechatInfoProvider.INSTANCE.appId(), refreshToken);
        AlpsHttpResponse response = HttpRequestExecutor.instance(HttpClientType.POOLING).get(url).execute();
        if (null == response.getResponseString()) {
            logger.warn("Refresh access_token(wechat OAuth) failed, response nothing.");
            return null;
        }

        Map<String, Object> res = JsonUtils.fromJson(response.getResponseString());
        if (MapUtils.isEmpty(res)) {
            return null;
        }
        if (res.containsKey(ERROR_CODE)) {
            log.error("Refresh access_token error,{}", JsonUtils.toJson(res));
            return null;
        }

        String accessToken = res.get(ACCESS_TOKEN).toString();
        WechatCacheManager.INSTANCE.setOAuthAccessToken(openId, accessToken);

        return getWechatUserInfo(accessToken, openId);
    }

    private WechatOAuthUserInfo getWechatUserInfo(String accessToken, String openId) {
        String url = StringUtils.formatMessage(OAUTH_GET_USER_INFO_URL, accessToken, openId);
        AlpsHttpResponse response = HttpRequestExecutor.instance(HttpClientType.POOLING).get(url).execute();

        if (null == response.getResponseString()) {
            logger.warn("Get userinfo(wechat OAuth) failed, response nothing.");
            return null;
        }

        Map<String, Object> res = JsonUtils.fromJson(response.getResponseString());
        if (MapUtils.isEmpty(res)) {
            return null;
        }

        String nick = SafeConverter.toString(res.get("nickname"));
        String sex = SafeConverter.toString(res.get("sex"));
        String province = SafeConverter.toString(res.get("province"));
        String city = SafeConverter.toString(res.get("city"));
        String headImgUrl = SafeConverter.toString(res.get("headimgurl"));

        WechatOAuthUserInfo info = new WechatOAuthUserInfo();
        info.setNickName(nick);
        info.setHeadImgUrl(headImgUrl);
        info.setSex(sex);
        info.setProvince(province);
        info.setCity(city);
        info.setOpenId(openId);

        WechatCacheManager.INSTANCE.setOAuthUserInfo(openId, info);
        return info;
    }

    protected boolean canGetWechatUserInfo() {
        String openId = getRequestContext().getAuthenticatedOpenId();
        if (StringUtils.isEmpty(openId)) {
            return false;
        }

        WechatOAuthUserInfo userInfo = WechatCacheManager.INSTANCE.getOAuthUserInfo(openId);
        String accessToken = WechatCacheManager.INSTANCE.getOAuthAccessToken(openId);
        String refreshToken = WechatCacheManager.INSTANCE.getOAuthRefreshToken(openId);

        // 只要有一个不是空就可以拉取到信息
        return (userInfo != null) || StringUtils.isNotEmpty(accessToken) || StringUtils.isNotEmpty(refreshToken);
    }
}
