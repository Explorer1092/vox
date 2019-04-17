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

package com.voxlearning.ucenter.controller.connect.impl;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.ucenter.controller.connect.AbstractSsoConnector;
import com.voxlearning.utopia.service.user.api.constants.SsoConnections;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.inject.Named;
import java.util.List;
import java.util.Map;

/**
 * JSE教育平台SSO对接
 *
 * @author Jia HuanYin
 * @since 2015/6/16
 */
@Named
public class YzeduSsoConnector extends AbstractSsoConnector {

    private static final String DOMAIN_TEST = "http://apiyz.demo.jiaoyu365.net";
    private static final String DOMAIN = "http://api.jse.edu.cn";
    private static final String ACCESS_TOKEN_URI = DOMAIN + "/oauth2/access_token";
    private static final String USER_INFO_API = DOMAIN + "/user/real/baseinfo.json";

    private static final String CLIENT_ID = SsoConnections.Yzedu.getClientId();
    private static final String CLIENT_SECRET = SsoConnections.Yzedu.getSecretId();
    private static final String GRANT_TYPE = "authorization_code";

    @Override
    public MapMessage validateToken(SsoConnections connectionInfo, String token) {
        if (StringUtils.isBlank(token)) {
            logger.error("validateToken - Blank token");
            return MapMessage.errorMessage("Blank token");
        }

        Map<String, Object> params = MiscUtils.m("client_id", CLIENT_ID, "client_secret", CLIENT_SECRET,
                "grant_type", GRANT_TYPE, "code", token);
        String URL = UrlUtils.buildUrlQuery(ACCESS_TOKEN_URI, params);
        String resp = HttpRequestExecutor.defaultInstance().get(URL).execute().getResponseString();
        Map<String, Object> respMap = JsonUtils.fromJson(resp);
        if (respMap == null) {
            logger.error("validateToken - Illegal access token resp = {}", resp);
            return MapMessage.errorMessage("Illegal access token response");
        }
        String accessToken = respMap.get("access_token") == null ? null : String.valueOf(respMap.get("access_token"));
        if (StringUtils.isBlank(accessToken)) {
            logger.error("validateToken - Got blank accessToken with resp = {}", resp);
            return MapMessage.errorMessage("Got blank accessToken");
        }

        params = MiscUtils.m("access_token", accessToken);
        URL = UrlUtils.buildUrlQuery(USER_INFO_API, params);
        resp = HttpRequestExecutor.defaultInstance().get(URL).execute().getResponseString();
        List<UserInfo> respList = JsonUtils.fromJsonToList(resp, UserInfo.class);
        if (respList == null || respList.isEmpty()) {
            logger.error("validateToken - Illegal user info resp = {}", resp);
            return MapMessage.errorMessage("Illegal user info response");
        }
        UserInfo userInfo = respList.get(0);
        String id = userInfo.getId();
        if (StringUtils.isBlank(id)) {
            logger.error("validateToken - Blank id with user info resp = {}", resp);
            return MapMessage.errorMessage("Got blank id");
        }
        String name = userInfo.getName();
        if (StringUtils.isBlank(name)) {
            name = "";
        }

        MapMessage message = MapMessage.successMessage();
        message.add("userId", id);
        message.add("userName", name);
        message.add("userCode", ""); // 用户身份未知
        return message;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    private static class UserInfo {
        private String id;
        private String name;
    }
}