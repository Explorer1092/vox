package com.voxlearning.washington.controller.open.v2.test;

import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.http.client.execute.POST;
import com.voxlearning.alps.lang.util.DigestSignUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class TestVerificationApi2Controller {

    public static void main(String[] args) {
//        final String appKey = "Shensz";
//        final String secretKey = "qaP4ElgkY8ss";
//        final String sessionKey = "ee0c425acbe33e18a06e74e26c6b0094";
        final String appKey = "17JuniorPar";
        final String secretKey = "Unbkseke9zSI";
//        final String sessionKey = "ee0c425acbe33e18a06e74e26c6b0094";
        // 计算SIG
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("app_key", appKey);
//        paramMap.put("session_key", sessionKey);
        paramMap.put("user_code", "15291576108");
        paramMap.put("captchaCode", "3184");
        paramMap.put("imei", "35b7200c-ac94-444f-aa83-7ba31268f1de");
        paramMap.put("captchaToken", "875048ae460f4190b13f38db6f9a2dbf1543318115611");
        String apiURL = "http://www.test.17zuoye.net/v2/user/parent/verifycode/get.vpage";
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
}
