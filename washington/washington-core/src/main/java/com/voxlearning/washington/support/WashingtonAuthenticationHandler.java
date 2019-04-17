/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.washington.support;

import com.voxlearning.alps.annotation.meta.RoleType;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.lang.util.MobileRule;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.runtime.TopLevelDomain;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.alps.webmvc.cookie.AuthCookieMappingInfo;
import com.voxlearning.alps.webmvc.cookie.AuthCookieSnapshot;
import com.voxlearning.alps.webmvc.support.authentication.AuthenticationHandler;
import com.voxlearning.alps.webmvc.support.context.UtopiaHttpRequestContext;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.OperationSourceType;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.business.consumer.MiscServiceClient;
import com.voxlearning.utopia.service.footprint.client.AsyncFootprintServiceClient;
import com.voxlearning.utopia.service.invitation.client.AsyncInvitationServiceClient;
import com.voxlearning.utopia.service.user.api.ThirdPartyService;
import com.voxlearning.utopia.service.user.api.constants.AccountStatus;
import com.voxlearning.utopia.service.user.api.constants.LoginMethod;
import com.voxlearning.utopia.service.user.api.constants.UserRecordMode;
import com.voxlearning.utopia.service.user.api.entities.InviteHistory;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserExtAttribute;
import com.voxlearning.utopia.service.user.api.mappers.UserSecurity;
import com.voxlearning.utopia.service.user.client.UserLoginServiceClient;
import com.voxlearning.utopia.service.user.consumer.*;
import com.voxlearning.utopia.service.wechat.client.WechatLoaderClient;
import com.voxlearning.washington.cache.WashingtonCacheSystem;
import com.voxlearning.washington.helpers.SsoHelper;

import javax.inject.Inject;
import javax.servlet.ServletException;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.alps.annotation.meta.RoleType.ROLE_STUDENT;
import static com.voxlearning.alps.annotation.meta.RoleType.ROLE_TEACHER;
import static com.voxlearning.utopia.api.legacy.MemcachedKeyConstants.MULTI_USER_LOGIN_PREFIX;
import static com.voxlearning.utopia.service.user.api.constants.InvitationType.TEACHER_INVITE_TEACHER_SMS;

public class WashingtonAuthenticationHandler extends AuthenticationHandler {

    @Inject private RaikouSystem raikouSystem;

    @Inject private AsyncFootprintServiceClient asyncFootprintServiceClient;
    @Inject private AsyncInvitationServiceClient asyncInvitationServiceClient;

    @Inject
    private MiscServiceClient miscServiceClient;
    @Inject
    private UserLoaderClient userLoaderClient;
    @Inject
    private StudentLoaderClient studentLoaderClient;
    @Inject
    private TeacherLoaderClient teacherLoaderClient;
    @Inject
    private UserLoginServiceClient userLoginServiceClient;
    @Inject
    private UserServiceClient userServiceClient;
    @Inject
    private WashingtonCacheSystem washingtonCacheSystem;
    @Inject
    private WechatLoaderClient wechatLoaderClient;
    @Inject
    private SensitiveUserDataServiceClient sensitiveUserDataServiceClient;
    @Inject
    private SsoHelper ssoHelper;

    @ImportService(interfaceClass = ThirdPartyService.class) private ThirdPartyService thirdPartyService;

    @Override
    protected boolean isLoginRequest(String servletPath) {
        return StringUtils.equals("/j_spring_security_check", servletPath);
    }

    @Override
    protected boolean isLogoutRequest(String servletPath) {
        return StringUtils.equals("/ucenter/logout.vpage", servletPath) ||
                StringUtils.equals("/ucenter/logout.shtml", servletPath);
    }

    @Override
    protected boolean processLoginRequest(UtopiaHttpRequestContext context) throws ServletException, IOException {
        //处理登录 -- j_username:30002, j_password:1, _spring_security_remember_me:on
        String token = StringUtils.defaultString(context.getRequest().getParameter("j_username"));
        String password = StringUtils.defaultString(context.getRequest().getParameter("j_password"));
        String rememberMe = StringUtils.defaultString(context.getRequest().getParameter("_spring_security_remember_me"));
        // 当下面两个参数不为空时，表示手机号码和密码都一致时选择登陆哪个用户的请求
        String userType = StringUtils.defaultString(context.getRequest().getParameter("j_userType"));
        String key = StringUtils.defaultString(context.getRequest().getParameter("j_key"));
        // 初始化上次输入密码登陆的cookie
        initLastUsingPwdLoginDate((WashingtonRequestContext) context);
        if (StringUtils.isBlank(userType) && StringUtils.isBlank(key)) {
            return firstRoundAuthentication((WashingtonRequestContext) context, token, password, rememberMe);
        } else { // 处理用户名密码都一样的用户的二次登陆
            return secondRoundAuthentication((WashingtonRequestContext) context, token, key, userType);
        }
    }

    @Override
    protected boolean processLogoutRequest(UtopiaHttpRequestContext context) throws ServletException, IOException {
        // 一个完整的登录流程，去用户中心再次logout
        // 同二级域名下回清两次cookie，不过先这样吧
        String url;
        String webAppBaseUrl = context.getWebAppBaseUrl();
        if (!StringUtils.equals(ProductConfig.getUcenterUrl(), webAppBaseUrl)) {
            // 如果当前不是在用户中心，提供一个返回当前站点的url，好让用户做切换账号之类的操作更加方便
            url = UrlUtils.buildUrlQuery(
                    ProductConfig.getUcenterUrl() + "/ucenter/logout.vpage",
                    MiscUtils.m("returnURL", webAppBaseUrl));
        } else {
            url = ProductConfig.getUcenterUrl() + "/ucenter/logout.vpage";
        }
        context.getResponse().sendRedirect(url);
        return true;
    }

    @Override
    protected void recordUserLogin(UtopiaHttpRequestContext context) throws ServletException, IOException {

        // 检查用户的登录态
        User curUser = raikouSystem.loadUser(context.getUserId());
        if (curUser == null) {
            return;
        }

        UserExtAttribute userExtAttribute = null;
        if (curUser.isStudent()) {
            userExtAttribute = studentLoaderClient.loadStudentExtAttribute(curUser.getId());
        } else if (curUser.isTeacher()) {
            userExtAttribute = teacherLoaderClient.loadTeacherExtAttribute(curUser.getId());
            if (userExtAttribute != null && AccountStatus.PAUSE == userExtAttribute.getAccountStatus() && !StringUtils.containsAny(context.getRequest().getServletPath(), "/v1/", "/v2/", "/v3/", "pauseAccount", "onlinecs_new", "sendTCPWcode")) {
                if (!HttpRequestContextUtils.isRequestAjax(context.getRequest())) {
                    context.getResponse().sendRedirect(ProductConfig.getUcenterUrl() + "/pauseAccount.vpage");
                    return;
                }
            }
        }

        if (userExtAttribute != null && (userExtAttribute.isForbidden() || userExtAttribute.isFreezing())) {
            context.cleanupAuthenticationStates();
            context.getResponse().sendRedirect("/login.vpage#error=true&type=forbidden");
            return;
        }

        switch (context.getAuthenticationSource()) {
            case NEW_SESSION: {
                asyncFootprintServiceClient.getAsyncFootprintService().postUserLogin(context.getUserId(),
                        context.getRealRemoteAddress(),
                        UserRecordMode.VALIDATE,
                        OperationSourceType.pc,
                        false);
                break;
            }
            case COOKIE: {
                // FIXME 客户端相关一些请求，比如心跳检测，网络协议啥的，统统忽略
                String url = context.getOriginalRequest().getRequestURI();
                if (!url.contains("/client/")) {
                    asyncFootprintServiceClient.getAsyncFootprintService().postUserLogin(context.getUserId(),
                            context.getRealRemoteAddress(),
                            UserRecordMode.VALIDATE,
                            OperationSourceType.pc,
                            true);
                }

                break;
            }
            default: {
                break;
            }
        }

    }

    @Override
    protected void needLogin(UtopiaHttpRequestContext context) throws ServletException, IOException {
        if (HttpRequestContextUtils.isRequestAjax(context.getRequest())) {
            //ajax过程中，需要返回json信息
            context.getResponse().setContentType("application/json;charset=UTF-8");

            //在 GET 请求中，防止一些违规的小区宽带的缓存系统把内容给缓存了
            context.getResponse().addHeader("Cache-Control", "no-store, no-cache");
            context.getResponse().addHeader("Pragma", "no-cache");
            context.getResponse().setDateHeader("Expires", 1L);

            context.getResponse().getWriter().write(JsonUtils.toJson(MapMessage.errorMessage("请返回首页重新登录")));
        } else {
            //非ajax
            if (context.getUserId() == null) {
                String returnUrl = ssoHelper.generateSsoReturnUrl(context,
                        context.getRequest().getServletPath(), context.getRequest().getQueryString());
                if (returnUrl == null) {
                    context.getResponse().getWriter().write(JsonUtils.toJson(MapMessage.errorMessage("请返回首页重新登录")));
                } else {
                    context.getResponse().sendRedirect(returnUrl);
                }
            } else {
                // 用户已经有cookie了，实际上只是因为role type权限检查未通过
                // 此时不再走sso登录流程，直接跳转到用户中心的login.vpage
                String returnUrl = context.getRequest().getServletPath();
                //加入参数
                if (StringUtils.isNotEmpty(context.getRequest().getQueryString()))
                    returnUrl += "?" + context.getRequest().getQueryString();
                context.getResponse().sendRedirect(
                        UrlUtils.buildUrlQuery(ProductConfig.getUcenterUrl() + "/login.vpage",
                                MiscUtils.m("returnURL", returnUrl)));
            }
        }
    }

    @Override
    protected void onProcessReturnTrue(UtopiaHttpRequestContext context) throws ServletException, IOException {
        bindUser((WashingtonRequestContext) context);
    }

    private void updateContextByAuthCookieContent(WashingtonRequestContext context, AuthCookieSnapshot snapshot)
            throws ServletException, IOException {
        if (snapshot.getMappingInfo() != null) {
            AuthCookieMappingInfo mappingInfo = snapshot.getMappingInfo();
            List<RoleType> roleTypes = mappingInfo.getRoleTypes();
            if (roleTypes == null || roleTypes.isEmpty()) {
                roleTypes = Collections.singletonList(RoleType.ROLE_ANONYMOUS);
            }
            context.setUserId(mappingInfo.getUserId());
            context.setRoleTypes(roleTypes);
            context.setSaltedPassword(mappingInfo.getPassword());
            if (snapshot.isNeedRecordUserLogin()) {
                asyncFootprintServiceClient.getAsyncFootprintService().postUserLogin(context.getUserId(),
                        context.getRealRemoteAddress(),
                        UserRecordMode.LOGIN,
                        OperationSourceType.pc);
            }
        } else {
            context.setUserId(null);
            context.setRoleTypes(new ArrayList<>());
        }
    }

    private boolean firstRoundAuthentication(WashingtonRequestContext context,
                                             String token,
                                             String password,
                                             String rememberMe) throws ServletException, IOException {
        List<UserSecurity> userSecurities = userLoaderClient.loadUserSecurities(token, null);
        // 如果没有找到人，判断一下token是不是手机号码，如果是手机号码，看看是不是邀请的手机
        if (CollectionUtils.isEmpty(userSecurities) && MobileRule.isMobile(token)) {
            Set<Long> inviteeIds = asyncInvitationServiceClient.loadByMobile(token)
                    .originalLocationsAsList()
                    .stream()
                    .filter(t -> t.getType() == TEACHER_INVITE_TEACHER_SMS)
                    .filter(t -> t.getInviteeId() != 0)
                    .map(InviteHistory.Location::getInviteeId)
                    .collect(Collectors.toSet());
            userSecurities = userLoaderClient.loadUserSecurities(inviteeIds);
        }
        List<UserSecurity> candidates = authenticate(userSecurities, token, password, context.getRealRemoteAddr());
        if (candidates.isEmpty()) {
            onAuthenticationFailure(context, userSecurities, token);
        } else if (candidates.size() == 1) { // 如果只找到一个人，登陆啦~~~

            int expire = -1;
            if ("on".equals(rememberMe)) {
                expire = 14 * 86400; // 之前 spring 也是 14 天过期
            }
            UserSecurity us = candidates.get(0);

            context.saveAuthenticationStates(expire, us);
            updateContextByAuthCookieContent(context, new AuthCookieSnapshot(
                    new AuthCookieMappingInfo(us.getUserId(), us.getRoleTypes(), us.getPassword()),
                    true));
            // 如果通过输入密码登录成功，更新相关cookie
            updateLastUsingPwdLoginDate(context);
            onAuthenticationSuccess(context);
            //如果是短信邀请进来的，默认绑定手机号
            miscServiceClient.bindInvitedTeacherMobile(candidates.get(0).getUserId());

        } else { // 如果找到多个人，*>__<*，将候选人信息放入缓存，让用户选择登陆哪个用于
            String mckey = RandomUtils.randomString(24);
            Map<String, Object> map = new HashMap<>();
            map.put("token", token);
            map.put("password", password);
            map.put("rememberMe", rememberMe);
            map.put("candidates", candidates);
            String dataKey = StringUtils.defaultString(context.getRequest().getParameter("dataKey"));
            if (StringUtils.isNotBlank(dataKey)) {
                map.put("dataKey", dataKey);
            }

            Boolean ret = washingtonCacheSystem.CBS.unflushable.set(MULTI_USER_LOGIN_PREFIX + mckey, 180, map);
            if (Boolean.TRUE.equals(ret)) {
                context.getLastUserNameCookieManager().setLastUserNameCookie(-1, token);
            } else {
                onAuthenticationFailure(context, token);
            }
            context.getResponse().sendRedirect("redirect:/ucenter/authorize/selectuser.vpage?key=" + mckey);
        }
        return true;
    }

    private boolean secondRoundAuthentication(WashingtonRequestContext context,
                                              String token,
                                              String key,
                                              String userType) throws ServletException, IOException {
        CacheObject<Map<String, Object>> cacheObject = washingtonCacheSystem.CBS.unflushable.get(MULTI_USER_LOGIN_PREFIX + key);
        if (cacheObject == null) {
            onAuthenticationFailure(context, token);
            return true;
        }
        Map<String, Object> map = cacheObject.getValue();
        if (map == null || !map.containsKey("token") || !map.containsKey("password") || !map.containsKey("rememberMe")) {
            onAuthenticationFailure(context, token);
            return true;
        }
        UserSecurity userSecurity = authenticateWithUserType(ConversionUtils.toString(map.get("token")), ConversionUtils.toString(map.get("password")), userType, context.getRealRemoteAddr());
        if (null == userSecurity) {
            onAuthenticationFailure(context, token);
            return true;
        }
        int expire = -1;
        if ("on".equals(ConversionUtils.toString(map.get("rememberMe")))) {
            expire = 14 * 86400; // 之前 spring 也是 14 天过期
        }

        context.saveAuthenticationStates(expire, userSecurity);

        updateContextByAuthCookieContent(context, new AuthCookieSnapshot(
                new AuthCookieMappingInfo(userSecurity.getUserId(), userSecurity.getRoleTypes(), userSecurity.getPassword()),
                true));
        // 如果通过输入密码登录成功，更新相关cookie
        updateLastUsingPwdLoginDate(context);
        onAuthenticationSuccess(context);
        //如果是短信邀请进来的，默认绑定手机号
        miscServiceClient.bindInvitedTeacherMobile(userSecurity.getUserId());

        return true;
    }

    private UserSecurity authenticateWithUserType(String token, String password, String userType, String ip) {
        List<UserSecurity> securities = userLoaderClient.loadUserSecurities(token, UserType.valueOf(userType));
        UserSecurity userSecurity = MiscUtils.firstElement(securities);
        if (userSecurity == null || !userSecurity.match(password)) {
            asyncFootprintServiceClient.createLoginDetail(userSecurity, token, false, ip);
            //记录用户最后登录失败时间
            if (userSecurity != null) {
//                userServiceClient.recordUserLoginFailure2(userSecurity.getUserId());
                asyncFootprintServiceClient.getAsyncFootprintService().recordUserLoginFailure(userSecurity.getUserId());
            }
            return null;
        }

        // 认证成功记录登陆信息
        asyncFootprintServiceClient.createLoginDetail(userSecurity, token, true, ip);
        return userSecurity;
    }

    private List<UserSecurity> authenticate(List<UserSecurity> userSecurities, String token, String password, String ip) {
        List<UserSecurity> candidates = new ArrayList<>();
        if (userSecurities.isEmpty()) {
            return Collections.emptyList();
        } else if (userSecurities.size() == 1) {
            UserSecurity userSecurity = userSecurities.get(0);

            // 临时密码校验 xuesong.zhang 2015-11-19
            Long userId = userSecurity.getUserId();
            if (StringUtils.isBlank(password) || !StringUtils.equalsIgnoreCase(userLoaderClient.loadUserTempPassword(userId), password)) {
                if (!userSecurity.match(password)) {
                    asyncFootprintServiceClient.createLoginDetail(userSecurity, token, false, ip);
                    //记录用户最后登录失败时间
//                    userServiceClient.recordUserLoginFailure2(userSecurity.getUserId());
                    asyncFootprintServiceClient.getAsyncFootprintService().recordUserLoginFailure(userSecurity.getUserId());
                    return Collections.emptyList();
                }
            }

            // 认证成功记录登陆信息
            asyncFootprintServiceClient.createLoginDetail(userSecurity, token, true, ip);
            candidates.add(userSecurity);
            return candidates;
        } else {
            for (UserSecurity userSecurity : userSecurities) {
                if (userSecurity.match(password)) {
                    candidates.add(userSecurity);
                }
            }
            if (candidates.isEmpty()) {
                for (UserSecurity userSecurity : userSecurities) {
                    asyncFootprintServiceClient.createLoginDetail(userSecurity, token, false, ip);
                    //记录用户最后登录失败时间
//                    userServiceClient.recordUserLoginFailure2(userSecurity.getUserId());
                    asyncFootprintServiceClient.getAsyncFootprintService().recordUserLoginFailure(userSecurity.getUserId());
                }
                return Collections.emptyList();
            } else {
                return candidates;
            }
        }
    }

    private void onAuthenticationSuccess(WashingtonRequestContext context) throws IOException {
        // AJAX 可能用户绑定家长直接从学生帐号登录
        if (HttpRequestContextUtils.isRequestAjax(context.getRequest())) {
            context.getResponse().setContentType("application/json;charset=UTF-8");
            context.getResponse().getWriter().write(JsonUtils.toJson(MiscUtils.<String, Object>map().add("success", true).add("userId", context.getUserId())));
        } else {
            //跳转回登录之前的页面
            String returnURL = context.getRequest().getParameter("returnURL");
            String resolvedURL = resolveReturnURL(returnURL);
            context.getResponse().sendRedirect(resolvedURL);
        }
    }

    private String resolveReturnURL(String returnURL) {
        if (StringUtils.isBlank(returnURL)) {
            return "redirect:/ucenter/home.vpage";
        }
        returnURL = returnURL.trim();
        if (StringUtils.startsWith(returnURL, "//")) {
            // unsupported malformed, redirect to home
            logger.warn("Unsupported malformed returnURL: {}", returnURL);
            return "redirect:/ucenter/home.vpage";
        }
        if (StringUtils.startsWithIgnoreCase(returnURL, "http://") ||
                StringUtils.startsWithIgnoreCase(returnURL, "https://")) {
            try {
                URL url = new URL(returnURL);
                String host = url.getHost();
                String tld = TopLevelDomain.getTopLevelDomain();
                if (StringUtils.equalsIgnoreCase(host, tld) ||
                        StringUtils.endsWithIgnoreCase(host, "." + tld)) {
                    return returnURL;
                } else {
                    // not in our domain, redirect to home
                    logger.warn("Redirect denied: {}", returnURL);
                    return "redirect:/ucenter/home.vpage";
                }
            } catch (Exception ex) {
                logger.warn("Failed to resolve returnURL: {}, the exception message is: {}",
                        returnURL, ex.getMessage());
                return "/ucenter/home.vpage";
            }
        } else {
            // should be relative path, redirect directly
            return returnURL;
        }
    }

    private void onAuthenticationFailure(WashingtonRequestContext context, List<UserSecurity> candidates, String token) throws IOException {
        // AJAX 可能用户绑定家长直接从学生帐号登录
        if (HttpRequestContextUtils.isRequestAjax(context.getRequest())) {
            // 返回JSON字符串
            context.getResponse().setContentType("application/json;charset=UTF-8");
            context.getResponse().getWriter().write(JsonUtils.toJson(MiscUtils.<String, Object>map().add("success", false).add("userId", 0)));
        } else {
            if (processAccountBindError(context)) {
                return;
            }

            // 使用手机号+密码登录失败时，如果老师在7天内登录过，不弹出“找回密码”提示框
            if (LoginMethod.parse(token) == LoginMethod.MOBILE) {
                UserSecurity userSecurity = candidates.stream()
                        .filter(us -> ROLE_TEACHER == MiscUtils.firstElement(us.getRoleTypes()))
                        .findFirst().orElse(null);
                if (userSecurity != null) {
                    User user = raikouSystem.loadUser(userSecurity.getUserId());
                    if (user != null) {
//                        Date lastLoginTime = userLoaderClient.findUserLastLoginTime(user);
                        Date lastLoginTime = userLoginServiceClient.findUserLastLoginTime(user.getId());
                        if (lastLoginTime != null && lastLoginTime.after(DateUtils.calculateDateDay(new Date(), -7))) {
                            context.getLastUserNameCookieManager().setLastUserNameCookie(-1, token);
                            context.getResponse().sendRedirect("redirect:/login.vpage#error=true&record=true");
                            return;
                        }
                    }
                }
            }

            // 如果能定位到用户，并且绑定了手机，弹窗式找回密码（只适用于教师和学生）
            String redirect = "/login.vpage#error=true";
            List<UserSecurity> users = candidates.stream()
                    .filter(us -> Arrays.asList(ROLE_STUDENT, ROLE_TEACHER).contains(MiscUtils.firstElement(us.getRoleTypes())))
                    .collect(Collectors.toList());
            // 如果是token是账号，users肯定只有一个，如果token是手机号，users中任意一个都是绑定手机的，所以只需要取users的第一个判断即可
            UserSecurity us = MiscUtils.firstElement(users);
            if (us != null) {
                //UserAuthentication ua = userLoaderClient.loadUserAuthentication(us.getUserId());
                String mobile = sensitiveUserDataServiceClient.loadUserMobileObscured(us.getUserId());
                if (!StringUtils.isEmpty(mobile)) {
                    redirect += "&type=mobile&mobile=" + mobile + "&captchaToken=" + RandomUtils.randomString(24);
                } else if (MiscUtils.firstElement(us.getRoleTypes()) == ROLE_STUDENT &&
                        wechatLoaderClient.isStudentBinding(us.getUserId())) {
                    redirect += "&type=wechat";
                }
            }

            context.getLastUserNameCookieManager().setLastUserNameCookie(-1, token);
            context.getResponse().sendRedirect(redirect);
        }
    }

    private void onAuthenticationFailure(WashingtonRequestContext context, String token) throws IOException {
        // AJAX 可能用户绑定家长直接从学生帐号登录
        if (HttpRequestContextUtils.isRequestAjax(context.getRequest())) {
            // 返回JSON字符串
            context.getResponse().setContentType("application/json;charset=UTF-8");
            context.getResponse().getWriter().write(JsonUtils.toJson(MiscUtils.<String, Object>map().add("success", false).add("userId", 0)));
        } else {
            if (processAccountBindError(context)) {
                return;
            }
            context.getLastUserNameCookieManager().setLastUserNameCookie(-1, token);
            context.getResponse().sendRedirect("redirect:/login.vpage#error=adult");
        }
    }

    private void initLastUsingPwdLoginDate(WashingtonRequestContext context) {
        if (StringUtils.isBlank(context.getCookieManager().getCookie("lupld", ""))) {
            int exp = 86400 * 7;
            context.getCookieManager().setCookie("lupld", "1", exp);
        }
    }

    private void updateLastUsingPwdLoginDate(WashingtonRequestContext context) {
        int exp = 86400 * 7;
        context.getCookieManager().setCookie("lupld", "1", exp);
    }

    public void resetAuthCookie(WashingtonRequestContext context, int expire) {
        List<UserSecurity> securities = userLoaderClient.loadUserSecurities(context.getCurrentUser().getId().toString(), context.getCurrentUser().fetchUserType());
        UserSecurity userSecurity = MiscUtils.firstElement(securities);
        if (null != userSecurity) {
            context.saveAuthenticationStates(expire, userSecurity);
        }
    }

    private void bindUser(WashingtonRequestContext context) {
        String dataKey = StringUtils.defaultString(context.getRequest().getParameter("dataKey"));
        Long userId = context.getUserId();
        if (StringUtils.isNotBlank(dataKey) && userId != null) {
            Map map = washingtonCacheSystem.CBS.unflushable.load(dataKey);
            if (map == null) {
                return;
            }

            String sourceName = String.valueOf(map.get("source"));
            String sourceUid = String.valueOf(map.get("sourceUid"));
            String sourceUserName = String.valueOf(map.get("userName"));
            thirdPartyService.persistLandingSource(sourceName, sourceUid, sourceUserName, context.getUserId());
            washingtonCacheSystem.CBS.unflushable.delete(dataKey);
        }
    }

    private boolean processAccountBindError(WashingtonRequestContext context) throws IOException {
        String referer = context.getRequest().getHeader("Referer");
        if (StringUtils.contains(referer, "/ssologin") || StringUtils.contains(referer, "/qq/authorize")) {
            String dataKey = StringUtils.defaultString(context.getRequest().getParameter("dataKey"));
            context.getResponse().sendRedirect("redirect:/ssologinerror.vpage?dataKey=" + dataKey);
            return true;
        }
        return false;
    }

}
