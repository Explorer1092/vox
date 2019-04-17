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

package com.voxlearning.washington.controller.mobile.student.headline.helper;


import com.voxlearning.alps.cache.atomic.AtomicLockManager;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.utopia.service.action.api.document.Privilege;
import com.voxlearning.utopia.service.privilege.client.PrivilegeBufferServiceClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.consumer.DeprecatedGroupLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserAggregationLoaderClient;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalCategory;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalType;
import com.voxlearning.utopia.service.zone.api.entity.ClazzJournal;
import com.voxlearning.utopia.service.zone.api.entity.StudentInfo;
import com.voxlearning.utopia.service.zone.client.ClazzJournalLoaderClient;
import com.voxlearning.utopia.service.zone.client.PersonalZoneLoaderClient;
import com.voxlearning.washington.cache.WashingtonCacheSystem;

import javax.inject.Inject;
import javax.inject.Named;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 * User: qianxiaozhi
 * Date: 2017/1/5
 * Time: 17:53
 * 头条辅助类
 */
@Named
public class MobileStudentClazzHelper {
    private final AtomicLockManager atomicLockManager = AtomicLockManager.instance();

    @Inject private ClazzJournalLoaderClient clazzJournalLoaderClient;
    @Inject private PrivilegeBufferServiceClient privilegeBufferServiceClient;

    @Inject private DeprecatedGroupLoaderClient groupLoaderClient;
    @Inject private WashingtonCacheSystem washingtonCacheSystem;
    @Inject protected UserAggregationLoaderClient userAggregationLoaderClient;
    @Inject private PersonalZoneLoaderClient personalZoneLoaderClient;

    public Set<Long> getCurrentUserGroupIds(Long userId) {
        List<GroupMapper> groupMappers = groupLoaderClient.loadStudentGroups(userId, false);
        if (CollectionUtils.isEmpty(groupMappers)) return Collections.emptySet();
        return groupMappers.stream().map(GroupMapper::getId).collect(Collectors.toSet());
    }

    /**
     * 获取班级动态
     *
     * @param clazzId 班级id
     */
    public List<ClazzJournal> getClazzJournals(Long clazzId) {
        ;
        String journalsCacheKey = HeadlineCacheKeyGenerator.clazzHeadlineKey(clazzId);
        List<ClazzJournal> clazzJournals = washingtonCacheSystem.CBS.flushable.load(journalsCacheKey);
        if (null == clazzJournals) {
            Set<ClazzJournal.ComplexID> complexIDs = clazzJournalLoaderClient.getClazzJournalLoader().__queryByClazzId(clazzId);
            Set<Long> ids = complexIDs.stream()
                    .filter(p -> Objects.equals(ClazzJournalCategory.APPLICATION_STD.getId(), p.getCategory()) || Objects.equals(ClazzJournalType.BIRTHDAY.getId(), p.getType()))
                    .map(ClazzJournal.ComplexID::getId)
                    .collect(Collectors.toSet());
            if (CollectionUtils.isEmpty(ids)) {
                return Collections.emptyList();
            }

            Map<Long, ClazzJournal> clazzJournalMap = clazzJournalLoaderClient.getClazzJournalLoader().loadClazzJournals(ids);
            if (MapUtils.isEmpty(clazzJournalMap)) {
                return Collections.emptyList();
            }

            //所有的班级动态
            clazzJournals = clazzJournalMap.values()
                    .stream()
                    //要7天以内的记录
                    .filter(cj -> Instant.now().minusSeconds(7 * 24 * 60 * 60).isBefore(Instant.ofEpochMilli(cj.getCreateDatetime().getTime())))
                    .collect(Collectors.toList());
            washingtonCacheSystem.CBS.flushable.set(journalsCacheKey, 30 * 60, clazzJournals);
        }

        //每次读取的时候排序
        if (CollectionUtils.isNotEmpty(clazzJournals)) {
            // (Id自增 比时间更准确，时间可能出现时间相同的数据)
            clazzJournals.sort((c1, c2) -> c2.getId().compareTo(c1.getId()));
        }

        return clazzJournals;
    }

    /**
     * 清空班级动态
     */
    public void clearClazzJournalsCache(Long clazzId) {
        String journalsCacheKey = HeadlineCacheKeyGenerator.clazzHeadlineKey(clazzId);
        washingtonCacheSystem.CBS.flushable.delete(journalsCacheKey);
    }

    /**
     * 刷新过滤的个人成就
     */
    public boolean flushHiddenAchievement(String vid, Long userId) {
        String cacheKey = HeadlineCacheKeyGenerator.achievementHiddenKey(userId);
        String lockKey = cacheKey + "_lock";
        if (StringUtils.isBlank(vid)) {
            return false;
        }
        String[] cjIds = vid.split("_");
        try {
            atomicLockManager.acquireLock(lockKey, 3);

            final Map<Long, Long> hiddenClazzJournalIds = this.getHiddenAchievement(userId);
            List<Long> needRemoveId = new LinkedList<>();
            if (MapUtils.isNotEmpty(hiddenClazzJournalIds)) {
                hiddenClazzJournalIds.forEach((key, value) -> {
                    if (Instant.now().minusSeconds(HeadlineCacheKeyGenerator.CACHE_ONE_WEEK).isAfter(Instant.ofEpochMilli(value))) {
                        needRemoveId.add(key);
                    }
                });
            }

            //删除过期的数据
            if (CollectionUtils.isNotEmpty(needRemoveId)) {
                needRemoveId.forEach(hiddenClazzJournalIds::remove);
            }

            for (String cjId : cjIds) {
                if (!StringUtils.isNumeric(cjId)) {
                    continue;
                }
                hiddenClazzJournalIds.put(Long.parseLong(cjId), System.currentTimeMillis());
            }
            washingtonCacheSystem.CBS.flushable.set(cacheKey, HeadlineCacheKeyGenerator.CACHE_ONE_WEEK, hiddenClazzJournalIds);

        } finally {
            atomicLockManager.releaseLock(lockKey);
        }

        return true;
    }

    /**
     * 获取不需要展示的个人成就
     */
    public Map<Long, Long> getHiddenAchievement(Long userId) {
        String cacheKey = HeadlineCacheKeyGenerator.achievementHiddenKey(userId);
        Map<Long, Long> cacheObject = washingtonCacheSystem.CBS.flushable.load(cacheKey);
        return MapUtils.isNotEmpty(cacheObject) ? cacheObject : new HashMap<>();
    }

    /**
     * 获取班级在同一分组学生数量
     *
     * @param clazzId 班级id
     * @param userId  用户id
     */
    public int getClassStudentCount(Long userId, Long clazzId) {
        int studentCountInClazz = 0;
        String studentCountCacheKey = HeadlineCacheKeyGenerator.studentCountKey(userId);
        CacheObject<Integer> studentCountInClazzCacheObject = washingtonCacheSystem.CBS.flushable.get(studentCountCacheKey);
        if (null == studentCountInClazzCacheObject || null == studentCountInClazzCacheObject.getValue()) {
            studentCountInClazz = userAggregationLoaderClient.loadLinkedStudentsByClazzId(clazzId, userId).size();
            washingtonCacheSystem.CBS.flushable.set(studentCountCacheKey, DateUtils.getCurrentToDayEndSecond(), studentCountInClazz);
        } else {
            studentCountInClazz = studentCountInClazzCacheObject.getValue();
        }
        return studentCountInClazz;
    }

    /**
     * 获取班级同学生日列表
     *
     * @param userId  用户id
     * @param clazzId 班级id
     */
    public List<Map<String, Object>> queryClassmateBirthdayList(Long userId, Long clazzId) {
        List<User> classmates = getCacheClassmates(userId, clazzId);
        List<Map<String, Object>> classmateBirthdayList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(classmates)) {
            //班级生日单排序
            classmates = classmates.stream().filter(u -> !userId.equals(u.getId())).sorted((u1, u2) -> {
                Integer b1 = SafeConverter.toInt(u1.fetchBirthdayFormat("%s%02d%02d"));
                Integer b2 = SafeConverter.toInt(u2.fetchBirthdayFormat("%s%02d%02d"));
                return Integer.compare(b2, b1);
            }).collect(Collectors.toList());
            //组织给客户端的数据格式 每条同学记录为size =3 数组 【index：0 头像 index：1 姓名 index：2 生日（m月d日）】
            classmates.forEach(c -> {
                String birthday = c.fetchBirthdayFormatWithoutYear("%s月%s日");
                if (StringUtils.isBlank(birthday)) {
                    birthday = "未填写";
                }
                Map<String, Object> classMate = new HashMap<>();
                classMate.put("birthday", birthday);
                classMate.put("image", c.fetchImageUrl());
                classMate.put("name", StringUtils.isBlank(c.fetchRealname()) ? c.getId() : c.fetchRealname());
                classMate.put("headWearImg", getHeadWear(c.getId()));
                classmateBirthdayList.add(classMate);
            });
        }

        return classmateBirthdayList;
    }

    /**
     * 获取班级同学 同一个group 缓存一个自然日 第二天失效
     *
     * @param userId  学生用户id
     * @param clazzId 班级id
     */
    public List<User> getCacheClassmates(Long userId, Long clazzId) {
        StringBuilder cacheKeyBuilder = new StringBuilder();
        cacheKeyBuilder.append("STUDENT_APP_HEADLINE_STUDENT_CLASSMATES_");
        cacheKeyBuilder.append(userId);
        cacheKeyBuilder.append("_");
        cacheKeyBuilder.append(clazzId);

        List<User> classmates = washingtonCacheSystem.CBS.flushable.load(cacheKeyBuilder.toString());
        if (classmates == null) {
            classmates = userAggregationLoaderClient.loadLinkedStudentsByClazzId(clazzId, userId);
            washingtonCacheSystem.CBS.flushable.set(cacheKeyBuilder.toString(), DateUtils.getCurrentToDayEndSecond(), classmates);
        }
        return classmates;
    }

    /**
     * 获取当前用户所在班级的同学
     */
    private List<User> getCurrentClassmates(Long clazzId, HeadlineMapperContext context) {
        Map<Long, List<User>> currentClassmates = context.getCurrentClassmates();
        if (MapUtils.isEmpty(currentClassmates)) {
            currentClassmates = new HashMap<>();
            List<User> classmates = getCacheClassmates(context.getCurrentUserId(), clazzId);
            currentClassmates.put(clazzId, classmates);
            context.setCurrentClassmates(currentClassmates);
            return classmates;
        }
        if (CollectionUtils.isEmpty(currentClassmates.get(clazzId))) {
            List<User> classmates = getCacheClassmates(context.getCurrentUserId(), clazzId);
            currentClassmates.put(clazzId, classmates);
            return classmates;
        }

        return currentClassmates.get(clazzId);
    }

    /**
     * 判断是否是同班同学
     */
    public boolean isClassmate(Long clazzId, Long relevantUserId, HeadlineMapperContext context) {
        //如果是本人 则认为是同班同学
        if (Objects.equals(relevantUserId, context.getCurrentUserId())) {
            return true;
        }
        List<User> classmates = getCurrentClassmates(clazzId, context);
        return CollectionUtils.isNotEmpty(classmates) && classmates.stream().anyMatch(c -> Objects.equals(c.getId(), relevantUserId));
    }

    /**
     * 获取头饰
     */
    public String getHeadWear(Long userId) {
        if (userId == null || userId <= 0L) {
            return null;
        }
        StudentInfo studentInfo = personalZoneLoaderClient.getPersonalZoneLoader().loadStudentInfo(userId);
        if (studentInfo == null) {
            return null;
        }
        Privilege headWearPrivilege = privilegeBufferServiceClient.getPrivilegeBuffer().loadById(studentInfo.getHeadWearId());
        return (headWearPrivilege != null) ? headWearPrivilege.getImg() : null;
    }

}
