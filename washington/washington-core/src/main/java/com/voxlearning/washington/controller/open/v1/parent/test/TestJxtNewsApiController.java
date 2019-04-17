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

import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.DigestSignUtils;
import com.voxlearning.alps.web.UrlUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author malong
 * @since 2016/7/26
 */
public class TestJxtNewsApiController {
    public static void main(String[] args) {
        try {
            System.out.println("Test Start...");

            final String appKey = "17Parent";
            final String secretKey = "iMMrxI3XMQtd";
            final String sessionKey = "317ffd8586e75fa4069c010c1ab3542c";

            // 计算SIG
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("app_key", appKey);
            paramMap.put("session_key", sessionKey);
            //paramMap.put("tag_id", SafeConverter.toString(5L));
//            paramMap.put("tag_id", SafeConverter.toString(4L));
//            paramMap.put("page", SafeConverter.toString(0));
//            paramMap.put("sid", SafeConverter.toString(333875292L));
            paramMap.put("search_key_word", "动画");
            paramMap.put("search_content_type", SafeConverter.toString(-1));
            paramMap.put("search_source",SafeConverter.toString(1));
            paramMap.put("search_json","");
            paramMap.put("sid",SafeConverter.toString(30001));
            String sig = DigestSignUtils.signMd5(paramMap, secretKey);

            paramMap.put("sig", sig);
            paramMap.put("ver", "1.9.0.1002");

            //String apiURL = "http://10.200.4.52:8081/v1/parent/jxt/news/getJxtNewsNoticeCount.vpage";
            String apiURL = "http://10.200.4.153:8081/v2/parent/jxtnews/channel_list.vpage";
            String URL = UrlUtils.buildUrlQuery(apiURL, paramMap);
            System.out.println(HttpRequestExecutor.defaultInstance().post(URL).execute().getResponseString());
            System.out.println("Test End...");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
