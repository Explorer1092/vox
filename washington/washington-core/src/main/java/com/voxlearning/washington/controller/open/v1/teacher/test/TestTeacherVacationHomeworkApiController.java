package com.voxlearning.washington.controller.open.v1.teacher.test;

import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.http.client.execute.POST;
import com.voxlearning.alps.lang.util.DigestSignUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tanguohong on 2016/12/2.
 */
public class TestTeacherVacationHomeworkApiController {
    private final static String appKey = "17Teacher";
    private final static String secretKey = "gvUKQN1EFXKp";
    //    private final static String sessionKey = "d2a99f64ac8fc7892cb906f6abaa30ed";    // 126504
//    private final static String sessionKey = "cf1e4811bf856a0837c4f6fadd70ce01";    // 125075
//    private final static String sessionKey = "4dd142ee89fd69fccf773b78226d003c";    // 123120
    private final static String sessionKey = "6990af76e20742cdd4adf87d60032084"; // 126287

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

    private static void testLoadClazzList() {
        String apiURL = "http://localhost:8081/v1/teacher/vacation/index.api";
        execute(apiURL);
    }

    private static void testLoadBookPlanInfo() {
        String apiURL = "http://api.test.17zuoye.net/v1/teacher/vacation/book/planinfo.api";
        paramMap.put("book_id", "BK_10300000419955");
        execute(apiURL);
    }

    private static void testAssign() {
        String apiURL = "http://localhost:8081/v1/teacher/vacation/assign.api";
        paramMap.put("homework_data", "{\"startTime\":1498838459000,\"endTime\":1503158400000,\"clazzBookMap\":{\"33946485_31628\":{\"bookId\":\"BK_10200001572455\",\"subject\":\"MATH\"},\"33946486_79775\":{\"bookId\":\"BK_10200001572455\",\"subject\":\"MATH\"}}}");
        execute(apiURL);
    }

    private static void testDelete() {
        String apiURL = "http://localhost:8081/v1/teacher/vacation/delete.api";
        paramMap.put("homework_id", "5849820406d06a64ee2dcd82");
        execute(apiURL);
    }

    private static void testReport() {
        String apiURL = "http://localhost:8081/v1/teacher/vacation/report.api";
        paramMap.put("package_id", "592549f1af81a321387ae17a");
        execute(apiURL);
    }

    private static void testLoadCommentTemplates() {
        String apiURL = "http://api.test.17zuoye.net/v1/teacher/vacation/loadcommenttemplates.api";
        paramMap.put("homework_id", "5a1fc2268edbc85180db37ae-1-1-333905103");
        execute(apiURL);
    }

    private static void testWriteCommentAddIntegral() {
        String apiURL = "http://localhost:8081/v1/teacher/vacation/writecommentaddintegral.api";
        paramMap.put("homework_id", "5a309ecaac74599878ef3166-1-1-333917100");
        paramMap.put("comment", "");
        paramMap.put("audio_comment", "https://oss-data.17zuoye.com/app/test2017/12/18/20171218122142618831.mp3");
        paramMap.put("integral", "0");
        execute(apiURL);
    }

    public static void main(String[] args) {
        System.out.println("Test start...");
//        testAssign();
//        testDelete();
//        testLoadClazzList();
//        testLoadBookPlanInfo();
//        testReport();
//        testLoadCommentTemplates();
        testWriteCommentAddIntegral();
        System.out.println("Test End...");
    }
}
