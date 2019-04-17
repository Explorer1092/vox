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

package com.voxlearning.washington.interceptor;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.webmvc.RequestHandler;
import com.voxlearning.alps.spi.webmvc.ServletRequest;
import com.voxlearning.alps.spi.webmvc.ServletResponse;
import com.voxlearning.alps.web.UrlPathUtils;
import com.voxlearning.alps.webmvc.cookie.CookieManager;
import com.voxlearning.alps.webmvc.interceptor.AbstractRequestHandlerInterceptor;
import com.voxlearning.alps.webmvc.mapping.method.MethodMapping;
import com.voxlearning.alps.webmvc.support.DefaultContext;
import com.voxlearning.washington.support.AbstractController;
import com.voxlearning.washington.support.WashingtonRequestContext;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@NoArgsConstructor
public class ControllerRequestInterceptor extends AbstractRequestHandlerInterceptor {

    private final static int ONE_WEEK_EXP = 86400 * 7;

    private final static Set<String> ONLY_HTTP_SUPPORT_PAGES = new HashSet<String>() {
        {
            add("/tts/listening/index.vpage");// 这个页面会请求jiathis的资源，不支持https，所以先强行跳转回http
            add("/student/apps/index.vpage");// 第三方应用不支持https，所以发现是https请求，直接跳转回http
            add("/teacher/resource/share.vpage");// 论坛相关暂不支持https
        }
    };

    private final static Set<String> HTTPS_JUMP_FILTER_OUT_URLS = new HashSet<String>() {
        {
            add("/student/bubbles.vpage");
            add("/userpopup/getuserpopups.vpage");
            add("/student/appsList.vpage");
            add("/teacher/bubbles.vpage");
            add("/flash/gameurl.vpage");
            add("/ucenter/partner.vpage");
            add("/flash/resourceversion.vpage");
        }
    };

    @Override
    public boolean preHandle(ServletRequest request, ServletResponse response, RequestHandler handler) throws Exception {
        MethodMapping methodMapping = (MethodMapping) handler.getMethodMapping();
        String classMethodName = handler.getBeanType().getCanonicalName()
                + "." + methodMapping.getMethod().getName();
        String requestURI = request.getServletRequest().getRequestURI();
        String controllerName = handler.getBeanType().getSimpleName();
        String methodName = methodMapping.getMethod().getName();
        Object bean = handler.getBean();
        if (bean instanceof AbstractController) {
            AbstractController controller = (AbstractController) bean;

            // 一旦setThreadLocalContext了，就要保证 controller.setWebRequestContext(null) 被执行，否则会有内存泄露
            WashingtonRequestContext context = (WashingtonRequestContext) DefaultContext.get();
            context.setRaikouSystem(controller.getRaikouSystem());
            context.setPageBlockContentServiceClient(controller.getPageBlockContentServiceClient());
            context.setUserLoaderClient(controller.getUserLoaderClient());
            context.setTeacherLoaderClient(controller.getTeacherLoaderClient());
            context.setStudentLoaderClient(controller.getStudentLoaderClient());
            context.setResearchStaffLoaderClient(controller.getResearchStaffLoaderClient());
            context.setSensitiveUserDataServiceClient(controller.getSensitiveUserDataServiceClient());
            context.setGrayFunctionManagerClient(controller.getGrayFunctionManagerClient());

            if (!controller.onBeforeControllerMethod()) {
                return false; // skip controller
            }

            // redmine 24536
            // 当通过https访问一些不支持https的页面时，会自动跳转成http
            // 此时会设置一个cookie：jfhttps
            // 当回到其他支持https的页面时，需要检查这一cookie，如果有，则跳转成https，并删除cookie
            handleHttpsJump(request.getServletRequest(), response.getServletResponse(), requestURI, context);
        } else {
            if (!RuntimeMode.isProduction()) {
                throw new RuntimeException(bean.getClass().getName() + " is not an AbstractController. please fix it");
            }
        }

        return true;
    }

    /**
     * 处理页面http/https跳转
     *
     * @param request
     * @param response
     * @param requestURI
     * @param context
     * @throws IOException
     */
    private void handleHttpsJump(HttpServletRequest request, HttpServletResponse response, String requestURI, WashingtonRequestContext context) throws IOException {
        CookieManager cookieManager = context.getCookieManager();
        String jfhttps = cookieManager.getCookie("jfhttps", "");
        if (StringUtils.endsWith(requestURI, ".api")) {
            requestURI = requestURI.substring(0, requestURI.length() - 4) + UrlPathUtils.ANTI_HIJACK_EXT;
        }
        if (StringUtils.startsWith(requestURI, "//")) {// 处理两个"/"情况，小应用会有这个问题
            requestURI = requestURI.substring(1, requestURI.length());
        }
        // 包含jump from https的cookie的访问需要判断是否跳转回https
        if (!StringUtils.isEmpty(jfhttps)) {
            if (!ONLY_HTTP_SUPPORT_PAGES.contains(requestURI)
                    && !HTTPS_JUMP_FILTER_OUT_URLS.contains(requestURI)
                    && !requestURI.startsWith("/student/walker/elf")// 拯救精灵王的各种调用需要过滤掉。。。
                    ) {
                // 删除cookie
                cookieManager.deleteCookieTLD("jfhttps");

                // 跳转成https
                String webAppBaseUrl = context.getWebAppBaseUrl();
                webAppBaseUrl = webAppBaseUrl.replaceAll("^http://", "https://");
                String redirectUrl = webAppBaseUrl + request.getServletPath();
                if (StringUtils.isNotEmpty(request.getQueryString())) {
                    redirectUrl = redirectUrl + "?" + request.getQueryString();
                }
                response.sendRedirect(redirectUrl);
                return;
            }
        }
        if (context.isHttpsRequest()) {
            // 如果是https请求，对于列表中的不支持http的页面，强行跳转成http并设置jfhttps cookie
            if (ONLY_HTTP_SUPPORT_PAGES.contains(requestURI)) {
                // 设置cookie
                cookieManager.setCookieTLD("jfhttps", "1", ONE_WEEK_EXP);

                // 跳转成http
                String webAppBaseUrl = context.getWebAppBaseUrl();
                webAppBaseUrl = webAppBaseUrl.replaceAll("^https://", "http://");
                String redirectUrl = webAppBaseUrl + request.getServletPath();
                if (StringUtils.isNotEmpty(request.getQueryString())) {
                    redirectUrl = redirectUrl + "?" + request.getQueryString();
                }
                response.sendRedirect(redirectUrl);
            }
        }
    }
}
