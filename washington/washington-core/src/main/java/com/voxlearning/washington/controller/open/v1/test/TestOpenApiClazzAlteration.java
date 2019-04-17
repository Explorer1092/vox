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

package com.voxlearning.washington.controller.open.v1.test;

import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.http.client.execute.POST;
import com.voxlearning.alps.lang.util.DigestSignUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Yuechen Wang on 2016-05-12.
 */
public class TestOpenApiClazzAlteration {

    public static void main(String[] args) {
        try {
            System.out.println("Test Start...");

            final String appKey = "UserService";
            String secretKey = "crd599Ek1SHk";
            String alterationId = "2805";
            String[] alterationResult = new String[]{"", "wrong", "dial_success", "dial_failed", "dial_not_exist"};
            String[] alterationOption = new String[]{"", "wrong", "approve", "reject", "fake"};

            System.out.println("Testing process api...");
//            testProcessAll(appKey, secretKey, "0", alterationResult, alterationOption);
            testProcessSingle(appKey, secretKey, alterationId,
                    alterationResult[2], alterationOption[4]);

            //======================================================================
//            System.out.println("Testing cipher_key api...");
//            testGetCipher(appKey, secretKey);
//            System.out.println("Test End...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void testProcessAll(String appKey, String secretKey, String alterationId
            , String[] alterationResult, String[] alterationOption) {
        System.out.println(String.format(" %10s | %15s | %10s | %s ",
                "id", "result", "option", "json"));
        for (String result : alterationResult) {
            for (String option : alterationOption) {
                testProcessSingle(appKey, secretKey, alterationId, result, option);
            }
        }
    }

    private static void testProcessSingle(String appKey, String secretKey, String alterationId
            , String result, String option) {
        // 计算SIG
        Map<String, String> processParam = new HashMap<>();
        processParam.put("app_key", appKey);
        processParam.put("alteration_id", alterationId);
        processParam.put("alteration_result", result);
        if ("dial_success".equals(result)) {
            processParam.put("alteration_option", option);
        }
        String sig = DigestSignUtils.signMd5(processParam, secretKey);
        processParam.put("sig", sig);

        String URL = "http://localhost:8081/v1/clazz/alteration/process.vpage";
        POST processPost = HttpRequestExecutor.defaultInstance().post(URL);
        processParam.entrySet().forEach(e -> {
            String name = e.getKey();
            String value = e.getValue();
            processPost.addParameter(name, value);
        });
        System.out.println(String.format(" %10s | %15s | %10s | %s ",
                alterationId, result, option, processPost.execute().getResponseString()));
    }

    private static void testGetCipher(String appKey, String secretKey) {
        // FIXME correctToken = businessCacheClient.getBusinessCacheSystem().CBS.unflushable.load("CLAZZ_TEACHER_ALTERATION_KEY")
        String[] cipherToken = new String[]{"", "FA2C26AD", "C8F6F008", "A23S2133"};
        for (String token : cipherToken) {
            Map<String, String> tokenParam = new HashMap<>();
            tokenParam.put("app_key", appKey);
            tokenParam.put("cipher_token", token);
            String sig = DigestSignUtils.signMd5(tokenParam, secretKey);

            tokenParam.put("sig", sig);

            String tokenURL = "http://localhost:8081/v1/clazz/alteration/cipher_key.vpage";
            POST tokenPost = HttpRequestExecutor.defaultInstance().post(tokenURL);
            tokenParam.entrySet().forEach(e -> {
                String name = e.getKey();
                String value = e.getValue();
                tokenPost.addParameter(name, value);
            });

            System.out.println("token:" + token + " json:" + tokenPost.execute().getResponseString());
        }
    }
}
