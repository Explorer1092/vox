package com.voxlearning.washington.controller.open.v1.student.test;

import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.http.client.execute.POST;
import com.voxlearning.alps.lang.util.DigestSignUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/2/19.
 */
public class TestStudentRankApiController {

    public static void main(String[] args){
        integralRank();
    }

    public static void integralRank() {


        System.out.println("Test Start...");

        final String appKey = "17Student";
        final String secretKey = "kuLwGZMJBcQj";
        final String sessionKey = "9d89c09ae15b26d1d547d1f3d3d7afad";

        // 计算SIG
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("app_key", appKey);
        paramMap.put("session_key", sessionKey);
        String sig = DigestSignUtils.signMd5(paramMap, secretKey);
        paramMap.put("sig", sig);
        paramMap.put("ver", "1.8.0.0");

//        String apiURL = "http://10.200.6.161:8081/v1/student/rewardrank.vpage";
        String apiURL = "http://10.200.6.161:8081/v1/student/rewardrank.vpage";
        POST post = HttpRequestExecutor.defaultInstance().post(apiURL);
        paramMap.entrySet().forEach(e -> {
            String name = e.getKey();
            String value = e.getValue();
            post.addParameter(name, value);
        });
        System.out.println(post.execute().getResponseString());

        System.out.println("Test End...");
    }
}
