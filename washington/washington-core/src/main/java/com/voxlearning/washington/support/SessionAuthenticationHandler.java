package com.voxlearning.washington.support;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.webmvc.cookie.AuthCookieMappingInfo;
import com.voxlearning.alps.webmvc.cookie.CookieManager;
import com.voxlearning.alps.webmvc.support.authentication.AuthenticationSource;
import com.voxlearning.alps.webmvc.support.authentication.LoginUserInfo;
import com.voxlearning.alps.webmvc.support.authentication.handler.AbstractAuthenticationHandler;
import com.voxlearning.alps.webmvc.support.authentication.helper.AuthenticationHandlerHelper;
import com.voxlearning.alps.webmvc.support.authentication.utils.AuthSessionMappingInfo;
import com.voxlearning.alps.webmvc.support.authentication.utils.SessionKeyUtils;
import com.voxlearning.alps.webmvc.support.context.UtopiaHttpRequestContext;

import javax.inject.Named;
import java.util.List;
import java.util.Map;

/**
 * @author changyuan
 * @since 2016/12/28
 */
@Named
public class SessionAuthenticationHandler extends AbstractAuthenticationHandler {

    private final static String APP_KEY = "changyuan_hen_22";
    private final static String SESSION_KEY = "token";

    private final static String COOKIE_NAME_AUTH = "voxauth";
    private final static String COOKIE_NAME_AUTH_SESSION = "va_sess";
    private final static String COOKIE_NAME_UID = "uid";

    @Override
    protected void initilizeAuthHandlerHelper(List<AuthenticationHandlerHelper> helpers) {
        helpers.add(new AuthenticationHandlerHelper() {
            @Override
            public LoginUserInfo getLoginUserInfo(UtopiaHttpRequestContext context) {
                String sessionStr = context.getRequest().getParameter(SESSION_KEY);
                if (StringUtils.isNotBlank(sessionStr)) {// 有session key，走session验证模式
                    try {
                        AuthSessionMappingInfo authSessionMappingInfo = SessionKeyUtils.decodeFromSessionKey(APP_KEY, sessionStr);
                        return new LoginUserInfo(authSessionMappingInfo.getUserId(), authSessionMappingInfo.getRoleTypes(), authSessionMappingInfo.getPassword());
                    } catch (Exception e) {
                        logger.warn("Decode from sessionKey error, ex:{}, msg:{}", e.getMessage(), e);
                        return null;
                    }
                } else {// 没有session key，cookie验证，这是逗比学松的需求
                    CookieManager cookieManager = context.getCookieManager();
                    String cookieValue = cookieManager.getCookieDecrypt(COOKIE_NAME_AUTH_SESSION, null);
                    AuthCookieMappingInfo mappingInfo = AuthCookieMappingInfo.parse(cookieValue);
                    if (mappingInfo == null) {
                        // 如果没有 session cookie， 则使用“记住我”的cookie
                        cookieValue = cookieManager.getCookieDecrypt(COOKIE_NAME_AUTH, null);
                        mappingInfo = AuthCookieMappingInfo.parse(cookieValue);

                        // 如果记住我的cookie有效，则更新 session cookie ，用户是从新会话开始访问的
                        if (mappingInfo != null) {
                            cookieManager.setCookieEncryptTLD(COOKIE_NAME_AUTH_SESSION, cookieValue, -1);
                            cookieManager.setCookieTLD(COOKIE_NAME_UID, mappingInfo.getUserId().toString(), -1);
                            context.setAuthenticationSource(AuthenticationSource.NEW_SESSION);
                        }
                    } else {
                        // 已经成功地从cookie中解析出了AutoCookieMappingInfo
                        // 将尝试直接从cookie认证登陆了，设置context的source
                        context.setAuthenticationSource(AuthenticationSource.COOKIE);
                    }

                    if (mappingInfo != null) {
                        return new LoginUserInfo(mappingInfo.getUserId(), mappingInfo.getRoleTypes(), mappingInfo.getPassword());
                    } else {
                        return null;
                    }
                }
            }

            @Override
            public void storeLoginUserInfo(UtopiaHttpRequestContext context, Map<String, Object> authSuccessInfo) {
            }

            @Override
            public void clearLoginUserInfo(UtopiaHttpRequestContext context) {

            }
        });
    }

    @Override
    protected boolean isLoginRequest(String servletPath) {
        return false;
    }

    @Override
    protected boolean isLogoutRequest(String servletPath) {
        return false;
    }

    @Override
    protected int authentication(UtopiaHttpRequestContext context, Map<String, Object> authInfo) {
        return 0;
    }
}
