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

package com.voxlearning.washington.controller.thirdparty.base.cors;


import com.voxlearning.alps.core.util.ReflectionUtils;
import com.voxlearning.alps.spi.webmvc.RequestHandler;
import com.voxlearning.alps.spi.webmvc.ServletRequest;
import com.voxlearning.alps.spi.webmvc.ServletResponse;
import com.voxlearning.alps.webmvc.handler.RequestHandlerResolver;
import com.voxlearning.alps.webmvc.interceptor.AbstractRequestHandlerInterceptor;
import com.voxlearning.alps.webmvc.mapping.method.MethodMapping;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

/**
 * 支持OPTOINS方法执行：http请求方法为{@link org.springframework.http.HttpMethod#OPTIONS}时，如果对应方法上有注解{@link OptionsMethod}，
 * 则会执行对应方法，绕开框架的统一处理；注意，该方法返回值忽略
 *
 * @author Wenlong Meng
 * @since Feb 1, 2019
 */
@Slf4j
public class OptionsMethodInterceptor extends AbstractRequestHandlerInterceptor {

    //Logic

    /**
     * {@link org.springframework.http.HttpMethod#OPTIONS}
     *
     * @param req
     * @param resp
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(ServletRequest req, ServletResponse resp, RequestHandler handler) throws Exception {
        // OPTIONS
        if("OPTIONS".equals(req.getServletRequest().getMethod())){
            Method method = ((MethodMapping)(handler.getMethodMapping())).getMethod();
            boolean optionsMethod = method.isAnnotationPresent(OptionsMethod.class);
            if(optionsMethod){
                try {
                    Object bean = handler.getBean();
                    RequestHandlerResolver handlerResolver = RequestHandlerResolver.getInstance();
                    Object[] args = handlerResolver.resolve(handler, req, resp);
                    ReflectionUtils.invoke(bean, method, args);
                } catch (Throwable ex) {
                    log.error("OPTIONS {}", req.getLookupPath(), ex);
                }
                return false;
            }

        }

        return super.preHandle(req, resp, handler);
    }

}
