
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
 * Created by shiwei.liao on 2015/6/8.
 */
public class Testo2o {

    public static void main(String[] args) {
        Map<String, Object> paramMap = new HashMap<>();
//        paramMap.put("tutorStr","english3");
//        paramMap.put("title","wechattest");
//        paramMap.put("content","test");
//        paramMap.put("type","ABOUT_AUTH");
//        paramMap.put("status","FOLLOWING");
//        paramMap.put("teacherId","");
//        paramMap.put("userName","shiwei.liao");
//        paramMap.put("partId","1");
//        paramMap.put("userId",333876063);
//        paramMap.put("userId","2150247");
//        paramMap.put("examId","555b16246670c5a1b86dab3c");
//        paramMap.put("uid","12345678");
//        paramMap.put("identity",0);
//        paramMap.put("startTime","1433906518000");
//        paramMap.put("endTime","1433930805000");
//        paramMap.put("answerJson","{\"02-02-00-00\":\"0\",\"02-02-0-00\":\"1\",\"02-02-02-00\":\"1\"}");
//        String apiURL = "http://localhost:8080/open/wechat/o2o/anserrecord.vpage";
        String apiURL = "http://localhost:8081/open/wechat/crm/teacher/record/index.vpage";
        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().post(apiURL).json(paramMap).execute();
        System.out.println(response.getResponseString());
//        System.out.println(DateUtils.stringToDate(DateUtils.dateToString(new Date(1433929787000L), DateUtils.FORMAT_SQL_DATETIME)));
    }
}
