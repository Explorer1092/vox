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

package com.voxlearning.washington.controller.open.v1.teacher.test;

import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.http.client.execute.POST;
import com.voxlearning.alps.lang.util.DigestSignUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Shuai Huan on 2015/1/16.
 */
public class TestTeacherMessage {

    public static void main(String[] args) {
        try {
            System.out.println("Test Start...");

            final String appKey = "17Teacher";
            final String secretKey = "gvUKQN1EFXKp";
            final String sessionKey = "3e886795c139045db7019ccdbdb27a33";
            final String clazzId = "32304";

            // 计算SIG
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("app_key", appKey);
            paramMap.put("session_key", sessionKey);
//            paramMap.put("clazz_id", clazzId);
            paramMap.put("user_type", "3");

            String sig = DigestSignUtils.signMd5(paramMap, secretKey);

            paramMap.put("sig", sig);

            String apiURL = "http://localhost:8080/v1/teacher/message/getclazzstudents.vpage";
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
