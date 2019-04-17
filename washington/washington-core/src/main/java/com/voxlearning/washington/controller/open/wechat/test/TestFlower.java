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

import com.voxlearning.alps.lang.calendar.DateRange;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.utopia.data.SchoolYear;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * Created by Shuai Huan on 2015/6/1.
 */
public class TestFlower {

    public static void main(String[] args) {
        try {
            System.out.println("Test Start...");
            float avgScore = new BigDecimal(100).divide(new BigDecimal(3), 2, BigDecimal.ROUND_HALF_UP).floatValue();
            // 计算SIG
            Map paramMap = new HashMap();
            paramMap.put("sid", "333742817");
            paramMap.put("hid", "456");
            paramMap.put("htype", "ENGLISH");
            paramMap.put("tid", "12345");

            String apiURL = "http://localhost:8080/open/wechat/flower/sendflower.vpage";
//            HttpUtils.HttpResponse response = HttpUtils.httpPostJson(apiURL, JsonUtils.toJson(paramMap));
//            System.out.println(response.getResponseString());

            // FIXME 学年按8月10日分隔，所以这里需要特殊处理，8月10日至8月31日统一为之前学期
            Date currentDate = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(currentDate);
            if (calendar.get(Calendar.MONTH) == Calendar.AUGUST && calendar.get(Calendar.DAY_OF_MONTH) >=10) {
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                currentDate = calendar.getTime();
            }
            SchoolYear schoolYear = SchoolYear.newInstance(currentDate);
            DateRange dateRange = schoolYear.getSchoolYearDateRange();
            System.out.println("Test End..."+ DateUtils.stringToDate("2015-09-01 00:00:00"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
