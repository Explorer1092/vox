package com.voxlearning.washington.controller.open.v1.teacher.test;

import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.http.client.execute.POST;
import com.voxlearning.alps.lang.util.DigestSignUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author guoqiang.li
 * @since 2017/5/3
 */
public class TestTeacherNewExamApiController {
    private final static String appKey = "17Teacher";
    private final static String secretKey = "gvUKQN1EFXKp";
    private final static String sessionKey = "d2a99f64ac8fc7892cb906f6abaa30ed"; // 126504
//    private final static String sessionKey = "cf1e4811bf856a0837c4f6fadd70ce01";    // 125075

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

    private static void testLoadPaperList() {
        String url = "http://api.hydra.17zuoye.net/v1/teacher/newexam/paperlist.api";
        paramMap.put("book_id", "BK_10300000585752");
        execute(url);
    }

    private static void testSaveNewExam() {
        String url = "http://api.hydra.17zuoye.net/v1/teacher/newexam/assign.api";
        paramMap.put("exam_data", "{\"paperId\":\"P_10300008429469-6\",\"groupIds\":\"31628\",\"startTime\":\"2017-05-03 00:00:00\",\"endTime\":\"2017-05-03 23:59:59\",\"durationMinutes\":30}");
        execute(url);
    }

    private static void testLoaderTeacherClazzList() {
        String url = "http://api.hydra.17zuoye.net/v1/teacher/newexam/report/clazzlist.api";
        execute(url);
    }

    private static void testLoaderExamList() {
        String url = "http://api.hydra.17zuoye.net/v1/teacher/newexam/report/examlist.api";
        paramMap.put("group_id", "31628");
        paramMap.put("page_number", "1");
        execute(url);
    }

    private static void testDeleteExam() {
        String url = "http://api.hydra.17zuoye.net/v1/teacher/newexam/delete.api";
        paramMap.put("exam_id", "E_10200002249876");
        execute(url);
    }

    public static void main(String[] args) {
//        testLoadPaperList();
//        testSaveNewExam();
//        testLoaderTeacherClazzList();
        testLoaderExamList();
//        testDeleteExam();
    }
}
