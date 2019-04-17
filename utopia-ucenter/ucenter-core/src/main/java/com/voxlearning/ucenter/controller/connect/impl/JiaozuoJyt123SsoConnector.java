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

import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.DigestSignUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.ucenter.controller.connect.AbstractSsoConnector;
import com.voxlearning.utopia.service.user.api.constants.SsoConnections;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 * 焦作家校通对接
 * Created by Alex on 14-12-29.
 */
@Named
public class JiaozuoJyt123SsoConnector extends AbstractSsoConnector {

    private static final String USER_GET_URL_TEST = "http://115.56.231.253:8080/third/17zuoye.jsp";
    private static final String USER_GET_URL_PROD = "http://www.jyt123.com/third/17zuoye.jsp";

    @Override
    public MapMessage validateToken(SsoConnections connectionInfo, String token) {
        // Step1 生成验证Token用的SIG
        Map<String, String> sigParams = new HashMap<>();
        sigParams.put("token", token);
        sigParams.put("app_key", connectionInfo.getClientId());
        String sig = DigestSignUtils.signMd5(sigParams, SsoConnections.GWCHINA.getSecretId());

        // Step2 调用对方API获取用户信息
        String sendUrl = USER_GET_URL_TEST;
        if (RuntimeMode.isStaging() || RuntimeMode.isProduction()) {
            sendUrl = USER_GET_URL_PROD;
        }
        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance()
                .post(sendUrl)
                .addParameter("token", token)
                .addParameter("sig", sig)
                .execute();
        String validateResponse = response.getResponseString();
        Map<String, Object> apiResult = JsonUtils.fromJson(validateResponse);
        if (apiResult == null || !apiResult.containsKey("id") || apiResult.get("id") == null) {
            logger.warn("token validate api response:{}", validateResponse);
            return MapMessage.errorMessage("token validate failed!");
        }

        String userId = String.valueOf(apiResult.get("id"));
        String userName = String.valueOf(apiResult.get("name"));
        String type = String.valueOf(apiResult.get("type"));

        MapMessage result = MapMessage.successMessage();
        result.add("userId", userId);
        result.add("userName", userName);
        if ("1".equals(type)) {
            result.add("userCode", "3");  // 学生
        } else if ("2".equals(type)) {
            result.add("userCode", "1");  // 老师
        } else {
            return MapMessage.errorMessage("unknown user type:" + type);
        }

        return result;
    }

}
