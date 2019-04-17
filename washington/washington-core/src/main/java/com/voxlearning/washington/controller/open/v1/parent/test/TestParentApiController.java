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

package com.voxlearning.washington.controller.open.v1.parent.test;

import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.http.client.execute.POST;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.DigestSignUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Hailong Yang on 2015/11/2.
 */
public class TestParentApiController {

    public static final String APP_KEY = "17Parent";
    public static final String SECRET_KEY = "iMMrxI3XMQtd";
    public static final String VER = "1.8.0.0";
    public static final String UUID = "0123456789";

    public static final String LOGIN_URL = "http://localhost:8081/v1/parent/applogin.vpage";
    public static final String SESSION_API_URL = "http://localhost:8081/v1/user/moblie/parent/bindMoblie.vpage";

    public static final String USERNAME = "256206";
    public static final String PASSWORD = "123456";

    public static void main(String[] args) {
        //Map<String, Object> result = login(USERNAME, PASSWORD);
        Map<String, String> sessionApiParamsMap = new HashMap<>();
        sessionApiParamsMap.put("user_code", "15928824060");
        sessionApiParamsMap.put("imei", "30016");

        testHasSessionApi(sessionApiParamsMap, SESSION_API_URL);
    }

    public static void testHasSessionApi(Map<String, String> paramMap, String url) {
        try {
            System.out.println("testHasSessionApi Start...");

            // 计算SIG
            paramMap.put("app_key", APP_KEY);
            String sig = DigestSignUtils.signMd5(paramMap, SECRET_KEY);

            paramMap.put("sig", sig);
            paramMap.put("ver", VER);

            POST post = HttpRequestExecutor.defaultInstance().post(url);
            paramMap.entrySet().forEach(e -> {
                String name = e.getKey();
                String value = e.getValue();
                post.addParameter(name, value);
            });
            System.out.println(post.execute().getResponseString());

            System.out.println("testHasSessionApi End...");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Map<String, Object> login(String parentId, String password) {
        try {
            System.out.println("login Start...");

            // 计算SIG
            Map<String, String> paramMap = new HashMap();
            paramMap.put("app_key", APP_KEY);
            paramMap.put("user_code", parentId);
            paramMap.put("passwd", password);
            String sig = DigestSignUtils.signMd5(paramMap, SECRET_KEY);
            paramMap.put("sig", sig);
            paramMap.put("ver", VER);
            paramMap.put("uuid", UUID);


            POST post = HttpRequestExecutor.defaultInstance().post(LOGIN_URL);
            paramMap.entrySet().forEach(e -> {
                String name = e.getKey();
                String value = e.getValue();
                post.addParameter(name, value);
            });
            String result = post.execute().getResponseString();
            System.out.println(result);
            System.out.println("login End...");
            return JsonUtils.convertJsonObjectToMap(result);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void loginNoPassword() {
        try {
            System.out.println("Test Start...");

            final String appKey = "17Parent";
            final String secretKey = "iMMrxI3XMQtd";

            // 计算SIG
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("app_key", appKey);
            paramMap.put("secret", "PPb+snJoAAswKvAZM/I6cmKeZpKDMWGGcxeBHkG3fwfKATSap64sMzxRcUlXWchUBQGHoM3lqYcsurCRq3PlUw==");
            String sig = DigestSignUtils.signMd5(paramMap, secretKey);
            paramMap.put("sig", sig);
            paramMap.put("ver", "1.8.0.0");

            String apiURL = "http://localhost:8081/v1/parent/twoDimensionCodeLogin.vpage";
            POST post = HttpRequestExecutor.defaultInstance().post(apiURL);
            paramMap.entrySet().forEach(e -> {
                String name = e.getKey();
                String value = e.getValue();
                post.addParameter(name, value);
            });
            System.out.println(post.execute().getResponseString());

            System.out.println("Test End...");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
