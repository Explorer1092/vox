package com.voxlearning.washington.controller.open.v1.test;

import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.http.client.execute.POST;
import com.voxlearning.alps.lang.util.DigestSignUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ruib
 * @version 0.1
 * @since 2016/4/18
 */
public class TestOpenApiVendorsEnglishQuestion {

    public static void main(String[] args) {
        try {
            System.out.println("Test Start...");

            final String appKey = "GreatAdventure";
            //final String sessionKey = "285452cbbde88dfb0284cb29defec1cd";
            final String sessionKey = "0d93cd3f32ebf6c30d77172b3c33f753";
            // final String secretKey = "kig4HDXGJ8sl";
            final String secretKey = "KyLNjBGO0ptY";

            final String pointCount = "5";

            // 计算SIG
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("app_key", appKey);
            paramMap.put("session_key", sessionKey);
            paramMap.put("point_count", pointCount);
            paramMap.put("book_id", "100399");
            paramMap.put("practice_type_list", "");

            String sig = DigestSignUtils.signMd5(paramMap, secretKey);

            paramMap.put("sig", sig);

            String apiURL = "http://api.17zuoye.com/v1/appquestion/vendors/english.vpage";
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
