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

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * Created by Summer Yang on 2016/1/11.
 * 老师使用一键奖励奖励学生学豆管理器 一天只能使用一次
 * 目前只有老师微信端在使用这个限制
 * @deprecated 只有写没有读的代码
 */
@Deprecated
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class TeacherBatchRewardStudentDayCacheManager extends PojoCacheObject<TeacherBatchRewardStudentDayCacheManager.TeacherWithHomeworkId, String> {

    public TeacherBatchRewardStudentDayCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public void record(Long teacherId, String homeworkId) {
        if (teacherId == null || StringUtils.isBlank(homeworkId)) return;
        add(new TeacherWithHomeworkId(teacherId, homeworkId), "dummy");
    }

    public boolean useToday(Long teacherId, String homeworkId) {
        return teacherId == null || StringUtils.isBlank(homeworkId) ||
                load(new TeacherWithHomeworkId(teacherId, homeworkId)) != null;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeacherWithHomeworkId {
        public Long teacherId;
        public String homeworkId;

        @Override
        public String toString() {
            return "TID=" + teacherId + ",HID=" + homeworkId;
        }
    }
}
