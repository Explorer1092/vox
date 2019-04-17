package com.voxlearning.washington.controller.open.test;

import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.http.client.execute.POST;
import com.voxlearning.alps.lang.util.DigestSignUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author malong
 * @since 2018/3/15
 */
public class TestParentGroupMessageApiController {
    public static final String APP_KEY = "17Parent";
    public static final String SECRET_KEY = "iMMrxI3XMQtd";
    public static final String VER = "2.5.0.0";
    public static final String SESSION_KEY = "d020dd6bc7e7847af072b2b8c5c4a34f";
    public static final String BASE_URL = "http://10.200.5.83:8081";

    public static void main(String[] args) {
        getMessageList();
//        getClazzList();
//        getClazz();
//        getNoticeList();
//        getMenuList();
//        getGroups();
    }

    public static void reloadMessage() {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("app_key", APP_KEY);
        paramMap.put("session_key", SESSION_KEY);
        paramMap.put("sid", "333875333");
        paramMap.put("request_type", "FLOWER");
        paramMap.put("type_id", "201812_5c22ed67e92b1b2fd383dd86_2");
        paramMap.put("type_name", "HOMEWORK_NEW");
        String sig = DigestSignUtils.signMd5(paramMap, SECRET_KEY);
        paramMap.put("sig", sig);
        String apiUrl = BASE_URL + "/v2/parent/group_message/reload_card.vpage";
        POST post = HttpRequestExecutor.defaultInstance().post(apiUrl);
        paramMap.forEach(post::addParameter);
        System.out.println(post.execute().getResponseString());
    }

    public static void getClazzList() {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("app_key", APP_KEY);
        paramMap.put("session_key", SESSION_KEY);
        paramMap.put("sid", "333895360");
        paramMap.put("create_time", "0");
        paramMap.put("group_name", "");
        String sig = DigestSignUtils.signMd5(paramMap, SECRET_KEY);
        paramMap.put("sig", sig);
        String apiUrl = BASE_URL + "/v2/parent/group_message/list.vpage";
        POST post = HttpRequestExecutor.defaultInstance().post(apiUrl);
        paramMap.forEach(post::addParameter);
        System.out.println(post.execute().getResponseString());
    }

    public static void getMessageList() {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("app_key", APP_KEY);
        paramMap.put("session_key", SESSION_KEY);
        paramMap.put("sid", "333875333");
        paramMap.put("create_time", "0");
        String sig = DigestSignUtils.signMd5(paramMap, SECRET_KEY);
        paramMap.put("sig", sig);
        String apiUrl = BASE_URL + "/v2/parent/group_message/message_list.vpage";
        POST post = HttpRequestExecutor.defaultInstance().post(apiUrl);
        paramMap.forEach(post::addParameter);
        System.out.println(post.execute().getResponseString());
    }


    public static void getClazz() {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("app_key", APP_KEY);
        paramMap.put("session_key", SESSION_KEY);
        paramMap.put("sid", "333895360");
        paramMap.put("notice_time", "0");
        String sig = DigestSignUtils.signMd5(paramMap, SECRET_KEY);
        paramMap.put("sig", sig);
        String apiUrl = BASE_URL + "/v2/parent/group_message/clazz.vpage";
        POST post = HttpRequestExecutor.defaultInstance().post(apiUrl);
        paramMap.forEach(post::addParameter);
        System.out.println(post.execute().getResponseString());
    }

    public static void getNoticeList() {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("app_key", APP_KEY);
        paramMap.put("session_key", SESSION_KEY);
        paramMap.put("sid", "333875333");
        String sig = DigestSignUtils.signMd5(paramMap, SECRET_KEY);
        paramMap.put("sig", sig);
        String apiUrl = BASE_URL + "/v2/parent/group_message/top_notices.vpage";
        POST post = HttpRequestExecutor.defaultInstance().post(apiUrl);
        paramMap.forEach(post::addParameter);
        System.out.println(post.execute().getResponseString());
    }

    public static void getMenuList() {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("app_key", APP_KEY);
        paramMap.put("session_key", SESSION_KEY);
        paramMap.put("sid", "333875333");
        String sig = DigestSignUtils.signMd5(paramMap, SECRET_KEY);
        paramMap.put("ver", VER);
        paramMap.put("sig", sig);
        String apiUrl = BASE_URL + "/v2/parent/group_message/menu.vpage";
        POST post = HttpRequestExecutor.defaultInstance().post(apiUrl);
        paramMap.forEach(post::addParameter);
        System.out.println(post.execute().getResponseString());
    }

    public static void getGroups() {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("app_key", APP_KEY);
        paramMap.put("session_key", SESSION_KEY);
        paramMap.put("sid", "333875333");
        String sig = DigestSignUtils.signMd5(paramMap, SECRET_KEY);
        paramMap.put("sig", sig);
        String apiUrl = BASE_URL + "/v2/parent/group_message/groups.vpage";
        POST post = HttpRequestExecutor.defaultInstance().post(apiUrl);
        paramMap.forEach(post::addParameter);
        System.out.println(post.execute().getResponseString());
    }
}
