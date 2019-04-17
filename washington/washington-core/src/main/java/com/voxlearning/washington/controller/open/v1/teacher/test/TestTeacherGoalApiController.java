package com.voxlearning.washington.controller.open.v1.teacher.test;

import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.http.client.execute.POST;
import com.voxlearning.alps.lang.util.DigestSignUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author guoqiang.li
 * @since 2017/3/2
 */
public class TestTeacherGoalApiController {
    private final static String appKey = "17Teacher";
    private final static String secretKey = "gvUKQN1EFXKp";
    //    private final static String sessionKey = "3c6f584122eda6a813e00a56c0e0b791";
    private final static String sessionKey = "cf1e4811bf856a0837c4f6fadd70ce01";

    private static Map<String, String> paramMap = new HashMap<>();

    static {
        paramMap.put("app_key", appKey);
        paramMap.put("session_key", sessionKey);
    }

    private static void execute(String apiURL) {
        String sig = DigestSignUtils.signMd5(paramMap, secretKey);
        paramMap.put("sig", sig);
        POST post = HttpRequestExecutor.defaultInstance().post(apiURL);
        paramMap.entrySet().forEach(e -> {
            String name = e.getKey();
            String value = e.getValue();
            post.addParameter(name, value);
        });
        System.out.println(post.execute().getResponseString());
    }

    private static void testLoadClazzList() {
        String url = "http://localhost:8081/v1/teacher/goal/clazzlist.api";
//        String url = "http://api.dragon.17zuoye.net/v1/teacher/goal/clazzlist.api";
        execute(url);
    }

    private static void testLoadSummaryInfo() {
        String url = "http://localhost:8081/v1/teacher/goal/summary.api";
//        String url = "http://api.dragon.17zuoye.net/v1/teacher/goal/summary.api";
        paramMap.put("group_id", "80316");
        paramMap.put("unit_id", "BKC_10200088918920");
//        paramMap.put("group_id", "20669");
//        paramMap.put("unit_id", "BKC_10300009238258");

        execute(url);
    }

    private static void testSaveTeachingObjective() {
        String url = "http://localhost:8081/v1/teacher/goal/saveteachingobjective.api";
//        String url = "http://api.dragon.17zuoye.net/v1/teacher/goal/saveteachingobjective.api";
        paramMap.put("group_id", "20669");
        paramMap.put("unit_id", "BKC_10300009238258");
        paramMap.put("closed_kp_ids", "KP_10300036075974");
        execute(url);
    }

    public static void main(String[] args) {
        try {
//            testLoadClazzList();
            testLoadSummaryInfo();
//            testSaveTeachingObjective();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
