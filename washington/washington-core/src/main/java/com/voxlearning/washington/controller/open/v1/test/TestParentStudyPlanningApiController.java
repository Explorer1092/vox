package com.voxlearning.washington.controller.open.v1.test;

import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.http.client.execute.POST;
import com.voxlearning.alps.lang.util.DigestSignUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author malong
 * @since 2018/10/18
 */
public class TestParentStudyPlanningApiController {
    public static final String APP_KEY = "17Parent";
    public static final String SECRET_KEY = "iMMrxI3XMQtd";
    public static final String SESSION_KEY = "d020dd6bc7e7847af072b2b8c5c4a34f";
    public static final String BASE_URL = "http://10.200.5.83:8081";

    public static void main(String[] args) {
        testIndex();
//        testFinish();
    }


    public static void testIndex() {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("app_key", APP_KEY);
        paramMap.put("session_key", SESSION_KEY);
        paramMap.put("sid", "333875333");
        String sig = DigestSignUtils.signMd5(paramMap, SECRET_KEY);
        paramMap.put("ver", "2.6.0");
        paramMap.put("sig", sig);
        String apiUrl = BASE_URL + "/v1/parent/study_planning/info.vpage";
        POST post = HttpRequestExecutor.defaultInstance().post(apiUrl);
        paramMap.forEach(post::addParameter);
        System.out.println(post.execute().getResponseString());
    }

    public static void testFinish() {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("app_key", APP_KEY);
        paramMap.put("session_key", SESSION_KEY);
        paramMap.put("sid", "333875333");
        paramMap.put("type", "AFENTI_ENGLISH");
        String sig = DigestSignUtils.signMd5(paramMap, SECRET_KEY);
        paramMap.put("sig", sig);
        String apiUrl = BASE_URL + "/v1/parent/study_planning/finish.vpage";
        POST post = HttpRequestExecutor.defaultInstance().post(apiUrl);
        paramMap.forEach(post::addParameter);
        System.out.println(post.execute().getResponseString());
    }

}
