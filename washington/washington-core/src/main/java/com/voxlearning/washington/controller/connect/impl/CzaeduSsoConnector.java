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

import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.service.user.api.constants.SsoConnections;
import com.voxlearning.washington.controller.connect.AbstractSsoConnector;

import javax.inject.Named;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Alex on 15-4-3.
 */
@Named
@Deprecated
public class CzaeduSsoConnector extends AbstractSsoConnector {

    private static final String USER_INFO_GET_URL = "http://open.aedu.cn/passport/getuser";

    @Override
    public MapMessage validateToken(SsoConnections connectionInfo, String token) {
        // 根据Token获取用户信息
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("appcode", connectionInfo.getClientId());
        paramMap.put("token", token);
        paramMap.put("format", "json");

        String URL = UrlUtils.buildUrlQuery(USER_INFO_GET_URL, paramMap);
        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().get(URL).execute();
        if (response != null && response.getStatusCode() == 200) {
            Map<String, Object> apiResult = JsonUtils.fromJson(response.getResponseString());
            if (apiResult == null || !apiResult.containsKey("Result") || !(boolean) apiResult.get("Result")) {
                return MapMessage.errorMessage("failed to get user info with result:" + response.getResponseString());
            }

            List userList = (List) apiResult.get("Items");
            if (userList == null || userList.size() == 0) {
                return MapMessage.errorMessage("failed to get user info with result:" + response.getResponseString());
            }

            Map<String, Object> userInfo = (Map) userList.get(0);

            String userId = String.valueOf(userInfo.get("Id"));
            String name = String.valueOf(userInfo.get("UserName"));
            String userType = String.valueOf(userInfo.get("UserRole"));
            if ("老师".equals(userType)) {
                userType = "1";
            } else {
                userType = "3";
            }

            MapMessage result = MapMessage.successMessage();
            result.add("userId", userId);
            result.add("userCode", userType);
            result.add("userName", name);
            return result;
        }

        return MapMessage.errorMessage("failed to get user info with result:" + String.valueOf(response));
    }

    public static void main(String[] args) {
        try {
            String clientId = "5oUerVExJnM=";
            String token = "F095EFED07BC413B992BFF1CA5582D651083375819";
            String sendUrl = MessageFormat.format(USER_INFO_GET_URL, clientId, token);
            System.out.println(sendUrl);

            String responseStr = HttpRequestExecutor.defaultInstance().get(sendUrl).execute().getResponseString();
            System.out.println(responseStr);
            Map<String, Object> apiResult = JsonUtils.fromJson(responseStr);
            if (apiResult == null || !apiResult.containsKey("Result") || !(boolean) apiResult.get("Result")) {
                System.out.println("failed to get user info with result:" + responseStr);
            }

            List userList = (List) apiResult.get("Items");
            if (userList == null || userList.size() == 0) {
                System.out.println("failed to get user info with result:" + responseStr);
            }

            Map<String, Object> userInfo = (Map) userList.get(0);

            String userId = String.valueOf(userInfo.get("Id"));
            String name = String.valueOf(userInfo.get("UserName"));
            String userType = String.valueOf(userInfo.get("UserRole"));
            if ("老师".equals(userType)) {
                userType = "1";
            } else {
                userType = "3";
            }

            MapMessage result = MapMessage.successMessage();
            result.add("userId", userId);
            result.add("userCode", userType);
            result.add("userName", name);

            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}