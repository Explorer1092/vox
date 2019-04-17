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

package com.voxlearning.washington.interceptor;

import com.voxlearning.alps.spi.webmvc.RequestHandler;
import com.voxlearning.alps.spi.webmvc.ServletRequest;
import com.voxlearning.alps.spi.webmvc.ServletResponse;
import com.voxlearning.alps.webmvc.interceptor.AbstractRequestHandlerInterceptor;
import com.voxlearning.washington.controller.open.AbstractApiController;
import com.voxlearning.washington.controller.open.OpenApiRequestContext;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
public class OpenApiRequestInterceptor extends AbstractRequestHandlerInterceptor {

    @Override
    public boolean preHandle(ServletRequest request, ServletResponse response, RequestHandler handler) throws Exception {
        Object bean = handler.getBean();

        if (bean instanceof AbstractApiController) {
            AbstractApiController controller = (AbstractApiController) bean;
            OpenApiRequestContext requestContext = new OpenApiRequestContext(request.getServletRequest(), response.getServletResponse());
            request.getServletRequest().setAttribute(OpenApiRequestContext.class.getName(), requestContext);

            // do logger
            controller.logApiCallInfo();

            // record login count
            controller.logLoginCount();

            // manually insert userName(userId) into MDC,so that we can see userId in logs
            controller.insertUserNameIntoMDC();
        }
//        if (bean instanceof AbstractParentApiController) {
//            AbstractParentApiController controller = (AbstractParentApiController) bean;
//            if(!controller.onBeforeControllerMethod()){
//                return false;
//            }
//        }
        return true;
    }
}
