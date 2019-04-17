package com.voxlearning.washington.controller.open.v2.test;

import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.http.client.execute.POST;
import com.voxlearning.alps.lang.util.DigestSignUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xuesong.zhang
 * @since 2019/3/21
 */
public class TestOpenApiTeacherV2 {

    private final static String appKey = "17Teacher";
    private final static String secretKey = "gvUKQN1EFXKp";
    //    private final static String sessionKey = "18f3126e814a618206037e9fb04d1c2d"; // 12988885
    private final static String sessionKey = "5e1aaa49bb739f7e5fd70ab791304a1b"; // 12988869
    private static Map<String, String> paramMap = new HashMap<>();

    static {
        paramMap.put("app_key", appKey);
        paramMap.put("session_key", sessionKey);
    }

    private static void execute(String apiURL) {
        System.out.println("============================================");
        String sig = DigestSignUtils.signMd5(paramMap, secretKey);
        paramMap.put("sig", sig);
        POST post = HttpRequestExecutor.defaultInstance().post(apiURL);
        paramMap.forEach(post::addParameter);
        System.out.println(post.execute().getResponseString());
        System.out.println("============================================");
    }

    private static void testExitClazz() {
        String url = "http://localhost:8081/v2/teacher/exitclazz.api";
        paramMap.put("clazz_id", "40101247");
        execute(url);
    }

    private static void testChangeSchoolWithoutClazz() {
        String url = "http://localhost:8081/v2/teacher/changeschoolwithoutclazz.api";
        paramMap.put("school_id", "74525");
        paramMap.put("enforce", "true");
        execute(url);
    }

    private static void testChangeSubject() {
        String url = "http://localhost:8081/v2/teacher/changesubject.api";
        paramMap.put("subject", "ENGLISH");
        execute(url);
    }

    public static void main(String[] args) {
//        testExitClazz();
//        testChangeSchoolWithoutClazz();
        testChangeSubject();
//        teacherLogin();
    }


    private static void teacherLogin() {
        try {
            System.out.println("Test Start...");
            String secretKey = "FP7lk6WDSSXy";

            // 12988885/1
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("app_key", "17Teacher");
            paramMap.put("app_product_id", "301");
            paramMap.put("model", "iPhone10,3");

            paramMap.put("user_code", "12981800");
            paramMap.put("passwd", "1");
            paramMap.put("user_type", "1");

            paramMap.put("uuid", "947f449f-fce6-4a3d-9d99-3ee9bfba48fc");
            // paramMap.put("uuid", "158CA437-4C3A-471D-BAB2-6A546A5CD2AA");
            paramMap.put("ver", "1.9.4.332");
            paramMap.put("sys", "ios");

            String sig = DigestSignUtils.signMd5(paramMap, secretKey);
            paramMap.put("sig", sig);

            String apiURL = "http://localhost:8081/v1/teacher/user/login.vpage";
            POST post = HttpRequestExecutor.defaultInstance().post(apiURL);
            paramMap.forEach(post::addParameter);
            System.out.println(post.execute().getResponseString());

            System.out.println("Test End...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
