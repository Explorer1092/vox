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

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.service.surl.module.monitor.HandlerCountManager;
import com.voxlearning.utopia.service.surl.module.monitor.HandlerType;
import com.voxlearning.utopia.service.surl.service.ShortUrlService;
import com.voxlearning.utopia.service.surl.utils.ShortUrlUtils;
import org.eclipse.jetty.server.Request;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by xin.xin on 9/25/15.
 * 处理短网址请求的重定向
 */
public class ShortUrlDecodeHandler extends ShortUrlHandler {
    private static final Logger logger = LoggerFactory.getLogger(ShortUrlDecodeHandler.class);

    private ShortUrlService shortUrlService;

    public ShortUrlDecodeHandler(ApplicationContext applicationContext) {
        super(applicationContext);
        this.shortUrlService = applicationContext.getBean(ShortUrlService.class);
    }

    @Override
    public void doHandle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        HandlerCountManager.Companion.getInstance().increment(HandlerType.DECODE_HANDLER);

        response.setHeader("Content-Type", "text/html;charset=UTF-8");
        try {
            String code = ShortUrlUtils.getShortUrl(request);
            if (!ShortUrlUtils.isShortUrl(code)) {
                //不是短地址，跳过此handler去WebAppContext
                if (code.length() > 0) {
                    response.sendRedirect("/");
                    baseRequest.setHandled(true);
                }
                return;
            }

            //设置日志标记,需要记录日志
            setLogFlag(request, true);

            String longUrl = shortUrlService.decodeToLongUrl(code);
            if (null == longUrl) {
                response.getWriter().write("无效地址:(");
            } else {
                response.sendRedirect(longUrl);
            }
        } catch (Exception ex) {
            logger.error("short url redirect failed.url:{}", request.getRequestURL(), ex);
            response.getWriter().write("服务器错误");
        }

        baseRequest.setHandled(true);
    }

    @Override
    protected void postHandle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
        super.postHandle(target, baseRequest, request, response);

        if (!needLog(request)) return;

        Map<String, String> logMap = new HashMap<>();

        try {
            logMap.put("type", SHORT_URL_HANDLE_TYPE_DECODE);

            if (null != request.getQueryString()) {
                //兼容不是键值对形式的queryString
                try {
                    logMap.put("queryString", JsonUtils.toJson(UrlUtils.parseQueryString(request.getQueryString())));
                } catch (Exception ex) {
                    logMap.put("queryStringTxt", request.getQueryString());
                }
            }

            String shortUrl = ShortUrlUtils.getShortUrl(request);
            logMap.put("shortUrl", shortUrl);
            logMap.put("longUrl", shortUrlService.decodeToLongUrl(shortUrl));

            super.log(request, response, logMap);
        } catch (Exception ex) {
            logger.warn("short url decode log error,{},{},{}", JsonUtils.toJson(logMap), request.getRequestURL(), request.getQueryString(), ex);
        }
    }
}
