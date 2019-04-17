package com.voxlearning.washington.controller.open.v1.test;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.http.client.execute.POST;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.DigestSignUtils;
import com.voxlearning.utopia.service.business.api.entity.TeachingResource;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TestDaiteApiController {

    public static void main(String[] args) {
        try {
            System.out.println("Test Start...");
            final String appKey = "Daite";
            String secretKey = "hpKg6k4Xgrrd";
//            Map<String, String> paramMap = new HashMap<>();
//            paramMap.put("lat", "47.8721439689");
//            paramMap.put("lon", "79.8046875000");
//            paramMap.put("schoolLevel", "2");
//            paramMap.put("app_key", "17Teacher");
//            String sig = DigestSignUtils.signMd5(paramMap, secretKey);
//            System.out.println(sig);
            // 检查用户名密码
//            testCheckUserPassword(appKey, secretKey);
//            testGetSchoolInfo(appKey, secretKey);
//            testGetClassInfo(appKey, secretKey);
//            testUpsertClassInfo(appKey, secretKey);
//            testGetUserInfo(appKey, secretKey);
//            testUpsertUserInfo(appKey, secretKey);
//            testGetTeacherRelationInfo(appKey, secretKey);
//            testGetStudentFamilyRelationInfo(appKey, secretKey);
//            testGetParentFamilyRelationInfo(appKey, secretKey);
//            testUpdateFamilyRelation(appKey, secretKey);
//            testInsertStudentRelation(appKey, secretKey);
//            testDeleteStudentRelation(appKey, secretKey);
//            testDeleteTeacherRelation(appKey, secretKey);
//            testInsertTeacherRelation(appKey, secretKey);
//            testGetSchoolClazz(appKey, secretKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void testInsertTeacherRelation(String appKey, String secretKey) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("app_key", appKey);
        paramMap.put("teacher_id", "12980049");
        paramMap.put("class_id", "39223150");
        paramMap.put("timestamp", System.currentTimeMillis()+"");
        String sig = DigestSignUtils.signMd5(paramMap, secretKey);
        paramMap.put("sig", sig);

        String apiURL = "http://localhost:8081/v1/daite/insertTeacherRelation.vpage";
        POST post = HttpRequestExecutor.defaultInstance().post(apiURL);
        paramMap.entrySet().forEach(e -> {
            String name = e.getKey();
            String value = e.getValue();
            post.addParameter(name, value);
        });
        System.out.println(post.execute().getResponseString());
    }

    private static void testDeleteTeacherRelation(String appKey, String secretKey) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("app_key", appKey);
        paramMap.put("teacher_id", "12980049");
        paramMap.put("class_id", "39223150");
        paramMap.put("timestamp", System.currentTimeMillis()+"");
        String sig = DigestSignUtils.signMd5(paramMap, secretKey);
        paramMap.put("sig", sig);

        String apiURL = "http://localhost:8081/v1/daite/deleteTeacherRelation.vpage";
        POST post = HttpRequestExecutor.defaultInstance().post(apiURL);
        paramMap.entrySet().forEach(e -> {
            String name = e.getKey();
            String value = e.getValue();
            post.addParameter(name, value);
        });
        System.out.println(post.execute().getResponseString());
    }
    private static void testDeleteStudentRelation(String appKey, String secretKey) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("app_key", appKey);
        paramMap.put("student_id", "333931651");
        paramMap.put("class_id", "39223149");
        paramMap.put("timestamp", System.currentTimeMillis()+"");
        String sig = DigestSignUtils.signMd5(paramMap, secretKey);
        paramMap.put("sig", sig);

        String apiURL = "http://localhost:8081/v1/daite/deleteStudentRelation.vpage";
        POST post = HttpRequestExecutor.defaultInstance().post(apiURL);
        paramMap.entrySet().forEach(e -> {
            String name = e.getKey();
            String value = e.getValue();
            post.addParameter(name, value);
        });
        System.out.println(post.execute().getResponseString());
    }

    private static void testInsertStudentRelation(String appKey, String secretKey) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("app_key", appKey);
        paramMap.put("student_id", "333931651");
        paramMap.put("teacher_id", "12980049");
        paramMap.put("class_id", "39223150");
        paramMap.put("timestamp", System.currentTimeMillis()+"");
        String sig = DigestSignUtils.signMd5(paramMap, secretKey);
        paramMap.put("sig", sig);

        String apiURL = "http://localhost:8081/v1/daite/insertStudentRelation.vpage";
        POST post = HttpRequestExecutor.defaultInstance().post(apiURL);
        paramMap.entrySet().forEach(e -> {
            String name = e.getKey();
            String value = e.getValue();
            post.addParameter(name, value);
        });
        System.out.println(post.execute().getResponseString());
    }

    private static void testUpdateFamilyRelation(String appKey, String secretKey) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("app_key", appKey);
        paramMap.put("student_id", "333931651");

        paramMap.put("relations", JsonUtils.toJson(Arrays.asList(MapUtils.m("parent_id", 2050002, "relation", "妈妈"), MapUtils.m("parent_id", 2050003, "relation", "妈妈"))));
        paramMap.put("timestamp", System.currentTimeMillis()+"");
        String sig = DigestSignUtils.signMd5(paramMap, secretKey);
        paramMap.put("sig", sig);

        String apiURL = "http://localhost:8081/v1/daite/updateFamilyRelation.vpage";
        POST post = HttpRequestExecutor.defaultInstance().post(apiURL);
        paramMap.entrySet().forEach(e -> {
            String name = e.getKey();
            String value = e.getValue();
            post.addParameter(name, value);
        });
        System.out.println(post.execute().getResponseString());
    }

    private static void testGetParentFamilyRelationInfo(String appKey, String secretKey) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("app_key", appKey);
        paramMap.put("user_id", "2050003");
        paramMap.put("timestamp", System.currentTimeMillis()+"");
        String sig = DigestSignUtils.signMd5(paramMap, secretKey);
        paramMap.put("sig", sig);

        String apiURL = "http://localhost:8081/v1/daite/getParentFamilyRelationInfo.vpage";
        POST post = HttpRequestExecutor.defaultInstance().post(apiURL);
        paramMap.entrySet().forEach(e -> {
            String name = e.getKey();
            String value = e.getValue();
            post.addParameter(name, value);
        });
        System.out.println(post.execute().getResponseString());
    }
    private static void testGetStudentFamilyRelationInfo(String appKey, String secretKey) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("app_key", appKey);
        paramMap.put("user_id", "333931651");
        paramMap.put("timestamp", System.currentTimeMillis()+"");
        String sig = DigestSignUtils.signMd5(paramMap, secretKey);
        paramMap.put("sig", sig);

        String apiURL = "http://localhost:8081/v1/daite/getStudentFamilyRelationInfo.vpage";
        POST post = HttpRequestExecutor.defaultInstance().post(apiURL);
        paramMap.entrySet().forEach(e -> {
            String name = e.getKey();
            String value = e.getValue();
            post.addParameter(name, value);
        });
        System.out.println(post.execute().getResponseString());
    }
    private static void testGetTeacherRelationInfo(String appKey, String secretKey) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("app_key", appKey);
        paramMap.put("user_id", "12980049");
        paramMap.put("timestamp", System.currentTimeMillis()+"");
        String sig = DigestSignUtils.signMd5(paramMap, secretKey);
        paramMap.put("sig", sig);
        String apiURL = "http://localhost:8081/v1/daite/getTeacherRelationInfo.vpage";
        POST post = HttpRequestExecutor.defaultInstance().post(apiURL);
        paramMap.entrySet().forEach(e -> {
            String name = e.getKey();
            String value = e.getValue();
            post.addParameter(name, value);
        });
        System.out.println(post.execute().getResponseString());
    }
    private static void testUpsertUserInfo(String appKey, String secretKey) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("app_key", appKey);
//        paramMap.put("user_id", "12980051");
        paramMap.put("timestamp", System.currentTimeMillis()+"");
        paramMap.put("realname", "齐晓一");
//        paramMap.put("mobile", "12245213700");
        paramMap.put("avatar", "GIRL100");
        paramMap.put("password", "1");
        paramMap.put("gender", "F");
        paramMap.put("user_type", "1");
        paramMap.put("subject", TeachingResource.Subject.CHINESE.name());
        paramMap.put("school_id", "30636");
        String sig = DigestSignUtils.signMd5(paramMap, secretKey);
        paramMap.put("sig", sig);

        String apiURL = "http://localhost:8081/v1/daite/upsertUserInfo.vpage";
        POST post = HttpRequestExecutor.defaultInstance().post(apiURL);
        paramMap.entrySet().forEach(e -> {
            String name = e.getKey();
            String value = e.getValue();
            post.addParameter(name, value);
        });
        System.out.println(post.execute().getResponseString());
    }
    private static void testGetUserInfo(String appKey, String secretKey) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("app_key", appKey);
        paramMap.put("user_id", "12980049");
        paramMap.put("timestamp", System.currentTimeMillis()+"");
        String sig = DigestSignUtils.signMd5(paramMap, secretKey);
        paramMap.put("sig", sig);

        String apiURL = "http://localhost:8081/v1/daite/getUserInfo.vpage";
        POST post = HttpRequestExecutor.defaultInstance().post(apiURL);
        paramMap.entrySet().forEach(e -> {
            String name = e.getKey();
            String value = e.getValue();
            post.addParameter(name, value);
        });
        System.out.println(post.execute().getResponseString());
    }
    private static void testUpsertClassInfo(String appKey, String secretKey) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("app_key", appKey);
//        paramMap.put("class_id", "40098058");
        paramMap.put("timestamp", System.currentTimeMillis()+"");
        paramMap.put("school_id", "30636");
        paramMap.put("class_type", "3");
        paramMap.put("name", "13");
        paramMap.put("class_level", "7");
        paramMap.put("edu_system", "J4");
        paramMap.put("teacher_id", "12980049");
        String sig = DigestSignUtils.signMd5(paramMap, secretKey);
        paramMap.put("sig", sig);

        String apiURL = "http://localhost:8081/v1/daite/upsertClassInfo.vpage";
        POST post = HttpRequestExecutor.defaultInstance().post(apiURL);
        paramMap.entrySet().forEach(e -> {
            String name = e.getKey();
            String value = e.getValue();
            post.addParameter(name, value);
        });
        System.out.println(post.execute().getResponseString());
    }
    private static void testGetClassInfo(String appKey, String secretKey) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("app_key", appKey);
        paramMap.put("class_id", "40098858");
        paramMap.put("timestamp", System.currentTimeMillis()+"");
        String sig = DigestSignUtils.signMd5(paramMap, secretKey);
        paramMap.put("sig", sig);

        String apiURL = "http://localhost:8081/v1/daite/getClassInfo.vpage";
        POST post = HttpRequestExecutor.defaultInstance().post(apiURL);
        paramMap.entrySet().forEach(e -> {
            String name = e.getKey();
            String value = e.getValue();
            post.addParameter(name, value);
        });
        System.out.println(post.execute().getResponseString());
    }

    private static void testGetSchoolInfo(String appKey, String secretKey) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("app_key", appKey);
        paramMap.put("school_id", "50819");
        paramMap.put("timestamp", System.currentTimeMillis()+"");
        String sig = DigestSignUtils.signMd5(paramMap, secretKey);
        paramMap.put("sig", sig);

        String apiURL = "http://localhost:8081/v1/daite/getSchoolInfo.vpage";
        POST post = HttpRequestExecutor.defaultInstance().post(apiURL);
        paramMap.entrySet().forEach(e -> {
            String name = e.getKey();
            String value = e.getValue();
            post.addParameter(name, value);
        });
        System.out.println(post.execute().getResponseString());
    }

    private static void testCheckUserPassword(String appKey, String secretKey) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("app_key", appKey);
        paramMap.put("user_ids", "12979800,12978766");
        paramMap.put("password", "1");
        String sig = DigestSignUtils.signMd5(paramMap, secretKey);
        paramMap.put("sig", sig);

        String apiURL = "http://localhost:8081/v1/daite/checkUserPassword.vpage";
        POST post = HttpRequestExecutor.defaultInstance().post(apiURL);
        paramMap.entrySet().forEach(e -> {
            String name = e.getKey();
            String value = e.getValue();
            post.addParameter(name, value);
        });
        System.out.println(post.execute().getResponseString());
    }

    private static void testGetSchoolClazz(String appKey, String secretKey) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("app_key", appKey);
        paramMap.put("class_id", "40098911");
        paramMap.put("timestamp", System.currentTimeMillis()+"");
        String sig = DigestSignUtils.signMd5(paramMap, secretKey);
        paramMap.put("sig", sig);

        String apiURL = "http://localhost:8081/v1/daite/getClazzStudentsInfo.vpage";
        POST post = HttpRequestExecutor.defaultInstance().post(apiURL);
        paramMap.entrySet().forEach(e -> {
            String name = e.getKey();
            String value = e.getValue();
            post.addParameter(name, value);
        });
        System.out.println(post.execute().getResponseString());
    }
}
