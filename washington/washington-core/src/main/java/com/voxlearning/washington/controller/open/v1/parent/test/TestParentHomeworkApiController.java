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

import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.http.client.execute.POST;
import com.voxlearning.alps.lang.util.DigestSignUtils;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.service.user.consumer.ParentLoaderClient;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lyl on 2016/1/11.
 */
public class TestParentHomeworkApiController {

    public static final String PARENT_APP_KEY = "17Parent";
    public static final String PARENT_SECRET_KEY = "iMMrxI3XMQtd";
    public static final String STUDENT_APP_KEY = "17Student";
    public static final String STUDENT_SECRET_KEY ="kuLwGZMJBcQj";
    public static final String imei = "355499060894099";
    public static final String sessionKey = "2fd0dfda9b514367c20002f8577d6839";

    @Inject
    protected ParentLoaderClient parentLoaderClient;

    public static void main(String[] args) {

        homeworksDynamicState();
    }


    public static void identityList(){
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

    public static void homeworksDynamicState(){
        try {
            System.out.println("Test Start...");

            final String appKey = "17Parent";
            final String secretKey = "iMMrxI3XMQtd";

            final Long sid = 333875405L;
            final Long time = 1453827661L;

            // 计算SIG
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("session_key", sessionKey);
            paramMap.put("app_key", appKey);
            paramMap.put("sid", sid.toString());
            paramMap.put("time", "");

            String sig = DigestSignUtils.signMd5(paramMap, secretKey);

            paramMap.put("sig", sig);

            String apiURL = "http://10.200.6.5:8081/v1/parent/homework/homeworksDynamicState.vpage";
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
