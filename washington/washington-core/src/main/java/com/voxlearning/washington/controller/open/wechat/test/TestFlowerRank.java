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
 * Created by Shuai Huan on 2015/6/1.
 */
public class TestFlowerRank {

    public static void main(String[] args) {
        try {
            System.out.println("Test Start...");

            // 计算SIG
            Map paramMap = new HashMap();
            paramMap.put("cid", "32304");

            String apiURL = "http://localhost:8080/open/wechat/flower/flowerrankbyparent.vpage";
            AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().post(apiURL).json(paramMap).execute();
            System.out.println(response.getResponseString());

            System.out.println("Test End...");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
