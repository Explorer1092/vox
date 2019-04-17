package com.voxlearning.washington.controller.open.test;

import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.util.DigestSignUtils;
import com.voxlearning.alps.web.UrlUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author malong
 * @since 2017/3/22
 */
public class TestParentSignConfig {
    public static final String APP_KEY = "17Parent";
    public static final String SECRET_KEY = "iMMrxI3XMQtd";
    public static final String SESSION_KEY = "527f0d646179f714e9bbabc1fcf2ef2e";

    public static void main(String[] args) {
        System.out.println("Test Start...");
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("sid", "333875333");
        paramMap.put("app_key", APP_KEY);
        paramMap.put("session_key", SESSION_KEY);
        String sig = DigestSignUtils.signMd5(paramMap, SECRET_KEY);
        paramMap.put("sig", sig);

        String Url = UrlUtils.buildUrlQuery("http://10.200.4.52:8081/v1/parent/ucenter/sign/config.vpage", paramMap);
        System.out.println(HttpRequestExecutor.defaultInstance().get(Url).execute().getResponseString());
        System.out.println("Test End...");
    }
}
