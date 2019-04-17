package com.voxlearning.washington.controller.open.v1.parent.test;

import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.http.client.execute.POST;
import com.voxlearning.alps.lang.util.DigestSignUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhangbin
 * @since 2016/11/28 20:05
 */
public class TestParentMistakenNotebookApiController {
    private final static String appKey = "17Parent";
    private final static String secretKey = "iMMrxI3XMQtd";
    private final static String sessionKey = "527f0d646179f714e9bbabc1fcf2ef2e";
//    private final static String appKey = "17Student";
//    private final static String secretKey = "kuLwGZMJBcQj";
//    private final static String sessionKey = "a368a8f01702a49f9b47a18965689ca7";

    private static Map<String, String> paramMap = new HashMap<>();
    static {
        paramMap.put("app_key", appKey);
        paramMap.put("session_key", sessionKey);
    }

    private static void execute(String apiURL) {
        POST post = HttpRequestExecutor.defaultInstance().post(apiURL);
        String sig = DigestSignUtils.signMd5(paramMap, secretKey);
        paramMap.put("sig", sig);
        paramMap.entrySet().forEach(e -> {
            String name = e.getKey();
            String value = e.getValue();
            post.addParameter(name, value);
        });
        System.out.println(post.execute().getResponseString());
    }

    private static void testSubjectCount(){
        String apiURL = "http://localhost:8081//v1/parent/mistakenNotebook/subjectCount.api";
        paramMap.put("sid", "333907913");//333907913
        execute(apiURL);
    }

    private static void testGetBookUnitInfo(){
        String apiURL = "http://localhost:8081//v1/parent/mistakenNotebook/book/unit.api";
        paramMap.put("sid", "333907913");
        paramMap.put("subject", "ENGLISH");
        execute(apiURL);
    }

    private static void testGetUnitInfo(){
        String apiURL = "http://localhost:8081//v1/parent/mistakenNotebook/unit.api";
        paramMap.put("sid", "333907913");
        paramMap.put("unit_ids", "BKC_10300048551849");
        execute(apiURL);
    }

    public static void main(String[] args){
        System.out.println("test begin...");
        //testSubjectCount();
        //testGetBookUnitInfo();
        testGetUnitInfo();
        System.out.println("test end...");
    }


}
