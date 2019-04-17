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
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.DigestSignUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Test student clazz api
 * Created by Shuai Huan on 2015/8/10.
 */
public class TestStudentClazzApi {
    final String appKey = "17Student";
    final String secretKey = "kuLwGZMJBcQj";

    public static void main(String[] args) {
        TestStudentClazzApi tester = new TestStudentClazzApi();

        try {
            System.out.println("Test Start...");

//            tester.testHomework();

            tester.testCheckClazzInfo();


            System.out.println("Test End...");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void testHomework() {
        // 计算SIG
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("app_key", appKey);
        paramMap.put("session_key", "f244c8b37c956e873390abef4ba09b4c");
        paramMap.put("homework_id", "5616256fa475cb6b9d9f6863");
        paramMap.put("homework_type", "ENGLISH");
        String sig = DigestSignUtils.signMd5(paramMap, secretKey);
        paramMap.put("sig", sig);
        paramMap.put("ver", "1.8.0.0");

//            String apiURL = "http://www.hydra.17zuoye.net/v1/student/report/errors.vpage";
//            String apiURL = "http://localhost:8081/v1/student/homework/go.vpage";
        String apiURL = "http://www.test.17zuoye.net/v1/student/homework/go.vpage";
        POST post = HttpRequestExecutor.defaultInstance().post(apiURL);
        paramMap.forEach(post::addParameter);
        System.out.println(post.execute().getResponseString());
    }

    void testCheckClazzInfo() {
        // 计算SIG
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("app_key", appKey);
        paramMap.put("id", "11513122");
        paramMap.put("sid", "333915271");
        String sig = DigestSignUtils.signMd5(paramMap, secretKey);
        paramMap.put("sig", sig);
        paramMap.put("ver", "2.8.10.0");
        paramMap.put("session_key", "70fc3d9c7b52b299c6fc290929f12453");

        String apiURL = "http://localhost:8081/v1/student/clazz/checkclazzinfo.vpage";
        POST post = HttpRequestExecutor.defaultInstance().post(apiURL);
        paramMap.forEach(post::addParameter);
        System.out.println(JsonUtils.toJsonPretty(JsonUtils.convertJsonObjectToMap(post.execute().getResponseString())));
    }
}
