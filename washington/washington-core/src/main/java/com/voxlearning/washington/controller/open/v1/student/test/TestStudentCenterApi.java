package com.voxlearning.washington.controller.open.v1.student.test;

import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.http.client.execute.POST;
import com.voxlearning.alps.lang.util.DigestSignUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by malong on 2016/4/27.
 */
public class TestStudentCenterApi {
    public static void main(String args[]) {
        try {
            System.out.println("测试开始 Start...");

            final String appKey = "17Student";
            final String secretKey = "kuLwGZMJBcQj";
            final String sessionKey = "4c524bb24584dcdf91a8ec2a35c50ee1";

            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("app_key", appKey);
            paramMap.put("session_key", sessionKey);
            paramMap.put("ad_position", "student_middle");
            String sig = DigestSignUtils.signMd5(paramMap, secretKey);
            paramMap.put("sig", sig);
            paramMap.put("ver", "2.2.0.0");

            String apiURL = "http://10.200.7.133:8081/v1/student/activity/center/getadvertisementinfo.vpage";
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
