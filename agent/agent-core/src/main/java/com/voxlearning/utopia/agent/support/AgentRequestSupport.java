package com.voxlearning.utopia.agent.support;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.webmvc.util.HttpRequestContextUtils;
import com.voxlearning.utopia.agent.interceptor.AgentHttpRequestContext;

import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

/**
 * AgentRequestSupport
 *
 * @author song.wang
 * @date 2018/8/7
 */
@Named
public class AgentRequestSupport {

    public boolean isMobileRequest(HttpServletRequest request) {
        return isIOSRequest(request) || isAndroidRequest(request);
    }

    public boolean isIOSRequest(HttpServletRequest request) {
        AgentHttpRequestContext context = (AgentHttpRequestContext) HttpRequestContextUtils.currentRequestContext(request);
        String userAgent = request.getHeader(context.getHeaderMap().getOrDefault("user-agent", "User-Agent"));
        if(StringUtils.isNotBlank(userAgent) && StringUtils.contains(userAgent, "Tianji")){
            return StringUtils.containsIgnoreCase(userAgent, "iOS") ||
                    StringUtils.containsIgnoreCase(userAgent, "iPhone") ||
                    StringUtils.containsIgnoreCase(userAgent, "iPad") ||
                    StringUtils.containsIgnoreCase(userAgent, "iPod");
        }else {
            String sys = request.getParameter("sys");
            return StringUtils.containsIgnoreCase(sys, "ios");
        }
    }

    public boolean isAndroidRequest(HttpServletRequest request) {
        AgentHttpRequestContext context = (AgentHttpRequestContext)HttpRequestContextUtils.currentRequestContext(request);
        String userAgent = request.getHeader(context.getHeaderMap().getOrDefault("user-agent", "User-Agent"));
        if(StringUtils.isNotBlank(userAgent) && StringUtils.contains(userAgent, "Tianji")){     // 从 ua 中判断设备类型
            return StringUtils.containsIgnoreCase(userAgent, "Android");
        }else {                  // 从参数中判断设备类型
            String sys = request.getParameter("sys");
            return StringUtils.containsIgnoreCase(sys, "android");
        }
    }

    public boolean isAjaxRequest(HttpServletRequest request){
        AgentHttpRequestContext context = (AgentHttpRequestContext) HttpRequestContextUtils.currentRequestContext(request);
        String requestType = request.getHeader(context.getHeaderMap().getOrDefault("x-requested-with", "X-Requested-With"));
        if(requestType != null && "XMLHttpRequest".equals(requestType)){
            return true;
        }
        return false;
    }

    public String getDeviceId(HttpServletRequest request) {
        String deviceId = "";
        AgentHttpRequestContext context = (AgentHttpRequestContext)HttpRequestContextUtils.currentRequestContext(request);
        String userAgent = request.getHeader(context.getHeaderMap().getOrDefault("user-agent", "User-Agent"));
        if(StringUtils.isNotBlank(userAgent) && StringUtils.contains(userAgent, "Tianji")){   // 从ua中获取设备UUID    h5
            int index = userAgent.indexOf("Tianji");
            if(index > -1){
                String info = userAgent.substring(index + 6).trim();
                if (StringUtils.isNotBlank(info)) {
                    String[] infoArr = StringUtils.split(info, " ");
                    deviceId = infoArr[0];
                }
            }
        }else {   // 从参数中获取 UUID,     客户端调用（原生）
            String uuid = request.getParameter("uuid");
            if(StringUtils.isNotBlank(uuid)){
                deviceId = uuid;
            }
        }
        return deviceId;
    }
}
