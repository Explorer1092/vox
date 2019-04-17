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
 * Created by XiaoPeng.Yang on 15-1-26.
 */
public class TestOpenApiSaveQuestionResult {
    public static void main(String[] args) {
        try {
            System.out.println("Test Start...");

            final String appKey = "TravelAmerica";
            String sessionKey = "f43cff42fb4963ed6f526fa7a67a0c13";
            String secretKey = "Tzpw9PIj8zM";

            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("app_key", appKey);
            paramMap.put("session_key", sessionKey);

            String sig = DigestSignUtils.signMd5(paramMap, secretKey);
            paramMap.put("sig", sig);
            paramMap.put("question_results", "{\"studyType\":\"TravelAmerica\",\"version\":\"1.0.0.1\",\"gameType\":\"word-practicing\",\"userId\":30013,\"clazzId\":10007,\"totalTime\":55368,\"totalScore\":100,\"subject\":\"English\",\"questions\":[{\"id\":3032,\"content\":\"usually\",\"isdo\":true,\"duration\":1421300049115,\"correct\":true,\"score\":14},{\"id\":655,\"content\":\"compose\",\"isdo\":true,\"duration\":1421300059438,\"correct\":true,\"score\":14},{\"id\":192,\"content\":\"American\",\"isdo\":true,\"duration\":1421300064485,\"correct\":true,\"score\":14},{\"id\":3066,\"content\":\"waiter\",\"isdo\":true,\"duration\":1421300070029,\"correct\":true,\"score\":14},{\"id\":1821,\"content\":\"never\",\"isdo\":true,\"duration\":1421300078370,\"correct\":true,\"score\":14},{\"id\":3019,\"content\":\"uniform\",\"isdo\":true,\"duration\":1421300085901,\"correct\":true,\"score\":14},{\"id\":1594,\"content\":\"live\",\"isdo\":true,\"duration\":1421300090114,\"correct\":true,\"score\":14}]}");

            String apiURL = "http://localhost/v1/user/learning/saveresult.vpage";
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
