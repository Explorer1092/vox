/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.washington.controller.open.v1.student.test;

import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.http.client.execute.POST;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.DigestSignUtils;
import com.voxlearning.utopia.service.newhomework.api.mapper.OcrMentalImageDetail;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhangbin on 2016/10/24.
 */
public class TestStudentHomeworkApiController {
    private final static String appKey = "17Student";
    private final static String secretKey = "kuLwGZMJBcQj";
    private final static String sessionKey = "863d9b2e557ca84eaab4a86254f7af4a";

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

    private static void testDoNewHomework() {
        String url = "http://localhost:8081/v1/newhomework/do.api";
        paramMap.put("homework_id", "201807_5b56dc73ac74593e8941f321_1");
        paramMap.put("objective_config_type", "OCR_MENTAL_ARITHMETIC");
        paramMap.put("learning_type", "homework");
        paramMap.put("sid", "333905103");
        execute(url);
    }

    private static void testLoadNewHomeworkQuestions() {
        String url = "http://localhost:8081/v1/student/newhomework/dubbing/questions.api";
        paramMap.put("homework_id", "201712_5a26425daf81a312c8e850d1_1");
        paramMap.put("objective_config_type", "DUBBING");
        paramMap.put("dubbing_id", "D_10300001379952-2");
        paramMap.put("learning_type", "homework");
        execute(url);
    }

    private static void testLoadNewHomeworkQuestionsAnswer() {
        String url = "http://localhost:8081/v1/student/newhomework/dubbing/questions/answer.api";
        paramMap.put("homework_id", "201712_5a30c5cde92b1b3d3bad78e3_1");
        paramMap.put("objective_config_type", "DUBBING");
        paramMap.put("dubbing_id", "D_10300001742831-1");
        paramMap.put("learning_type", "homework");
        execute(url);
    }

    private static void testBatchProcessNewHomeworkResult() {
        String url = "http://localhost:8081/v1/newhomework/batch/processresult.api";
        Map<String, Object> homeworkResultData = new LinkedHashMap<>();
        homeworkResultData.put("homework_id", "201812_5c062d88ac74594bec48c6bc_2");
        homeworkResultData.put("objective_config_type", "OCR_MENTAL_ARITHMETIC");
        homeworkResultData.put("learning_type", "homework");
        homeworkResultData.put("consume_time", 35784);

        String json = "[{\"img_id\":\"\",\"number\":80,\"code\":0,\"img_width\":1080,\"message\":\"\",\"img_height\":1620,\"img_url\":\"https://cdn-live-image.17zuoye.cn/training/acf/20181208/e392e82e666042609f4b05b748fb72e0\",\"version\":\"17zuoye-acf V2.0\",\"forms\":[{\"box\":{\"top\":281,\"width\":132,\"left\":763,\"height\":45},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"13-5=8\"},{\"box\":{\"top\":317,\"width\":142,\"left\":59,\"height\":39},\"judge\":0,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"13-4=7\"},{\"box\":{\"top\":301,\"width\":140,\"left\":527,\"height\":39},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"11-3=8\"},{\"box\":{\"top\":313,\"width\":136,\"left\":295,\"height\":37},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"13-5=8\"},{\"box\":{\"top\":333,\"width\":138,\"left\":765,\"height\":49},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"14-5=9\"},{\"box\":{\"top\":375,\"width\":145,\"left\":58,\"height\":33},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"12-5=7\"},{\"box\":{\"top\":365,\"width\":140,\"left\":295,\"height\":44},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"12-3=9\"},{\"box\":{\"top\":357,\"width\":137,\"left\":529,\"height\":40},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"11-4=7\"},{\"box\":{\"top\":383,\"width\":150,\"left\":767,\"height\":52},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"11-2=9\"},{\"box\":{\"top\":401,\"width\":141,\"left\":529,\"height\":48},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"11-5=6\"},{\"box\":{\"top\":425,\"width\":135,\"left\":57,\"height\":46},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"12-3=9\"},{\"box\":{\"top\":419,\"width\":141,\"left\":294,\"height\":38},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"12-4=8\"},{\"box\":{\"top\":450,\"width\":145,\"left\":531,\"height\":54},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"11-2=9\"},{\"box\":{\"top\":474,\"width\":145,\"left\":57,\"height\":46},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"13-4=9\"},{\"box\":{\"top\":449,\"width\":138,\"left\":770,\"height\":45},\"judge\":0,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"12-3=8\"},{\"box\":{\"top\":473,\"width\":147,\"left\":294,\"height\":39},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"11-3=8\"},{\"box\":{\"top\":538,\"width\":145,\"left\":56,\"height\":38},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"11-4=7\"},{\"box\":{\"top\":504,\"width\":144,\"left\":773,\"height\":45},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"11-5=6\"},{\"box\":{\"top\":529,\"width\":147,\"left\":295,\"height\":42},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"11-2=9\"},{\"box\":{\"top\":520,\"width\":143,\"left\":533,\"height\":44},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"11-5=6\"},{\"box\":{\"top\":578,\"width\":148,\"left\":56,\"height\":52},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"13-4=9\"},{\"box\":{\"top\":554,\"width\":155,\"left\":775,\"height\":50},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"12-4=8\"},{\"box\":{\"top\":575,\"width\":139,\"left\":535,\"height\":46},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"14-5=9\"},{\"box\":{\"top\":588,\"width\":152,\"left\":295,\"height\":43},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"11-3=8\"},{\"box\":{\"top\":638,\"width\":142,\"left\":54,\"height\":48},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"14-5=9\"},{\"box\":{\"top\":629,\"width\":140,\"left\":536,\"height\":47},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"12-5=7\"},{\"box\":{\"top\":620,\"width\":153,\"left\":778,\"height\":47},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"11-4=7\"},{\"box\":{\"top\":645,\"width\":140,\"left\":296,\"height\":39},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"12-5=7\"},{\"box\":{\"top\":706,\"width\":143,\"left\":53,\"height\":42},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"12-4=8\"},{\"box\":{\"top\":699,\"width\":142,\"left\":296,\"height\":44},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"13-5=8\"},{\"box\":{\"top\":681,\"width\":178,\"left\":779,\"height\":42},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"9=(12)-3\"},{\"box\":{\"top\":694,\"width\":170,\"left\":537,\"height\":42},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"6=(11)-5\"},{\"box\":{\"top\":765,\"width\":171,\"left\":50,\"height\":39},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"8=(11)-3\"},{\"box\":{\"top\":759,\"width\":173,\"left\":295,\"height\":46},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"9=(14)-5\"},{\"box\":{\"top\":753,\"width\":168,\"left\":539,\"height\":38},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"8=(12)-4\"},{\"box\":{\"top\":742,\"width\":175,\"left\":784,\"height\":48},\"judge\":0,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"9=(4)-2\"},{\"box\":{\"top\":824,\"width\":168,\"left\":51,\"height\":41},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"9=(13)-4\"},{\"box\":{\"top\":818,\"width\":171,\"left\":296,\"height\":47},\"judge\":0,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"7=(4)-4\"},{\"box\":{\"top\":812,\"width\":167,\"left\":542,\"height\":37},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"7=(12)-5\"},{\"box\":{\"top\":801,\"width\":175,\"left\":786,\"height\":40},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"7=(12)-5\"},{\"box\":{\"top\":885,\"width\":166,\"left\":52,\"height\":38},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"7=(11)-4\"},{\"box\":{\"top\":877,\"width\":170,\"left\":297,\"height\":42},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"9=(13)-4\"},{\"box\":{\"top\":871,\"width\":171,\"left\":542,\"height\":39},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"8=(11)-3\"},{\"box\":{\"top\":861,\"width\":175,\"left\":789,\"height\":40},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"6=(11)-5\"},{\"box\":{\"top\":946,\"width\":166,\"left\":52,\"height\":37},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"8=(12)-4\"},{\"box\":{\"top\":937,\"width\":171,\"left\":298,\"height\":50},\"judge\":0,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"269+353=512\"},{\"box\":{\"top\":930,\"width\":173,\"left\":544,\"height\":44},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"9=(14)-5\"},{\"box\":{\"top\":921,\"width\":178,\"left\":791,\"height\":42},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"9=(11)-2\"},{\"box\":{\"top\":1004,\"width\":170,\"left\":50,\"height\":44},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"8=(13)-5\"},{\"box\":{\"top\":997,\"width\":176,\"left\":296,\"height\":46},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"8=(12)-4\"},{\"box\":{\"top\":990,\"width\":175,\"left\":545,\"height\":53},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"6=(11)-5\"},{\"box\":{\"top\":983,\"width\":178,\"left\":795,\"height\":46},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"6=(11)-5\"},{\"box\":{\"top\":1063,\"width\":171,\"left\":50,\"height\":51},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"8=(13)-5\"},{\"box\":{\"top\":1057,\"width\":178,\"left\":296,\"height\":53},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"9=(14)-5\"},{\"box\":{\"top\":1050,\"width\":176,\"left\":547,\"height\":52},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"(14)-5=9\"},{\"box\":{\"top\":1044,\"width\":181,\"left\":799,\"height\":45},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"(12)-3=9\"},{\"box\":{\"top\":1122,\"width\":171,\"left\":51,\"height\":47},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"(13)-4=9\"},{\"box\":{\"top\":1119,\"width\":176,\"left\":298,\"height\":43},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"(12)-5=7\"},{\"box\":{\"top\":1113,\"width\":176,\"left\":549,\"height\":43},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"(12)-5=7\"},{\"box\":{\"top\":1108,\"width\":183,\"left\":801,\"height\":42},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"(11)-4=7\"},{\"box\":{\"top\":1186,\"width\":173,\"left\":50,\"height\":51},\"judge\":0,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"(6)-4=7\"},{\"box\":{\"top\":1181,\"width\":178,\"left\":298,\"height\":45},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"(11)-3=8\"},{\"box\":{\"top\":1175,\"width\":176,\"left\":552,\"height\":43},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"(11)-3=8\"},{\"box\":{\"top\":1170,\"width\":186,\"left\":804,\"height\":43},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"(12)-4=8\"},{\"box\":{\"top\":1252,\"width\":174,\"left\":49,\"height\":43},\"judge\":0,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"(12)-5=8\"},{\"box\":{\"top\":1245,\"width\":179,\"left\":299,\"height\":44},\"judge\":0,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"(17)-5=8\"},{\"box\":{\"top\":1239,\"width\":178,\"left\":553,\"height\":42},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"(11)-5=6\"},{\"box\":{\"top\":1236,\"width\":186,\"left\":808,\"height\":42},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"(11)-5=6\"},{\"box\":{\"top\":1312,\"width\":173,\"left\":50,\"height\":51},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"(13)-4=9\"},{\"box\":{\"top\":1310,\"width\":179,\"left\":300,\"height\":43},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"(12)-3=9\"},{\"box\":{\"top\":1303,\"width\":192,\"left\":555,\"height\":52},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"11-(4)=7\"},{\"box\":{\"top\":1299,\"width\":200,\"left\":812,\"height\":52},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"11-(3)=8\"},{\"box\":{\"top\":1375,\"width\":187,\"left\":50,\"height\":48},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"13-(5)=8\"},{\"box\":{\"top\":1375,\"width\":191,\"left\":300,\"height\":42},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"11-(2)=9\"},{\"box\":{\"top\":1368,\"width\":192,\"left\":557,\"height\":45},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"12-(5)=7\"},{\"box\":{\"top\":1366,\"width\":198,\"left\":816,\"height\":44},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"11-(2)=9\"},{\"box\":{\"top\":1444,\"width\":187,\"left\":49,\"height\":50},\"judge\":0,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"11-(7)=8\"},{\"box\":{\"top\":1437,\"width\":192,\"left\":300,\"height\":45},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"12-(5)=7\"},{\"box\":{\"top\":1433,\"width\":192,\"left\":559,\"height\":63},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"12-(3)=9\"},{\"box\":{\"top\":1430,\"width\":199,\"left\":820,\"height\":50},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"11-(5)=6\"}]},{\"img_id\":\"\",\"number\":20,\"code\":0,\"img_width\":1080,\"message\":\"\",\"img_height\":1620,\"img_url\":\"https://cdn-live-image.17zuoye.cn/training/acf/20181208/b36c541368d0459a9f66b907c1162b7e\",\"version\":\"17zuoye-acf V2.0\",\"forms\":[{\"box\":{\"top\":250,\"width\":199,\"left\":50,\"height\":45},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"13-(5)=8\"},{\"box\":{\"top\":218,\"width\":202,\"left\":827,\"height\":62},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"14-(5)=9\"},{\"box\":{\"top\":262,\"width\":191,\"left\":315,\"height\":49},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"12-(3)=9\"},{\"box\":{\"top\":260,\"width\":191,\"left\":569,\"height\":44},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"11-(5)=6\"},{\"box\":{\"top\":312,\"width\":200,\"left\":48,\"height\":53},\"judge\":0,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"12-(3)=7\"},{\"box\":{\"top\":278,\"width\":189,\"left\":827,\"height\":66},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"3+9-3=9\"},{\"box\":{\"top\":322,\"width\":195,\"left\":312,\"height\":50},\"judge\":0,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"12-(3)=8\"},{\"box\":{\"top\":312,\"width\":191,\"left\":569,\"height\":48},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"4+8-5=7\"},{\"box\":{\"top\":371,\"width\":202,\"left\":45,\"height\":48},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"5+8-5=8\"},{\"box\":{\"top\":346,\"width\":191,\"left\":829,\"height\":64},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"5+9-5=9\"},{\"box\":{\"top\":382,\"width\":183,\"left\":312,\"height\":43},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"8+3-5=6\"},{\"box\":{\"top\":382,\"width\":183,\"left\":568,\"height\":42},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"4+9-4=9\"},{\"box\":{\"top\":433,\"width\":206,\"left\":43,\"height\":47},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"2+9-3=8\"},{\"box\":{\"top\":442,\"width\":204,\"left\":311,\"height\":41},\"judge\":0,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"4+7-5=10\"},{\"box\":{\"top\":417,\"width\":201,\"left\":828,\"height\":56},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"18-5-4=9\"},{\"box\":{\"top\":440,\"width\":202,\"left\":569,\"height\":46},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"11-4-3=4\"},{\"box\":{\"top\":495,\"width\":217,\"left\":42,\"height\":50},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"12-3-5=4\"},{\"box\":{\"top\":502,\"width\":202,\"left\":310,\"height\":55},\"judge\":0,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"13-4-2=3\"},{\"box\":{\"top\":482,\"width\":204,\"left\":830,\"height\":51},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"11-4-2=5\"},{\"box\":{\"top\":504,\"width\":206,\"left\":570,\"height\":40},\"judge\":1,\"usertext\":\"\",\"keypoint\":\"\",\"text\":\"14-5-7=2\"}]}]";
        List<OcrMentalImageDetail> imageDetails = JsonUtils.fromJsonToList(json, OcrMentalImageDetail.class);
        homeworkResultData.put("ocr_mental_image_details", imageDetails);


//        homeworkResultData.put("homework_id", "201808_5b85190c77748773ad992266_2");
//        homeworkResultData.put("objective_config_type", "DUBBING_WITH_SCORE");
//        homeworkResultData.put("learning_type", "homework");
//        homeworkResultData.put("consume_time", 15928);
//        homeworkResultData.put("dubbing_id", "D_10300002917868-1");
//        homeworkResultData.put( "video_url", "https://17zy-homework.oss-cn-beijing.aliyuncs.com/dubbing/test/D_10300003002628-1_1535447830451.mp4");
//        String json = "[\n" +
//                "    {\n" +
//                "        \"question_id\": \"Q_10312908509804-1\",\n" +
//                "        \"duration_milliseconds\": 7964,\n" +
//                "        \"voiceEngineType\": \"Vox17\",\n" +
//                "        \"voiceCoefficient\": 8,\n" +
//                "        \"voiceMode\": 12,\n" +
//                "        \"voiceScoringMode\": \"Normal\",\n" +
//                "        \"sentenceType\": 0,\n" +
//                "        \"file_urls\": [\n" +
//                "            []\n" +
//                "        ],\n" +
//                "        \"oralScoreDetails\": [\n" +
//                "            [\n" +
//                "                {\n" +
//                "                    \"audio\": \"https://cdn-gomark.17zuoye.cn/bj/0001535335014218671/cef731250a8c41239ba610565a2e3d9a\",\n" +
//                "                    \"macScore\": 6.66,\n" +
//                "                    \"standardScore\": 0,\n" +
//                "                    \"businessLevel\": 1\n" +
//                "                }\n" +
//                "            ]\n" +
//                "        ]\n" +
//                "    },\n" +
//                "    {\n" +
//                "        \"question_id\": \"Q_10312908510238-1\",\n" +
//                "        \"duration_milliseconds\": 7964,\n" +
//                "        \"voiceEngineType\": \"Vox17\",\n" +
//                "        \"voiceCoefficient\": 8,\n" +
//                "        \"voiceMode\": 12,\n" +
//                "        \"voiceScoringMode\": \"Normal\",\n" +
//                "        \"sentenceType\": 0,\n" +
//                "        \"file_urls\": [\n" +
//                "            []\n" +
//                "        ],\n" +
//                "        \"oralScoreDetails\": [\n" +
//                "            [\n" +
//                "                {\n" +
//                "                    \"audio\": \"https://cdn-gomark.17zuoye.cn/bj/0001535335014218671/cef731250a8c41239ba610565a2e3d9a\",\n" +
//                "                    \"macScore\": 6.66,\n" +
//                "                    \"standardScore\": 0,\n" +
//                "                    \"businessLevel\": 1\n" +
//                "                }\n" +
//                "            ]\n" +
//                "        ]\n" +
//                "    }\n" +
//                "]";
//        List<StudentHomeworkAnswer> dubbingList = JsonUtils.fromJsonToList(json, StudentHomeworkAnswer.class);
//        homeworkResultData.put("student_homework_answers", dubbingList);
        paramMap.put("homework_result_data", JsonUtils.toJson(homeworkResultData));
        paramMap.put("sid", "333894050");
        execute(url);
    }

    private static void testDoDubbingNewHomework() {
        String url = "http://localhost:8081/v1/newhomework/do.vpage";
        paramMap.put("homework_id", "201808_5b84c8cae92b1b69e42f83a9_2");
        paramMap.put("objective_config_type", "DUBBING");
        execute(url);
    }

    public static void main(String args[]) {
        try {
//            testDoNewHomework();
//            testLoadNewHomeworkQuestions();
//            testLoadNewHomeworkQuestionsAnswer();
            testBatchProcessNewHomeworkResult();
//            testDoDubbingNewHomework();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
