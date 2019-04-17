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

package com.voxlearning.luffy.interceptor;

import com.voxlearning.alps.spi.webmvc.RequestHandler;
import com.voxlearning.alps.spi.webmvc.ServletRequest;
import com.voxlearning.alps.spi.webmvc.ServletResponse;
import com.voxlearning.alps.webmvc.interceptor.AbstractRequestHandlerInterceptor;
import com.voxlearning.alps.webmvc.support.DefaultContext;
import com.voxlearning.luffy.context.LuffyRequestContext;
import com.voxlearning.luffy.controller.AbstractXcxController;
import com.voxlearning.luffy.controller.MiniProgramAbstractController;
import com.voxlearning.utopia.service.footprint.client.AsyncFootprintServiceClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserServiceClient;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
        if (bean instanceof AbstractXcxController) {
            AbstractXcxController controller = (AbstractXcxController) bean;

            _process(controller.getAsyncFootprintServiceClient(), controller.getUserLoaderClient(), controller.getTeacherLoaderClient(), controller.getUserServiceClient());

            if (!controller.onBeforeControllerMethod()) {
                return false; //skip controller
            }
        } else if (bean instanceof MiniProgramAbstractController) {
            MiniProgramAbstractController controller = (MiniProgramAbstractController) bean;
            _process(controller.getAsyncFootprintServiceClient(), controller.getUserLoaderClient(), controller.getTeacherLoaderClient(), controller.getUserServiceClient());
            if (!controller.onBeforeControllerMethod()) {
                return false; //skip controller
            }
        }
        return true;
    }


    private void _process(AsyncFootprintServiceClient p1, UserLoaderClient p2, TeacherLoaderClient p3, UserServiceClient p4) {
        LuffyRequestContext context = (LuffyRequestContext) DefaultContext.get();
        context.setAsyncFootprintServiceClient(p1);
        context.setUserLoaderClient(p2);
        context.setTeacherLoaderClient(p3);
        context.setUserServiceClient(p4);
    }
}
