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
 * @author malong
 * @since 2016/6/1
 */
public class TestMobileParentHomeworkDetailControlle {
    public static void main(String[] args) {
        testList();
    }

    private static void testList() {
        try {
            Map paramMap = new HashMap();

            String apiURL = "http://localhost:8081/parentMobile/homework/appHomeworkDetail.vpage?sid=333879367&hid=5747dab42aa6381a905e124d&htype=ENGLISH";
            String URL = UrlUtils.buildUrlQuery(apiURL, paramMap);
            System.out.println(HttpRequestExecutor.defaultInstance().get(URL).execute().getResponseString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
