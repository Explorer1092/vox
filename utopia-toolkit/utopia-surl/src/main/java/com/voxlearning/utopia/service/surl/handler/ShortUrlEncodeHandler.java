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

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.DigestSignUtils;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.service.surl.Configure;
import com.voxlearning.utopia.service.surl.module.monitor.HandlerCountManager;
import com.voxlearning.utopia.service.surl.module.monitor.HandlerType;
import com.voxlearning.utopia.service.surl.service.ShortUrlService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.UrlValidator;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.server.Request;
import org.springframework.context.ApplicationContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.*;

/**
 * Created by xin.xin on 9/25/15.
 * 处理生成短网址请求
 */
@Slf4j
public class ShortUrlEncodeHandler extends ShortUrlHandler {
    private static final String REQUEST_FIELD_URL = "url";
    private static final String REQUEST_FIELD_TIMESTAMP = "timestamp";
    private static final String REQUEST_FIELD_SIGN = "sign";
    private static final String REQUEST_FIELD_NONCE = "nonceStr";

    private ShortUrlService shortUrlService;

    public ShortUrlEncodeHandler(ApplicationContext applicationContext) {
        super(applicationContext);

        this.shortUrlService = applicationContext.getBean(ShortUrlService.class);
    }

    @Override
    public void doHandle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        HandlerCountManager.Companion.getInstance().increment(HandlerType.ENCODE_HANDLER);

        String longUrl;
        longUrl = getLongUrl(request);

        if (null == longUrl) {
            log.warn("long url must not be null.");
            response(baseRequest, response, "");
            return;
        }

        try {
            longUrl = formatUrl(longUrl);

            if (!validateUrl(longUrl)) {
                log.warn("long url {} is illegal url.", longUrl);
                response(baseRequest, response, "");
                return;
            }

            if (isBlocked(longUrl)) {
                response(baseRequest, response, "");
                return;
            }

            if (!isWihte(longUrl) && !validateSignature(request)) {
                //没在白名单中,验签名
                response(baseRequest, response, "");
                return;
            }

            //设置日志标记,需要记录日志
            setLogFlag(request, true);

            String code = shortUrlService.encodeToShortUrl(longUrl);

            request.setAttribute("code", code);

            response(baseRequest, response, null == code ? "" : code);
        } catch (Exception e) {
            log.error("short url encode failed. longUrl:{}", longUrl, e);
        }
        response(baseRequest, response, "");
    }

    @Override
    protected void postHandle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
        super.postHandle(target, baseRequest, request, response);

        if (!needLog(request)) return;

        Map<String, String> logMap = new HashMap<>();
        try {
            logMap.put("type", SHORT_URL_HANDLE_TYPE_ENCODE);
            logMap.put("longUrl", getLongUrl(request));
            if (null != request.getAttribute("code")) {
                logMap.put("shortUrl", request.getAttribute("code").toString());
            }

            super.log(request, response, logMap);
        } catch (Exception ex) {
            log.error("Short Url Encode log error,{}", JsonUtils.toJson(logMap), ex);
        }
    }

    private boolean validateSignature(HttpServletRequest request) {
        Map<String, String> signParams = new TreeMap<>();

        String sign;
        if (HttpMethod.GET.name().equals(request.getMethod())) {
            Map<String, String> params = UrlUtils.parseQueryString(request.getQueryString());

            if (!requireParams(params.keySet())) return false;

            signParams.put(REQUEST_FIELD_URL, params.get(REQUEST_FIELD_URL));
            signParams.put(REQUEST_FIELD_TIMESTAMP, params.get(REQUEST_FIELD_TIMESTAMP));
            signParams.put(REQUEST_FIELD_NONCE, params.get(REQUEST_FIELD_NONCE));
            sign = params.get(REQUEST_FIELD_SIGN);
        } else {
            if (!requireParams(request.getParameterMap().keySet())) return false;

            signParams.put(REQUEST_FIELD_URL, request.getParameter(REQUEST_FIELD_URL));
            signParams.put(REQUEST_FIELD_TIMESTAMP, request.getParameter(REQUEST_FIELD_TIMESTAMP));
            signParams.put(REQUEST_FIELD_NONCE, request.getParameter(REQUEST_FIELD_NONCE));
            sign = request.getParameter(REQUEST_FIELD_SIGN);
        }

        if (!sign.equals(DigestSignUtils.signMd5(signParams, Configure.getSignatureKey()))) return false;

        return !Instant.now().isAfter(Instant.ofEpochMilli(Long.parseLong(signParams.get(REQUEST_FIELD_TIMESTAMP))).plusSeconds(5 * 60));
    }

    private boolean requireParams(Collection<String> keySet) {
        return keySet.contains(REQUEST_FIELD_URL) && keySet.contains(REQUEST_FIELD_NONCE) && keySet.contains(REQUEST_FIELD_TIMESTAMP) && keySet.contains(REQUEST_FIELD_SIGN);
    }

    private boolean isBlocked(String longUrl) {
        List<String> blockDomains = Configure.getBlockDomainList();
        if (CollectionUtils.isEmpty(blockDomains)) return false;

        String mainDomain = getMainDomain(longUrl);

        if (longUrl.endsWith(mainDomain) || longUrl.length() == mainDomain.length() + 1) {
            return true; //只有域名,不必生成短地址
        }

        for (String domain : blockDomains) {
            if (mainDomain.endsWith(domain)) return true;
        }

        return false;
    }

    private boolean isWihte(String longUrl) {
        List<String> wihteDomains = Configure.getWhiteDomainList();
        if (CollectionUtils.isEmpty(wihteDomains)) return false;

        String mainDomain = getMainDomain(longUrl);
        if (longUrl.endsWith(mainDomain) || longUrl.length() == mainDomain.length() + 1) {
            return false; //只有域名,不必生成短地址
        }

        for (String domain : wihteDomains) {
            if (mainDomain.endsWith(domain)) return true;
        }

        return false;
    }

    private String getMainDomain(String longUrl) {
        int index;
        if (longUrl.startsWith("http://")) {
            index = longUrl.indexOf("/", 7);
        } else if (longUrl.startsWith("https://")) {
            index = longUrl.indexOf("/", 8);
        } else {
            index = longUrl.indexOf("/");
        }

        if (index <= 0) {
            return longUrl;
        }

        return longUrl.substring(0, index);
    }

    private void response(Request baseRequest, HttpServletResponse response, String content) throws IOException {
        response.getWriter().write(content);
        baseRequest.setHandled(true);
    }

    private boolean validateUrl(String longUrl) {
        if (!UrlValidator.getInstance().isValid(longUrl)) return false;

        String mainDomain = getMainDomain(longUrl);
        return !(longUrl.endsWith(mainDomain) || longUrl.length() == mainDomain.length() + 1);

    }

    private String formatUrl(String longUrl) {
        //仅支持http://或https://
        if (!longUrl.startsWith("http://") && !longUrl.startsWith("https://")) {
            longUrl = "http://" + longUrl;
        }
        return longUrl;
    }

    private String getLongUrl(HttpServletRequest request) {
        String longUrl;
        if (HttpMethod.GET.name().equals(request.getMethod())) {
            Map<String, String> params = UrlUtils.parseQueryString(request.getQueryString());
            longUrl = params.get(REQUEST_FIELD_URL);
        } else {
            longUrl = request.getParameter(REQUEST_FIELD_URL);
        }
        return longUrl;
    }
}
