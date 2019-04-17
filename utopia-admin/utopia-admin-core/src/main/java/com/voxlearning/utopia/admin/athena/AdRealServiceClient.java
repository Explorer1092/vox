/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.admin.athena;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.athena.api.AdRealService;
import com.voxlearning.athena.bean.AdRealData;
import com.voxlearning.athena.cenum.AdEnum;
import lombok.Getter;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

@Named("com.voxlearning.utopia.admin.athena.AdRealServiceClient")
public class AdRealServiceClient {

    // 大数据广告实时数据接口
    @Getter
    @ImportService(interfaceClass = AdRealService.class)
    private AdRealService adRealService;

    public Map<AdEnum, List<AdRealData>> mockData() {
        return MapUtils.map(
                AdEnum.MINUTE_CLICK, mockMinuteData(50),
                AdEnum.MINUTE_SHOW, mockMinuteData(300),
                AdEnum.HOUR_CLICK, mockHourData(50),
                AdEnum.HOUR_SHOW, mockHourData(300)
        );
    }

    private List<AdRealData> mockMinuteData(int limit) {
        List<AdRealData> mock = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, 7);
        cal.set(Calendar.DATE, 15);
        cal.set(Calendar.HOUR, 8);
        cal.set(Calendar.MINUTE, 0);
        for (int i = 0; i < 10; ++i) {
            AdRealData data = new AdRealData();
            data.setDate(DateUtils.dateToString(cal.getTime(), "yyyy-MM-dd HH:mm"));
            data.setPv(RandomUtils.nextInt(20, limit));
            data.setUv(RandomUtils.nextInt(20, data.getPv()));
            cal.add(Calendar.MINUTE, 5);
            mock.add(data);
        }
        return mock;
    }

    private List<AdRealData> mockHourData(int limit) {
        List<AdRealData> mock = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, 7);
        cal.set(Calendar.DATE, 15);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        for (int i = 0; i < 10; ++i) {
            AdRealData data = new AdRealData();
            data.setDate(DateUtils.dateToString(cal.getTime(), "yyyy-MM-dd HH"));
            data.setPv(RandomUtils.nextInt(20, limit));
            data.setUv(RandomUtils.nextInt(20, data.getPv()));
            cal.add(Calendar.HOUR, 1);
            mock.add(data);
        }
        return mock;
    }

}
