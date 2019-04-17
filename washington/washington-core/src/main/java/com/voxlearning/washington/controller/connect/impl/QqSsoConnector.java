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

package com.voxlearning.washington.controller.connect.impl;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.alps.webmvc.support.DefaultContext;
import com.voxlearning.alps.webmvc.support.context.UtopiaHttpRequestContext;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.user.api.constants.SsoConnections;
import com.voxlearning.washington.controller.connect.AbstractSsoConnector;

import javax.inject.Named;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Named
@Deprecated
public class QqSsoConnector extends AbstractSsoConnector {
    public static final String ACCESSTOKEN_URL = "https://graph.qq.com/oauth2.0/token";
    public static final String OPENID_URL = "https://graph.qq.com/oauth2.0/me";
    public static final String GET_USER_INFO_URL = "https://graph.qq.com/user/get_user_info";
    public static final String REDIRECT_URI = "/qq/authorize.vpage";
    public static final String REDIRECT_URI_TEST = "qq.test.17zuoye.net/qq/authorize.vpage";
    public static final String GRANT_TYPE = "authorization_code";
    public static final String FORMAT = "json";

    @Override
    public MapMessage validateToken(SsoConnections connectionInfo, String token) {
        if (null == connectionInfo || StringUtils.isBlank(token)) {
            return MapMessage.errorMessage();
        }

        // 用authorizationCode获取accesToken
        String accessToken = this.getAccessToken(connectionInfo, token);
        if (StringUtils.isBlank(accessToken)) {
            return MapMessage.errorMessage();
        }

        // 用accessToken获取openId
        String openId = this.getOpenId(accessToken);
        if (StringUtils.isBlank(openId)) {
            return MapMessage.errorMessage();
        }

        // 用accessToken和openId获取用户个人信息
        Map<String, Object> userInfoMap = this.getUserInfo(connectionInfo, accessToken, openId);
        if (null == userInfoMap) {
            return MapMessage.errorMessage();
        }

        return MapMessage.successMessage().add("openId", openId)
                .add("img", ConversionUtils.toString(userInfoMap.get("figureurl_qq_2")))
                .add("nickname", ConversionUtils.toString(userInfoMap.get("nickname")));
    }

    // 如果成功了responseString是个字符串，如果失败了responseJson是callback(json)
    private String getAccessToken(SsoConnections sc, String token) {
        UtopiaHttpRequestContext context = DefaultContext.get();
        Map<String, Object> params = MiscUtils.m("client_id", sc.getClientId(), "client_secret", sc.getSecretId(),
                "grant_type", GRANT_TYPE, "code", token, "redirect_uri", ProductConfig.getMainSiteBaseUrl() + REDIRECT_URI);
        if (RuntimeMode.lt(Mode.STAGING)) {
            params.put("redirect_uri", REDIRECT_URI_TEST);
        }
        String URL = UrlUtils.buildUrlQuery(ACCESSTOKEN_URL, params);
        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().get(URL).execute();
        String rs = response.getResponseString();
        if (StringUtils.isNotBlank(rs)) {
            if (!StringUtils.contains(rs, "callback")) { // 成功了，返回的是个字符串，字符串中有个access_token
                Matcher m1 = Pattern.compile("^access_token=(\\w+)&expires_in=(\\w+)&refresh_token=(\\w+)$").matcher(rs);
                if (m1.find()) {
                    return m1.group(1);
                } else {
                    Matcher m2 = Pattern.compile("^access_token=(\\w+)&expires_in=(\\w+)$").matcher(rs);
                    if (m2.find()) {
                        return m2.group(1);
                    }
                }
            } else {
                Map<String, Object> map = JsonUtils.fromJson(StringUtils.substringBetween(response.getResponseString(), "(", ")").trim());
                logger.error("GET ACCESS TOKEN ERROR: ERROR CODE [{}], ERROR MESSAGE [{}]",
                        map.get("error"), map.get("error_description"));
                return null;
            }
        }
        return null;
    }

    // responseJson是callback(json)
    private String getOpenId(String accessToken) {
        Map<String, Object> params = MiscUtils.m("access_token", accessToken);
        String URL = UrlUtils.buildUrlQuery(OPENID_URL, params);
        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().get(URL).execute();
        String temp = StringUtils.substringBetween(response.getResponseString(), "(", ")");
        Map<String, Object> map = null;
        if (StringUtils.isNotBlank(temp)) {
            map = JsonUtils.fromJson(temp.trim());
        }
        if (null == map) {
            logger.error("GET OPEN_ID ERROR, THE RESPONSESTRING IS [{}]", response.getResponseString());
            return null;
        }
        if (!map.containsKey("openid")) {
            logger.error("GET OPEN_ID ERROR: ERROR CODE [{}], ERROR MESSAGE [{}]",
                    map.get("error"), map.get("error_description"));
            return null;
        }
        return ConversionUtils.toString(map.get("openid"));
    }

    // responseJson是json
    private Map<String, Object> getUserInfo(SsoConnections sc, String accessToken, String openId) {
        Map<String, Object> params = MiscUtils.m("access_token", accessToken, "oauth_consumer_key",
                sc.getClientId(), "openid", openId, "format", FORMAT);
        String URL = UrlUtils.buildUrlQuery(GET_USER_INFO_URL, params);
        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().get(URL).execute();
        Map<String, Object> map = JsonUtils.fromJson(response.getResponseString());
        if (null == map) {
            String temp = StringUtils.substringBetween(response.getResponseString(), "(", ")");
            if (StringUtils.isNotBlank(temp)) {
                map = JsonUtils.fromJson(temp.trim());
            }
        }
        if (null == map) {
            logger.error("GET USER INFO ERROR, THE RESPONSESTRING IS [{}]", response.getResponseString());
            return null;
        }
        if (!map.containsKey("nickname") || !map.containsKey("figureurl_qq_2")) {
            logger.error("GET USER INFO ERROR: ERROR CODE [{}], ERROR MESSAGE [{}]",
                    map.get("error"), map.get("error_description"));
            return null;
        }
        return map;
    }
}
