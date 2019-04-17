package com.voxlearning.washington.controller.open.v1.teacher.test;

import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.http.client.execute.POST;
import com.voxlearning.alps.lang.util.DigestSignUtils;

import java.util.HashMap;
import java.util.Map;

public class TestTeacherVacationHomeworkReportApiController {
    private final static String appKey = "17Teacher";
    private final static String secretKey = "gvUKQN1EFXKp";
    //    private final static String sessionKey = "d2a99f64ac8fc7892cb906f6abaa30ed";    // 126504
//    private final static String sessionKey = "cf1e4811bf856a0837c4f6fadd70ce01";    // 125075
    private final static String sessionKey = "4dd142ee89fd69fccf773b78226d003c";    // 123120

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

    private static void testPushShareJztMsgReport() {
        String apiURL = "http://localhost:8081/v1/teacher/vacation/report/sharejztmsg.api";
        paramMap.put("packages", "5a1fc2268edbc85180db37ae");
        execute(apiURL);
    }

    private static void testShareReportWeiXin() {
        String apiURL = "http://localhost:8081/v1/teacher/vacation/report/shareweixin.api";
        paramMap.put("packages", "5a1fc2268edbc85180db37ae");
        execute(apiURL);
    }


    public static void main(String[] args) {
        System.out.println("Test start...");
        testPushShareJztMsgReport();
        System.out.println("Test End...");
    }
}
