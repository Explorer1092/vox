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
import com.voxlearning.utopia.business.api.cache.IStudentMissionNoticeCacheManager;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 *
 * @author RuiBao
 * @version 0.1
 * @since 7/7/2015
 */
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class StudentMissionNoticeCacheManager
        extends PojoCacheObject<StudentMissionNoticeCacheManager.StudentWithMissionAndWechatNoticeType, String>
        implements IStudentMissionNoticeCacheManager {

    public StudentMissionNoticeCacheManager(UtopiaCache cache) {
        super(cache);
    }

    @Override
    public void record(Long studentId, Long missionId, String wechatNoticeType) {
        if (studentId == null || missionId == null || StringUtils.isBlank(wechatNoticeType)) return;
        add(new StudentWithMissionAndWechatNoticeType(studentId, missionId, wechatNoticeType), "dummy");
    }

    @Override
    public boolean sendToday(Long studentId, Long missionId, String wechatNoticeType) {
        return studentId == null || missionId == null || StringUtils.isBlank(wechatNoticeType) ||
                load(new StudentWithMissionAndWechatNoticeType(studentId, missionId, wechatNoticeType)) != null;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class StudentWithMissionAndWechatNoticeType {
        public Long studentId;
        public Long missionId;
        public String wechatNoticeType;

        @Override
        public String toString() {
            return "SID=" + studentId + ",MID=" + missionId + ",WNT=" + wechatNoticeType;
        }
    }
}
