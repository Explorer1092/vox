package com.voxlearning.washington.controller.open.v1.teacher.test;

import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.http.client.execute.POST;
import com.voxlearning.alps.lang.util.DigestSignUtils;

import java.util.HashMap;
import java.util.Map;

public class TestTeacherBasicReviewHomeworkReportApiController {
    private final static String appKey = "17Teacher";
    private final static String secretKey = "gvUKQN1EFXKp";

    private final static String sessionKey = "531dd1acc93b0ed70c8f8f21d804e9dc";

    private static Map<String, String> paramMap = new HashMap<>();

    static {
        paramMap.put("app_key", appKey);
        paramMap.put("session_key", sessionKey);
    }

    private static void execute(String apiURL) {
        String sig = DigestSignUtils.signMd5(paramMap, secretKey);
        paramMap.put("sig", sig);
        POST post = HttpRequestExecutor.defaultInstance().post(apiURL);
        paramMap.forEach(post::addParameter);
        System.out.println(post.execute().getResponseString());
    }

    private static void testFetchClazzList() {
        String url = "http://10.200.4.41:8081//v1/teacher/basicreview/report/clazzlist.api";
        paramMap.put("subject", "ENGLISH");
        execute(url);
    }
    private static void testFetchStageListToClazz() {
        String url = "http://10.200.4.41:8081//v1/teacher/basicreview/report/stagelist.api";
        paramMap.put("package_id", "BR_5a0be59daf81a324b4bde228");
        execute(url);
    }


    public static void main(String[] args) {
        try {
            testFetchStageListToClazz();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
