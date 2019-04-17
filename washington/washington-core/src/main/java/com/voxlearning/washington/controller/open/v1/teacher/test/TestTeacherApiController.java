package com.voxlearning.washington.controller.open.v1.teacher.test;

import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.http.client.execute.POST;
import com.voxlearning.alps.lang.util.DigestSignUtils;

import java.util.HashMap;
import java.util.Map;

public class TestTeacherApiController {

    public static void main(String[] args) {
        final String appKey = "17Teacher";
        final String secretKey = "gvUKQN1EFXKp";
        Map<String, String> paramMap = new HashMap<>();
//        paramMap.put("lat", "39.991836111111115");
        paramMap.put("session_key", "3e886795c139045db7019ccdbdb27a33");
        paramMap.put("teaching_years", "2");
        paramMap.put("app_key", appKey);
        String sig = DigestSignUtils.signMd5(paramMap, secretKey);
        paramMap.put("sig", sig);
        String apiURL = "http://www.test.17zuoye.net/v1/teacher//teachingYears/update.vpage";
        POST post = HttpRequestExecutor.defaultInstance().post(apiURL);
        paramMap.entrySet().forEach(e -> {
            String name = e.getKey();
            String value = e.getValue();
            post.addParameter(name, value);
        });
        System.out.println(post.execute().getResponseString());
    }
}
