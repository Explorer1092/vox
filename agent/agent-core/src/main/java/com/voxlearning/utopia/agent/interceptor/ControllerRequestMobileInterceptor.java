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

package com.voxlearning.utopia.agent.interceptor;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.DigestSignUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.webmvc.RequestHandler;
import com.voxlearning.alps.spi.webmvc.ServletRequest;
import com.voxlearning.alps.spi.webmvc.ServletResponse;
import com.voxlearning.alps.webmvc.cookie.CookieManager;
import com.voxlearning.alps.webmvc.interceptor.AbstractRequestHandlerInterceptor;
import com.voxlearning.alps.webmvc.mapping.method.MethodMapping;
import com.voxlearning.alps.webmvc.support.DefaultContext;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.agent.annotation.OperationCode;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.cache.AgentCacheSystem;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.controller.AgentAuthController;
import com.voxlearning.utopia.agent.persist.internal.InternalAuthDataLoader;
import com.voxlearning.utopia.agent.service.AgentApiAuth;
import com.voxlearning.utopia.agent.service.common.BaseUserService;
import com.voxlearning.utopia.agent.service.log.AsyncLogService;
import com.voxlearning.utopia.agent.service.notify.AgentNotifyService;
import com.voxlearning.utopia.agent.support.AgentRequestSupport;
import com.voxlearning.utopia.agent.support.AgentUserSupport;
import com.voxlearning.utopia.agent.utils.OfficeIPUtils;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Shuai.Huan on 2014/7/3.
 * Modified by Yuechen.Wang on 2016/08/19
 */
@Slf4j
public class ControllerRequestMobileInterceptor extends AbstractRequestHandlerInterceptor {

    @Inject private BaseUserService baseUserService;
    @Inject private AsyncLogService asyncLogService;
    @Inject AgentApiAuth agentApiAuth;
    @Inject AgentCacheSystem agentCacheSystem;
    @Inject protected InternalAuthDataLoader internalAuthDataLoader;
    @Inject private AgentNotifyService agentNotifyService;
    @Inject private AgentUserSupport agentUserSupport;
    @Inject private AgentRequestSupport agentRequestSupport;


    private static final String H5_LOGIN_PAGE = "/mobile/login.vpage";
    private static final String H5_INDEX_PAGE = "/mobile/index.vpage";
    private static final String OPEN_CLIENT_LOGIN_PAGE = "/view/mobile/crm/login/redirect_login.vpage";
    private static final String H5_RESET_PASSWORD_PAGE = "/resetPassword.vpage?client=h5&status=0";

    private static final Set<String> AUTH_IGNORE_URI;

    static {
        AUTH_IGNORE_URI = new HashSet<>();
        AUTH_IGNORE_URI.add("/management/api/getUserAppPath.vpage");
        AUTH_IGNORE_URI.add("/management/api/isHasUserAppPathRight.vpage");
        AUTH_IGNORE_URI.add("/mobile/task/dispatch_ugc_school_task.vpage");
        AUTH_IGNORE_URI.add("/captcha.vpage");
        AUTH_IGNORE_URI.add("/mobile/download.vpage");
        AUTH_IGNORE_URI.add("/mobile/getSMSCode.vpage");
        AUTH_IGNORE_URI.add("/mobile/welcoming.vpage");
        AUTH_IGNORE_URI.add("/mobile/file/osscallback.vpage");
        AUTH_IGNORE_URI.add("/mobile/trainingcenter/article/article_detail.vpage");
        AUTH_IGNORE_URI.add("/mobile/messageCenter/get_user_tag.vpage");
//        AUTH_IGNORE_URI.add("/mobile/file/upload.vpage");
    }

    private static final Set<String> LOGIN_URIS = new HashSet<>();

    static {
        LOGIN_URIS.add("/mobile/login.vpage");             // get, post
        LOGIN_URIS.add("/mobile/login_by_mobile.vpage");  // get, post
    }

    @Override
    public boolean preHandle(ServletRequest servletRequest,
                             ServletResponse servletResponse,
                             RequestHandler handler) throws Exception {
        HttpServletRequest request = servletRequest.getServletRequest();
        HttpServletResponse response = servletResponse.getServletResponse();
        response.setCharacterEncoding("UTF-8");
        AgentHttpRequestContext context = (AgentHttpRequestContext) DefaultContext.get();

        // 设置网站URL的根路径
        context.setWebAppContextPath(HttpRequestContextUtils.getWebAppContextPath(request));
        // 请求的相对路径
        String relativeUriPath = request.getRequestURI().substring(request.getContextPath().length());
        relativeUriPath = StringUtils.replace(relativeUriPath, "//", "/");
        context.setRelativeUriPath(relativeUriPath);

        Map<String, String> headerMap = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while(headerNames.hasMoreElements()){
            String headerName = headerNames.nextElement();
            headerMap.put(StringUtils.lowerCase(headerName), headerName);
        }
        // 保存到context中
        context.getHeaderMap().putAll(headerMap);

        initAlertMessageManager(context, handler, request);
        if (AUTH_IGNORE_URI.contains(relativeUriPath)) {
            return super.preHandle(servletRequest, servletResponse, handler);
        }


        CookieManager cookieManager = context.getCookieManager();
        Long userId = SafeConverter.toLong(cookieManager.getCookie("userId", ""));
        String sign = cookieManager.getCookie("sign", "");

        boolean checkUserResult = true;
        if(userId < 1 || StringUtils.isBlank(sign) || !agentApiAuth.isSignValid(sign, userId)){
            checkUserResult = false;

        }

        AuthCurrentUser currentUser = null;
        if(checkUserResult){
            currentUser = agentCacheSystem.getAuthCurrentUser(userId);
            if(currentUser == null){
                checkUserResult = false;
            }
        }

        // 身份验证失败
        if(!checkUserResult){
            if(LOGIN_URIS.contains(relativeUriPath)){
                return true;
            }else {
                if (agentRequestSupport.isMobileRequest(request)) { // 如果是手机端请求，返回错误码，让用户重新登录
                    if(agentRequestSupport.isAjaxRequest(request)){
                        response.setContentType("application/json");
                        response.getWriter().write(JsonUtils.toJson(MapMessage.errorMessage("签名验证失败，请重新登陆").add("errorCode", 900)));
                    }else {
                        response.sendRedirect("redirect:" + context.getWebAppContextPath() + OPEN_CLIENT_LOGIN_PAGE);
                    }
                } else {  // 如果是网页登录，跳转到网页版登录页
                    if (agentRequestSupport.isAjaxRequest(request)) {
                        response.setContentType("application/json");
                        response.getWriter().write(JsonUtils.toJson(MapMessage.errorMessage("您已退出，请从新登陆").add("errorCode", 900).add("loginUrl", "/mobile/login.vpage")));
                    } else {
                        response.sendRedirect("redirect:" + context.getWebAppContextPath() + H5_LOGIN_PAGE);
                    }
                }
                return false;
            }
        }

        // 身份验证成功，且用户处于登录状态

        // 更新用户角色列表及权限列表
        if(agentCacheSystem.needRefreshUserAuth(currentUser.getUserId())){
            agentUserSupport.refreshCurrentUserData(currentUser);
            agentCacheSystem.setAuthCurrentUser(currentUser.getUserId(), currentUser);
            agentCacheSystem.updateUserAuthRefreshTime(currentUser.getUserId());
        }

        // 更新用户的未读消息数
        Integer unreadCount = agentCacheSystem.getUserUnreadNotifyCount(currentUser.getUserId());
        if(unreadCount == null){
            int totalCount = agentNotifyService.getTotalUnreadNotifyCount(null, currentUser.getUserId());
            agentCacheSystem.updateUserUnreadNotifyCount(currentUser.getUserId(), totalCount);
            context.setUnreadNotifyCount(totalCount);
        }else{
            context.setUnreadNotifyCount(unreadCount);
        }

        context.setCurrentUser(currentUser);

        // 添加访问日志
        addAccessLog(currentUser, request, relativeUriPath);

        // staging环境和release环境验证deviceId
        if ((RuntimeMode.isProduction() || RuntimeMode.isStaging())) {
            String deviceId = agentRequestSupport.getDeviceId(request);
            if (StringUtils.isNotBlank(deviceId)) {  // app端， 验证设备ID
                String userDeviceId = currentUser.getDeviceId();
                if (StringUtils.isBlank(userDeviceId) || !Objects.equals(deviceId, userDeviceId)) {
                    // 设备ID 不一致的情况下，返回错误页
                    clearSession(currentUser.getUserId());
                    response.sendError(401);
                    return false;
                }
            }else {  // 网页端
                // 判断是不是内网IP,  内网情况下不做校验，外网返回错误页
                String userRealIp = context.getRealRemoteAddress();
                if (!OfficeIPUtils.isOfficeIp(userRealIp)) {
                    clearSession(currentUser.getUserId());
                    response.sendError(401);
                    return false;
                }
            }
        }

        // 登陆状态下，请求登陆页面，直接跳转到首页
        if(LOGIN_URIS.contains(relativeUriPath)){
            if(agentRequestSupport.isMobileRequest(request)){  // 客户端请求
                response.sendRedirect("redirect:" + context.getWebAppContextPath() + H5_INDEX_PAGE);
            }else {
                response.sendRedirect("redirect:" + context.getWebAppContextPath() + H5_INDEX_PAGE);
            }
            return false;
        }

        // 判断用户是否有该接口的访问权限
        if(!checkAccessPermission(handler, currentUser)){
            if(agentRequestSupport.isAjaxRequest(request)){
                response.setContentType("application/json");
                response.getWriter().write(JsonUtils.toJson(MapMessage.errorMessage("您没有权限进行此操作")));
            }else {
                response.sendRedirect("redirect:" + context.getWebAppContextPath() + H5_INDEX_PAGE);
            }
            return false;
        }


        if(currentUser.getStatus() == null){
            // 兼容线上缓存
//        }else if(currentUser.getStatus() == 0){ // 新建用户， 跳转到更新密码页面，强制用户更新密码
////            response.sendRedirect(H5_RESET_PASSWORD_PAGE);
////            return false;
        }else if(currentUser.getStatus() == 9){  // 用户已关闭
            clearSession(currentUser.getUserId());
            if(agentRequestSupport.isMobileRequest(request)){   // 手动端登录，打开原生登录页
                response.sendRedirect("redirect:" + context.getWebAppContextPath() + OPEN_CLIENT_LOGIN_PAGE);
            }else {
                response.sendRedirect("redirect:" + context.getWebAppContextPath() + H5_INDEX_PAGE);
            }
            return false;
        }
        return super.preHandle(servletRequest, servletResponse, handler);
    }

    private void initAlertMessageManager(AgentHttpRequestContext context, RequestHandler handler, HttpServletRequest request) {
        // 把 context 放到 controller 的 ThreadLocalObject 中
        AbstractAgentController controller = (AbstractAgentController) handler.getBean();

        // 初始化消息管理器，读取之前可能存在的消息
        controller.initAlertMessageManager();
    }

    private void addAccessLog(AuthCurrentUser currentUser, HttpServletRequest request, String relativeUriPath){
        if (currentUser != null) {
            AgentHttpRequestContext context = (AgentHttpRequestContext) HttpRequestContextUtils.currentRequestContext(request);

            asyncLogService.logUserAction(currentUser, relativeUriPath, "", "h5");
            Map<String, String[]> paramMap = request.getParameterMap();
            Map<String, String> targetMap = new HashMap<>();
            targetMap.put("app", "agent");
            targetMap.put("env", RuntimeMode.current().getStageMode());
            targetMap.put("user_ip", context.getRealRemoteAddress());
            targetMap.put("userAgent", request.getHeader(context.getHeaderMap().getOrDefault("user-agent", "User-Agent")));
            targetMap.put("url", relativeUriPath);
            targetMap.put("requestMethod", request.getMethod());
            targetMap.put("requestTime", DateUtils.dateToString(new Date(), "yyyyMMddHHmmss.SSS"));
            targetMap.put("userId", currentUser.getUserId() == null ? "" : String.valueOf(currentUser.getUserId()));
            targetMap.put("userName", currentUser.getUserName());
            StringBuilder params = new StringBuilder();
            paramMap.forEach((k, v) -> params.append(k).append("=").append(StringUtils.join(v, ",")).append("&"));
            targetMap.put("params", params.toString());
            targetMap.put("client", "h5");
            LogCollector.info("agent_user_access_log", targetMap);
        }
    }

    @Override
    public void afterCompletion(ServletRequest request, ServletResponse response, RequestHandler handler, Exception ex) throws Exception {
        AbstractAgentController controller = (AbstractAgentController) handler.getBean();

        // 如果消息管理器中还有消息，说明需要在下一个页面中再显示，先保存到session中
        // 这个方案只适用于后台管理，网站最好用其他高性能机制
//        controller.saveAlertMessagesToSession();
        controller.saveAlertMessageToCache();
        if (ex != null) {
            log.error("App-Tianji catches a Exception, ex={}", ex.getMessage(), ex);
            HttpServletResponse httpResponse = response.getServletResponse();
            httpResponse.sendError(500);
        }
    }


    private void clearSession(Long userId) {
        try {
            agentCacheSystem.removeUserSession(userId);
//            agentCacheSystem.getAlertMessageCache().evict(userId);
        } catch (Exception ignored) {

        }
    }

    private boolean checkAccessPermission(RequestHandler handler, AuthCurrentUser currentUser){
        MethodMapping methodMapping = (MethodMapping)handler.getMethodMapping();
        Method method = methodMapping.getMethod();
        OperationCode operationCode = method.getAnnotation(OperationCode.class);
        if(operationCode == null || StringUtils.isBlank(operationCode.value())){
            return true;
        }else {
            String operationCodeValue = operationCode.value();
            List<String> userOperationCodes = currentUser.getOperationCodes();
            if(CollectionUtils.isNotEmpty(userOperationCodes) && userOperationCodes.contains(operationCodeValue)){
                return true;
            }
            return false;
        }
    }
}
