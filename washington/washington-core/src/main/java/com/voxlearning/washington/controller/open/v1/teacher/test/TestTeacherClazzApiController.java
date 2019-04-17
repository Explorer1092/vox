/*
 *
 *  * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *  *
 *  * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *  *
 *  * NOTICE: All information contained herein is, and remains the property of
 *  * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 *  * and technical concepts contained herein are proprietary to Shanghai Sunny
 *  * Education, Inc. and its suppliers and may be covered by patents, patents
 *  * in process, and are protected by trade secret or copyright law. Dissemination
 *  * of this information or reproduction of this material is strictly forbidden
 *  * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 *
 */

package com.voxlearning.washington.controller.open.v1.teacher.test;

import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.http.client.execute.POST;
import com.voxlearning.alps.lang.util.DigestSignUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author changyuan
 * @since 2016/8/18
 */
public class TestTeacherClazzApiController {
    private final static String appKey = "17Teacher";
    private final static String secretKey = "gvUKQN1EFXKp";
    // 125075
//    private final static String sessionKey = "f5a0129503f3667122f87f8028e15f80";
    // 126160
//    private final static String sessionKey = "ca2c090b65911e5e60af3eef5fda02b5";
    // 127150
//    private final static String sessionKey = "f348950e197a8f30fc3baf6e15c2ddb5";
    // 121677
//    private final static String sessionKey = "9096eddbd10e86a74b27b96c60b54447";
    // 125513
    private final static String sessionKey = "5813381ce2b56f8979cf1ebbf6c2acdd";


    private static Map<String, String> paramMap = new HashMap<>();

    static {
        paramMap.put("app_key", appKey);
        paramMap.put("session_key", sessionKey);
    }

    private static void execute(String apiURL) {
        String sig = DigestSignUtils.signMd5(paramMap, secretKey);
        paramMap.put("sig", sig);
        POST post = HttpRequestExecutor.defaultInstance().post(apiURL);
        paramMap.entrySet().forEach(e -> {
            String name = e.getKey();
            String value = e.getValue();
            post.addParameter(name, value);
        });
        System.out.println(post.execute().getResponseString());
    }

    private static void testTeacherClazzList() {
        String url = "http://www.test.17zuoye.net/v1/teacher/clazz/list.vpage";
        execute(url);
    }

    private static void testClazzListBySchool() {
//        String url = "http://www.test.17zuoye.net/v1/teacher/school/clazz/list.vpage";
        String url = "http://localhost:8081/v1/teacher/school/clazz/list.vpage";
        paramMap.put("school_id", "418665");
        paramMap.put("clazz_type", "1");
        execute(url);
    }

    private static void testFindClazzInfo() {
        String url = "http://localhost:8081/v1/teacher/findclazzinfo.vpage";
//        String url = "http://localhost:8081/v1/teacher/findclazzinfo.vpage";
        String json = "\n" +
                "{\n" +
                "    \"clazzIds\": [\n" +
                "        {\n" +
                "            \"clazzLevel\": \"53\",\n" +
                "            \"clazzs\": [\n" +
                "                {\n" +
                "                    \"id\": -42014,\n" +
                "                    \"name\": \"4班\"\n" +
                "                }\n" +
                "            ]\n" +
                "        },\n" +
                "        {\n" +
                "            \"clazzLevel\": \"52\",\n" +
                "            \"clazzs\": [\n" +
                "                {\n" +
                "                    \"id\": -42015,\n" +
                "                    \"name\": \"4班\"\n" +
                "                }\n" +
                "            ]\n" +
                "        }\n" +
                "    ]\n" +
                "}";
        paramMap.put("json", json);
        paramMap.put("subject", "ENGLISH");
        execute(url);
    }

    private static void testAdjustClazz() {
        String url = "http://localhost:8081/v1/teacher/clazz/adjust.vpage";
        String json = "{\n  \"adjustWalkingClazzs\" : {\n\n  },\n  \"takeovergroups\" : [\n\n  ],\n  \"newClazzs\" : [\n\n  ],\n  \"adjustClazzs\" : [\n    40090287,\n    -12013,\n    -22013\n  ]\n}";
        paramMap.put("json", json);
        paramMap.put("subject", "ENGLISH");
        execute(url);
    }

    private static void testClazzTeacherList() {
//        String url = "http://www.test.17zuoye.net/v1/teacher/clazz/teacher/list.vpage";
        String url = "http://localhost:8081/v1/teacher/clazz/teacher/list.vpage";
        paramMap.put("clazz_id", "887896");
        execute(url);
    }

    private static void testTeacherClazzDetail() {
        String url = "http://localhost:8081/v1/teacher/clazz/detail.vpage";
        paramMap.put("group_id", "7469");
        execute(url);
    }

    private static void testClazzAddTeacher() {
        String url = "http://localhost:8081/v1/teacher/clazz/teacher/add.vpage";
        paramMap.put("group_id", "19569");
        paramMap.put("teacher_id", "125075");
        paramMap.put("subject", "ENGLISH");
        execute(url);
    }

    private static void testClazzSetShowRank(){
        String url = "http://localhost:8081/v1/teacher/clazz/teacher/set_show_rank.vpage";
        paramMap.put("clazz_id", "36203");
        paramMap.put("teacher_id", "10016");
        paramMap.put("subject", "ENGLISH");
        execute(url);
    }

    public static void main(String[] args) {
        try {
            testTeacherClazzList();
//            testClazzListBySchool();
   //         testFindClazzInfo();
//            testAdjustClazz();
//            testClazzTeacherList();
//            testTeacherClazzDetail();
            // testClazzAddTeacher();
//            testClazzSetShowRank();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
