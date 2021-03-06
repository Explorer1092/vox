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

import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.util.DigestSignUtils;
import com.voxlearning.alps.web.UrlUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Alex on 14-12-17.
 */
public class TestOpenApiWechatShare {
    public static void main(String[] args) {
        try {
            System.out.println("Test Start...");

            final String appKey = "DrawTogether";
            final String sessionKey = "9095ddb2cb3820c6f6496c37282de603";
            final String comment = "";
            final String picUrl = "http://i3.sinaimg.cn/blog/2014/1029/S129809T1414550868715.jpg";

            // 计算SIG
            Map paramMap = new HashMap();
            paramMap.put("app_key", appKey);
            paramMap.put("session_key", sessionKey);
            paramMap.put("share_text", comment);
            paramMap.put("share_img", picUrl);


            String secretKey = "AQeupmRmUO01";

            String sig = DigestSignUtils.signMd5(paramMap, secretKey);

            paramMap.put("sig", sig);

            String apiURL = "http://api.17zuoye.com/v1/user/wechat/share.api";
            String URL = UrlUtils.buildUrlQuery(apiURL, paramMap);
            System.out.println(HttpRequestExecutor.defaultInstance().post(URL).execute().getResponseString());

            System.out.println("Test End...");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
