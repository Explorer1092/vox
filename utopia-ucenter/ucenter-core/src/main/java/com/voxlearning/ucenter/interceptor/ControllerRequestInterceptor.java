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

package com.voxlearning.ucenter.interceptor;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.webmvc.RequestHandler;
import com.voxlearning.alps.spi.webmvc.ServletRequest;
import com.voxlearning.alps.spi.webmvc.ServletResponse;
import com.voxlearning.alps.webmvc.cookie.CookieManager;
import com.voxlearning.alps.webmvc.interceptor.AbstractRequestHandlerInterceptor;
import com.voxlearning.alps.webmvc.support.DefaultContext;
import com.voxlearning.ucenter.support.context.UcenterRequestContext;
import com.voxlearning.ucenter.support.controller.AbstractController;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
public class ControllerRequestInterceptor extends AbstractRequestHandlerInterceptor {

    @Override
    public boolean preHandle(ServletRequest request, ServletResponse response, RequestHandler handler) throws Exception {

        Object bean = handler.getBean();
        if (bean instanceof AbstractController) {
            AbstractController controller = (AbstractController) bean;

            // 一旦setThreadLocalContext了，就要保证 controller.setWebRequestContext(null) 被执行，否则会有内存泄露
            UcenterRequestContext context = (UcenterRequestContext) DefaultContext.get();
            context.setRaikouSystem(controller.getRaikouSystem());
            context.setPageBlockContentServiceClient(controller.getPageBlockContentServiceClient());
            context.setUserLoaderClient(controller.getUserLoaderClient());
            context.setTeacherLoaderClient(controller.getTeacherLoaderClient());
            context.setStudentLoaderClient(controller.getStudentLoaderClient());
            context.setResearchStaffLoaderClient(controller.getResearchStaffLoaderClient());
            context.setGrayFunctionManagerClient(controller.getGrayFunctionManagerClient());

            if (!controller.onBeforeControllerMethod()) {
                return false; // skip controller
            }

            // redmine 24536
            // 当通过https访问一些不支持https的页面时，会自动跳转成http
            // 此时会设置一个cookie：jfhttps
            // 当回到其他支持https的页面时，需要检查这一cookie，如果有，则跳转成https，并删除cookie
            // 用户中心这边因为没有不支持https的url，所以不需要处理特殊url集合
            CookieManager cookieManager = context.getCookieManager();
            String jfhttps = cookieManager.getCookie("jfhttps", "");
            if (!StringUtils.isEmpty(jfhttps)) {
                // 删除cookie
                cookieManager.deleteCookieTLD("jfhttps");

                // 跳转成https
                String webAppBaseUrl = context.getWebAppBaseUrl();
                webAppBaseUrl = webAppBaseUrl.replaceAll("^http://", "https://");
                String redirectUrl = webAppBaseUrl + request.getServletRequest().getServletPath();
                if (StringUtils.isNotEmpty(request.getServletRequest().getQueryString())) {
                    redirectUrl = redirectUrl + "?" + request.getServletRequest().getQueryString();
                }
                response.getServletResponse().sendRedirect(redirectUrl);
            }
        } else {
            if (!RuntimeMode.isProduction()) {
                throw new RuntimeException(bean.getClass().getName() + " is not an AbstractController. please fix it");
            }
        }

        return true;
    }
}
