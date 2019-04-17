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
 * Created by Alex on 14-12-10.
 */
public class TestOpenApiSaveFeedback {
    public static void main(String[] args) {
        try {
            System.out.println("Test Start...");

            final String appKey = "17ZuoyeMobile";
            final String sessionKey = "bd5619baeef92c79848f1d81221ea436";
            String secretKey = "Ly3EdAWHZVXZ";
            final String type = "移动版问题";

            final String content = "sdfsdf";
            final String mobile = "sdffsdf";
            final String qq = "sdfsdf";

            // 计算SIG
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("app_key", appKey);
            paramMap.put("session_key", sessionKey);
            paramMap.put("content", content);
            paramMap.put("mobile", mobile);
            paramMap.put("qq", qq);
            paramMap.put("type", type);


            String sig = DigestSignUtils.signMd5(paramMap, secretKey);

            paramMap.put("sig", sig);

            String apiURL = "http://www.test.17zuoye.net/v1/user/feedback/add.api";
            POST post = HttpRequestExecutor.defaultInstance().post(apiURL);
            paramMap.entrySet().forEach(e -> {
                String name = e.getKey();
                String value = e.getValue();
                post.addParameter(name, value);
            });
            System.out.println(post.execute().getResponseString());


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
