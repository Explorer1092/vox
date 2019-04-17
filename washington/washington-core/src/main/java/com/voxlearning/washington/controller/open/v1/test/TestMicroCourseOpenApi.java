/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.washington.controller.open.v1.test;

/**
 * Created by Yuechen Wang on 2016-05-12.
 */
public class TestMicroCourseOpenApi {

/*
    @ImportService(interfaceClass = TalkFunService.class)
    private TalkFunService talkFunService;

    public static void main(String[] args) {
        String courseId = "18957";
        try {
            System.out.println("Test Micro Course Start...");
            //======================================================================
            System.out.println("Testing finish class api...");
            testFinishClazz(courseId, Mode.DEVELOPMENT);
            //======================================================================
//            System.out.println("Testing replay remind api...");
//            testReplayRemind(courseId, Mode.DEVELOPMENT);
            //======================================================================
            System.out.println("Test End...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void testFinishClazz(String courseId, Mode runtime) throws Exception {
        String url = "http://localhost:8081/v1/microcourse/talkfun/callback.vpage";
        Map<String, String> queryMap = new TreeMap<>();
        queryMap.put("openID", TalkFunUtils.openID(runtime)); // 合作方唯一标识码
        queryMap.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000)); // 当前Unix时间戳
        queryMap.put("cmd", "live.stop"); // 调用接口的名称

        Map<String, Object> data = new HashMap<>();
        data.put("course_id", courseId);
        queryMap.put("params", TalkFunUtils.safeUrlEncode(JsonUtils.toJson(data))); // 接口参数
        queryMap.put("ver", "1.0"); // 协议版本号，默认1.0
        // 拼接参数
        StringBuilder sb = new StringBuilder();
        queryMap.entrySet().forEach(e -> sb.append(e.getKey()).append(e.getValue()));
        sb.append(TalkFunUtils.openToken(runtime));
        String sign = DigestUtils.md5Hex(sb.toString().getBytes("UTF-8")); // MD5
        queryMap.put("sign", sign); // 签名

        String URL = UrlUtils.buildUrlQuery(url, queryMap);
        POST processPost = HttpRequestExecutor.defaultInstance().post(URL);
        System.out.println(String.format("course_id: %10s | %s ", courseId, processPost.execute().getResponseString()));
    }

    private static void testReplayRemind(String courseId, Mode runtime) throws Exception {
        String url = "http://localhost:8081/v1/microcourse/talkfun/callback.vpage";
        Map<String, String> queryMap = new TreeMap<>();
        queryMap.put("openID", TalkFunUtils.openID(runtime)); // 合作方唯一标识码
        queryMap.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000)); // 当前Unix时间戳
        queryMap.put("cmd", "live.playback"); // 调用接口的名称

        Map<String, Object> data = new HashMap<>();
        data.put("course_id", courseId);
        queryMap.put("params", TalkFunUtils.safeUrlEncode(JsonUtils.toJson(data))); // 接口参数
        queryMap.put("ver", "1.0"); // 协议版本号，默认1.0
        // 拼接参数
        StringBuilder sb = new StringBuilder();
        queryMap.entrySet().forEach(e -> sb.append(e.getKey()).append(e.getValue()));
        sb.append(TalkFunUtils.openToken(runtime));
        String sign = DigestUtils.md5Hex(sb.toString().getBytes("UTF-8")); // MD5
        queryMap.put("sign", sign); // 签名

        String URL = UrlUtils.buildUrlQuery(url, queryMap);
        POST processPost = HttpRequestExecutor.defaultInstance().post(URL);
        System.out.println(String.format("course_id: %10s | %s ", courseId, processPost.execute().getResponseString()));
    }

*/
}
