package com.voxlearning.washington.controller.open.v1.test;

import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.http.client.execute.POST;
import com.voxlearning.alps.lang.util.DigestSignUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author malong
 * @since 2018/08/31
 */
public class TestParentStudyIndexController {
    public static final String APP_KEY = "17Parent";
    public static final String SECRET_KEY = "iMMrxI3XMQtd";
    public static final String VER = "2.4.0.0";
    public static final String SESSION_KEY = "d020dd6bc7e7847af072b2b8c5c4a34f";
    public static final String BASE_URL = "http://10.200.5.83:8081";

    public static void main(String[] args) {
//        getStudyResource();
        getCommon();
//        getAdList();
    }

    private static void getStudyResource() {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("app_key", APP_KEY);
        paramMap.put("session_key", SESSION_KEY);
        paramMap.put("sid", "333875333");
        String sig = DigestSignUtils.signMd5(paramMap, SECRET_KEY);
        paramMap.put("ver", VER);
        paramMap.put("sig", sig);
        String apiUrl = BASE_URL + "/v2/parent/study/all_study_resource.vpage";
        POST post = HttpRequestExecutor.defaultInstance().post(apiUrl);
        paramMap.forEach(post::addParameter);
        System.out.println(post.execute().getResponseString());
    }

    private static void getCommon() {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("app_key", APP_KEY);
        paramMap.put("session_key", SESSION_KEY);
        paramMap.put("sid", "333875333");
        String sig = DigestSignUtils.signMd5(paramMap, SECRET_KEY);
        paramMap.put("ver", VER);
        paramMap.put("sig", sig);
        String apiUrl = BASE_URL + "/v2/parent/study/common.vpage";
        POST post = HttpRequestExecutor.defaultInstance().post(apiUrl);
        paramMap.forEach(post::addParameter);
        System.out.println(post.execute().getResponseString());
    }

    private static void getAdList() {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("app_key", APP_KEY);
        paramMap.put("session_key", SESSION_KEY);
        paramMap.put("sid", "333875333");
        String sig = DigestSignUtils.signMd5(paramMap, SECRET_KEY);
        paramMap.put("ver", VER);
        paramMap.put("sig", sig);
        String apiUrl = BASE_URL + "/v2/parent/study/hot_activities.vpage";
        POST post = HttpRequestExecutor.defaultInstance().post(apiUrl);
        paramMap.forEach(post::addParameter);
        System.out.println(post.execute().getResponseString());
    }
}
