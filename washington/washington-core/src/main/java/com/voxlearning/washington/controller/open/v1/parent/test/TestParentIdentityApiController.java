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

package com.voxlearning.washington.controller.open.v1.parent.test;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.http.client.execute.POST;
import com.voxlearning.alps.lang.util.DigestSignUtils;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.consumer.ParentLoaderClient;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by lyl on 2016/1/11.
 */
public class TestParentIdentityApiController {

    public static final String PARENT_APP_KEY = "17Parent";
    public static final String PARENT_SECRET_KEY = "iMMrxI3XMQtd";
    public static final String STUDENT_APP_KEY = "17Student";
    public static final String STUDENT_SECRET_KEY = "kuLwGZMJBcQj";
    public static final String imei = "355499060894099";

    @Inject
    protected ParentLoaderClient parentLoaderClient;

    public static void main(String[] args) {

        StudentParentRef ref1 = new StudentParentRef();
        ref1.setId("1");
        ref1.setCallName("");
        StudentParentRef ref2 = new StudentParentRef();
        ref2.setId("2");
        ref2.setCallName("爸爸");
        StudentParentRef ref3 = new StudentParentRef();
        ref3.setId("3");
        ref3.setCallName("");
        List<StudentParentRef> list = new ArrayList<>();
        list.add(ref1);
        list.add(ref2);
        list.add(ref3);
        List<StudentParentRef> collect = list.stream().filter(p -> !p.getCallName().equals("")).filter(p -> p.getId().equals("2")).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(collect)) {
            System.out.println("is empty");
        } else {
            System.out.println(collect.get(0).getId() + " not");
        }
        for (StudentParentRef ref : list) {

            System.out.println(ref.getStudentId() + "--->" + ref.getCallName());
        }
    }


    public static void identityList() {
        try {
            System.out.println("Test Start...");

            final String appKey = "17Parent";
            final String secretKey = "iMMrxI3XMQtd";

            final Long sid = 333892601L;

            // 计算SIG
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("app_key", appKey);
            paramMap.put("sid", sid.toString());

            String sig = DigestSignUtils.signMd5(paramMap, secretKey);

            paramMap.put("sig", sig);

            String apiURL = "http://10.200.5.165:8081/v1/parent/identity/identitylist.vpage";
            String URL = UrlUtils.buildUrlQuery(apiURL, paramMap);

            AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().get(URL).execute();

            System.out.println(response.getResponseString());

            System.out.println("Test End...");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void hasBindedMobileSendCode() {
        try {
            System.out.println("Test Start...");

            final String appKey = "17Parent";
            final String secretKey = "iMMrxI3XMQtd";

            final Long pid = 256685L;

            // 计算SIG
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("app_key", appKey);
            paramMap.put("pid", pid.toString());
            paramMap.put("imei", imei);

            String sig = DigestSignUtils.signMd5(paramMap, secretKey);

            paramMap.put("sig", sig);

            String apiURL = "http://10.200.5.165:8081/v1/user/parent/login/verifycode/get.vpage";
            POST post = HttpRequestExecutor.defaultInstance().post(apiURL);
            paramMap.entrySet().forEach(e -> {
                String name = e.getKey();
                String value = e.getValue();
                post.addParameter(name, value);
            });
            System.out.println(post.execute().getResponseString());

            System.out.println("Test End...");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void verifyCodeAndBindIdentityAndLogin() {
        try {
            System.out.println("Test Start...");

            final String appKey = "17Parent";
            final String secretKey = "iMMrxI3XMQtd";


            final Long studentId = 333892601L;
            final String mobile = "18001366518";
            final Integer callname = 1;
            String code = "123456";

            // 计算SIG
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("app_key", appKey);
            paramMap.put("sid", studentId.toString());
            paramMap.put("mobile", mobile);
            paramMap.put("callname", callname.toString());
            paramMap.put("verify_code", code);

            String sig = DigestSignUtils.signMd5(paramMap, secretKey);

            paramMap.put("sig", sig);

            String apiURL = "http://10.200.5.165:8081/v1/parent/verifyandbindandlogin.vpage";
            POST post = HttpRequestExecutor.defaultInstance().post(apiURL);
            paramMap.entrySet().forEach(e -> {
                String name = e.getKey();
                String value = e.getValue();
                post.addParameter(name, value);
            });
            System.out.println(post.execute().getResponseString());

            System.out.println("Test End...");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void testGetMobileAfterChooseIdentity() {
        try {
            System.out.println("Test Start...");


//            String apiURL = "http://10.200.4.71:8081/v1/appmessage/loaduserMessage.vpage";
//            String  apiURL = "http://www.test.17zuoye.net/v1/parent/identity/verifyidentity.vpage?sid=333808631&callname=1";
            String apiURL = "http://10.200.5.165:8081/v1/parent/identity/verifyidentity.vpage?sid=333808631&callname=1";
//            String url = UrlUtils.buildUrlQuery(apiURL, paramMap);
            AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().get(apiURL).execute();
            System.out.println(response.getResponseString());
            System.out.println("Test End...");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void testParentIdentityList() {
        try {
            System.out.println("Test Start...");

            final String appKey = STUDENT_APP_KEY;
            final String secretKey = STUDENT_SECRET_KEY;
            final String sessionKey = "b02cbe66cc00d8de9243b4a98744db81";
//
//            Map<String,Object> extInfo = new HashMap<>();
//            extInfo.put("ext1","123");
//            extInfo.put("ext2","234");
//            List<Long> userIdList = new ArrayList<>();
//            userIdList.add(333875333L);
            Map<String, String> paramMap = new HashMap<>();

            paramMap.put("session_key", sessionKey);
            paramMap.put("page", "0");
//            String ds = "2015-12-30";
//            String de = "2016-06-30";
            paramMap.put("app_key", appKey);
//            paramMap.put(REQ_APP_MESSAGE_TYPE,"1");
//            paramMap.put(REQ_APP_MESSAGE_SOURCE,"XUESHE");
//            paramMap.put(REQ_APP_MESSAGE_UID, "[333878885]");
//            paramMap.put(REQ_APP_MESSAGE_CONTENT,"测试测试测试111111");
//            paramMap.put(REQ_APP_MESSAGE_IMGURL,"");
//            paramMap.put(REQ_APP_MESSAGE_LINKURL,"");
//            paramMap.put(REQ_APP_MESSAGE_LINKTYPE,"0");
////            paramMap.put(REQ_APP_MESSAGE_PUSHTAG,"");
//            paramMap.put(REQ_APP_MESSAGE_EXTINFO, "{\"s\":1001,\"t\":\"msg_list\",\"key\":\"m\",\"timestamp\":1451547004}");
//
//
//            paramMap.put(REQ_APP_MESSAGE_BTN_CONTENT,"立即参加");
//            paramMap.put(REQ_APP_MESSAGE_START, SafeConverter.toString(DateUtils.stringToDate(ds, DateUtils.FORMAT_SQL_DATE).getTime()));
//            paramMap.put(REQ_APP_MESSAGE_END, SafeConverter.toString(DateUtils.stringToDate(de, DateUtils.FORMAT_SQL_DATE).getTime()));
//            paramMap.put(REQ_APP_MESSAGE_RANK,"1");


            // 计算SIG
            String sig = DigestSignUtils.signMd5(paramMap, secretKey);
            paramMap.put("sig", sig);

//            String apiURL = "http://10.200.4.71:8081/v1/appmessage/loaduserMessage.vpage";
            String apiURL = "http://10.200.5.165:8081/v1/user/parent/login/verifycode/get.vpage";
            String url = UrlUtils.buildUrlQuery(apiURL, paramMap);
            POST post = HttpRequestExecutor.defaultInstance().post(apiURL);

            AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().get(url).execute();
            System.out.println(response.getResponseString());
            System.out.println("Test End...");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendSidCode() {
        try {
            System.out.println("Test Start...");

            final String appKey = "17Parent";
            final String secretKey = "iMMrxI3XMQtd";
            final Long parentId = 256685L;

            // 计算SIG
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("app_key", appKey);
            paramMap.put("pid", parentId.toString());
            paramMap.put("imei", imei);

            String sig = DigestSignUtils.signMd5(paramMap, secretKey);

            paramMap.put("sig", sig);

            String apiURL = "http://10.200.5.165:8081/v1/user/parent/login/verifycode/get.vpage";
            POST post = HttpRequestExecutor.defaultInstance().post(apiURL);
            paramMap.entrySet().forEach(e -> {
                String name = e.getKey();
                String value = e.getValue();
                post.addParameter(name, value);
            });
            System.out.println(post.execute().getResponseString());

            System.out.println("Test End...");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void notNeedPasswordToLogin() {
        try {
            System.out.println("Test Start...");

            final String appKey = "17Parent";
            final String secretKey = "iMMrxI3XMQtd";
            final String user_code = "18001366518";
            final String verifyCode = "123456";
            final String ver = "1.3.6.1001";

            // 计算SIG
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("app_key", appKey);
            paramMap.put("pid", "");
            paramMap.put("user_code", user_code);
            paramMap.put("verify_code", verifyCode);
            paramMap.put("ver", ver);

            String sig = DigestSignUtils.signMd5(paramMap, secretKey);

            paramMap.put("sig", sig);

            String apiURL = "http://10.200.5.165:8081/v1/parent/verifycode.vpage";
            POST post = HttpRequestExecutor.defaultInstance().post(apiURL);
            paramMap.entrySet().forEach(e -> {
                String name = e.getKey();
                String value = e.getValue();
                post.addParameter(name, value);
            });
            System.out.println(post.execute().getResponseString());

            System.out.println("Test End...");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendParentBindIdentityVerifyCode() {
        try {
            System.out.println("Test Start...");

            final String appKey = "17Parent";
            final String secretKey = "iMMrxI3XMQtd";

            final Long studentId = 333892601L;

            final String mobile = "123*****444";

            // 计算SIG
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("app_key", appKey);
            paramMap.put("imei", imei);
            paramMap.put("mobile", mobile);
            paramMap.put("sid", studentId.toString());
            paramMap.put("pid", "");

            String sig = DigestSignUtils.signMd5(paramMap, secretKey);

            paramMap.put("sig", sig);

            String apiURL = "http://10.200.5.165:8081/v1/user/moblie/parent/identity/bindMoblie.vpage";
            POST post = HttpRequestExecutor.defaultInstance().post(apiURL);
            paramMap.entrySet().forEach(e -> {
                String name = e.getKey();
                String value = e.getValue();
                post.addParameter(name, value);
            });
            System.out.println(post.execute().getResponseString());

            System.out.println("Test End...");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
