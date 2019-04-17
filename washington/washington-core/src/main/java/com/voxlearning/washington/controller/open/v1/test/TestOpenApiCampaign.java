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

import com.voxlearning.alps.core.transcoder.Transcoders;
import com.voxlearning.alps.spi.queue.Message;

import java.util.Base64;

/**
 * Created by Administrator on 2014/10/8.
 */
public class TestOpenApiCampaign {

    public static void main(String[] args) {
        try {
            System.out.println("Test Start...");

            String message = "6sQAAEMwJmNvbS52b3hsZWFybmluZy5hbHBzLnNwaS5xdWV1ZS5NZXNzYWdllQltZXNzYWdlSWQIYmxvY2tpbmcEdHlwZQRib2R5CmV4dGVuc2lvbnNgGDU4" +
                    "M2MxZGFkMmRiYjZiOTUxZmU4NmQxN0YFSzpCOkM0vXsiVCI6IlVTRVJfUkVDT1JEIiwiVFMiOjE0ODAzMzQ3NjUxNzcsIlVSIjp7ImlkIjpudWxsLCJ1c2Vy" +
                    "SWQiOjM3MjY4NTI2NCwiaXAiOiIzOS43NS41MC4yMjMiLCJhZGRyZXNzIjpudWxsLCJ1c2VyVHlwZSI6bnVsbCwib3BlcmF0aW9uU291cmNlVHlwZSI6ImFw" +
                    "cCIsImNyZWF0ZVRpbWUiOm51bGwsInVwZGF0ZVRpbWUiOm51bGx9fU0XamF2YS51dGlsLkxpbmtlZEhhc2hNYXAKZW5jb2RlTW9kZUMwMGNvbS52b3hsZWFy" +
                    "bmluZy5hbHBzLnNwaS5xdWV1ZS5NZXNzYWdlRW5jb2RlTW9kZZEEbmFtZWEDQklOWg==";
            byte[] data = Base64.getDecoder().decode(message);
            Message obj = (Message) Transcoders.DEFAULT_TRANSCODER.decode(data);
            Object body = obj.decodeBody();
            System.out.println("aa");


//            final String appKey = "17Teacher";
//            final String sessionKey = "3a8d53fc29f138206031a8fdfae3df02";
//            String secretKey = "gvUKQN1EFXKp";
//
//            // 计算SIG
//            Map<String, String> paramMap = new HashMap<>();
//            paramMap.put("app_key", appKey);
//            paramMap.put("session_key", sessionKey);
//            //paramMap.put("duty", "副校长");
//
//            String sig = DigestSignUtils.signMd5(paramMap, secretKey);
//
//            paramMap.put("sig", sig);
//
//            String apiURL = "http://www.test.17zuoye.net/v1/teacher/user/profile.vpage";
//            POST post = HttpRequestExecutor.defaultInstance().post(apiURL);
//            paramMap.entrySet().forEach(e -> {
//                String name = e.getKey();
//                String value = e.getValue();
//                post.addParameter(name, value);
//            });
//            System.out.println(post.execute().getResponseString());

//            paramMap = new HashMap<>();
//            paramMap.put("app_key", appKey);
//            paramMap.put("session_key", sessionKey);
//            sig = DigestSignUtils.signMd5(paramMap, secretKey);
//            paramMap.put("sig", sig);
//
//            apiURL = "http://www.test.17zuoye.net/v1/teacher/user/profile.vpage";
//            POST post1 = HttpRequestExecutor.defaultInstance().post(apiURL);
//            paramMap.entrySet().forEach(e -> {
//                String name = e.getKey();
//                String value = e.getValue();
//                post1.addParameter(name, value);
//            });
//            System.out.println(post.execute().getResponseString());

            System.out.println("Test End...");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
