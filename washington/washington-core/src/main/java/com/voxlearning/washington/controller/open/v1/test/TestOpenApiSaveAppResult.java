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
 * Created by Alex on 15-3-27.
 */
public class TestOpenApiSaveAppResult {
    public static void main(String[] args) {
        try {
            System.out.println("Test Start...");

            final String appKey = "SanguoDmz";
            String sessionKey = "ebef17e622eff45a72dc7223a80ab3fa";
            String secretKey = "AcoMRXlv59U1";

            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("app_key", appKey);
            paramMap.put("session_key", sessionKey);

            String sig = DigestSignUtils.signMd5(paramMap, secretKey);
            paramMap.put("sig", sig);
            paramMap.put("app_results", "{\"subject\":\"English\",\"studyType\":\"SanguoDmz\",\"userId\":30002,\"clazzId\":31273,\"bookId\":100138,\"lessonId\":10024983,\"practiceId\":81,\"questionAnswers\":[{\"questionId\":63700200012012,\"startTime\":\"2015-03-26 14:20:38\",\"duration\":27500,\"ekList\":\"foot\",\"isRight\":true,\"score\":100},{\"questionId\":63700200012013,\"startTime\":\"2015-03-26 14:20:58\",\"duration\":27100,\"ekList\":\"window\",\"isRight\":false,\"score\":0}]}");

            String apiURL = "http://www.testing.17zuoye.net/v1/user/learning/saveappresult.api";
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
