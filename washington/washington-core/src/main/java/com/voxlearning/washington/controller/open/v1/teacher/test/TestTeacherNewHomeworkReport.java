package com.voxlearning.washington.controller.open.v1.teacher.test;

import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.http.client.execute.POST;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.DigestSignUtils;

import java.util.HashMap;
import java.util.Map;

public class TestTeacherNewHomeworkReport {
    private final static String appKey = "17Teacher";
    private final static String secretKey = "gvUKQN1EFXKp";
    //    private final static String sessionKey = "1c69fbe57e7dbff515fd011f11f71296";
//    private final static String sessionKey = "8d471610ff0f5734307ac75e3575fe07";
//    private final static String sessionKey = "0bbaa4999c4fa9a4deb7ac330e59635d";
//    private final static String sessionKey = "f5a0129503f3667122f87f8028e15f80";
//    private final static String sessionKey = "4ae74444ba377f5d3b538dd945eacb14";
//    private final static String sessionKey = "53d0bb7b616f14f4ab19a230672f77f7"; // 学前 12975739
    private final static String sessionKey = "f51f047fdf9ab2c41c564911504e59d1"; // 学前 12975755
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

    private static void teacherclazzlist() {
        String apiURL = "http://localhost:8081/v1/teacher/new/report/teacherclazzlist.vpage";
        execute(apiURL);
    }

    private static void uncheckedHomeworkList() {
        String apiURL = "http://localhost:8081/v1/teacher/new/report/uncheckedhomeworklist.vpage";
        execute(apiURL);
    }

    private static void homeworklist() {
        String apiURL = "http://localhost:8081/v1/teacher/new/report/homeworklist.vpage";
        paramMap.put("clazz_group_ids", "81464");
        paramMap.put("page_number", "1");
        paramMap.put("homework_status", "all");
        execute(apiURL);
    }

    private static void adjustindex() {
        String apiURL = "http://localhost:8081/v1/teacher/new/report/adjust/index.vpage";
        paramMap.put("homework_id", "570e1fbb54dc9d1fb4edc458");
        execute(apiURL);
    }

    private static void homeworkfinishinfo() {
        String apiURL = "http://api.dragon.17zuoye.net/v1/teacher/new/report/homeworkfinishinfo.vpage";
        paramMap.put("homework_id", "570e1fbb54dc9d1fb4edc458");
        execute(apiURL);
    }

    private static void englishuncheckedhomeworklist() {
        String apiURL = "http://localhost:8081/v1/teacher/new/report/englishuncheckedhomeworklist.vpage";
        execute(apiURL);
    }
//
//    private static void homeworklist(){
//        String apiURL = "http://localhost:8081/v1/teacher/new/report/teacherclazzlist.vpage";
//        execute(apiURL);
//    }
//
//    private static void homeworklist(){
//        String apiURL = "http://localhost:8081/v1/teacher/new/report/teacherclazzlist.vpage";
//        execute(apiURL);
//    }
//
//    private static void homeworklist(){
//        String apiURL = "http://localhost:8081/v1/teacher/new/report/teacherclazzlist.vpage";
//        execute(apiURL);
//    }
//
//    private static void homeworklist(){
//        String apiURL = "http://localhost:8081/v1/teacher/new/report/teacherclazzlist.vpage";
//        execute(apiURL);
//    }
//
//    private static void homeworklist(){
//        String apiURL = "http://localhost:8081/v1/teacher/new/report/teacherclazzlist.vpage";
//        execute(apiURL);
//    }

    private static void testNeedcorrect() {
        String apiURL = "http://api.dragon.17zuoye.net/v1/teacher/new/report/needcorrect.vpage";
        paramMap.put("homework_id", "570e1fbb54dc9d1fb4edc458");
        execute(apiURL);
    }

    private static void testBatchCorrectQuestion() {
        String apiURL = "http://localhost:8081/v1/teacher/new/report/batchcorrectquestion.vpage";
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("homeworkId", "570e1fbb54dc9d1fb4edc458");
        jsonMap.put("questionId", "Q_10200744084313-1");
        jsonMap.put("type", "EXAM");
        jsonMap.put("isBatch", "true");
//        jsonMap.put("isBatch", "false");
//        Map<String, Object> corrections = new HashMap<>();
//        corrections.put("userId", "");
//        corrections.put("review", "true");
//        corrections.put("correction", "EXCELLENT");
//        jsonMap.put("corrections", corrections);
        paramMap.put("correct_json", JsonUtils.toJson(jsonMap));
        execute(apiURL);
    }

    private static void testHomeworkWriteComment() {
        String apiURL = "http://localhost:8081/v1/teacher/new/report/homeworkwritecomment.vpage";
        paramMap.put("homework_id", "570e1fbb54dc9d1fb4edc458");
        paramMap.put("student_list", "333894383,333894432,333894555");
        paramMap.put("comment", "做得太棒了！");
        execute(apiURL);
    }


    public static void main(String[] args) {
        try {
            System.out.println("Test Start...");
//            testNeedcorrect();
            teacherclazzlist();
//            uncheckedHomeworkList();
//            homeworklist();
//            adjustindex();
//            homeworkfinishinfo();
//            testBatchCorrectQuestion();
//            englishuncheckedhomeworklist();
            System.out.println("Test End...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
