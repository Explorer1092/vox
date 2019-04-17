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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhangbin
 * @since 2016/10/26 10:18
 */
public class TestStudentIndexApiController {
    public static void main(String args[]) {
        try {
            System.out.println("Test Start...");


            final String appKey = "17Student";
            final String secretKey = "kuLwGZMJBcQj";
            final String sessionKey = "213561f06f288bc487e3ffc07cb5b9ad";//5d736eb30f59c383c1ccd31e7bb37a4c
//            final String sessionKey = "71abc598ba2e05ff03a87838b4ae0d3b";

            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("app_key", appKey);
            paramMap.put("session_key", sessionKey);
            Map<String, Object> data = new HashMap<>();
            data.put("imgUrl", "https://cdn-live-image.17zuoye.cn/training/acf/20190307/277bbebea54e4f70a2f6e807a4049982");
            List<String> textList = new ArrayList<>();
            textList.add("25\\\\times[(637\\\\div63+28)]=175");
            textList.add("8+5=10");
            textList.add("11-4=8");
            data.put("texts", textList);
            paramMap.put("data", JsonUtils.toJson(data));

            String sig = DigestSignUtils.signMd5(paramMap, secretKey);
            paramMap.put("sig", sig);
            paramMap.put("sys", "ios");
            paramMap.put("ver", "2.8.9");

            String apiURL = "http://localhost:8081/v1/newhomework/independent/ocr/mental/symptomanalysis.vpage";
            POST post = HttpRequestExecutor.defaultInstance().post(apiURL);
            paramMap.forEach(post::addParameter);

            System.out.println(post.execute().getResponseString());
            System.out.println("Test End...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
