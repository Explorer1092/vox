package com.voxlearning.washington.controller.open.v1.student.test;

import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.http.client.execute.POST;
import com.voxlearning.alps.lang.util.DigestSignUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author malong
 * @since 2016/8/8
 */
public class TestStudentActivityCenterApiController {
    public static void main(String[] args) {
        try {
            System.out.println("Test Start");
            final String appKey = "17Student";
            final String secretKey = "kuLwGZMJBcQj";
            final String sessionKey = "2979d480e28493b55ed559cb9ea8cd65";

            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("app_key", appKey);
            paramMap.put("ad_position", "student_top,student_middle");
            paramMap.put("session_key", sessionKey);

            String sig = DigestSignUtils.signMd5(paramMap, secretKey);

            paramMap.put("sig", sig);
            paramMap.put("ver", "1.9.0.0");
            paramMap.put("sys", "ios");

            String apiURL = "http://localhost:8081/v1/student/activity/center/getadvertisementinfo.vpage";
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
