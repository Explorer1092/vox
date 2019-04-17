package com.voxlearning.washington.controller.open.v2.teacher.test;

import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.http.client.execute.POST;
import com.voxlearning.alps.lang.util.DigestSignUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by alex on 2017/1/4.
 */
public class TestTeacherHomeApi {

    private static void testGetTaskList() throws Exception {
        final String appKey = "17Teacher";
        final String secretKey = "gvUKQN1EFXKp";
        final String sessionKey = "98c7708da8e89089470e274874cba2bf";

        // 计算SIG
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("app_key", appKey);
        paramMap.put("session_key", sessionKey);

        String sig = DigestSignUtils.signMd5(paramMap, secretKey);
        paramMap.put("sig", sig);

        String apiURL = "http://localhost:8081/v2/teacher/home/index.vpage";
        POST post = HttpRequestExecutor.defaultInstance().post(apiURL);
        paramMap.entrySet().forEach(e -> {
            String name = e.getKey();
            String value = e.getValue();
            post.addParameter(name, value);
        });

        System.out.println(post.execute().getResponseString());

        System.out.println("Test End...");
    }


    public static void main(String[] args) {
        try {
            testGetTaskList();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
