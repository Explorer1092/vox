/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.washington.controller.open.v1.test;

import com.voxlearning.alps.core.event.EventBusBoss;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.http.client.execute.POST;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.DigestSignUtils;
import com.voxlearning.alps.web.UrlUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author shiwe.liao
 * @since 2015/12/30
 */
public class TestAppMessageApiLoadUserMessage {
    public static final String PARENT_APP_KEY = "17Parent";
    public static final String PARENT_SECRET_KEY = "iMMrxI3XMQtd";
    public static final String STUDENT_APP_KEY = "17Student";
    public static final String STUDENT_SECRET_KEY = "kuLwGZMJBcQj";
    public static final String TACHER_APP_KEY = "17Teacher";
    public static final String TEACHER_SECRET_KEY = "gvUKQN1EFXKp";



    private static String teacherSmartClazzDetailApi = "/v1/teacher/smartclazz/clazzdetail.vpage";
    private static String teacherSmartExchangeApi = "/v1/teacher/smartclazz/exchange_integral.vpage";
    private static String testStudentOverTimePushApi = "/v1/student/notifyovertime.vpage";

    public static void main(String[] args) {


//            System.out.println(-1/86400000);
//            System.out.println(86400001/86400000);
//        System.out.println(DateUtils.dateToString(new Date(), "M月dd日"));/
//        autoFollowOfficialAccount();
        testSendPopupMessage();
//        List<String> list = new ArrayList<>();
//        list.add("1111");
//        list.add("2222");
//        System.out.println(StringUtils.join(list,","));
//        Long userId = 333905674L;
//        String appKey = STUDENT_APP_KEY;
//        String version = null;
//
//        Map<String, String> paramMap = new HashMap<>();
//        paramMap.put("notify_type", "OVER_CONTINUE");
////        paramMap.put("integral_count", "5");
//
//        String host = "http://localhost:8081/";
////        String host = "https://api.test.17zuoye.net/";
////        String host = "https://api.staging.17zuoye.net/";
//
//        String s = testRequestApi(appKey, host , testStudentOverTimePushApi, userId , paramMap, version);
    }


    private static String testRequestApi(String appKey, String host, String apiUrl, Long userId, Map<String, String> params, String version){

        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("appKey", appKey);
        paramMap.put("apiUrl", apiUrl);
        paramMap.put("uid", userId.toString());
        paramMap.put("params", JsonUtils.toJson(params));
        if (StringUtils.isNotBlank(version))
            paramMap.put("version", version);

        String url = UrlUtils.buildUrlQuery(host + "/devtest/mock_api_test.vpage", paramMap);
        EventBusBoss boss = EventBusBoss.getInstance();
        boss.initialize();
        boss.subscribeInstalledListeners();
        boss.subscribeConfiguredListeners();
        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().post(url).execute();
        String responseString = response.getResponseString();
        System.out.println(responseString);
        System.out.println("Test End...");
        return responseString;
    }


    private static void testSendPopupMessage() {
        try {
            System.out.println("Test Start...");

            final String appKey = PARENT_APP_KEY;
            final String secretKey = PARENT_SECRET_KEY;
            final String sessionKey = "a0da0a80fcb38e814413bcf158d9770b";
//
            Map<String, Object> extInfo = new HashMap<>();
            extInfo.put("s", 1001);
            extInfo.put("t", "msg_list");
            extInfo.put("k", "m");
            extInfo.put("timestamp", 1451547004);
//            List<Long> userIdList = new ArrayList<>();
//            userIdList.add(333875333L);
            Map<String, String> paramMap = new HashMap<>();

            paramMap.put("session_key", sessionKey);
//            paramMap.put("page","0");
//            String ds = "2015-12-30";
//            String de = "2016-06-30";
            paramMap.put("app_key", appKey);
//            paramMap.put("ease_mob_group_id", "192632304399548864");
            paramMap.put("sid","333879088");
            paramMap.put("type_name","");
            paramMap.put("create_time","");
//            paramMap.put("time","");
//            paramMap.put("search_key_word","语文同步课堂");
//            paramMap.put("search_content_type","-1");
//            paramMap.put("search_source","2");
//            paramMap.put("search_json","{\"unit_id\":\"BKC_10300077829335\",\"section_id\":\"\"}");
//            paramMap.put("max_show_time","0");
//            paramMap.put("had_local","false");
//            paramMap.put("teacher_id","129108");
//            paramMap.put("clazz_id","33768385");
//                        paramMap.put("region_code","110101");
//            paramMap.put("time","");
//            paramMap.put("notice_id","5717901eddb86529282385b8-124578");
//            paramMap.put("group_ids", JsonUtils.toJson(Collections.singletonList(4732)));
//            paramMap.put("content", "测试通知content");
////            paramMap.put("img_url_list", JsonUtils.toJson(Arrays.asList("avatar-124578-55b0b399a475cb2988375387.jpg","avatar-124578-55b0b399a475cb2988375387.jpg")));
//            paramMap.put("voice_url", "avatar-124578-55b0b399a475cb2988375387.jpg");
//            paramMap.put("end_time", "2016-04-30 12:00:00");
//            paramMap.put("auto_remind", "true");
//            paramMap.put("need_feedback",  "true");
//            paramMap.put("user_share_id", "5714e85cddb8651d00884dee");
//            paramMap.put("sid","333875333");
//            paramMap.put("user_share_content","测试content");
//            paramMap.put("user_share_img","avatar-124578-55b0b399a475cb2988375387.jpg");


//            paramMap.put("create_time","");
//            paramMap.put(REQ_APP_MESSAGE_TYPE,"1002");
//            paramMap.put(REQ_APP_MESSAGE_SOURCE,"XUESHE");
//            paramMap.put(REQ_APP_MESSAGE_UID, "[333893226]");
//            paramMap.put(REQ_APP_MESSAGE_TITLE,"标题");
//            paramMap.put(REQ_APP_MESSAGE_CONTENT,"正文内容");
//            paramMap.put(REQ_APP_MESSAGE_IMGURL,"");
//            paramMap.put(REQ_APP_MESSAGE_LINKURL,"");
//            paramMap.put(REQ_APP_MESSAGE_LINKTYPE,"0");
//            paramMap.put(REQ_APP_MESSAGE_PUSHTAG,"");
//            paramMap.put(REQ_APP_MESSAGE_EXTINFO, JsonUtils.toJson(extInfo));
//
//
//            paramMap.put(REQ_APP_MESSAGE_BTN_CONTENT,"立即参加");
//            paramMap.put(REQ_APP_MESSAGE_START, SafeConverter.toString(DateUtils.stringToDate(ds, DateUtils.FORMAT_SQL_DATE).getTime()));
//            paramMap.put(REQ_APP_MESSAGE_END, SafeConverter.toString(DateUtils.stringToDate(de, DateUtils.FORMAT_SQL_DATE).getTime()));
//            paramMap.put(REQ_APP_MESSAGE_RANK,"1");


            // 计算SIG
            String sig = DigestSignUtils.signMd5(paramMap, secretKey);
            paramMap.put("sig", sig);
            paramMap.put("ver","2.2.5.1.1001");
            String apiURL = "https://api.test.17zuoye.net/v2/parent/group_message/list.vpage";
//            String apiURL = "http://10.200.6.37:8081/v2/parent/jxt/student_progress.vpage";
//            String apiURL = "http://10.200.6.71:8081/v2/parent/jxtnews/search_news_v1.vpage";
//            String apiURL = "http://api.17zuoye.com/v1/parent//getUserInfo.vpage";
//            String apiURL = "http://api.dragon.17zuoye.net/v1/parent/jxt/easemob_list.vpage";
//            String apiURL = "http://localhost:8081/v1/parent/school_clazz/get_school_by_region.vpage";

//            String apiURL = "http://api.test.17zuoye.net/v1/parent/jxt/ext_tab.vpage";
            String url = UrlUtils.buildUrlQuery(apiURL, paramMap);
            EventBusBoss boss = EventBusBoss.getInstance();
            boss.initialize();
            boss.subscribeInstalledListeners();
            boss.subscribeConfiguredListeners();
            AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().post(url).execute();
            System.out.println(response.getResponseString());
            System.out.println("Test End...");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //修复资讯点赞数
    private static void fixVoteCount() {
        System.out.println("Fix Start...");
        final String appKey = PARENT_APP_KEY;
        final String secretKey = PARENT_SECRET_KEY;
        final String sessionKey = "03164d2ccfe2db78c267c989222b2d3e";

        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("session_key", sessionKey);
        paramMap.put("app_key", appKey);

        // 计算SIG
        String sig = DigestSignUtils.signMd5(paramMap, secretKey);
        paramMap.put("sig", sig);

        String apiURL = "http://localhost:8081/userMobile/jxt/fixvotecount.vpage";
        POST post = HttpRequestExecutor.defaultInstance().post(apiURL);
        paramMap.entrySet().stream().forEach(e -> {
            String name = e.getKey();
            String value = e.getValue();
            post.addParameter(name, value);
        });

        System.out.println(post.execute().getResponseString());
        System.out.println("Fix End...");
    }

    // 自动关注点赞
    private static void autoFollowOfficialAccount() {
        System.out.println("Fix Start...");
        final String appKey = PARENT_APP_KEY;
        final String secretKey = PARENT_SECRET_KEY;
        final String sessionKey = "b9292b4c5a015eb50c45304e1e0e7234";

        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("session_key", sessionKey);
        paramMap.put("app_key", appKey);
        paramMap.put("subId","584fa5c2777487649716df45");

        // 计算SIG
        String sig = DigestSignUtils.signMd5(paramMap, secretKey);
        paramMap.put("sig", sig);

        String apiURL = "http://localhost:8081/userMobile/jxtNews/subAlbum.vpage";
        POST post = HttpRequestExecutor.defaultInstance().post(apiURL);
        paramMap.entrySet().stream().forEach(e -> {
            String name = e.getKey();
            String value = e.getValue();
            post.addParameter(name, value);
        });

        System.out.println(post.execute().getResponseString());
        System.out.println("Fix End...");
    }


}
