package com.voxlearning.washington.controller.open.v1.test;

import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.http.client.execute.POST;
import com.voxlearning.alps.lang.util.DigestSignUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试家长端公众号
 * Created by haitian.gan on 2016/12/7.
 */
public class TestParentOfficialAccount {

    public static void main(String[] args){
        try {
            System.out.println("Test Start...");

            final String appKey = "17Parent";
            String secretKey = "iMMrxI3XMQtd";
            final String sessionKey = "071bb9d6dd42e51a8672da75df05f65b";
            final Long accountId = 12L;

            // 计算SIG
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("app_key", appKey);
            paramMap.put("session_key", sessionKey);
            paramMap.put("accountId", accountId.toString());

            String sig = DigestSignUtils.signMd5(paramMap, secretKey);

            paramMap.put("sig", sig);

            String apiURL = "http://www.test.17zuoye.net/v1/parent/officialaccount/loadfollow.vpage";
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

