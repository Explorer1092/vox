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

package com.voxlearning.utopia.service.action.impl.loader;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.utopia.service.action.api.ActionLoader;
import com.voxlearning.utopia.service.action.api.document.*;
import com.voxlearning.utopia.service.action.api.event.ActionEventType;
import com.voxlearning.utopia.service.action.api.support.AchievementType;
import com.voxlearning.utopia.service.action.api.support.ClazzAttendanceInfo;
import com.voxlearning.utopia.service.action.api.support.SchoolAttendanceRank;
import com.voxlearning.utopia.service.action.impl.dao.*;
import com.voxlearning.utopia.service.action.impl.support.ActionCacheSystem;
import com.voxlearning.utopia.service.clazz.client.AsyncGroupServiceClient;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Default {@link ActionLoader} implementation.
 *
 * @author Xiaohai Zhang
 * @since Aug 3, 2016
 */
@Named
@Slf4j
@ExposeService(interfaceClass = ActionLoader.class)
public class ActionLoaderImpl implements ActionLoader {

    @Inject private ClazzAchievementLogDao clazzAchievementLogDao;
    @Inject private ClazzAttendanceCountDao clazzAttendanceCountDao;
    @Inject private UserAchievementRecordDao userAchievementRecordDao;
    @Inject private UserAchievementLogDao userAchievementLogDao;
    @Inject private UserAttendanceCountDao userAttendanceCountDao;
    @Inject private UserGrowthDao userGrowthDao;
    @Inject private UserGrowthLogDao userGrowthLogDao;
    @Inject private UserGrowthRewardLogDao userGrowthRewardLogDao;
    @Inject private ActionCacheSystem actionCacheSystem;
    @Inject private AsyncGroupServiceClient asyncGroupServiceClient;

    private final AtomicLockManager atomicLockManager = AtomicLockManager.instance();

    private final String rankGrowthCacheKey = "CLAZZ_ZONE:RANK:GROWTH:SNAPSHOT:%s";
    private final String rankGrowthFlagCacheKey = "CLAZZ_ZONE:RANK:GROWTH:FLAG:SNAPSHOT:%s";
    private final String rankGrowthLockCacheKey = "CLAZZ_ZONE:RANK:GROWTH:LOCK:SNAPSHOT:%s";

    @Override
    public Map<Long, UserGrowth> loadUserGrowths(Collection<Long> userIds) {
        return userGrowthDao.loads(userIds);
    }

    @Override
    public List<UserAchievementRecord> loadUserAchievementRecords(Long userId) {
        return userAchievementRecordDao.findByUser(userId);
    }

    @Override
    public List<UserGrowthLog> getUserGrowthLogs(Long userId) {
        if (userId == null) return Collections.emptyList();
        return userGrowthLogDao.findByUserId(userId);
    }

    @Override
    public List<ClazzAchievementLog> getClazzAchievementWall(Long clazzId) {
        if (null == clazzId || 0 == clazzId) return Collections.emptyList();

        return clazzAchievementLogDao.findByClazzId(clazzId);
    }

    @Override
    public MapMessage rebuildClazzAchievementData(ClazzAchievementLog cal) {
        if (log == null) return MapMessage.errorMessage();

        Long userId = cal.getUserId();
        String at = cal.getAchievementType();

        ActionEventType actionEventType = null;
        for (ActionEventType aet : AchievementBuilder.titles.keySet()) {
            AchievementType achievementType = AchievementBuilder.titles.get(aet);
            if (Objects.equals(achievementType.name(), at)) {
                actionEventType = aet;
                break;
            }
        }

        if (actionEventType == null) {
            return MapMessage.errorMessage();
        }

        UserAchievementRecord.UserAchievementRecordId uarId = new UserAchievementRecord.UserAchievementRecordId(userId, actionEventType.name());
        UserAchievementRecord uar = userAchievementRecordDao.load(uarId.toString());
        if (uar == null) {
            return MapMessage.errorMessage();
        }

        Achievement achievement = AchievementBuilder.build(uar, actionEventType);
        if (achievement == null) {
            return MapMessage.errorMessage();
        }

        if (achievement.getRank() < cal.getAchievementLevel()) {
            cal.setAchievementLevel(achievement.getRank());
            clazzAchievementLogDao.upsert(cal);
        }

        return MapMessage.successMessage();
    }

    @Override
    public List<UserAchievementLog> getUserAchievements(Collection<Long> userIds, AchievementType type, Integer level) {
        if (CollectionUtils.isEmpty(userIds) || null == type || null == level || 0 == level)
            return Collections.emptyList();

        Set<String> ids = new HashSet<>();
        userIds.forEach(id -> ids.add(UserAchievementLog.generateId(id, type.name(), level)));

        Map<String, UserAchievementLog> userAchievementLogMap = userAchievementLogDao.loads(ids);
        if (MapUtils.isEmpty(userAchievementLogMap)) return Collections.emptyList();

        return userAchievementLogMap.values().stream()
                .sorted(Comparator.comparing(UserAchievementLog::getCreateTime))
                .collect(Collectors.toList());
    }

    @Override
    public List<UserAchievementLog> getUserAchievementLogs(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }

        return userAchievementLogDao.findByUserId(userId);
    }

    @Override
    public List<UserGrowthRewardLog> getUserGrowthLevelRewards(Long userId) {
        if (null == userId || 0 == userId) {
            return Collections.emptyList();
        }
        return userGrowthRewardLogDao.findByUserId(userId);
    }

    @Override
    public SchoolAttendanceRank getClazzAttendanceRank(Long schoolId) {
        if (null == schoolId || 0 == schoolId)
            return null;

        List<ClazzAttendanceCount> clazzAttendanceCounts = clazzAttendanceCountDao.findBySchoolId(schoolId);
        if (CollectionUtils.isEmpty(clazzAttendanceCounts))
            return null;

        List<ClazzAttendanceInfo> infos = new ArrayList<>();
        clazzAttendanceCounts.forEach(count -> {
            ClazzAttendanceInfo rank = new ClazzAttendanceInfo();
            rank.setClazzId(count.getClazzId());
            rank.setCount(count.getCount());
            rank.setTotalCount(count.getTotalCount());
            rank.setRate(SafeConverter.toDouble(count.getCount()) / SafeConverter.toDouble(count.getTotalCount()));
            infos.add(rank);
        });
        List<ClazzAttendanceInfo> clazzAttendanceInfos = infos.stream()
                .sorted((i1, i2) -> i2.getRate().compareTo(i1.getRate()))
                .limit(30)
                .collect(Collectors.toList());

        SchoolAttendanceRank rank = new SchoolAttendanceRank();
        rank.setRanks(clazzAttendanceInfos);
        rank.setSchoolId(schoolId);

        return rank;
    }

    @Override
    public ClazzAttendanceInfo getClazzAttendanceInfo(Long schoolId, Long clazzId) {
        if (0 == schoolId || null == clazzId || 0 == clazzId) return null;

        ClazzAttendanceCount count = clazzAttendanceCountDao.load(ClazzAttendanceCount.generateId(schoolId, clazzId));
        if (null == count) return null;

        ClazzAttendanceInfo rank = new ClazzAttendanceInfo();
        rank.setClazzId(count.getClazzId());
        rank.setCount(count.getCount());
        rank.setTotalCount(count.getTotalCount());
        rank.setRate(SafeConverter.toDouble(count.getCount()) / SafeConverter.toDouble(count.getTotalCount()));
        return rank;
    }

    @Override
    public Map<String, UserAttendanceCount> getUserAttendanceCountCurrentMonth(Collection<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) return Collections.emptyMap();

        Set<String> ids = new HashSet<>();
        userIds.forEach(userId -> ids.add(UserAttendanceCount.generateId(userId)));

        return userAttendanceCountDao.loads(ids);
    }

    @Override
    public void rankGrowthSnapshot(Long clazzId) {
        if (clazzId == null) {
            return;
        }
        try {
            atomicLockManager.acquireLock(String.format(rankGrowthLockCacheKey, clazzId), 60);

            if (snapshotted(clazzId))
                return;
            //加载所有班级学生
            List<Long> studentIds = asyncGroupServiceClient.getAsyncGroupService()
                    .findStudentIdsByClazzId(clazzId);
            if (CollectionUtils.isEmpty(studentIds)) {
                return;
            }
            Set<Long> studentIdSet = new TreeSet<>(studentIds);

            //成长值
            Map<Long, UserGrowth> userGrowthMap = loadUserGrowths(studentIdSet);
            if (MapUtils.isNotEmpty(userGrowthMap))
                actionCacheSystem.CBS.flushable.set(String.format(rankGrowthCacheKey, clazzId), 60 * 60 * 24 * 60, userGrowthMap);
            //生成快照成功
            actionCacheSystem.CBS.flushable.set(String.format(rankGrowthFlagCacheKey, clazzId), 60 * 60 * 24 * 60, true);
        } catch (Exception e) {

        } finally {
            atomicLockManager.releaseLock(String.format(rankGrowthLockCacheKey, clazzId));
        }

    }

    /**
     * 是否已生成过快照
     *
     * @param clazzId
     * @return
     */
    private boolean snapshotted(Long clazzId) {
        CacheObject<Boolean> flag = actionCacheSystem.CBS.flushable.get(String.format(rankGrowthFlagCacheKey, clazzId));
        if (flag != null && flag.getValue() != null)
            return flag.getValue();
        else
            return false;
    }


    @Override
    public Map<Long, UserGrowth> loadUserGrowthSnapshot(Long clazzId, Collection<Long> userIds) {
        if (clazzId == null || CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyMap();
        }

        if (!snapshotted(clazzId)) {
            return loadUserGrowths(userIds);
        }

        CacheObject<Map<Long, UserGrowth>> cacheObject = actionCacheSystem.CBS.flushable.get(String.format(rankGrowthCacheKey, clazzId));
        if (cacheObject == null || MapUtils.isEmpty(cacheObject.getValue())) {
            return Collections.emptyMap();
        }
        Map<Long, UserGrowth> target = new HashMap<>();
        userIds.forEach(id -> {
                    if (cacheObject.getValue().get(id) != null &&
                            cacheObject.getValue().get(id).getGrowthValue() != null) {
                        target.put(id, cacheObject.getValue().get(id));
                    }
                }
        );
        return target;
    }
}
