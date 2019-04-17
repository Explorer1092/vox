package com.voxlearning.washington.controller.open.v1.student.test;

import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.http.client.execute.POST;
import com.voxlearning.alps.lang.util.DigestSignUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author malong
 * @since 2016/5/18
 */
public class TestStudentApiController {
    public static void main(String[] args) {
        try {
            System.out.println("Test Start...");
            final String appKey = "17Student";
            final String secretKey = "kuLwGZMJBcQj";
            final String sessionKey = "4e40748b3bf6ce480b5796e073123c81";
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("app_key", appKey);
//            paramMap.put("user_code", "333906449");
//            paramMap.put("passwd", "1");
            paramMap.put("session_key", sessionKey);
//            paramMap.put("scan_number", "11223");
            String sig = DigestSignUtils.signMd5(paramMap, secretKey);
            paramMap.put("sig", sig);
            paramMap.put("ver", "2.2.0.0");

            String apiURL = "http://localhost:8081/v1/student/eyetimes.vpage";
            POST post = HttpRequestExecutor.defaultInstance().post(apiURL);
            paramMap.entrySet().forEach(e -> {
                String name = e.getKey();
                String value = e.getValue();
                post.addParameter(name, value);
            });

            System.out.println(post.execute().getResponseString());;
            System.out.println("Test End...");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
