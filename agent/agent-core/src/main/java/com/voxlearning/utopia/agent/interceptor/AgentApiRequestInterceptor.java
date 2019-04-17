package com.voxlearning.utopia.agent.interceptor;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.DigestSignUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.webmvc.RequestHandler;
import com.voxlearning.alps.spi.webmvc.ServletRequest;
import com.voxlearning.alps.spi.webmvc.ServletResponse;
import com.voxlearning.alps.webmvc.interceptor.AbstractRequestHandlerInterceptor;
import com.voxlearning.alps.webmvc.mapping.method.MethodMapping;
import com.voxlearning.alps.webmvc.support.DefaultContext;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.agent.annotation.OperationCode;
import com.voxlearning.utopia.agent.auth.AuthCurrentUser;
import com.voxlearning.utopia.agent.cache.AgentCacheSystem;
import com.voxlearning.utopia.agent.constants.AgentConstants;
import com.voxlearning.utopia.agent.service.AgentApiAuth;
import com.voxlearning.utopia.agent.service.log.AsyncLogService;
import com.voxlearning.utopia.agent.service.notify.AgentNotifyService;
import com.voxlearning.utopia.agent.support.AgentRequestSupport;
import com.voxlearning.utopia.agent.support.AgentUserSupport;
import com.voxlearning.utopia.agent.utils.ApiMapMessage;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.*;

/**
 * AgentApiRequestInterceptor
 *
 * @author song.wang
 * @date 2018/8/8
 */
public class AgentApiRequestInterceptor extends AbstractRequestHandlerInterceptor {

    @Inject private AgentApiAuth agentApiAuth;
    @Inject private AgentCacheSystem agentCacheSystem;
    @Inject private AgentUserSupport agentUserSupport;
    @Inject private AgentRequestSupport agentRequestSupport;
    @Inject private AgentNotifyService agentNotifyService;
    @Inject private AsyncLogService asyncLogService;


//    private static final Set<String> LOGIN_URIS = new HashSet<>();

    private static final Set<String> IGNORE_URI = new HashSet<>();
    private static final List<String> UNSIGNED_PARAMS = new ArrayList<>();

    static {
        IGNORE_URI.add("/v1/auth/loginByPwd.vpage");  //   客户端密码登录
        IGNORE_URI.add("/v1/auth/loginBySmsCode.vpage");  // 客户端短信验证码登录


        UNSIGNED_PARAMS.add(AgentApiAuth.PARAM_SIG);
        UNSIGNED_PARAMS.add("sys");
        UNSIGNED_PARAMS.add("ver");
        UNSIGNED_PARAMS.add("channel");
        UNSIGNED_PARAMS.add("app_product_id");
        UNSIGNED_PARAMS.add("uuid");
        UNSIGNED_PARAMS.add("model");

    }



    @Override
    public boolean preHandle(ServletRequest servletRequest,
                             ServletResponse servletResponse,
                             RequestHandler handler) throws Exception {
        HttpServletRequest request = servletRequest.getServletRequest();
        HttpServletResponse response = servletResponse.getServletResponse();
        response.setContentType("application/json;charset=UTF-8");
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

        if (IGNORE_URI.contains(relativeUriPath)) {
            return super.preHandle(servletRequest, servletResponse, handler);
        }

        String sessionKey = SafeConverter.toString(request.getParameter(AgentApiAuth.PARAM_SESSION_KEY), "");
        if(StringUtils.isBlank(sessionKey) || !agentApiAuth.isSessionKeyValid(sessionKey)){
            response.getWriter().write(JsonUtils.toJson(ApiMapMessage.errorMessage(AgentConstants.API_NEED_LOGIN, "用户信息失效，请重新登陆")));
            return false;
        }

        // 验证签名
        Map<String, String> paramMap = new HashMap<>();
        Map<String, String[]> requestParamMap = request.getParameterMap();
        requestParamMap.forEach((k, v) -> {
            if(!UNSIGNED_PARAMS.contains(k)){
                paramMap.put(k, StringUtils.join(v, ","));
            }
        });

        String sign = SafeConverter.toString(request.getParameter(AgentApiAuth.PARAM_SIG), "");
        String serverSign;
        if(RuntimeMode.lt(Mode.STAGING)){
            serverSign = DigestSignUtils.signMd5(paramMap, AgentApiAuth.APP_SECRET_KEY_TEST);
        }else {
            serverSign = DigestSignUtils.signMd5(paramMap, AgentApiAuth.APP_SECRET_KEY);
        }
        // 签名验证失败，
        if(!StringUtils.equalsIgnoreCase(sign, serverSign)){
            response.getWriter().write(JsonUtils.toJson(ApiMapMessage.errorMessage(AgentConstants.API_NEED_LOGIN, "用户信息失效，请重新登陆")));
            return false;
        }

        // 签名验证成功的情况下
        Long userId = agentApiAuth.fetchUserIdBySessionKey(sessionKey);
        if(userId == null) {
            response.getWriter().write(JsonUtils.toJson(ApiMapMessage.errorMessage(AgentConstants.API_NEED_LOGIN, "用户信息失效，请重新登陆")));
            return false;
        }

        AuthCurrentUser currentUser = agentCacheSystem.getAuthCurrentUser(userId);
        if (currentUser == null) {  // currentUser不存在的情况下，创建currentUser并保存到缓存中， for web page
            currentUser = agentUserSupport.createCurrentUserById(userId);
            if (currentUser != null) {
                agentCacheSystem.setAuthCurrentUser(currentUser.getUserId(), currentUser);
                agentCacheSystem.updateUserAuthRefreshTime(currentUser.getUserId());
            } else {
                response.getWriter().write(JsonUtils.toJson(ApiMapMessage.errorMessage(AgentConstants.API_NEED_LOGIN, "该用户不存在，请重新登陆")));
                return false;
            }
        }

        // 更新用户角色列表及权限列表
        if (agentCacheSystem.needRefreshUserAuth(currentUser.getUserId())) {
            agentUserSupport.refreshCurrentUserData(currentUser);
            agentCacheSystem.setAuthCurrentUser(currentUser.getUserId(), currentUser);
            agentCacheSystem.updateUserAuthRefreshTime(currentUser.getUserId());
        }

        // 更新用户的未读消息数
        Integer unreadCount = agentCacheSystem.getUserUnreadNotifyCount(currentUser.getUserId());
        if (unreadCount == null) {
            int totalCount = agentNotifyService.getTotalUnreadNotifyCount(null, currentUser.getUserId());
            agentCacheSystem.updateUserUnreadNotifyCount(currentUser.getUserId(), totalCount);
            context.setUnreadNotifyCount(totalCount);
        } else {
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
                    response.getWriter().write(JsonUtils.toJson(ApiMapMessage.errorMessage(AgentConstants.API_BAD_REQUEST,  "该账号已经绑定其他设备")));
                    return false;
                }
            }
        }

        // 判断用户是否有该接口的访问权限
        if(!checkAccessPermission(handler, currentUser)){
            response.getWriter().write(JsonUtils.toJson(ApiMapMessage.errorMessage(AgentConstants.API_BAD_REQUEST, "您没有权限访问该数据")));
            return false;
        }

//        if(currentUser.getStatus() == null || currentUser.getStatus() == 0){ // 新建用户， 跳转到更新密码页面，强制用户更新密码
//            response.getWriter().write(JsonUtils.toJson(ApiMapMessage.errorMessage(AgentConstants.API_BAD_REQUEST, "请更新密码")));
//            return false;
//        }
        return super.preHandle(servletRequest, servletResponse, handler);
    }

    private void addAccessLog(AuthCurrentUser currentUser, HttpServletRequest request, String relativeUriPath){
        if (currentUser != null) {
            AgentHttpRequestContext context = (AgentHttpRequestContext) HttpRequestContextUtils.currentRequestContext(request);

            asyncLogService.logUserAction(currentUser, relativeUriPath, "", "android/ios");

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
            targetMap.put("client", "android/ios");
            LogCollector.info("agent_user_access_log", targetMap);
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
