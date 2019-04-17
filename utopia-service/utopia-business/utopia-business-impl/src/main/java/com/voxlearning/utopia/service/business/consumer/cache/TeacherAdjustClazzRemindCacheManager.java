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
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.temp.NewSchoolYearActivity;

/**
 * Created by Summer Yang on 2015/8/7.
 * 老师调整班级记录
 */
public class TeacherAdjustClazzRemindCacheManager extends PojoCacheObject<Long, String> {
    public TeacherAdjustClazzRemindCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public void record(Long teacherId) {
        if (teacherId == null) {
            return;
        }
        set(teacherId, "Real_Madrid");
    }

    public boolean done(Long teacherId) {
        return teacherId == null || load(teacherId) != null;
    }


    @Override
    public int expirationInSeconds() {
        return (int) (NewSchoolYearActivity.getSummerEndDate().getTime() / 1000);
    }
}
