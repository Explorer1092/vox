/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.washington.controller.open.test;

import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.DigestSignUtils;
import com.voxlearning.alps.web.UrlUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author peng.zhang.a
 * @since 2016/5/24
 */
public class TestAppMessageApiController {
    public static final String BASE_URL = "http://www.17zuoye.com";
//    public static final String BASE_URL = "http://www.test.17zuoye.net";

    //	public static final String BASE_URL = "http://www.staging.17zuoye.net";
    public static void main(String arg[]) {
//        testSendJpushMessage();

//        testSendJpushMessage3();

//        testSendUserMessage();

//        testHaveViewMessage();

//        testSendUserMessage2();
//         testLoaduserMessage();

//        testSendUserMessage();

//        Map<String, String> paramMap = new HashMap<>();
//        String url = UrlUtils.buildUrlQuery("http://10.7.7.41:5000/get_question", paramMap);
//        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().post(url).execute();
//        System.out.println(response.getResponseString());


//        testCurrentPushMessage();

        Map data = new HashMap();
        data.put("paramValues","[\"老师端push消息测试\",\"JUNIOR_TEACHER\",[12975982],{}]");
        data.put("paramTypes","[\"java.lang.String\",\"java.lang.String\",\"java.util.List\",\"java.util.Map\"]");

        String url = UrlUtils.buildUrlQuery("http://192.168.100.79:1889?service=com.voxlearning.utopia.service.vendor.api.DPVendorService&version=2016.08.19&group=alps-hydra-test&method=sendAppJpushMessageByIds", data);
        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().post(url).execute();
        System.out.println(response.getStatusCode());








    }

    private static void testCurrentPushMessage() {

        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("messageLink","https://www.baidu.com");


        String apiURL = BASE_URL + "/push/msg/currentPushMessage.vpage";
        String url = UrlUtils.buildUrlQuery(apiURL, paramMap);
        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().get(url).execute();
        System.out.println(response.getResponseString());
        System.out.println("Test End...");
    }

    private static void testLoaduserMessage() {
        String secretKey = "rQRw0I6j09ZA";
        Map<String, String> paramMap = new HashMap<>();
        //paramMap.put("page","0");
        paramMap.put("app_key","17Parent");


        final String sessionKey = "7186a703b0a56d0de9b89e2f301225f1";
        paramMap.put("session_key",sessionKey);
        // 计算SIG
        String sig = DigestSignUtils.signMd5(paramMap, secretKey);

        paramMap.put("sig", sig);
        paramMap.put("ver", "2.5.9.246");

        System.out.println(sig);

        String apiURL = BASE_URL + "/v1/appmessage/loaduserpopupmessage.vpage";
        String url = UrlUtils.buildUrlQuery(apiURL, paramMap);
        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().post(url).execute();
        System.out.println(response.getResponseString());
        System.out.println("Test End...");
    }

    private static void testHaveViewMessage() {
//        String secretKey = "qaP4ElgkY8ss";
        Map<String, String> paramMap = new HashMap<>();
//        paramMap.put("app_key","Shensz");

//        final String sessionKey = "e1e76579f7f8e53c06a958a2438ec66c"; // 30002 test
//        paramMap.put("session_key",sessionKey);

        final String sessionKey = "067cd7d4910c7393287248188bbcaadd";
        paramMap.put("app_key","17Student");
        paramMap.put("session_key",sessionKey);
        String secretKey = "kuLwGZMJBcQj";



        // 计算SIG
        String sig = DigestSignUtils.signMd5(paramMap, secretKey);
        paramMap.put("sig", sig);

        String apiURL = BASE_URL + "/v1/appmessage/haveViewMessage.vpage";
        String url = UrlUtils.buildUrlQuery(apiURL, paramMap);
        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().post(url).execute();
        System.out.println(response.getResponseString());
        System.out.println("Test End...");
    }

    private static void testSendUserMessage() {
        String secretKey = "Unbkseke9zSI";
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("uid","269808");
        paramMap.put("title","新作业6");
        paramMap.put("content","学科：初数作业\r\n截止时间：不限时间\\n作业内容《勿操作_na专属_布置解答题》");
        paramMap.put("type","1006");
        paramMap.put("linkType","1");
        paramMap.put("linkUrl","https://www.baidu.com");
        paramMap.put("imgUrl","https://www.baidu.com");
        Map<String,Object> extInfoMap = new LinkedHashMap<>();
        extInfoMap.put("paper_id","3912a1c4-6114-4780-aad0-50ece9771329");
        extInfoMap.put("paper_type",1);
        extInfoMap.put("parent_id",null);
        String extinfoJsonStr = JsonUtils.toJson(extInfoMap);
        System.out.println(extinfoJsonStr);
        paramMap.put("extInfo",extinfoJsonStr);
        paramMap.put("app_key","17JuniorPar");

//        final String sessionKey = "ec9a13aea1f06098a7f7016a4c6224a5"; // 30002 test
//        paramMap.put("session_key",sessionKey);
        // 计算SIG
        String sig = DigestSignUtils.signMd5(paramMap, secretKey);
        paramMap.put("sig", sig);
        String apiURL = BASE_URL + "/v2/appmessage/sendusermessage.vpage";
        String url = UrlUtils.buildUrlQuery(apiURL, paramMap);
        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().post(url).execute();
        System.out.println(response.getResponseString());
        System.out.println("Test End...");
    }

    private static void testSendUserMessage2() {
        String secretKey = "Unbkseke9zSI";

        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("uid","[269808]");
        paramMap.put("title","站内信测试");
        paramMap.put("content","站内信测试content");
        paramMap.put("type","1000");
        paramMap.put("linkType","0");
        paramMap.put("linkUrl","");
        paramMap.put("imgUrl","");
        paramMap.put("extInfo","");
        paramMap.put("app_key","17JuniorPar");

//        final String sessionKey = "e1e76579f7f8e53c06a958a2438ec66c"; // 30002 test
//        paramMap.put("session_key",sessionKey);


        // 计算SIG
        String sig = DigestSignUtils.signMd5(paramMap, secretKey);
        paramMap.put("sig", sig);

        String apiURL = BASE_URL + "/v1/appmessage/sendusermessage.vpage";
        String url = UrlUtils.buildUrlQuery(apiURL, paramMap);
        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().post(url).execute();
        System.out.println(response.getResponseString());
        System.out.println("Test End...");
    }

    private static void testSendJpushMessage2() {
        String secretKey = "qaP4ElgkY8ss";

        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("userIds","3353");
        paramMap.put("content","老师喊你回classin啦");
        paramMap.put("source","JUNIOR_TEACHER");
        paramMap.put("sendTimeEpochMilli","0");
        paramMap.put("messageType","1003");
        paramMap.put("messageLink","https://www.baidu.com");
        paramMap.put("schoolLevel","m");
        paramMap.put("app_key","Shensz");
        Map<String,Object> extInfo = new LinkedHashMap<>();
        extInfo.put("key","value");
        paramMap.put("extInfo", JsonUtils.toJson(extInfo));

        final String sessionKey = "e1e76579f7f8e53c06a958a2438ec66c"; // 30002 test
        paramMap.put("session_key",sessionKey);


        // 计算SIG
        String sig = DigestSignUtils.signMd5(paramMap, secretKey);
        paramMap.put("sig", sig);

        String apiURL = BASE_URL + "/v1/apppush/users.vpage";
        String url = UrlUtils.buildUrlQuery(apiURL, paramMap);
        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().post(url).execute();
        System.out.println(response.getResponseString());
        System.out.println("Test End...");
    }

    private static void testSendJpushMessage3() {
        String secretKey = "qaP4ElgkY8ss";

        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("userIds","371566");
//        paramMap.put("title","安排小老师");
        paramMap.put("content","老师指定你为小老师，快来查看");
        paramMap.put("source","JUNIOR_TEACHER");
        paramMap.put("sendTimeEpochMilli","0");
        paramMap.put("messageType","1008");
        paramMap.put("messageLink","https://www.baidu.com");
        paramMap.put("schoolLevel","m");
        paramMap.put("app_key","Shensz");


        paramMap.put("extInfo", "{}");

        final String sessionKey = "ec9a13aea1f06098a7f7016a4c6224a5"; // 30002 test
        paramMap.put("session_key",sessionKey);

        // 计算SIG
        String sig = DigestSignUtils.signMd5(paramMap, secretKey);
        paramMap.put("sig", sig);

        String apiURL = BASE_URL + "/v1/apppush/users.vpage";
        String url = UrlUtils.buildUrlQuery(apiURL, paramMap);
        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().post(url).execute();
        System.out.println(response.getResponseString());
        System.out.println("Test End...");
    }

    public static void testSendJpushMessage() {

//        System.out.println("Test Start...");
//
//        //洛亚传说 HS_VENDOR.VOX_VENDOR_APPS
//        final String appKey = AppMessageSource.A17ZYSPG.appKey;
//        final String secretKey = "Fca4vpuD1bjR"; //AppMessageSource
//        final String sessionKey = "19cf0bc141890bae548a84383acad8bf"; // 30002 test
////		final String sessionKey = "a25e16f2936dedbf1a3d5b4b1850bbfa"; //30002 online
////
//        Map<String, Object> extInfo = new HashMap<>();
////		extInfo.put("url", "http://just4test-spg-report.daoapp.io/?session_key=d87cb32724f6aec86b6d21ac5de3699e&appkey=A17ZYSPG&ramdom=" + new Date());
//        extInfo.put("url", "http://just4test-spg-report.daoapp.io/?session_key=905fc9c3e3ef2563806730d841b91ae3&appkey=A17ZYSPG");
//        Map<String, String> paramMap = new HashMap<>();
//
//        paramMap.put("session_key", sessionKey);
//        paramMap.put("app_key", appKey);
//        paramMap.put("content", "dd");
//        paramMap.put("extInfo", JsonUtils.toJson(extInfo));
//        // 计算SIG
//        String sig = DigestSignUtils.signMd5(paramMap, secretKey);
//        paramMap.put("sig", sig);
//
//        String apiURL = BASE_URL + "/v1/appmessage/sendjpushmessagetostudentparents.vpage";
//        String url = UrlUtils.buildUrlQuery(apiURL, paramMap);
//        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().post(url).execute();
//        System.out.println(response.getResponseString());
//        System.out.println("Test End...");

//        String appKey = AppMessageSource.JUNIOR_STUDENT.name();
//        String secretKey = "NzVe9rUVkWQt";
//
//        Map<String, Object> extInfo = new HashMap<>();
//        extInfo.put("url", "http://just4test-spg-report.daoapp.io/?session_key=905fc9c3e3ef2563806730d841b91ae3&appkey=A17ZYSPG");
//        Map<String, String> paramMap = new HashMap<>();
//
//        paramMap.put("uid","333901729");
//        paramMap.put("content", "dd");
//        paramMap.put("source",appKey);
//        paramMap.put("extInfo", JsonUtils.toJson(extInfo));
//        paramMap.put("app_key","17JuniorStu");
//
//        // 计算SIG
//        String sig = DigestSignUtils.signMd5(paramMap, secretKey);
//        paramMap.put("sig", sig);
//
//        String apiURL = BASE_URL + "/v1/appjpushmsg/senduserjpushmsg.vpage";
//        String url = UrlUtils.buildUrlQuery(apiURL, paramMap);
//        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().post(url).execute();
//        System.out.println(response.getResponseString());
//        System.out.println("Test End...");




//        int type = TeacherMessageType.HOMEWORK.getType();
//        String secretKey = "NzVe9rUVkWQt";
//
//        Map<String, Object> extInfo = new HashMap<>();
//        extInfo.put("url", "http://just4test-spg-report.daoapp.io/?session_key=905fc9c3e3ef2563806730d841b91ae3&appkey=A17ZYSPG");
//        Map<String, String> paramMap = new HashMap<>();
//
//        paramMap.put("type",""+type);
//        paramMap.put("uid","333901729");
//        paramMap.put("title","ceshi1");
//        paramMap.put("content", "dd");
//        paramMap.put("imgUrl","ddddd");
//        paramMap.put("linkUrl","aaaaaaa");
//        paramMap.put("linkType",""+0);
//        paramMap.put("extInfo", JsonUtils.toJson(extInfo));
//        paramMap.put("app_key","17JuniorStu");
//
//        // 计算SIG
//        String sig = DigestSignUtils.signMd5(paramMap, secretKey);
//        paramMap.put("sig", sig);
//
//        String apiURL = BASE_URL + "/v1/appmessage/sendusermessage.vpage";
//        String url = UrlUtils.buildUrlQuery(apiURL, paramMap);
//        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().post(url).execute();
//        System.out.println(response.getResponseString());
//        System.out.println("Test End...");


        String secretKey = "NzVe9rUVkWQt";

        Map<String, Object> extInfo = new HashMap<>();
        extInfo.put("url", "http://just4test-spg-report.daoapp.io/?session_key=905fc9c3e3ef2563806730d841b91ae3&appkey=A17ZYSPG");
        Map<String, String> paramMap = new HashMap<>();

        final String sessionKey = "19cf0bc141890bae548a84383acad8bf"; // 30002 test
        paramMap.put("page","1");
        paramMap.put("app_key","17JuniorStu");
        paramMap.put("session_key",sessionKey);

        // 计算SIG
        String sig = DigestSignUtils.signMd5(paramMap, secretKey);
        paramMap.put("sig", sig);

        String apiURL = BASE_URL + "/v1/appmessage/loaduserMessage.vpage";
        String url = UrlUtils.buildUrlQuery(apiURL, paramMap);
        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().post(url).execute();
        System.out.println(response.getResponseString());
        System.out.println("Test End...");
    }







}
