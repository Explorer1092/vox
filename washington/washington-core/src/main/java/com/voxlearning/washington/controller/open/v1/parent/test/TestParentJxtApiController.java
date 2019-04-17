package com.voxlearning.washington.controller.open.v1.parent.test;

import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.util.DigestSignUtils;
import com.voxlearning.alps.web.UrlUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author malong
 * @since 2017/4/26
 */
public class TestParentJxtApiController {
    public static void main(String[] args) {
        try {
            System.out.println("Test Start...");

            final String appKey = "17Parent";
            final String secretKey = "iMMrxI3XMQtd";
            final String sessionKey = "3f9ee0e7b8aab29adcae3850b051eed0";

            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("app_key", appKey);
            paramMap.put("session_key", sessionKey);
            paramMap.put("sid", "30005");
            String sig = DigestSignUtils.signMd5(paramMap, secretKey);
            paramMap.put("sig", sig);
            paramMap.put("ver", "1.9.1");

            String apiUrl = "http://10.200.6.158:8081/v2/parent/jxt/student_progress.vpage";
            String URL = UrlUtils.buildUrlQuery(apiUrl, paramMap);
            System.out.println(HttpRequestExecutor.defaultInstance().get(URL).execute().getResponseString());
            System.out.println("Test End...");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
