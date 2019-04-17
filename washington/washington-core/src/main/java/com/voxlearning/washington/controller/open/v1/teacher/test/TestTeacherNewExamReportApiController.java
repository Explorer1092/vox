package com.voxlearning.washington.controller.open.v1.teacher.test;

import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.http.client.execute.POST;
import com.voxlearning.alps.lang.util.DigestSignUtils;

import java.util.HashMap;
import java.util.Map;

public class TestTeacherNewExamReportApiController {
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

    private static void testclazzList() {
        String url = "http://localhost:8081/v1/teacher/newexam/report/newclazzlist.api";

        execute(url);
    }

    private static void testExamList(){

        String url = "http://localhost:8081/v1/teacher/newexam/report/newexamlist.api";
        paramMap.put("page","0");
        paramMap.put("subject","ENGLISH");
        paramMap.put("clazz_id","36149");
        execute(url);
    }

    private static void testAttendance(){
        String url = "http://localhost:8081/v1/teacher/newexam/report/newattendance.api";
        paramMap.put("clazz_id","36149");
        paramMap.put("exam_id","E_10300292247864");
        execute(url);
    }

    private static void testStudents(){
        String url = "http://localhost:8081/v1/teacher/newexam/report/newstudents.api";
        paramMap.put("clazz_id","36149");
        paramMap.put("exam_id","E_10300292251797");
        execute(url);
    }
    private static void testClazzquestions(){
        String url = "http://localhost:8081/v1/teacher/newexam/report/newclazzquestions.api";
        paramMap.put("clazz_id","36149");
        paramMap.put("exam_id","E_10300292254761");
        execute(url);
    }

    private static void testPersonalquestions(){
        String url = "http://localhost:8081/v1/teacher/newexam/report/personalquestions.api";
        paramMap.put("clazz_id","39391013");
        paramMap.put("exam_id","E_10300292239047");
        paramMap.put("student_id","333918300");
        execute(url);
    }


    private static void testNewstatistics(){
        String url = "http://localhost:8081/v1/teacher/newexam/report/newstatistics.api";
        paramMap.put("clazz_id","36149");
        paramMap.put("exam_id","E_10300292251797");
        execute(url);
    }




    public static void main(String[] args) {
        try {
            testClazzquestions();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
