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

package com.voxlearning.utopia.service.action.api;

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.remote.NoResponseWait;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.action.api.document.*;
import com.voxlearning.utopia.service.action.api.support.AchievementType;
import com.voxlearning.utopia.service.action.api.support.ClazzAttendanceInfo;
import com.voxlearning.utopia.service.action.api.support.SchoolAttendanceRank;
import com.voxlearning.utopia.service.action.api.support.UserLikeType;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Action loader abstraction.
 *
 * @author Xiaohai Zhang
 * @since Aug 3, 2016
 */
@ServiceVersion(version = "2016.08.03")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface ActionLoader extends IPingable {
    default UserGrowth loadUserGrowth(Long userId) {
        if (userId == null) {
            return null;
        }
        return loadUserGrowths(Collections.singleton(userId)).get(userId);
    }

    @CacheMethod(type = UserGrowth.class, writeCache = false)
    Map<Long, UserGrowth> loadUserGrowths(@CacheParameter(multiple = true) Collection<Long> userIds);

    @CacheMethod(type = UserAchievementRecord.class, writeCache = false)
    List<UserAchievementRecord> loadUserAchievementRecords(Long userId);

//    MapMessage reloadUserAchievement(Long userId);

    @CacheMethod(type = UserGrowthLog.class, writeCache = false)
    List<UserGrowthLog> getUserGrowthLogs(@CacheParameter("UID") Long userId);

    @CacheMethod(type = ClazzAchievementLog.class)
    List<ClazzAchievementLog> getClazzAchievementWall(@CacheParameter("CID") Long clazzId);

    MapMessage rebuildClazzAchievementData(ClazzAchievementLog log);

    List<UserAchievementLog> getUserAchievements(Collection<Long> userIds, AchievementType type, Integer level);

    List<UserAchievementLog> getUserAchievementLogs(Long userId);

    @CacheMethod(type = UserGrowthRewardLog.class, writeCache = false)
    List<UserGrowthRewardLog> getUserGrowthLevelRewards(@CacheParameter("UID") Long userId);

    @CacheMethod(type = SchoolAttendanceRank.class,
            expiration = @UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.fixed, value = 4 * 60 * 60))
    SchoolAttendanceRank getClazzAttendanceRank(@CacheParameter("SID") Long schoolId);

    ClazzAttendanceInfo getClazzAttendanceInfo(Long schoolId, Long clazzId);

    Map<String, UserAttendanceCount> getUserAttendanceCountCurrentMonth(Collection<Long> userIds);

    @NoResponseWait
    void rankGrowthSnapshot(Long clazzId);

    Map<Long, UserGrowth> loadUserGrowthSnapshot(Long clazzId, Collection<Long> userIds);

}
