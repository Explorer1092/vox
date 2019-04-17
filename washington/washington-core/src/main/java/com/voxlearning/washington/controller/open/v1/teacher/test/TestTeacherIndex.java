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
 * Created by Alex on 14-12-29.
 */
public class TestTeacherIndex {

    public static void main(String[] args) {
        try {
            System.out.println("Test Start...");

            final String appKey = "17Teacher";
            final String secretKey = "iQu2TuXxg3xg";
            final String sessionKey = "7457e35e5f769bebee23520fa65b9724";

            // 计算SIG
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("app_key", appKey);
            paramMap.put("session_key", sessionKey);
//            paramMap.put("unit_id", "11");

            String sig = DigestSignUtils.signMd5(paramMap, secretKey);
            paramMap.put("sig", sig);
//            paramMap.put("page_number", "1");
//            List<Map> maps = new ArrayList<>();
//            maps.add(MiscUtils.map("homework_id", "54afb22df0eb79874aedd471").add("clazz_id", "32375"));
//            maps.add(MiscUtils.map("homework_id", "547c3bf3f0ebc18199048d5b").add("clazz_id", "35746"));
//            maps.add(MiscUtils.map("homework_id", "5481c80fde450f35626be0ff").add("clazz_id", "35747"));
//            paramMap.put("homeworkList", JsonUtils.toJson(maps));
            paramMap.put("homework_id", "555d4b3b040c7bccc0971097");
            paramMap.put("user_code", "31039");

//            String apiURL = "http://192.168.140.251:8080/v1/teacher/report/homework/detail.vpage";
            String apiURL = "http://192.168.140.251:8080/v1/teacher/report/student/homework/detail.vpage";
//            String apiURL = "http://www.17zuoye.com/v1/teacher/report/homework/detail.vpage";
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
