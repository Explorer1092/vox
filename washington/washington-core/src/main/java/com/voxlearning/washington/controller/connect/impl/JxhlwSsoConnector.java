/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
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

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.DigestSignUtils;
import com.voxlearning.utopia.service.user.api.constants.SsoConnections;
import com.voxlearning.washington.controller.connect.AbstractSsoConnector;

import javax.inject.Named;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Alex on 15-3-16.
 */
@Named
@Deprecated
public class JxhlwSsoConnector extends AbstractSsoConnector {

    private static final String USER_AUTH_URL = "http://zh.jxhlw.com/api.php?method=xueji.zuoye.getauth&token={0}&sig={1}&format=json";

    @Override
    public MapMessage validateToken(SsoConnections connectionInfo, String token) {
        // Step1 计算sig
        Map<String, String> params = new HashMap<>();
        params.put("app_key", connectionInfo.getClientId());
        params.put("token", token);
        String sig = DigestSignUtils.signMd5(params, connectionInfo.getSecretId());

        // Step2 访问对方网站验证token并获取用户信息
        try {
            String sendUrl = MessageFormat.format(USER_AUTH_URL, token, sig);
            AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().get(sendUrl).execute();
            String validateResponse = response.getResponseString();
            Map<String, Object> apiResult = JsonUtils.fromJson(validateResponse);
            if (apiResult == null || !apiResult.containsKey("zuoye_getauth_response")) {
                logger.warn("token validate api response:{}", validateResponse);
                return MapMessage.errorMessage("token validate failed!");
            }

            Map userData = (Map) apiResult.get("zuoye_getauth_response");
            MapMessage result = MapMessage.successMessage();
            result.add("userId", userData.get("user_id"));
            result.add("userName", userData.get("user_name"));
            String userType = (String) userData.get("user_type");
            if ("1".equals(userType)) {
                result.add("userCode", "1");
            } else {
                result.add("userCode", "2");
            }

            if (userData.containsKey("mobile")) {
                result.add("userMobile", userData.get("mobile"));
            }

            return result;
        } catch (Exception e) {
            logger.error("token validate failed", e);
            return MapMessage.errorMessage(e.getMessage());
        }
    }


    public static void main(String[] args) {
        try {
            String appKey = "17zuoye";
            String secretKey = "52b435269811a0fcf7bc52392867cfd907c851d2";
            String token = "8690c2d851bbf8b4daf5535b6e13762a";
            Map<String, String> params = new HashMap<>();
            params.put("app_key", appKey);
            params.put("token", token);
            String sig = DigestSignUtils.signMd5(params, secretKey);

            System.out.println(MessageFormat.format(USER_AUTH_URL, token, sig));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
