package com.voxlearning.washington.controller.open.v1.teacher.test;

import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.http.client.execute.POST;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.DigestSignUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author guoqiang.li
 * @since 2016/4/14
 */
public class TestTeacherNewHomeworkApiController {
    private final static String appKey = "17Teacher";
    private final static String secretKey = "gvUKQN1EFXKp";
    //    private final static String sessionKey = "6990af76e20742cdd4adf87d60032084";
//    private final static String sessionKey = "d2a99f64ac8fc7892cb906f6abaa30ed";
//    private final static String sessionKey = "3c6f584122eda6a813e00a56c0e0b791";
//    private final static String sessionKey = "53d0bb7b616f14f4ab19a230672f77f7"; // 学前 12975739
//    private final static String sessionKey = "f51f047fdf9ab2c41c564911504e59d1"; // 学前 12975755
    private final static String sessionKey = "6990af76e20742cdd4adf87d60032084"; // 126287
//    private final static String sessionKey = "aad05bd4c9eb2abb6345f60b922da1ad";
//    private final static String sessionKey = "de5444ed6979135b49092f5c25e3bfce"; // 102583

//    private final static String sessionKey = "531dd1acc93b0ed70c8f8f21d804e9dc";

    private static Map<String, String> paramMap = new HashMap<>();

    static {
        paramMap.put("app_key", appKey);
        paramMap.put("session_key", sessionKey);
    }

    private static void execute(String apiURL) {
        String sig = DigestSignUtils.signMd5(paramMap, secretKey);
        paramMap.put("sig", sig);
        paramMap.put("ver", "1.8.1.1451");
        POST post = HttpRequestExecutor.defaultInstance().post(apiURL);
        paramMap.forEach(post::addParameter);
        System.out.println(post.execute().getResponseString());
    }

    private static void testLoadTeacherClazzList() {
        String url = "http://localhost:8081/v1/teacher/new/homework/clazzlist.api";
        execute(url);
    }

    private static void testLoadClazzBook() {
        String url = "http://localhost:8081/v1/teacher/new/homework/clazzbook.api";
        paramMap.put("clazzid_groupid_list", "40088012_81834");
        execute(url);
    }

    private static void testLoadBookList() {
        String url = "http://localhost:8081/v1/teacher/new/homework/booklist.api";
        paramMap.put("clazz_level", "54");
        paramMap.put("book_term", "1");
        execute(url);
    }

    private static void testLoadUnitList() {
        String url = "http://localhost:8081/v1/teacher/new/homework/unitlist.api";
        paramMap.put("book_id", "BK_10200001566128");
        execute(url);
    }

    private static void testChangeBook() {
        String url = "http://localhost:8081/v1/teacher/new/homework/changebook.api";
        paramMap.put("book_id", "BK_50300002719463");
        paramMap.put("clazz_ids", "40084969");
        execute(url);
    }

    private static void testLoadContentTypeList() {
        String url = "http://localhost:8081/v1/teacher/new/homework/typelist.api";
        paramMap.put("book_id", "BK_50300002719463");
        paramMap.put("unit_id", "BKC_50300146645877");
        paramMap.put("section_ids", "");
        execute(url);
    }

    private static void testLoadUncheckedHomeworkList() {
        String url = "http://api.dragon.17zuoye.net/v1/teacher/new/report/uncheckedhomeworklist.api";
        execute(url);
    }

    private static void testLoadHomeworkList() {
        String url = "http://localhost:8081/v1/teacher/new/report/homeworklist.api";
        paramMap.put("page_number", "1");
        paramMap.put("homework_status", "Checked");
        paramMap.put("clazz_group_ids", "21573,9302,19571,7469,7863");
        execute(url);
    }

    private static void testFeedBack() {
        String url = "http://localhost:8081/v1/teacher/new/homework/examfeedback.api";
        paramMap.put("content", "测试内容");
        paramMap.put("type", "测试类型");
        paramMap.put("question_id", "Q_10200786534654-2");
        execute(url);
    }

    private static void testHomeworkfinishinfo() {
        String url = "http://localhost:8081/v1/teacher/new/report/homeworkfinishinfo.api";
        //paramMap.put("homework_id","201610_5817195ae92b1b60f00d1fcc");
        paramMap.put("homework_id", "201611_58257c4b777487744670afd0");
        execute(url);
    }

    private static void testWriteCommentAddIntegral() {
        String url = "http://www.test.17zuoye.net/v1/teacher/new/report/writecommentaddintegral.api";
        paramMap.put("homework_id", "201710_59dcc9c2ac7459254261f00c_1");
        paramMap.put("student_list", "");
        paramMap.put("comment", "");
        paramMap.put("audio_comment", "");
        paramMap.put("integral_user_json", "[{ \"count\": 3, \"studentId\": \"333803068\"}]");
        execute(url);
    }

    private static void testLoadUnitProgress() {
        String url = "http://localhost:8081/v1/teacher/new/homework/unitprogress.api";
        paramMap.put("clazzid_groupid_list", "37245_19589,38247_3217");
        paramMap.put("book_id", "BK_10200001566128");
        paramMap.put("unit_id", "BKC_10200085894281");
        execute(url);
    }

    private static void testLoadIntelligenceQuestion() {
        String url = "http://localhost:8081/v1/teacher/new/report/urgenewhomework.api";
//        Map<String, String> jsonMap = new LinkedHashMap<>();
//        jsonMap.put("clazzid_groupid_list", "33946485_80266");
//        jsonMap.put("book_id", "BK_10300000266810");
//        jsonMap.put("unit_id", "BKC_10300009621682");
//        jsonMap.put("section_ids", "");
//        jsonMap.put("algo_type", "ENGLISH_VOCABULARY");
//        jsonMap.put("difficulty", "3");
//        jsonMap.put("question_count", "10");
//        jsonMap.put("kp_ids", "KP_10300035286823,KP_10300035015710,KP_10300034705640,KP_10300033791487,KP_10300033523045,KP_10300033261391,KP_10300033024206,KP_10300032177532,KP_10300031947200");
//        jsonMap.put("content_type_ids", "1031001,1031002,1031003,1031004,1031006,1031008,1031009,1031013,1031014,1031015,1031016,1031011,1031005,1031012,1031017,1031018");
//        paramMap.put("intelligence_data", JsonUtils.toJson(jsonMap));
//        paramMap.put("subject", "ENGLISH");
//
        paramMap.put("homework_id", "201709_59b28723ac7459573ec27cad_1");
        paramMap.put("is_correct", "true");
        paramMap.put("student_list", "333803068");

        execute(url);
    }

    private static void testLoadMaxIntegralCount() {
        String url = "http://api.dragon.17zuoye.net/v1/teacher/new/homework/maxic.api";
        paramMap.put("clazz_ids", "37245");
        execute(url);
    }

    private static void testLoadObjectiveList() {
        String url = "http://api.test.17zuoye.net/v1/teacher/new/homework/objective/list.api";
        paramMap.put("book_id", "BK_10300000266810");
        paramMap.put("unit_id", "BKC_10300009628186");
        paramMap.put("section_ids", "");
        execute(url);
    }

    private static void testCopyHomework() {
        String url = "http://localhost:8081/v1/teacher/new/homework/copy.api";
        Map<String, String> jsonMap = new LinkedHashMap<>();
        jsonMap.put("homeworkId", "201808_5b6162e79e2c8d2d3c0486ed_1");
        jsonMap.put("groupIds", "82179");
        jsonMap.put("endTime", "2018-08-01 23:59:00");
        paramMap.put("homework_data", JsonUtils.toJson(jsonMap));
        paramMap.put("subject", "MATH");
        execute(url);
    }

    private static void testFetchtypequestion() {
        String url = "http://localhost:8081/v1/teacher/new/report/fetchtypequestion.api";
        paramMap.put("homework_id", "201711_59fac273af81a320b096b7e4_1");
        execute(url);
    }

    private static void testfetchtypestudent() {
        String url = "http://localhost:8081/v1/teacher/new/report/fetchtypestudent.api";
        paramMap.put("homework_id", "201711_59f99076af81a32ca4720fbf_1");
        execute(url);
    }


    private static void testLoadSameLevelClazzList() {
        String url = "http://localhost:8081/v1/teacher/new/homework/copy/clazzlist.api";
        paramMap.put("homework_id", "201709_59b75b607774878a1ac96b90_1");
        execute(url);
    }


    private static void testhomeworkfinishinfo() {
        String url = "http://localhost:8081/v1/teacher/new/report/homeworkfinishinfo.api";
        paramMap.put("homework_id", "201710_59dcc9c2ac7459254261f00c_1");
        execute(url);
    }


    private static void testurgeNewHomework() {
        String url = "http://www.test.17zuoye.net/v1/teacher/new/report/urgenewhomework.api";

        paramMap.put("homework_id", "201709_59b75b607774878a1ac96b90_1");
        paramMap.put("student_list", "333905103");
        paramMap.put("is_correct", "false");
        execute(url);
    }

    private static void testclazzList() {
        String url = "http://localhost:8081/v1/teacher/newexam/report/newclazzlist.api";
        execute(url);
    }

    private static void testLoadObjectiveContent() {
        String url = "http://api.test.17zuoye.net/v1/teacher/new/homework/objective/content.api";
        Map<String, String> jsonMap = new HashMap<>();
        jsonMap.put("clazzid_groupid_list", "1001572_100776");
        jsonMap.put("book_id", "BK_10300000267732");
        jsonMap.put("unit_id", "BKC_10300009737986");
        jsonMap.put("section_ids", "");
        jsonMap.put("type", "ORAL_COMMUNICATION");
        jsonMap.put("objective_config_id", "OCN_02866545286");
        paramMap.put("content_data", JsonUtils.toJson(jsonMap));
        paramMap.put("subject", "ENGLISH");
        execute(url);
    }

    private static void testLoadDubbingAlbumList() {
        String url = "http://localhost:8081/v1/teacher/new/homework/dubbing/albumlist.api";
        paramMap.put("clazz_level", "6");
        execute(url);
    }

    private static void testLoadDubbingRecommendSearchWords() {
        String url = "http://localhost:8081/v1/teacher/new/homework/dubbing/recommendsearchwords.api";
        execute(url);
    }

    private static void testSearchDubbing() {
        String url = "http://localhost:8081/v1/teacher/new/homework/dubbing/search.api";
        Map<String, String> jsonMap = new HashMap<>();
        jsonMap.put("search_word", "");
        jsonMap.put("clazz_level", "1");
        jsonMap.put("book_id", "BK_10300000266810");
        jsonMap.put("unit_id", "BKC_10300009618602");
        paramMap.put("search_data", JsonUtils.toJson(jsonMap));
        paramMap.put("subject", "ENGLISH");
        execute(url);
    }

    private static void testLoadPictureBookPlusTopicList() {
        String url = "http://api.test.17zuoye.net/v1/teacher/new/homework/picturebookplus/topiclist.api";
        execute(url);
    }

    private static void testLoadPictureBookPlusSeriesList() {
        String url = "http://api.test.17zuoye.net/v1/teacher/new/homework/picturebookplus/serieslist.api";
        execute(url);
    }

    private static void testLoadPictureBookPlusRecommendSearchWords() {
        String url = "http://api.test.17zuoye.net/v1/teacher/new/homework/picturebookplus/recommendsearchwords.api";
        execute(url);
    }

    private static void testSearchPictureBookPlus() {
        String url = "https://api.test.17zuoye.net/v1/teacher/new/homework/picturebookplus/search.api";
        Map<String, String> jsonMap = new HashMap<>();
//        jsonMap.put("book_id", "BK_10300000266810");
//        jsonMap.put("unit_id", "BKC_10300009618602");
        jsonMap.put("search_word", "");
        paramMap.put("search_data", JsonUtils.toJson(jsonMap));
        paramMap.put("subject", "ENGLISH");
        execute(url);
    }

    private static void testLoadPictureBookPlusHistory() {
        String url = "http://localhost:8081/v1/teacher/new/homework/picturebookplus/history.api";
        Map<String, String> jsonMap = new HashMap<>();
//        jsonMap.put("book_id", "BK_10300000266810");
//        jsonMap.put("unit_id", "BKC_10300009618602");
        paramMap.put("history_data", JsonUtils.toJson(jsonMap));
        paramMap.put("subject", "ENGLISH");
        execute(url);
    }

    private static void testLoadIndexContent() {
        String url = "http://localhost:8081/v1/teacher/new/homework/index/content.api";
        paramMap.put("subject", "CHINESE");
        execute(url);
    }

    private static void testLoadClazzListForRecommend() {
        String url = "http://localhost:8081/v1/teacher/new/homework/recommend/clazzlist.api";
        paramMap.put("subject", "ENGLISH");
        execute(url);
    }

    private static void testDubbingDetail() {
        String url = "http://localhost:8081/v1/teacher/new/homework/dubbing/detail.vpage";
        paramMap.put("book_id", "BK_10300000398224");
        paramMap.put("dubbing_id", "D_10300002766947-3");
        paramMap.put("unit_id", "BKC_10300018666849");
        execute(url);
    }

    private static void testloadDubbingCollectionRecord() {
        String url = "http://localhost:8081/v1/teacher/new/homework/dubbing/collection/record.vpage";
        Map<String, String> jsonMap = new HashMap<>();
        jsonMap.put("book_id", "BK_10300000265057");
        jsonMap.put("unit_id", "BKC_10300009510931");
        paramMap.put("collection_data", JsonUtils.toJson(jsonMap));
        execute(url);
    }

    private static void testLoadOralCommunicationRecommendSearchWords() {
        String url = "http://localhost:8081/v1/teacher/new/homework/oralcommunication/recommendsearchwords.api";
        execute(url);
    }

    private static void testSearchOralCommunication() {
        String url = "http://localhost:8081/v1/teacher/new/homework/oralcommunication/search.api";
        Map<String, String> jsonMap = new HashMap<>();
        jsonMap.put("clazz_level", "1");
        jsonMap.put("book_id", "BK_10300000266810");
        jsonMap.put("unit_id", "BKC_10300009618602");
//        jsonMap.put("search_word", "测试");
        paramMap.put("search_data", JsonUtils.toJson(jsonMap));
        execute(url);
    }

    private static void testOralCommunicationDetail() {
        String url = "http://localhost:8081/v1/teacher/new/homework/oralcommunication/detail.vpage";
        paramMap.put("book_id", "BK_10300000398224");
        paramMap.put("oral_communication_id", "D_10300002766947-3");
        paramMap.put("unit_id", "BKC_10300018666849");
        execute(url);
    }


    public static void main(String[] args) {
        try {
//            testloadDubbingCollectionRecord();
            //testDubbingDetail();
//            testLoadTeacherClazzList();
//            testLoadClazzBook();
//            testLoadBookList();
//            testLoadUnitList();
//            testChangeBook();
//            testLoadContentTypeList();
//            testLoadUncheckedHomeworkList();
//            testFeedBack();
//            testHomeworkfinishinfo();
//            testLoadUnitProgress();
//            testLoadIntelligenceQuestion();
//            testLoadMaxIntegralCount();
//            testLoadObjectiveList();
//            testCopyHomework();
//            testurgeNewHomework();
//            testfetchtypestudent();
//            testFetchtypequestion();
//            testLoadSameLevelClazzList();
//            testhomeworkfinishinfo();
//            testclazzList();
//            testWriteCommentAddIntegral();
//			  testLoadHomeworkList();
//            testhomeworkfinishinfo();
//            testLoadObjectiveContent();
//            testLoadDubbingAlbumList();
//            testLoadDubbingRecommendSearchWords();
//            testSearchDubbing();
//            testLoadPictureBookPlusTopicList();
//            testLoadPictureBookPlusSeriesList();
//            testLoadPictureBookPlusRecommendSearchWords();
//            testSearchPictureBookPlus();
//            testLoadPictureBookPlusHistory();
//            testLoadIndexContent();
//            testLoadClazzListForRecommend();
            testLoadOralCommunicationRecommendSearchWords();
//            testSearchOralCommunication();
//            testOralCommunicationDetail();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
