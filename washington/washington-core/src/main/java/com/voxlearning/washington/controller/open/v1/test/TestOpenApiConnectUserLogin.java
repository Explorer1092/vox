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
 * Created by Alex on 14-10-27.
 */
public class TestOpenApiConnectUserLogin {
    public static void main(String[] args) {
        try {
            System.out.println("Test Start...");

            final String appKey = "StudyCraft";
            String secretKey = "8Un3gjOH67nY";

            final String userType = "3"; //1：教师，2：家长，3：学生
            final String userCode = "StudyC0001";
            String nickName = "口袋学社昵称";
            String passwd = "2";

            // 计算SIG
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("app_key", appKey);
            //paramMap.put("user_type", userType);
            paramMap.put("user_code", userCode);
            //paramMap.put("nick_name", nickName);
            paramMap.put("passwd", passwd);

            String sig = DigestSignUtils.signMd5(paramMap, secretKey);

            paramMap.put("sig", sig);

            String apiURL = "http://localhost:8080/v1/connect/user/login.vpage";
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
