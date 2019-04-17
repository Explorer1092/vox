package com.voxlearning.washington.controller.open.v1.teacher.test;

import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.http.client.execute.POST;
import com.voxlearning.alps.lang.util.DigestSignUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author zhangbin
 * @since 2016/11/23 10:45
 */
public class TestTeacherTermReviewApiController {
    private final static String appKey = "17Teacher";
    private final static String secretKey = "gvUKQN1EFXKp";
    //    private final static String sessionKey = "6990af76e20742cdd4adf87d60032084"; // 123420
//    private final static String sessionKey = "799d25c10701f6c64147cd6fcfa71c1a"; // 125075
//    private final static String sessionKey = "6893ba03e656d063d6b52cd4b91f665e";
    private final static String sessionKey = "6990af76e20742cdd4adf87d60032084";


    private static Map<String, String> paramMap = new HashMap<>();

    static {
        paramMap.put("app_key", appKey);
        paramMap.put("session_key", sessionKey);
    }

    private static void execute(String apiURL) {
        POST post = HttpRequestExecutor.defaultInstance().post(apiURL);
        String sig = DigestSignUtils.signMd5(paramMap, secretKey);
        paramMap.put("sig", sig);
        paramMap.forEach(post::addParameter);
        System.out.println(post.execute().getResponseString());
    }

    private static void testLoadClazzList() {
        String apiURL = "http://api.test.17zuoye.net/v1/teacher/termreview/clazzlist.api";
        paramMap.put("subject", "ENGLISH");
        execute(apiURL);
    }

    private static void testLoadClazzBook() {
        String apiURL = "http://localhost:8081/v1/teacher/termreview/clazzbook.api";
        paramMap.put("clazzid_groupid_list", "40095626_101920");
        execute(apiURL);
    }

    private static void testLoadBookList() {
        String apiURL = "http://localhost:8081/v1/teacher/termreview/booklist.api";
        paramMap.put("clazz_level", "1");
        paramMap.put("book_term", "1");
        execute(apiURL);
    }

    private static void testTypelist() {
        String apiURL = "http://api.test.17zuoye.net/v1/teacher/termreview/typelist.api";
        paramMap.put("book_id", "BK_10100001675679");
        paramMap.put("clazz_ids", "1001557_82178");
        execute(apiURL);
    }

    private static void testContent() {
        String apiURL = "http://api.test.17zuoye.net/v1/teacher/termreview/content.api";
        paramMap.put("content_type", "WORD");
        paramMap.put("clazz_ids", "1001557_82179");
        paramMap.put("book_id", "BK_10300000265057");
        execute(apiURL);
    }

    private static void testPreviewBasicReview() {
        String apiURL = "http://localhost:8081/v1/teacher/termreview/basicreview/preview.api";
        paramMap.put("book_id", "BK_10300000266810");
        paramMap.put("content_types", "WORD,SENTENCE");
        execute(apiURL);
    }

    private static void testAssignBasicReview() {
        String apiURL = "http://localhost:8081/v1/teacher/termreview/basicreview/assign.api";
        Map<String, Object> jsonMap = new LinkedHashMap<>();
//        jsonMap.put("bookId", "BK_10200001561544");
//        jsonMap.put("groupIds", "9302");
//        jsonMap.put("homeworkDays", "10");
//        jsonMap.put("contentTypes", Collections.singleton("CALCULATION"));
//        paramMap.put("homework_data", JsonUtils.toJson(jsonMap));
//        paramMap.put("subject", "MATH");

//        jsonMap.put("bookId", "BK_10300000266810");
//        jsonMap.put("groupIds", "20669");
//        jsonMap.put("homeworkDays", "10");
//        jsonMap.put("contentTypes", Arrays.asList("WORD", "SENTENCE"));
//        paramMap.put("homework_data", JsonUtils.toJson(jsonMap));

        paramMap.put("homework_data", "{\"bookId\":\"BK_10300000766865\",\"homeworkDays\":7,\"groupIds\":\"101757\",\"contentTypes\":[\"WORD\",\"SENTENCE\"]}");
        paramMap.put("subject", "ENGLISH");
        execute(apiURL);
    }

    public static void main(String[] args) {
        System.out.println("Test start...");

//        testLoadClazzList();
//        testLoadClazzBook();
//        testLoadBookList();
        testTypelist();
//        testContent();
//        testPreviewBasicReview();
//        testAssignBasicReview();

        System.out.println("Test End...");
    }
}
