package com.voxlearning.washington.controller.open.v2.teacher.test;

import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.http.client.execute.POST;
import com.voxlearning.alps.lang.util.DigestSignUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by alex on 2017/1/4.
 */
public class TestTeacherTaskApi {

    private static void testGetTaskList() throws Exception {
        final String appKey = "17Teacher";
        final String secretKey = "gvUKQN1EFXKp";
        final String sessionKey = "41b0a6879c98845ebdd4868eb54f0482";

        // 计算SIG
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("app_key", appKey);
        paramMap.put("session_key", sessionKey);

        String sig = DigestSignUtils.signMd5(paramMap, secretKey);
        paramMap.put("sig", sig);

        String apiURL = "http://10.200.4.75:8081/v2/teacher/task/list.vpage";
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
