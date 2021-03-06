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

package com.voxlearning.washington.controller.teacher.test;

import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;

/**
 * Created by Administrator on 2015/6/12.
 */
public class TestStudentChallege {
    public static void main(String[] args) {
//        Map<String,Object> paramMap = new HashMap<>();
        String apiURL = "http://localhost:8080/student/challenge/signup.vpage";
        System.out.println(HttpRequestExecutor.defaultInstance().get(apiURL).execute().getResponseString());
    }
}
