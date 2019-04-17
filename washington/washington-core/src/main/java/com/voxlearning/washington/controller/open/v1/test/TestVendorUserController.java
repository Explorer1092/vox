package com.voxlearning.washington.controller.open.v1.test;

import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.util.DigestSignUtils;
import com.voxlearning.alps.web.UrlUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author malong
 * @since 2016/9/19
 */
public class TestVendorUserController {
    public static void main(String[] args) {
        System.out.println("Test Start...");
        final String secretKey = "aolskdfjlajksdfl";
        final String appKey = "byhy";

        System.out.println("Test End...");

        testOpenService(appKey, secretKey);
        testCloseService(appKey, secretKey);
        testEffectiveUserNum(appKey, secretKey);
    }

    private static void testEffectiveUserNum(String appKey, String secretKey) {
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("app_key", appKey);
        paramsMap.put("date", "201901");
        String sig = DigestSignUtils.signMd5(paramsMap, secretKey);
        paramsMap.put("sig", sig);
        String apiUrl = "http://www.test.17zuoye.net/v1/vendor/user/effective.vpage";
        String URL = UrlUtils.buildUrlQuery(apiUrl, paramsMap);

        System.out.println(HttpRequestExecutor.defaultInstance().post(URL).execute().getResponseString());

        System.out.println("TestEffectiveUserNum End...");
    }


    private static void testCloseService(String appKey, String secretKey) {
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("app_key", appKey);
        paramsMap.put("mobile", "18935646887");
        String sig = DigestSignUtils.signMd5(paramsMap, secretKey);
        paramsMap.put("sig", sig);
        String apiUrl = "http://www.test.17zuoye.net/v1/vendor/user/close.vpage";
        String URL = UrlUtils.buildUrlQuery(apiUrl, paramsMap);

        System.out.println(HttpRequestExecutor.defaultInstance().post(URL).execute().getResponseString());

        System.out.println("TestCloseService End...");
    }

    private static void testOpenService(String appKey, String secretKey) {
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("app_key", appKey);
        paramsMap.put("mobile", "18935646887,12365987994");
        String sig = DigestSignUtils.signMd5(paramsMap, secretKey);
        paramsMap.put("sig", sig);
        String apiUrl = "http://www.test.17zuoye.net/v1/vendor/user/open.vpage";
        String URL = UrlUtils.buildUrlQuery(apiUrl, paramsMap);

        System.out.println(HttpRequestExecutor.defaultInstance().post(URL).execute().getResponseString());

        System.out.println("TestOpenService End...");
    }
}
