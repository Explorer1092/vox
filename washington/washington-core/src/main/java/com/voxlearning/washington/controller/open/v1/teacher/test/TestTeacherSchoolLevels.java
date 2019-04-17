package com.voxlearning.washington.controller.open.v1.teacher.test;

import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.http.client.execute.POST;
import com.voxlearning.alps.lang.util.DigestSignUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by alex on 2017/6/15.
 */
public class TestTeacherSchoolLevels {
    public static void main(String[] args) {
        try {
            System.out.println("Test Start...");

            final String appKey = "17Teacher";
            final String secretKey = "gvUKQN1EFXKp";
            final String sessionKey = "e482f2472b8c56d597a061ac55907e2a";

            // 计算SIG
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("app_key", appKey);
            paramMap.put("session_key", sessionKey);
            paramMap.put("ktwelve", "INFANT");
            paramMap.put("region_pcode", "110107");

            String sig = DigestSignUtils.signMd5(paramMap, secretKey);
            paramMap.put("sig", sig);
            String apiURL = "http://www.test.17zuoye.net/v1/teacher/user/school/get_by_region_ktwelve.vpage";
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
