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

package com.voxlearning.utopia.admin.interceptor;


import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.ArrayUtils;
import com.voxlearning.alps.core.util.ObjectUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.webmvc.RequestHandler;
import com.voxlearning.alps.spi.webmvc.ServletRequest;
import com.voxlearning.alps.spi.webmvc.ServletResponse;
import com.voxlearning.alps.webmvc.interceptor.AbstractRequestHandlerInterceptor;
import com.voxlearning.alps.webmvc.mapping.method.MethodMapping;
import com.voxlearning.alps.webmvc.support.DefaultContext;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.admin.annotation.AdminAcceptRoles;
import com.voxlearning.utopia.admin.annotation.AdminSystemPath;
import com.voxlearning.utopia.admin.auth.AuthCurrentAdminUser;
import com.voxlearning.utopia.admin.cache.AdminCacheSystem;
import com.voxlearning.utopia.admin.constant.AdminPageRole;
import com.voxlearning.utopia.admin.controller.AbstractAdminController;
import com.voxlearning.utopia.admin.controller.AdminAuthController;
import com.voxlearning.utopia.admin.util.AdminAuthUtils;
import com.voxlearning.utopia.service.config.client.CommonConfigServiceClient;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.user.api.sms.DPUserSmsService;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.voxlearning.utopia.service.config.api.constant.ConfigCategory.PRIMARY_PLATFORM_GENERAL;

@Slf4j
public class ControllerRequestInterceptor extends AbstractRequestHandlerInterceptor {

    private static final Set<String> IGNORE_URL = new HashSet<>();

    static {
        IGNORE_URL.add("/site/teacher/batchcreatetcspreview.vpage");
        IGNORE_URL.add("/site/teacher/marketbatchcreatetcs.vpage");
//        IGNORE_URL.add("/crm/user/marketresetpwdlog.vpage");   // 接口已删除
//        IGNORE_URL.add("/legacy/afenti/market/withdrawresult.vpage");
//        IGNORE_URL.add("/crm/finance/withdrawresult.vpage");
//        IGNORE_URL.add("/crm/finance/userpaymenthistory.vpage");
        IGNORE_URL.add("/crm/task/add_agent_task.vpage");
        IGNORE_URL.add("/crm/task/delete_crm_task.vpage");
        IGNORE_URL.add("/crm/user/createmainsubaccount.vpage");
        IGNORE_URL.add("/auth/auth.vpage");
        IGNORE_URL.add("/auth/loginUserInfo.vpage");
        IGNORE_URL.add("/crm/teachernew/authstuquery.vpage");
    }

    @Inject
    private AdminCacheSystem adminCacheSystem;
    @Inject private AdminAuthUtils adminAuthUtils;
    @Inject CommonConfigServiceClient commonConfigServiceClient;

    @ImportService(interfaceClass = DPUserSmsService.class)
    private DPUserSmsService smsService;

    @Override
    public boolean preHandle(ServletRequest servletRequest,
                             ServletResponse servletResponse,
                             RequestHandler handler) throws Exception {

        HttpServletRequest request = servletRequest.getServletRequest();
        HttpServletResponse response = servletResponse.getServletResponse();

        AdminHttpRequestContext context = (AdminHttpRequestContext) DefaultContext.get();

        // 设置网站URL的根路径
        context.setWebAppContextPath(HttpRequestContextUtils.getWebAppContextPath(request));
        // 请求的相对路径
        String relativeUriPath = request.getRequestURI().substring(request.getContextPath().length());
        context.setRelativeUriPath(relativeUriPath);
        //登陆验证
        if (relativeUriPath.endsWith("verify.vpage")) {
          saveContextToThreadLocal(context, handler, request);
          return super.preHandle(servletRequest, servletResponse, handler);
        }

        //marketing批量导入老师学生功能需要http访问crm的两个请求，这里先简单做一个登录排除======20150906
        String source = request.getParameter("source");
        if ((IGNORE_URL.contains(relativeUriPath) && StringUtils.isNotBlank(source) && source.contains("agent_batch")) || relativeUriPath.equals("/management/api/getUserAppPath.vpage") || relativeUriPath.equals("/management/api/isHasUserAppPathRight.vpage")
                || (context.isLoggedIn() && relativeUriPath.equals(AdminAuthController.RelativeUriPath_Index))) {
            saveContextToThreadLocal(context, handler, request);
            return super.preHandle(servletRequest, servletResponse, handler);
        }

        // 从cookie获取当前登录用户
        String userId = "";
        String cookieSign = "";
        AuthCurrentAdminUser currentAdminUser;
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            currentAdminUser = null;
        } else {
             for (Cookie cookie : cookies) {
                if (cookie.getName().equals("userId")) {
                    userId = cookie.getValue();
                } else if (cookie.getName().equals("sign")) {
                    cookieSign = cookie.getValue();
                }
            }
            //cookie没有cookieSign 重新登录去
            if (StringUtils.isBlank(userId) || StringUtils.isBlank(cookieSign)) {
                currentAdminUser = null;
            } else if (!adminAuthUtils.isSignValid(cookieSign, userId)) {
                currentAdminUser = null;
            } else {
                currentAdminUser = adminCacheSystem.loadAuthUser(SafeConverter.toLong(userId));
            }
        }

        // 当前登录用户
        if (currentAdminUser == null && !relativeUriPath.equals(AdminAuthController.RelativeUriPath_AuthLogin)) {
            response.sendRedirect(AdminAuthController.RelativeUriPath_AuthLogin);
            return false;
        }

        // 单点登录控制
        if (currentAdminUser != null) {
            String cacheKey = AbstractAdminController.generateAdminLoginKey(currentAdminUser);
            CacheObject<Long> cacheObject = adminCacheSystem.CBS.storage.get(cacheKey);
            Long cacheValue = null;
            if (cacheObject != null) {
                cacheValue = cacheObject.getValue();
            }
            Object sessionValue = request.getSession().getAttribute(cacheKey);
            // 被清除了缓存，补充一下
            if (cacheValue == null && sessionValue != null) {
                adminCacheSystem.CBS.storage.set(cacheKey, DateUtils.getCurrentToDayEndSecond(), sessionValue);
            } else if (cacheValue != null && sessionValue != null) {
                if (ObjectUtils.notEqual(cacheValue, sessionValue)) {
                    request.getSession().removeAttribute(cacheKey);
                    response.sendRedirect(AdminAuthController.RelativeUriPath_AuthLogin);
                    return false;
                }
            }

            // 记录日志
            Map<String, String[]> paramMap = request.getParameterMap();
            Map<String, String> logInfo = new HashMap<>();
            logInfo.put("app", "admin");
            logInfo.put("env", RuntimeMode.current().getStageMode());
            logInfo.put("user_ip", context.getRealRemoteAddress());
            logInfo.put("userAgent", request.getHeader("User-Agent"));
            logInfo.put("url", relativeUriPath);
            logInfo.put("requestMethod", request.getMethod());
            logInfo.put("requestTime", DateUtils.dateToString(new Date(), "yyyyMMddHHmmss.SSS"));
            logInfo.put("userId", currentAdminUser.getAdminUserName());
            logInfo.put("userName", currentAdminUser.getRealName());
            StringBuilder params = new StringBuilder();
            paramMap.forEach((k, v) -> params.append(k).append("=").append(StringUtils.join(v, ",")).append("&"));
            logInfo.put("params", params.toString());
            LogCollector.info("admin_user_access_log", logInfo);

            // sms notify if necessary
            String notify = commonConfigServiceClient.getCommonConfigBuffer().loadCommonConfigValue(PRIMARY_PLATFORM_GENERAL.name(), "sms_notify_flag");
            if ("1".equals(notify)) {
                smsService.sendOpsNotify(currentAdminUser.getAdminUserName());
            }

            String ck = "admin_access_count:" + currentAdminUser.getAdminUserName();
            Long operateAccount = adminCacheSystem.CBS.flushable.incr(ck, 1, 1, 180);
            if (operateAccount > 500) {
                smsService.sendSms("13240239065", currentAdminUser.getAdminUserName() + "访问频繁", SmsType.OPS_NOTIFY.name());
                // 踢出登录态
                adminCacheSystem.removeAuthUser(currentAdminUser.getFakeUserId());
            }

        }

        context.setCurrentAdminUser(currentAdminUser);
        saveContextToThreadLocal(context, handler, request);

        // 权限判定
        if (context.isLoggedIn()) {
            // 有登录用户，判断用户权限,若用户没有访问权限，跳转到首页，并提示;若有访问权限，什么也不做
            MethodMapping methodMapping = (MethodMapping) handler.getMethodMapping();
            Method method = methodMapping.getMethod();
            AdminAcceptRoles adminAcceptRoles = method.getAnnotation(AdminAcceptRoles.class);
            if (adminAcceptRoles == null) {
                adminAcceptRoles = method.getDeclaringClass().getAnnotation(AdminAcceptRoles.class);
            }
            if (adminAcceptRoles != null) {
                AdminPageRole[] pageRoles = null;
                if (request.getMethod().equals("GET")) {
                    pageRoles = adminAcceptRoles.getRoles();
                } else if (request.getMethod().equals("POST")) {
                    pageRoles = adminAcceptRoles.postRoles();
                }

                if (!ArrayUtils.contains(pageRoles, AdminPageRole.ALLOW_ALL)) {
                    // 获取systemName和url
                    AdminSystemPath adminSystemPath = method.getAnnotation(AdminSystemPath.class);
                    String systemPath = relativeUriPath;
                    if (adminSystemPath != null) {
                        systemPath = adminSystemPath.value();
                    }
                    Pattern pattern = Pattern.compile("[/]?([^/]+)/");
                    Matcher matcher = pattern.matcher(systemPath);
                    if (matcher.find()) {
                        String systemName = matcher.group(1);
                        int endIndex = systemPath.indexOf(systemName) + systemName.length();
                        String url = systemPath.substring(endIndex);
                        if (url.endsWith(".vpage")) {
                            url = url.substring(0, url.length() - 6);
                        }
                        if (url.startsWith("/")) {
                            url = url.substring(1);
                        }

                        if (!"auth".equals(systemName) && !currentAdminUser.checkAuth(systemName, url, pageRoles)) {
                            // 判断是否是ajax请求
                            String header = request.getHeader("X-Requested-With");
                            if (header != null && "XMLHttpRequest".equals(header)) {
                                response.setContentType("application/json;charset=UTF-8");
                                response.getWriter().write(JsonUtils.toJson(MapMessage.errorMessage("您没有权限进行此操作")));
                            } else {
                                context.getAlertMessageManager().addMessageError("您没有权限进行此操作");
                                response.sendRedirect(AdminAuthController.RelativeUriPath_Index);
                            }
                            return false;
                        }
                    }
                }
            }
        } else {
            // 没有登录用户，且不是登录页面，则跳到登录页面
            if (!relativeUriPath.equals(AdminAuthController.RelativeUriPath_AuthLogin)) {
                response.sendRedirect(AdminAuthController.RelativeUriPath_AuthLogin);
                return false;
            }
        }

        return super.preHandle(servletRequest, servletResponse, handler);
    }


    @Override
    public void afterCompletion(ServletRequest request, ServletResponse response, RequestHandler handler, Exception ex) throws Exception {
        AbstractAdminController controller = (AbstractAdminController) handler.getBean();

        // 如果消息管理器中还有消息，说明需要在下一个页面中再显示，先保存到session中
        // 这个方案只适用于后台管理，网站最好用其他高性能机制
        // controller.saveAlertMessagesToSession();
    }


    private void saveContextToThreadLocal(AdminHttpRequestContext context, RequestHandler handler, HttpServletRequest request) {
        AbstractAdminController controller = (AbstractAdminController) handler.getBean();

        // 初始化消息管理器，读取之前可能存在的消息
        controller.initAlertMessageManager();
    }
}
