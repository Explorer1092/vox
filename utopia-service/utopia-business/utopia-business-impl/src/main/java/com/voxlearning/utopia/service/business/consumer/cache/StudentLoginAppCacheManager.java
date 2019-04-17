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

package com.voxlearning.utopia.service.business.consumer.cache;

import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.spi.cache.UtopiaCache;

import java.util.Date;

/**
 * Created by Summer Yang on 2015/8/31.
 * 移动端推广——pc班级空间  活动期间记录学生首次登陆APP时间
 * @deprecated 早就过期了
 */
@Deprecated
public class StudentLoginAppCacheManager extends PojoCacheObject<Long, String> {
    public StudentLoginAppCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public void record(Long studentId) {
        if (studentId == null) {
            return;
        }
        String value = load(studentId);
        if (StringUtils.isBlank(value)) {
            set(studentId, DateUtils.dateToString(new Date()));
        }
    }

    public String loadRecord(Long studentId) {
        if (studentId == null) {
            return null;
        }
        return load(studentId);
    }

    @Override
    public int expirationInSeconds() {
        return (int) (DateUtils.stringToDate("2015-09-20 23:59:59").getTime() / 1000);
    }
}