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

package com.voxlearning.utopia.service.afenti.api.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import com.voxlearning.utopia.service.afenti.api.constant.AchievementStatus;
import com.voxlearning.utopia.service.afenti.api.constant.AchievementType;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户获得成就记录表
 *
 * @author peng.zhang.a
 * @since 16-7-25
 */
@Getter
@Setter
@DocumentConnection(configName = "hs_afenti")
@DocumentTable(table = "VOX_AFENTI_USER_ACHIEVEMENT_RECORD")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20160722")
public class AfentiUserAchievementRecord extends AbstractDatabaseEntity {

    private static final long serialVersionUID = 1127096945155242801L;
    @UtopiaSqlColumn private Long userId;
    @UtopiaSqlColumn private AchievementType achievementType;
    @UtopiaSqlColumn private Integer level;
    @UtopiaSqlColumn private Subject subject;
    @UtopiaSqlColumn private AchievementStatus status;

    public static String ck_id(Long id) {
        return CacheKeyGenerator.generateCacheKey(AfentiUserAchievementRecord.class, id);
    }


    static public AfentiUserAchievementRecord newInstance(Long userId,
                                                          AchievementType achievementType,
                                                          Integer level,
                                                          Subject subject,
                                                          AchievementStatus achievementStatus) {
        AfentiUserAchievementRecord afentiUserAchievementRecord = new AfentiUserAchievementRecord();
        afentiUserAchievementRecord.setUserId(userId);
        afentiUserAchievementRecord.setSubject(subject);
        afentiUserAchievementRecord.setAchievementType(achievementType);
        afentiUserAchievementRecord.setStatus(achievementStatus);
        afentiUserAchievementRecord.setLevel(level);
        return afentiUserAchievementRecord;
    }

    public static String ck_us(Long userId, Subject subject) {
        return CacheKeyGenerator.generateCacheKey(
                AfentiUserAchievementRecord.class,
                new String[]{"UID", "SJ"},
                new Object[]{userId, subject});
    }
}