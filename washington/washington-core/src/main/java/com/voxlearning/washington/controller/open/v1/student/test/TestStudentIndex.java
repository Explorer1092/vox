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

package com.voxlearning.washington.controller.open.v1.student.test;

import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.http.client.execute.POST;
import com.voxlearning.alps.lang.util.DigestSignUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Shuai Huan on 2015/5/8.
 */
public class TestStudentIndex {

    public static void main(String[] args) {
        try {
            System.out.println("Test Start...");

            final String appKey = "17Student";
            final String secretKey = "kuLwGZMJBcQj";
            final String sessionKey = "b3ae4e3cc78f1d10be68bd868dce8c03";

            // 计算SIG
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("app_key", appKey);
            paramMap.put("session_key", sessionKey);
//            paramMap.put("source", "homework");
//            paramMap.put("homework_id", "7");
//            paramMap.put("homework_finish_time", "14");
//            paramMap.put("homework_finished", "true");
//            paramMap.put("homework_standards", "true");
//            paramMap.put("workbook_id", "W_10300000204427");
//            paramMap.put("workbook_catalog_id", "WC_10300036867071");
//            paramMap.put("workbook_content_id", "WCO_10300013349463");

            String sig = DigestSignUtils.signMd5(paramMap, secretKey);

            paramMap.put("sig", sig);
            paramMap.put("ver", "1.9.0.0");
            paramMap.put("sys", "ios");

            String apiURL = "http://localhost:8081/v1/student/index.vpage";
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
