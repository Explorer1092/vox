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
import com.voxlearning.washington.controller.specialteacher.AbstractSpecialTeacherController;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
public class SpecialTeacherRequestInterceptor extends AbstractRequestHandlerInterceptor {

    @Override
    public boolean preHandle(ServletRequest request, ServletResponse response, RequestHandler handler) throws Exception {
        Object bean = handler.getBean();
        if (bean instanceof AbstractSpecialTeacherController) {
            AbstractSpecialTeacherController controller = (AbstractSpecialTeacherController) bean;

            // do logger
            controller.logOperation();

        }
        return true;
    }
}
