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

package com.voxlearning.washington.controller.open.v1.teacher.test;

import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.util.DigestSignUtils;
import com.voxlearning.alps.web.UrlUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by steven on 2016/4/14.
 */
public class TestTeacherLogin {
    public static void main(String[] args) {
        try {
            System.out.println("Test Start...");

            final String appKey = "17Teacher";
            String secretKey = "gvUKQN1EFXKp";
            final String userCode = "12975739";
            String passwd = "1";

            // 计算SIG
            Map paramMap = new HashMap();
            paramMap.put("app_key", appKey);
            paramMap.put("user_code", userCode);
            paramMap.put("passwd", passwd);
            String sig = DigestSignUtils.signMd5(paramMap, secretKey);
            paramMap.put("sig", sig);
            paramMap.put("categoryId",0L);
            paramMap.put("pageNum",1);


            String apiURL = "http://www.test.17zuoye.net/usermobile/giftMall/products/get_by_category.vpage";
            String URL = UrlUtils.buildUrlQuery(apiURL, paramMap);
            System.out.println(HttpRequestExecutor.defaultInstance()
                    .get(URL)
                    .execute().getResponseString());

            System.out.println("Test End...");

        } catch (Exception e) {
            e.printStackTrace();
        }











        // TODO(U)
        // sessionKey:1c69fbe57e7dbff515fd011f11f71296
        // userId:127109
    }
}
