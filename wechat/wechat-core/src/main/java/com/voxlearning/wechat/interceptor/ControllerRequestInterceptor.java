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

package com.voxlearning.wechat.interceptor;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.spi.core.RuntimeModeLoader;
import com.voxlearning.alps.spi.webmvc.RequestHandler;
import com.voxlearning.alps.spi.webmvc.ServletRequest;
import com.voxlearning.alps.spi.webmvc.ServletResponse;
import com.voxlearning.alps.webmvc.interceptor.AbstractRequestHandlerInterceptor;
import com.voxlearning.alps.webmvc.mapping.method.MethodMapping;
import com.voxlearning.alps.webmvc.support.DefaultContext;
import com.voxlearning.wechat.anotation.CorsHeader;
import com.voxlearning.wechat.context.WechatRequestContext;
import com.voxlearning.wechat.controller.AbstractController;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.reflect.Method;

/**
 * @author Xin Xin
 * @since 10/15/15
 */
@Slf4j
@NoArgsConstructor
public class ControllerRequestInterceptor extends AbstractRequestHandlerInterceptor {

    @Override
    public boolean preHandle(ServletRequest request, ServletResponse response, RequestHandler handler) throws Exception {
        Object bean = handler.getBean();
        if (bean instanceof AbstractController) {
            AbstractController controller = (AbstractController) bean;

            WechatRequestContext context = (WechatRequestContext) DefaultContext.get();
            context.setAsyncFootprintServiceClient(controller.getAsyncFootprintServiceClient());
            context.setUserLoaderClient(controller.getUserLoaderClient());
            context.setUserServiceClient(controller.getUserServiceClient());
            context.setTeacherLoaderClient(controller.getTeacherLoaderClient());

            //处理跨域请求
            setCorsHeaders(controller, handler, context);

            if (!controller.onBeforeControllerMethod()) {
                return false; //skip controller
            }
        }
        return true;
    }

    private static String allowOrigin;
    static {
        if (RuntimeModeLoader.getInstance().isProduction()) {
            allowOrigin = "https://www.17zuoye.com";
        } else if (RuntimeModeLoader.getInstance().isStaging()) {
            allowOrigin = "https://www.staging.17zuoye.net";
        } else if (RuntimeModeLoader.getInstance().isTest()) {
            allowOrigin = "https://www.test.17zuoye.net";
        }else {
            allowOrigin = "https://www.17zuoye.com";
        }
    }

    private void setCorsHeaders(AbstractController controller, RequestHandler handler, WechatRequestContext context){

        CorsHeader annotation = controller.getClass().getAnnotation(CorsHeader.class);
        Method method = ((MethodMapping) handler.getMethodMapping()).getMethod();
        if (annotation == null) {
            annotation = method.getAnnotation(CorsHeader.class);
            if (null == annotation) {
                return;
            }
        }
        RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
        if (requestMapping == null){
            throw new IllegalArgumentException("no RequestMapping annotation found!");
        }
        RequestMethod[] method1 = requestMapping.method();
        String methodStr = StringUtils.join(method1, ",");
        context.getResponse().addHeader("Access-Control-Allow-Origin", allowOrigin);
        context.getResponse().addHeader("Access-Control-Allow-Methods", methodStr);
        context.getResponse().addHeader("Access-Control-Allow-Headers", "x-requested-with");
        context.getResponse().addHeader("Access-Control-Max-Age", "1800");
        context.getResponse().addHeader("Access-Control-Allow-Credentials", "true");
    }
}
