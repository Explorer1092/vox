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

package com.voxlearning.utopia.admin.interceptor;

import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.runtime.collector.LogCollector;
import com.voxlearning.alps.spi.webmvc.RequestHandler;
import com.voxlearning.alps.spi.webmvc.ServletRequest;
import com.voxlearning.alps.spi.webmvc.ServletResponse;
import com.voxlearning.alps.webmvc.interceptor.AbstractRequestHandlerInterceptor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by XiaoPeng.Yang on 15-4-24.
 */
@Slf4j
@NoArgsConstructor
public class SiteInterceptor extends AbstractRequestHandlerInterceptor {

    @Override
    public boolean preHandle(ServletRequest request, ServletResponse response, RequestHandler handler) throws Exception {
        logApiCallInfo(request.getServletRequest());
        return true;
    }

    public void logApiCallInfo(HttpServletRequest request) {

        Map<String, String> loggingInfo = new HashMap<>();
        String apiName = request.getRequestURI();
        if (apiName.indexOf(".") > 0) {
            apiName = apiName.substring(0, apiName.indexOf("."));
        }
        loggingInfo.put("api_name", apiName);
        loggingInfo.put("mode", RuntimeMode.current().name());
        LogCollector.instance().info("vox_admin_site_statistic", loggingInfo);
    }

}