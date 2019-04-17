package com.voxlearning.washington.controller.open.v1.parent.test;

import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.util.DigestSignUtils;
import com.voxlearning.alps.web.UrlUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jiang wei on 2017/2/14.
 */
public class TestParentBottomTabController {

    public static void main(String[] args) {
        try {
            System.out.println("Test Start...");

            final String appKey = "17Parent";
            final String secretKey = "iMMrxI3XMQtd";
            final String sessionKey = "f86a13d3d6d6fceb38756ba639a12ac0";

            // 计算SIG
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("app_key", appKey);
            paramMap.put("session_key", sessionKey);
            String sig = DigestSignUtils.signMd5(paramMap, secretKey);

            paramMap.put("sig", sig);
            paramMap.put("ver", "1.8.5");

            //String apiURL = "http://10.200.4.52:8081/v1/parent/jxt/news/getJxtNewsNoticeCount.vpage";
            String apiURL = "http://10.200.4.52:8081/v1/parent/ucenter/tab/list.vpage";
            String URL = UrlUtils.buildUrlQuery(apiURL, paramMap);
            System.out.println(HttpRequestExecutor.defaultInstance().get(URL).execute().getResponseString());
            System.out.println("Test End...");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
