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

package com.voxlearning.washington.controller.open.wechat.test;

import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by XiaoPeng.Yang on 15-4-23.
 */
public class TestWechatAmbassadorLogin {
    public static void main(String[] args) {
        try {
            System.out.println("Test Start...");

            final String appKey = "17teacher";
            final String sessionKey = "7457e35e5f769bebee23520fa65b9724";
            String secretKey = "Tzpw9PIj8zM";

            // 计算SIG
            Map paramMap = new HashMap();
            paramMap.put("sid", 31039);
//            paramMap.put("clazzId", 31206);
//            paramMap.put("uid", 115163);
//            paramMap.put("homeworkId", "555f18f0a3103d6587b078ab");
            paramMap.put("hid", "555d4b3b040c7bccc0971097");
            paramMap.put("subject", "ENGLISH");
            paramMap.put("isQuiz", false);
            paramMap.put("pid", 0L);

            String apiURL = "http://localhost:8080/open/wechat/homework/homeworkdetail.vpage";
//            String apiURL = "http://localhost:8080/v1/campaign/user/get.vpage";
            AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().post(apiURL).json(paramMap).execute();
            System.out.println(response.getResponseString());

            System.out.println("Test End...");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
