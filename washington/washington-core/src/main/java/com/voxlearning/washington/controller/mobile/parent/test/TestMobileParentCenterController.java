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

package com.voxlearning.washington.controller.mobile.parent.test;

import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.web.UrlUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Hailong Yang on 2015/09/21
 */
public class TestMobileParentCenterController {

    public static void main(String[] args) {
        try {
            System.out.println("Test Start...");

            final String appKey = "TravelAmerica";
            final String sessionKey = "ed596630cb4a045db9d704239b829cdc";
            String secretKey = "Tzpw9PIj8zM";

            // 计算SIG
            Map paramMap = new HashMap();
            paramMap.put("sid", 333875333);
            /*paramMap.put("session_key", sessionKey);
            paramMap.put("campaign_id", "5");

            String sig = DigestSignUtils.signMd5(paramMap, secretKey);

            paramMap.put("sig", sig);*/

            String apiURL = "http://test.17zuoye.net:8180/parent/homework/loadhomeworks.vpage";
            String URL = UrlUtils.buildUrlQuery(apiURL, paramMap);
            System.out.println(HttpRequestExecutor.defaultInstance().get(URL).execute().getResponseString());

            System.out.println("Test End...");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
