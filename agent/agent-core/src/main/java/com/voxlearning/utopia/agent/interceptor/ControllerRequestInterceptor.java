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
import com.voxlearning.utopia.agent.service.log.AsyncLogService;
import com.voxlearning.utopia.agent.support.AgentRequestSupport;
import com.voxlearning.utopia.agent.support.AgentUserSupport;
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
 */
@Slf4j
public class ControllerRequestInterceptor extends AbstractRequestHandlerInterceptor {

    private static final Set<String> AUTH_IGNORE_URI;

    @Inject AsyncLogService asyncLogService;
    @Inject AgentApiAuth agentApiAuth;
    @Inject AgentCacheSystem agentCacheSystem;
    @Inject protected InternalAuthDataLoader internalAuthDataLoader;

    @Inject private AgentUserSupport agentUserSupport;
    @Inject private AgentRequestSupport agentRequestSupport;


    private static final String PC_LOGIN_PAGE = "/auth/login.vpage";
    private static final String PC_INDEX_PAGE = "/index.vpage";

    static {
        AUTH_IGNORE_URI = new HashSet<>();
        AUTH_IGNORE_URI.add("/management/api/getUserAppPath.vpage");
        AUTH_IGNORE_URI.add("/management/api/isHasUserAppPathRight.vpage");
        AUTH_IGNORE_URI.add("/captcha.vpage");
        AUTH_IGNORE_URI.add("/captcha");
        AUTH_IGNORE_URI.add("/task/crm/refundorder.vpage");
        AUTH_IGNORE_URI.add("/task/crm/cashwithdraw.vpage");
        AUTH_IGNORE_URI.add("/crm/validateschool.vpage");
        AUTH_IGNORE_URI.add("/crm/getteacheragent.vpage");
        AUTH_IGNORE_URI.add("/task/manage/update_task_detail_state.vpage");
        AUTH_IGNORE_URI.add("/crm/isdictschool.vpage");
        AUTH_IGNORE_URI.add("/crm/hasbusinessdeveloper.vpage");
        AUTH_IGNORE_URI.add("/mobile/common/teacher_info.vpage");
        AUTH_IGNORE_URI.add("/invitation/add_invitation.vpage");    //邀请函新增
        AUTH_IGNORE_URI.add("/invitation/getpwxjsapiconfig.vpage"); //活动类的微信分享用
        AUTH_IGNORE_URI.add("/agent/worksheet/work_sheet_event_handler.vpage"); //客服工单推送接口
        AUTH_IGNORE_URI.add("/surl/create.vpage"); //生成短连接接口
    }

    private static final Set<String> LOGIN_URIS = new HashSet<>();

    static {
        LOGIN_URIS.add("/auth/login.vpage");             // get , post
        LOGIN_URIS.add("/auth/login_by_mobile.vpage");  // get, post
    }


    @Override
    public boolean preHandle(ServletRequest servletRequest,
                             ServletResponse servletResponse,
                             RequestHandler handler) throws Exception {
        HttpServletRequest request = servletRequest.getServletRequest();
        HttpServletResponse response = servletResponse.getServletResponse();
        AgentHttpRequestContext context = (AgentHttpRequestContext) DefaultContext.get();

        // 设置网站URL的根路径
        context.setWebAppContextPath(HttpRequestContextUtils.getWebAppContextPath(request));
        // 请求的相对路径
        String relativeUriPath = request.getRequestURI().substring(request.getContextPath().length());
        relativeUriPath = StringUtils.replace(relativeUriPath, "//", "/");
        context.setRelativeUriPath(relativeUriPath);

        Map<String, String> headerMap = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
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
                if (agentRequestSupport.isAjaxRequest(request)) {
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(JsonUtils.toJson(MapMessage.errorMessage("您已退出，请从新登陆")));
                } else {
                    response.sendRedirect("redirect:" + context.getWebAppContextPath() + PC_LOGIN_PAGE);
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
        context.setCurrentUser(currentUser);

        // 添加访问日志
        addAccessLog(currentUser, request, relativeUriPath);

        // 判断用户是否有该接口的访问权限
        if(!checkAccessPermission(handler, currentUser)){
            if(agentRequestSupport.isAjaxRequest(request)){
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(JsonUtils.toJson(MapMessage.errorMessage("您没有权限进行此操作")));
            }else {
                response.sendRedirect("redirect:" + context.getWebAppContextPath() + PC_INDEX_PAGE);
            }
            return false;
        }
        return super.preHandle(servletRequest, servletResponse, handler);
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
            targetMap.put("client", "pc");
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
    }


    private void initAlertMessageManager(AgentHttpRequestContext context, RequestHandler handler, HttpServletRequest request) {
        // 把 context 放到 controller 的 ThreadLocalObject 中
        AbstractAgentController controller = (AbstractAgentController) handler.getBean();

        // 初始化消息管理器，读取之前可能存在的消息
        controller.initAlertMessageManager();
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
