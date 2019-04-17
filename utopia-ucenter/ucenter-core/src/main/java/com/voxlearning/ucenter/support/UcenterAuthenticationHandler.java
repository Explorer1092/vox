/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.ucenter.support;

import com.nature.commons.lang.util.StringUtil;
import com.voxlearning.alps.annotation.meta.PasswordState;
import com.voxlearning.alps.annotation.meta.RoleType;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.MobileRule;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.runtime.TopLevelDomain;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.alps.webmvc.cookie.AuthCookieMappingInfo;
import com.voxlearning.alps.webmvc.cookie.AuthCookieSnapshot;
import com.voxlearning.alps.webmvc.support.authentication.AuthenticationHandler;
import com.voxlearning.alps.webmvc.support.authentication.AuthenticationSource;
import com.voxlearning.alps.webmvc.support.context.UtopiaHttpRequestContext;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.ucenter.cache.UcenterWebCacheSystem;
import com.voxlearning.ucenter.service.user.AccountWebappService;
import com.voxlearning.ucenter.support.context.UcenterRequestContext;
import com.voxlearning.utopia.api.constant.AppAuditAccounts;
import com.voxlearning.utopia.api.constant.OperationSourceType;
import com.voxlearning.utopia.core.helper.StringRegexUtils;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.config.api.constant.ConfigCategory;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.footprint.client.AsyncFootprintServiceClient;
import com.voxlearning.utopia.service.invitation.client.AsyncInvitationServiceClient;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.user.api.ThirdPartyService;
import com.voxlearning.utopia.service.user.api.constants.AccountStatus;
import com.voxlearning.utopia.service.user.api.constants.LoginMethod;
import com.voxlearning.utopia.service.user.api.constants.UserRecordMode;
import com.voxlearning.utopia.service.user.api.constants.UserWebSource;
import com.voxlearning.utopia.service.user.api.entities.*;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.api.mappers.ClazzTeacher;
import com.voxlearning.utopia.service.user.api.mappers.UserSecurity;
import com.voxlearning.utopia.service.user.client.AsyncUserServiceClient;
import com.voxlearning.utopia.service.user.client.UserLoginServiceClient;
import com.voxlearning.utopia.service.user.consumer.*;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsUserRef;
import com.voxlearning.utopia.service.vendor.consumer.VendorLoaderClient;

import javax.inject.Inject;
import javax.servlet.ServletException;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.alps.annotation.meta.RoleType.ROLE_STUDENT;
import static com.voxlearning.alps.annotation.meta.RoleType.ROLE_TEACHER;
import static com.voxlearning.utopia.api.legacy.MemcachedKeyConstants.MULTI_USER_LOGIN_PREFIX;
import static com.voxlearning.utopia.service.user.api.constants.InvitationType.TEACHER_INVITE_TEACHER_SMS;

/**
 * @author changyuan.liu
 * @since 2015.12.7
 */
public class UcenterAuthenticationHandler extends AuthenticationHandler {

    @Inject private RaikouSystem raikouSystem;
    @Inject private CommonConfigServiceClient commonConfigServiceClient;

    @Inject private AsyncFootprintServiceClient asyncFootprintServiceClient;
    @Inject private AsyncInvitationServiceClient asyncInvitationServiceClient;
    @Inject private AsyncUserServiceClient asyncUserServiceClient;

    @Inject UserLoaderClient userLoaderClient;
    @Inject UserLoginServiceClient userLoginServiceClient;
    @Inject UserServiceClient userServiceClient;
    @Inject TeacherLoaderClient teacherLoaderClient;

    @Inject AccountWebappService accountWebappService;

    @Inject UcenterWebCacheSystem ucenterWebCacheSystem;

    @Inject SensitiveUserDataServiceClient sensitiveUserDataServiceClient;

    @Inject VendorLoaderClient vendorLoaderClient;
    @Inject StudentLoaderClient studentLoaderClient;
    @Inject UserAggregationLoaderClient userAggregationLoaderClient;
    @Inject SchoolExtServiceClient schoolExtServiceClient;


    @ImportService(interfaceClass = ThirdPartyService.class) private ThirdPartyService thirdPartyService;

    // authentication related url
    private final static String LOGIN_URL = "/login.vpage";
    private final static String HOMEPAGE_URL = "/ucenter/home.vpage";
    private final static String JUNIOR_HOMEPAGE_URL = "/";
    private final static String KUAILEXUE_HOMEPAGE_URL = "/";
    private final static String LOGIN_REQUEST_STR = "/j_spring_security_check";
    private final static String LOGOUT_REQUEST_STR_1 = "/ucenter/logout.vpage";
    private final static String LOGOUT_REQUEST_STR_2 = "/ucenter/logout.shtml";
    private final static String LOGIN_MULTI_USER_SELECTOR_PAGE = "redirect:/ucenter/authorize/selectuser.vpage";

    // authentication related field name
    private final static String TOKEN_PARAM = "j_username";
    private final static String PWD_PARAM = "j_password";
    private final static String REMEMBER_ME_PARAM = "_spring_security_remember_me";
    private final static String USER_TYPE_PARAM = "j_userType";
    private final static String KEY_PARAM = "j_key";

    // cookie related
    private final static String LAST_USING_PWD_LOGIN_DATE_COOKIE = "lupld";// 最后使用密码登录日期, last user password login date

    // date const
    private final static int ONE_WEEK_EXP = 86400 * 7;
    private final static int TWO_WEEK_EXP = 86400 * 14;

    // http const
    private final static String REFERER_HEADER = "Referer";
    private final static String JSON_CONTENT_TYPE = "application/json;charset=UTF-8";

    // sso login related
    private final static String SSO_LOGIN_URL = "/ssologin";
    private final static String QQ_LOGIN_URL = "/qq/authorize";
    private final static String SSO_LOGIN_ERROR_URL = "redirect:/ssologinerror.vpage";
    private final static String SSO_KEY_PARAM = "dataKey";
    private final static String RETURN_URL_PARAM = "returnURL";

    private static final String SSZ_REDIRECT_URL = "/redirector/apps/go.vpage?app_key=Shensz";


    @Override
    protected boolean isLoginRequest(String servletPath) {
        return StringUtils.equals(servletPath, LOGIN_REQUEST_STR);
    }

    @Override
    protected boolean isLogoutRequest(String servletPath) {
        return StringUtils.equals(servletPath, LOGOUT_REQUEST_STR_1)
                || StringUtils.equals(servletPath, LOGOUT_REQUEST_STR_2);
    }

    @Override
    protected boolean processLoginRequest(UtopiaHttpRequestContext context) throws ServletException, IOException {
        //处理登录 -- j_username:30002, j_password:1, _spring_security_remember_me:on
        String token = StringUtils.defaultString(context.getRequest().getParameter(TOKEN_PARAM));
        String password = StringUtils.defaultString(context.getRequest().getParameter(PWD_PARAM));
        String rememberMe = StringUtils.defaultString(context.getRequest().getParameter(REMEMBER_ME_PARAM));
        // 当下面两个参数不为空时，表示存在多个用户符合条件，需要用户选择用户类型登录
        String userType = StringUtils.defaultString(context.getRequest().getParameter(USER_TYPE_PARAM));
        String key = StringUtils.defaultString(context.getRequest().getParameter(KEY_PARAM));
        // 初始化上次输入密码登陆的cookie
        UcenterRequestContext ucenterRequestContext = (UcenterRequestContext) context;
        initLastUsingPwdLoginDate(ucenterRequestContext);
        if (StringUtils.isBlank(userType) && StringUtils.isBlank(key)) {
            // 只指定了token和password，第一轮登录验证
            return firstRoundAuthentication(ucenterRequestContext, token, password, rememberMe);
        } else {
            return secondRoundAuthentication(ucenterRequestContext, token, key, userType);
        }
    }

    @Override
    protected boolean processLogoutRequest(UtopiaHttpRequestContext context) throws ServletException, IOException {
        // FIXME 需要单点退出子站点，暂时先简单的放在这里
        // FIXME 注意，这并不是一个规范的做法
        // FIXME 平台共用同一cookie，所以只是简单的清除中学cookie
        // FIXME 目前感觉规范的做法是做一个通用的logout页面，然后通过jsonp的形式调用各个子站点的logout
        // FIXME 这里要注意子站点调用sso退出和sso调用个子站点退出必须是分开的接口，避免循环调用
        context.getCookieManager().deleteCookieTLD("user_id");
        context.getCookieManager().deleteCookieTLD("last_login");
        context.getCookieManager().deleteCookieTLD("token");

        // 子站点的返回地址
        // 当用户是通过点击子站点的退出从而退出登录时，返回到子站点
        String returnURL = context.getRequest().getParameter("returnURL");
        context.getResponse().sendRedirect("redirect:" + (returnURL == null ? "/index.vpage" : returnURL));
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
            if (userExtAttribute != null && AccountStatus.PAUSE == userExtAttribute.getAccountStatus() && !StringUtils.containsAny(context.getRequest().getServletPath(), "pauseAccount", "onlinecs_new", "sendTCPWcode")) {
                context.getResponse().sendRedirect("/pauseAccount.vpage");
                return;
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
                asyncFootprintServiceClient.getAsyncFootprintService().postUserLogin(context.getUserId(),
                        context.getRealRemoteAddress(),
                        UserRecordMode.VALIDATE,
                        OperationSourceType.pc,
                        true);
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
            context.getResponse().setContentType(JSON_CONTENT_TYPE);

            //在 GET 请求中，防止一些违规的小区宽带的缓存系统把内容给缓存了
            context.getResponse().addHeader("Cache-Control", "no-store, no-cache");
            context.getResponse().addHeader("Pragma", "no-cache");
            context.getResponse().setDateHeader("Expires", 1L);

            context.getResponse().getWriter().write(JsonUtils.toJson(MapMessage.errorMessage("请返回首页重新登录")));
        } else {
            //非ajax
            //设置returnURL
            String returnUrl = context.getRequest().getServletPath();
            //加入参数
            if (StringUtils.isNotEmpty(context.getRequest().getQueryString()))
                returnUrl += "?" + context.getRequest().getQueryString();
            context.getResponse().sendRedirect(UrlUtils.buildUrlQuery("redirect:/login.vpage", MapUtils.map("returnURL", returnUrl)));
        }
    }

    @Override
    protected void onProcessReturnTrue(UtopiaHttpRequestContext context) throws ServletException, IOException {
        bindUser((UcenterRequestContext) context);
    }

    /**
     * 第一轮验证
     * 核对用户的token和password
     *
     * @param context
     * @param token
     * @param password
     * @param rememberMe
     * @return
     * @throws ServletException
     * @throws IOException
     */
    private boolean firstRoundAuthentication(UcenterRequestContext context,
                                             String token,
                                             String password,
                                             String rememberMe) throws ServletException, IOException {
        List<User> tempUsers = userLoaderClient.loadUsers(token, null);
        if (tempUsers == null) {
            tempUsers = Collections.emptyList();
        }
        Set<Long> userIds = tempUsers.stream().map(User::getId).collect(Collectors.toSet());
        Map<Long, UserAuthentication> uaList = userLoaderClient.loadUserAuthentications(userIds);
        tempUsers = tempUsers.stream().filter(p -> uaList.containsKey(p.getId())).collect(Collectors.toList());

        List<User> users = new ArrayList<>();
        tempUsers.forEach(user -> {
            UserAuthentication ua = uaList.get(user.getId());
            if (ua.getPwdState() != null && ua.getPwdState() == 0 && StringUtils.equals(UserWebSource.happy_study.name(), user.getWebSource())) {//是快乐学老用户并且第一次登录时
                // 调用远程认证,如果认证通过,则更新密码
                //type:"mobile","email","loginname"
                String url = ProductConfig.getKuailexueLoginValidationUrl() + "/login/validate";
                LoginMethod loginMethod = LoginMethod.parse(token);
                String type = "";
                if (loginMethod == LoginMethod.MOBILE) {
                    type = "mobile";
                } else if (loginMethod == LoginMethod.EMAIL) {
                    type = "email";
                } else {
                    type = "loginname";
                }
                Map<String, Object> params = MapUtils.m("type", type, "account", token, "passwd", password);
                String URL = UrlUtils.buildUrlQuery(url, params);
                AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().get(URL).execute();
                if (response.getStatusCode() == 200) {
                    String result = response.getResponseString();
                    if (Objects.equals(result, "true")) {
                        userServiceClient.changePassword(user, "123456", password);
                        user = raikouSystem.loadUser(user.getId());
                        users.add(user);
                    } else if (!StringUtils.equals(password, "123456")) {
                        users.add(user);// 为了临时密码校验，因为默认导入的密码是123456，所以只要不是该密码，均可以往后走，从而走临时密码逻辑
                    }
                } else if (!StringUtils.equals(password, "123456")) {
                    users.add(user);// 为了临时密码校验，因为默认导入的密码是123456，所以只要不是该密码，均可以往后走，从而走临时密码逻辑
                }
            } else {//不是快乐学老账号时
                users.add(user);
            }
        });

        List<UserSecurity> userSecurities = users.stream()
                .map(user -> {
                    UserSecurity userSecurity = new UserSecurity();
                    userSecurity.setUserId(user.getId());
                    userSecurity.setRealname(user.fetchRealname());
                    userSecurity.setPassword(uaList.get(user.getId()).getPassword());
                    userSecurity.setSalt(uaList.get(user.getId()).getSalt());
                    userSecurity.setRoleTypes(new ArrayList<>(userLoaderClient.loadUserRoles(user)));
                    return userSecurity;
                })
                .collect(Collectors.toCollection(ArrayList::new));


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

        // 学前学段不允许登陆PC
        MapMessage checkResult = checkInfantUser(userSecurities, context);
        if (!checkResult.isSuccess()) {
            return false;
        } else {
            userSecurities = (List<UserSecurity>) checkResult.get("userSecurities");
        }

        // 认证通过的用户信息
        List<UserSecurity> candidates = authenticate(userSecurities, token, password, context.getRealRemoteAddress());
        if (CollectionUtils.isEmpty(candidates)) {// 没有认证合格的用户
            onAuthenticationFailure(context, userSecurities, token);
        } else if (candidates.size() == 1) {// 如果只找到一个人，登陆啦~~~
            int expire = StringUtils.equals(rememberMe, "on") ? TWO_WEEK_EXP : -1;// 是否记住密码
            UserSecurity us = candidates.get(0);

            context.saveAuthenticationStates(expire, us);
            updateContextByAuthCookieContent(context, new AuthCookieSnapshot(
                    new AuthCookieMappingInfo(us.getUserId(), us.getRoleTypes(), us.getPassword()),
                    true));
            // 如果通过输入密码登录成功，更新相关cookie
            updateLastUsingPwdLoginDate(context);
            onLoginSuccess(context);
            // 处理用户登录
            accountWebappService.onUserLogin(candidates.get(0).getUserId());

        } else {// 如果找到多个人，*>__<*，将候选人信息放入缓存，让用户选择登陆哪个
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

            Boolean ret = ucenterWebCacheSystem.CBS.unflushable.set(MULTI_USER_LOGIN_PREFIX + mckey, 180, map);
            if (Boolean.TRUE.equals(ret)) {
                context.getLastUserNameCookieManager().setLastUserNameCookie(-1, token);
            } else {
                onAuthenticationFailure(context, token);
            }
            context.getResponse().sendRedirect(UrlUtils.buildUrlQuery(LOGIN_MULTI_USER_SELECTOR_PAGE, MapUtils.m("key", mckey)));
        }
        return true;
    }

    private boolean secondRoundAuthentication(UcenterRequestContext context,
                                              String token,
                                              String key,
                                              String userType) throws ServletException, IOException {
        CacheObject<Map<String, Object>> cacheObject = ucenterWebCacheSystem.CBS.unflushable.get(MULTI_USER_LOGIN_PREFIX + key);
        if (cacheObject == null) {
            onAuthenticationFailure(context, token);
            return true;
        }
        Map<String, Object> map = cacheObject.getValue();
        if (map == null || !map.containsKey("token") || !map.containsKey("password") || !map.containsKey("rememberMe")) {
            onAuthenticationFailure(context, token);
            return true;
        }
        UserSecurity userSecurity = authenticateWithUserType(ConversionUtils.toString(map.get("token")), ConversionUtils.toString(map.get("password")), userType, context.getRealRemoteAddress());
        if (null == userSecurity) {
            onAuthenticationFailure(context, token);
            return true;
        }

        int expire = StringUtils.equals(SafeConverter.toString(map.get("rememberMe")), "on") ? TWO_WEEK_EXP : -1;// 是否记住密码
        context.saveAuthenticationStates(expire, userSecurity);
        context.setAuthenticationSource(AuthenticationSource.COOKIE);
        updateContextByAuthCookieContent(context, new AuthCookieSnapshot(
                new AuthCookieMappingInfo(userSecurity.getUserId(), userSecurity.getRoleTypes(), userSecurity.getPassword()),
                true));
        // 如果通过输入密码登录成功，更新相关cookie
        updateLastUsingPwdLoginDate(context);
        onLoginSuccess(context);
        // 处理用户登录
        accountWebappService.onUserLogin(userSecurity.getUserId());

        return true;
    }


    /**
     * 验证用户信息
     *
     * @param userSecurities
     * @param token
     * @param password
     * @param ip
     * @return
     */
    private List<UserSecurity> authenticate(List<UserSecurity> userSecurities, String token, String password, String ip) {
        if (CollectionUtils.isEmpty(userSecurities)) {
            return Collections.emptyList();
        }

        if (userSecurities.size() == 1) {// 对应一个账号的情况
            UserSecurity userSecurity = userSecurities.get(0);

            // 临时密码校验 xuesong.zhang 2015-11-19
            Long userId = userSecurity.getUserId();
            if (StringUtils.isBlank(password) || !StringUtils.equalsIgnoreCase(userLoaderClient.loadUserTempPassword(userId), password)) {
                if (!userSecurity.match(password)) {
                    asyncFootprintServiceClient.createLoginDetail(userSecurity, token, false, ip);
                    //记录用户最后登录失败时间
                    asyncFootprintServiceClient.getAsyncFootprintService().recordUserLoginFailure(userSecurity.getUserId());
                    return Collections.emptyList();
                }
            }

            // FIXME 西安交通大学附属中学（30636，属于名校的初中部）, 临时禁止登陆和注册
            User user = raikouSystem.loadUser(userId);
            if (user != null && user.isStudent()) {
                StudentDetail detail = studentLoaderClient.loadStudentDetail(userId);
                if (detail.getClazz() != null && AppAuditAccounts.isForbiddenStudentRegisterSchool(detail.getClazz().getSchoolId())) {
                    return Collections.emptyList();
                }
            }

            // 认证成功记录登陆信息
            asyncFootprintServiceClient.createLoginDetail(userSecurity, token, true, ip);
            return Collections.singletonList(userSecurity);
        } else {// 对应多个账号情况，如手机号登录
            List<UserSecurity> candidates = userSecurities.stream()
                    .filter(us -> us.match(password))
                    .collect(Collectors.toList());
            if (candidates.isEmpty()) {
                for (UserSecurity userSecurity : userSecurities) {
                    asyncFootprintServiceClient.createLoginDetail(userSecurity, token, false, ip);
                    //记录用户最后登录失败时间
//                    userServiceClient.recordUserLoginFailure2(userSecurity.getUserId());
                    asyncFootprintServiceClient.getAsyncFootprintService().recordUserLoginFailure(userSecurity.getUserId());
                }
            }

            // FIXME 西安交通大学附属中学（30636，属于名校的初中部）, 临时禁止登陆和注册
            List<UserSecurity> retList = new ArrayList<>();
            for (UserSecurity security : candidates) {
                User user = raikouSystem.loadUser(security.getUserId());
                if (user != null && user.isStudent()) {
                    StudentDetail detail = studentLoaderClient.loadStudentDetail(security.getUserId());
                    if (detail.getClazz() != null && AppAuditAccounts.isForbiddenStudentRegisterSchool(detail.getClazz().getSchoolId())) {
                        continue;
                    }
                }
                retList.add(security);
            }

            return retList;
        }
    }

    /**
     * 多账号选择登录情况下，指定user type进行验证
     *
     * @param token
     * @param password
     * @param userType
     * @param ip
     * @return
     */
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

    /**
     * 成功登陆后的处理
     *
     * @param context
     * @throws IOException
     */
    private void onLoginSuccess(UcenterRequestContext context) throws IOException {

        // 检查用户的登录态
        User curUser = raikouSystem.loadUser(context.getUserId());
        UserExtAttribute userExtAttribute = null;
        if (curUser.isStudent()) {
            userExtAttribute = studentLoaderClient.loadStudentExtAttribute(curUser.getId());
        } else if (curUser.isTeacher()) {
            userExtAttribute = teacherLoaderClient.loadTeacherExtAttribute(curUser.getId());
            if (userExtAttribute != null && AccountStatus.PAUSE == userExtAttribute.getAccountStatus() && !StringUtils.containsAny(context.getRequest().getServletPath(), "pauseAccount", "onlinecs_new", "sendTCPWcode")) {
                context.getResponse().sendRedirect("/pauseAccount.vpage");
                return;
            }
        }

        if (userExtAttribute != null && (userExtAttribute.isForbidden() || userExtAttribute.isFreezing())) {
            context.cleanupAuthenticationStates();
            context.getResponse().sendRedirect("/login.vpage#error=true&type=forbidden");
            return;
        }

        // 每一次成功登陆都记录
//        userServiceClient.createUserRecordDaily(context.getUserId(), context.getRealRemoteAddress(), UserRecordMode.LOGIN);
        // AJAX 可能用户绑定家长直接从学生帐号登录
        if (HttpRequestContextUtils.isRequestAjax(context.getRequest())) {
            context.getResponse().setContentType(JSON_CONTENT_TYPE);
            context.getResponse().getWriter().write(JsonUtils.toJson(MapUtils.<String, Object>map().add("success", true).add("userId", context.getUserId())));
        } else {
            //跳转回登录之前的页面
            String returnURL = context.getRequest().getParameter("returnURL");
            String resolvedURL = resolveReturnURL(returnURL, context);
            context.getResponse().sendRedirect("redirect:" + resolvedURL);
        }
    }

    /**
     * 验证失败的处理
     *
     * @param context
     * @param candidates
     * @param token
     */
    private void onAuthenticationFailure(UcenterRequestContext context, List<UserSecurity> candidates, String token) throws IOException {
        if (HttpRequestContextUtils.isRequestAjax(context.getRequest())) {
            // AJAX 可能用户绑定家长直接从学生帐号登录
            // 返回JSON字符串
            context.getResponse().setContentType(JSON_CONTENT_TYPE);
            context.getResponse().getWriter().write(JsonUtils.toJson(MapUtils.<String, Object>map().add("success", false).add("userId", 0)));
        } else {
            if (processAccountBindError(context)) {// 第三方账户绑定失败的处理
                return;
            }

            String returnUrl = context.getRequest().getParameter(RETURN_URL_PARAM);

            // redmine 27675
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
                            context.getResponse().sendRedirect("redirect:/login.vpage?returnURL=" + URLEncoder.encode(returnUrl, "UTF-8") + "#error=true&record=true");
                            return;
                        }
                    }
                }
            }

            // 如果能定位到用户，并且绑定了手机，弹窗式找回密码（只适用于教师和学生）
            String redirect;
            if (returnUrl != null) {
                redirect = "redirect:/login.vpage?returnURL=" + URLEncoder.encode(returnUrl, "UTF-8") + "#error=true";
            } else {
                redirect = "redirect:/login.vpage?#error=true";
            }
            List<UserSecurity> users = candidates.stream()
                    .filter(us -> Arrays.asList(ROLE_STUDENT, ROLE_TEACHER).contains(MiscUtils.firstElement(us.getRoleTypes())))
                    .collect(Collectors.toList());
            // 如果是token是账号，users肯定只有一个，如果token是手机号，users中任意一个都是绑定手机的，所以只需要取users的第一个判断即可
            UserSecurity us = users.stream().findFirst().orElse(null);
            if (us != null) {
                //UserAuthentication ua = userLoaderClient.loadUserAuthentication(us.getUserId());
                String mobile = sensitiveUserDataServiceClient.loadUserMobileObscured(us.getUserId());
                if (!StringUtils.isEmpty(mobile)) {
                    redirect += "&type=mobile&mobile=" + mobile + "&captchaToken=" + RandomUtils.randomString(24);
                } else if (MiscUtils.firstElement(us.getRoleTypes()) == ROLE_STUDENT) {
                    // redmine 27675
                    // pc端如果是学生且学生未绑定家长通App，则弹窗提示家长通重置密码
                    // TODO 这里与StudentMagicCastleService中的hasBindParentApp方法逻辑重复，需要重构
                    List<StudentParentRef> studentParentRefs = studentLoaderClient.loadStudentParentRefs(us.getUserId());
                    if (CollectionUtils.isNotEmpty(studentParentRefs)) {
                        Map<Long, VendorAppsUserRef> vendorAppsUserRefMap = vendorLoaderClient.loadVendorAppUserRefs("17Parent",
                                studentParentRefs.stream().map(StudentParentRef::getParentId).collect(Collectors.toSet()));
                        if (MapUtils.isNotEmpty(vendorAppsUserRefMap)) {
                            redirect += "&type=wechat";
                        }
                    }
                }
            }

            context.getLastUserNameCookieManager().setLastUserNameCookie(-1, token);
            context.getResponse().sendRedirect(redirect);
        }
    }

    /**
     * @param accountStatus
     * @param context
     * @throws IOException
     */
    private void onStudentAccountLimited(AccountStatus accountStatus, UcenterRequestContext context) throws IOException {
        String redirect;
        if (accountStatus == AccountStatus.FORBIDDEN) {
            redirect = "redirect:/login.vpage?returnURL=#error=true&type=studentForbidden";
        } else {
            return;
        }
        context.getResponse().sendRedirect(redirect);
    }


    /**
     * @param context
     * @param token
     * @throws IOException
     */
    private void onAuthenticationFailure(UcenterRequestContext context, String token) throws IOException {
        // AJAX 可能用户绑定家长直接从学生帐号登录
        if (HttpRequestContextUtils.isRequestAjax(context.getRequest())) {
            // 返回JSON字符串
            context.getResponse().setContentType(JSON_CONTENT_TYPE);
            context.getResponse().getWriter().write(JsonUtils.toJson(MapUtils.<String, Object>map().add("success", false).add("userId", 0)));
        } else {
            if (processAccountBindError(context)) {
                return;
            }
            String returnUrl = context.getRequest().getParameter(RETURN_URL_PARAM);
            context.getLastUserNameCookieManager().setLastUserNameCookie(-1, token);
            context.getResponse().sendRedirect("redirect:/login.vpage?returnURL=" + URLEncoder.encode(returnUrl, "UTF-8") + "#error=adult");
        }
    }

    /**
     * 根据验证后的cookie内容更新context
     *
     * @param context
     * @param snapshot
     * @throws ServletException
     * @throws IOException
     */
    private void updateContextByAuthCookieContent(UcenterRequestContext context, AuthCookieSnapshot snapshot)
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

    /**
     * 初始化最后使用密码登录日期
     *
     * @param context
     */
    private void initLastUsingPwdLoginDate(UcenterRequestContext context) {
        if (StringUtils.isBlank(context.getCookieManager().getCookie(LAST_USING_PWD_LOGIN_DATE_COOKIE, ""))) {
            context.getCookieManager().setCookieTLD(LAST_USING_PWD_LOGIN_DATE_COOKIE, "1", ONE_WEEK_EXP);
        }
    }

    /**
     * @param context
     */
    private void updateLastUsingPwdLoginDate(UcenterRequestContext context) {
        context.getCookieManager().setCookieTLD(LAST_USING_PWD_LOGIN_DATE_COOKIE, "1", ONE_WEEK_EXP);
    }

    /**
     * 处理第三方账户登录失败
     * 重定向到SSO登录失败页面
     *
     * @param context
     * @return
     * @throws IOException
     */
    private boolean processAccountBindError(UcenterRequestContext context) throws IOException {
        String referer = context.getRequest().getHeader(REFERER_HEADER);
        if (StringUtils.contains(referer, SSO_LOGIN_URL) || StringUtils.contains(referer, QQ_LOGIN_URL)) {
            String dataKey = StringUtils.defaultString(context.getRequest().getParameter(SSO_KEY_PARAM));
            context.getResponse().sendRedirect(UrlUtils.buildUrlQuery(SSO_LOGIN_ERROR_URL, MapUtils.m(SSO_KEY_PARAM, dataKey)));
            return true;
        }
        return false;
    }

    /**
     * 登录成功后，生成返回地址
     *
     * @param returnURL
     * @param context
     * @return
     */
    private String resolveReturnURL(String returnURL, UcenterRequestContext context) {
        Long userId = context.getUserId();

        // 返回地址，小学老师、小学学生为小学页面，中学老师为中学页面
        // 中学学生前面已返回错误标识
        User user = raikouSystem.loadUser(userId);

        // 是否跳转https
        boolean useHttps = false;

        // 默认跳转地址
        String mainSiteBaseUrl = ProductConfig.getMainSiteBaseUrl();
        String homepageUrl = HOMEPAGE_URL;

        // 如果需要改名, 强制去个人中心
        boolean supplementName = false;

        if (user.isTeacher()) {
            TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(userId);
            if (teacher.isPrimarySchool()) {
                supplementName = StringUtil.isEmpty(teacher.getProfile().getRealname());
            } else {
                supplementName = StringRegexUtils.isNotRealName(teacher.getProfile().getRealname());
            }
            // 判断老师是否需要去修改密码页面
            if (needForceModifyPassword(teacher.getTeacherSchoolId(), user)) {
                return "/teacher/center/index.vpage#/teacher/center/securitycenter.vpage?_tab=pandaria";
            }
            if (supplementName) {
                return "/teacher/center/index.vpage";
            }

            // 初中数学老师
            if (teacher.isJuniorMathTeacher()) {
                SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService()
                        .loadSchoolExtInfo(teacher.getTeacherSchoolId())
                        .getUninterruptibly();
                // 判断用户所在学校是否开通阅卷机权限
                // 未开通，则进入到极算首页
                if (schoolExtInfo == null || !schoolExtInfo.isScanMachineFlag()) {
                    homepageUrl = SSZ_REDIRECT_URL;
                } else {
                    String regionCode = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.MIDDLE_PLATFORM_GENERAL.name(), "ONLINE_BUSINESS_CODE");
                    if (teacher.getCityCode() != null && StringUtils.isNoneBlank(regionCode) && Arrays.asList(regionCode.split(",")).contains(String.valueOf(teacher.getCityCode()))) {
                        homepageUrl = cacheHomeUrl(teacher.getId());
                    }
                }
            } else if (teacher.isKLXTeacher() || teacher.isJuniorMathTeacher()) {
                mainSiteBaseUrl = ProductConfig.getKuailexueUrl();
                homepageUrl = KUAILEXUE_HOMEPAGE_URL;
            } else if (teacher.isJuniorTeacher()) {
                mainSiteBaseUrl = ProductConfig.getJuniorSchoolUrl();
                homepageUrl = JUNIOR_HOMEPAGE_URL;
            }
        } else if (user.isStudent()) {
            StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(userId);
            if (studentDetail.isPrimaryStudent()) {
                supplementName = StringUtil.isEmpty(studentDetail.getProfile().getRealname());
            } else {
                supplementName = StringRegexUtils.isNotRealName(studentDetail.getProfile().getRealname());
            }
            if (supplementName) {
                return "/student/center/index.vpage";
            }
            if (studentDetail.isJuniorStudent() && studentDetail.getClazz() != null) {
                SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService()
                        .loadSchoolExtInfo(studentDetail.getClazz().getSchoolId())
                        .getUninterruptibly();
                // 判断用户所在学校是否开通阅卷机权限
                // 未开通，则进入到极算首页
                if (schoolExtInfo == null || !schoolExtInfo.isScanMachineFlag()) {
                    homepageUrl = SSZ_REDIRECT_URL;
                } else {
                    String regionCode = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(ConfigCategory.MIDDLE_PLATFORM_GENERAL.name(), "ONLINE_BUSINESS_CODE");
                    if (studentDetail.getCityCode() != null && StringUtils.isNoneBlank(regionCode) && Arrays.asList(regionCode.split(",")).contains(String.valueOf(studentDetail.getCityCode()))) {
                        homepageUrl = cacheHomeUrl(studentDetail.getId());
                    }
                }
            } else if (studentDetail.isShensz()) {
                homepageUrl = SSZ_REDIRECT_URL;
            } else {
                List<ClazzTeacher> clazzTeachers = userAggregationLoaderClient.loadStudentTeachers(userId);
                ClazzTeacher teacher = clazzTeachers.stream().findFirst().orElse(null);

                if (teacher != null) {
                    Teacher t = teacher.getTeacher();

                    if (t.isKLXTeacher() || t.isJuniorTeacher()) {
                        mainSiteBaseUrl = ProductConfig.getJuniorSchoolUrl().replaceAll("^http://", "https://");
                        homepageUrl = JUNIOR_HOMEPAGE_URL;
                        useHttps = true;
                    } else {
                        School school = asyncUserServiceClient.getAsyncUserService()
                                .loadUserSchool(t)
                                .getUninterruptibly();
                        // redmine 36344
                        // 湖北地区学生用户访问PC端自动切至HTTPS
                        // 这里没有用灰度控制是因为灰度需要load StudentDetail，这个对象太大了，大量学生访问时会影响性能，所以直接在代码里hardcode
                        if (school != null && school.getRegionCode() != null && school.getRegionCode().toString().startsWith("42")) {
                            mainSiteBaseUrl = mainSiteBaseUrl.replaceAll("^http://", "https://");
                            useHttps = true;
                        }
                    }
                }
            }
        }
        // redmine 34800
        // 当用户为老师，根据指定灰度，登录后跳转成https
        if (user.isTeacher()) {
            if (teacherLoaderClient.isWebGrayFunctionAvailable(teacherLoaderClient.loadTeacherDetail(userId), "LoginToHttps", "Teacher", false)) {
                mainSiteBaseUrl = mainSiteBaseUrl.replaceAll("^http://", "https://");
                useHttps = true;
            }

        }

        if (StringUtils.isBlank(returnURL)) {
            return mainSiteBaseUrl + homepageUrl;
        }
        returnURL = returnURL.trim();
        if (StringUtils.startsWith(returnURL, "//")) {
            // unsupported malformed, redirect to home
            logger.warn("Unsupported malformed returnURL: {}", returnURL);
            return mainSiteBaseUrl + homepageUrl;
        }
        if (StringUtils.startsWithIgnoreCase(returnURL, "http://") ||
                StringUtils.startsWithIgnoreCase(returnURL, "https://")) {
            try {
                URL url = new URL(returnURL);
                String host = url.getHost();
                String tld = TopLevelDomain.getTopLevelDomain();
                if (StringUtils.equalsIgnoreCase(host, tld) ||
                        StringUtils.endsWithIgnoreCase(host, "." + tld)) {
                    if (useHttps) {
                        returnURL = returnURL.replaceAll("^http://", "https://");
                    }
                    return returnURL;
                } else {
                    // not in our domain, redirect to home
                    logger.warn("Redirect denied: {}", returnURL);
                    return mainSiteBaseUrl + homepageUrl;
                }
            } catch (Exception ex) {
                logger.warn("Failed to resolve returnURL: {}, the exception message is: {}",
                        returnURL, ex.getMessage());
                return mainSiteBaseUrl + homepageUrl;
            }
        } else {
            // should be relative path, redirect directly
            return returnURL;
        }
    }

    private String cacheHomeUrl(Long userId) {
        String key = "HOME_PAGE_URL:" + userId;
        CacheObject<String> cacheHomepageUrl = ucenterWebCacheSystem.CBS.persistence.get(key);
        if (cacheHomepageUrl.extractValue() != null) {
            return String.valueOf(cacheHomepageUrl.extractValue());
        }
        return JUNIOR_HOMEPAGE_URL;
    }

    private void bindUser(UcenterRequestContext context) {
        String dataKey = StringUtils.defaultString(context.getRequest().getParameter("dataKey"));
        Long userId = context.getUserId();
        if (StringUtils.isNotBlank(dataKey) && userId != null) {
            Map map = ucenterWebCacheSystem.CBS.unflushable.load(dataKey);
            if (map == null) {
                return;
            }

            String sourceName = String.valueOf(map.get("source"));
            String sourceUid = String.valueOf(map.get("sourceUid"));
            String sourceUserName = String.valueOf(map.get("userName"));
            thirdPartyService.persistLandingSource(sourceName, sourceUid, sourceUserName, context.getUserId());
            ucenterWebCacheSystem.CBS.unflushable.delete(dataKey);
        }
    }

    private MapMessage checkInfantUser(List<UserSecurity> userSecurities, UcenterRequestContext context) throws IOException {
        if (CollectionUtils.isEmpty(userSecurities)) {
            return MapMessage.successMessage().add("userSecurities", userSecurities);
        }

        List<UserSecurity> retUserSecurities = new ArrayList<>();
        for (UserSecurity userSecurity : userSecurities) {
            User user = raikouSystem.loadUser(userSecurity.getUserId());
            if (user != null && user.isStudent()) {
                StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(user.getId());
                if (!studentDetail.isInfantStudent()) {
                    retUserSecurities.add(userSecurity);
                }
            } else if (user != null && user.isTeacher()) {
                Teacher teacher = teacherLoaderClient.loadTeacher(user.getId());
                if (!teacher.isInfantTeacher()) {
                    retUserSecurities.add(userSecurity);
                }
            } else {
                retUserSecurities.add(userSecurity);
            }
        }

        if (CollectionUtils.isNotEmpty(retUserSecurities)) {
            return MapMessage.successMessage().add("userSecurities", retUserSecurities);
        }

        context.getResponse().sendRedirect("/login.vpage#error=true&type=infant");
        //context.getResponse().sendRedirect(HttpUtils.buildQueryString(LOGIN_URL, MiscUtils.m("infant", true)));

        return MapMessage.errorMessage();
    }

    /**
     * 如果老师是通过批量注册的方式注册进来，而且没改过密码，直接去修改密码页面
     */
    private boolean needForceModifyPassword(Long schoolId, User user) {

        if (AppAuditAccounts.isNoneForcePasswdUpdateSchool(schoolId) || !user.isBatchUser()) {
            return false;
        }

        UserAuthentication ua = userLoaderClient.loadUserAuthentication(user.getId());
        return ua != null && (ua.fetchPasswordState() == PasswordState.AUTO_GEN);
    }

}
