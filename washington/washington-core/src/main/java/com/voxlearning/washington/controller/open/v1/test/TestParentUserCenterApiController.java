package com.voxlearning.washington.controller.open.v1.test;

import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.http.client.execute.POST;
import com.voxlearning.alps.lang.util.DigestSignUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author malong
 * @since 2018/07/31
 */
public class TestParentUserCenterApiController {
    public static final String APP_KEY = "17Parent";
    public static final String SECRET_KEY = "iMMrxI3XMQtd";
    public static final String VER = "2.5.0.0";
    public static final String SESSION_KEY = "d020dd6bc7e7847af072b2b8c5c4a34f";
    public static final String BASE_URL = "http://10.200.5.83:8081";

    public static void main(String[] args) {
        getWebViewConfig();
//        getTabList();
//        cleanRemind();
    }

    public static void getTabList() {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("app_key", APP_KEY);
        paramMap.put("session_key", SESSION_KEY);
        String sig = DigestSignUtils.signMd5(paramMap, SECRET_KEY);
        paramMap.put("ver", VER);
        paramMap.put("sig", sig);
        String apiUrl = BASE_URL + "/v1/parent/ucenter/tab/list.vpage";
        POST post = HttpRequestExecutor.defaultInstance().post(apiUrl);
        paramMap.forEach(post::addParameter);
        System.out.println(post.execute().getResponseString());
    }

    public static void cleanRemind() {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("app_key", APP_KEY);
        paramMap.put("session_key", SESSION_KEY);
        paramMap.put("remind_position", "PARENT_APP_TAB_PARENT_TALK");
        String sig = DigestSignUtils.signMd5(paramMap, SECRET_KEY);
        paramMap.put("ver", VER);
        paramMap.put("sig", sig);
        String apiUrl = BASE_URL + "/v1/parent/ucenter/remind/clean.vpage";
        POST post = HttpRequestExecutor.defaultInstance().post(apiUrl);
        paramMap.forEach(post::addParameter);
        System.out.println(post.execute().getResponseString());
    }

    public static void getRecommend() {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("app_key", APP_KEY);
        paramMap.put("session_key", SESSION_KEY);
        paramMap.put("sid", "333875333");
        String sig = DigestSignUtils.signMd5(paramMap, SECRET_KEY);
        paramMap.put("ver", VER);
        paramMap.put("sig", sig);
        String apiUrl = BASE_URL + "/v1/parent/ucenter/study/plan/recommend.vpage";
        POST post = HttpRequestExecutor.defaultInstance().post(apiUrl);
        paramMap.forEach(post::addParameter);
        System.out.println(post.execute().getResponseString());
    }

    public static void getWebViewConfig() {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("app_key", APP_KEY);
        String sig = DigestSignUtils.signMd5(paramMap, SECRET_KEY);
        paramMap.put("ver", VER);
        paramMap.put("sig", sig);
        String apiUrl = BASE_URL + "/v1/parent/ucenter/webview/config.vpage";
        POST post = HttpRequestExecutor.defaultInstance().post(apiUrl);
        paramMap.forEach(post::addParameter);
        System.out.println(post.execute().getResponseString());
    }
}
