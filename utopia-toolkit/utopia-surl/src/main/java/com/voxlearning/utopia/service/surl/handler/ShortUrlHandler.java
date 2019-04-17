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

package com.voxlearning.utopia.service.surl.handler;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.runtime.collector.LogCollector;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.service.surl.utils.SurlRequestContext;
import com.voxlearning.utopia.service.surl.utils.useragent.UserAgent;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.springframework.context.ApplicationContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author xin.xin
 * @since 9/25/15
 */
public abstract class ShortUrlHandler extends AbstractHandler {

    public static final String SHORT_URL_CREATE_PATH = "/crt";
    private static final String SHORT_URL_VISIT_COLLECTION_NAME = "surl_logs";
    protected static final String SHORT_URL_HANDLE_TYPE_ENCODE = "encode";
    protected static final String SHORT_URL_HANDLE_TYPE_DECODE = "decode";

    protected ApplicationContext applicationContext;

    public ShortUrlHandler(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    protected UtopiaCache getCache() {
        return CacheSystem.CBS.getCacheBuilder().getCache("flushable");
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        //重置日志标记
        setLogFlag(request, false);

        doHandle(target, baseRequest, request, response);

        postHandle(target, baseRequest, request, response);
    }

    protected abstract void doHandle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException;

    protected void postHandle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
        return;
    }

    protected void log(HttpServletRequest request, HttpServletResponse response, Map<String, String> logParams) {

        fillUserAgentInfo(request, logParams);
        fillRequestInfo(request, response, logParams);

        AlpsThreadPool.getInstance().submit(() -> LogCollector.instance().info(SHORT_URL_VISIT_COLLECTION_NAME, logParams));
    }

    private void fillUserAgentInfo(HttpServletRequest request, Map<String, String> logParams) {
        UserAgent userAgent = new UserAgent(request.getHeader("User-Agent"));
        Map<String, String> agent = new HashMap<>();
        agent.put("browser", userAgent.getBrowser().getName());
        agent.put("browserVersion", userAgent.getBrowserVersion() == null ? "" : userAgent.getBrowserVersion().toString());
        agent.put("os", userAgent.getOperatingSystem().getName());
        agent.put("device", userAgent.getOperatingSystem().getDeviceType().getName());
        logParams.put("userAgent", JsonUtils.toJson(userAgent));
    }

    private void fillRequestInfo(HttpServletRequest request, HttpServletResponse response, Map<String, String> logParams) {
        SurlRequestContext context = new SurlRequestContext(request, response);
        logParams.put("ip", context.getRealRemoteAddr());
        logParams.put("referer", request.getHeader("Referer"));
    }

    protected void setLogFlag(HttpServletRequest request, Boolean flag) {
        request.setAttribute("logFlag", flag);
    }

    protected boolean needLog(HttpServletRequest request) {
        Object flag = request.getAttribute("logFlag");
        if (Objects.isNull(flag)) return false;

        if (flag instanceof Boolean && (Boolean) flag) {
            return true;
        }

        return false;
    }
}
