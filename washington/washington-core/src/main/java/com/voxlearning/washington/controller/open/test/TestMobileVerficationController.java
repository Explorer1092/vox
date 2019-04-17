package com.voxlearning.washington.controller.open.test;

import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.web.UrlUtils;

import java.util.HashMap;
import java.util.Map;

public class TestMobileVerficationController {

    public static void main(String[] args) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("captchaToken", "jIKOTMAzuTPbFwLMP2RlF8z");
        paramMap.put("captchaCode", "9147");
        paramMap.put("studentMobile", "15639110810");
        String Url = UrlUtils.buildUrlQuery("http://10.200.8.214:8081/user/verification/bindstudent.vpage", paramMap);
        System.out.println(HttpRequestExecutor.defaultInstance().get(Url).execute().getResponseString());
        System.out.println("Test End...");
    }
}
