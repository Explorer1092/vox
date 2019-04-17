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

package com.voxlearning.utopia.service.zone.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.spi.common.JsonStringSerializer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.zone.api.ClazzRecordService;
import com.voxlearning.utopia.service.zone.api.mapper.classrecord.ClazzRecordCardMapper;
import com.voxlearning.utopia.service.zone.api.mapper.classrecord.ClazzRecordHwMapper;
import com.voxlearning.utopia.service.zone.api.mapper.classrecord.RecordLikeMapper;
import com.voxlearning.utopia.service.zone.api.mapper.classrecord.RecordSoundShareMapper;
import com.voxlearning.utopia.service.zone.cache.ZoneCache;
import com.voxlearning.utopia.service.zone.support.ClazzRecordCacheKeyGenerator;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 * User: qianxiaozhi
 * Date: 2017/2/27
 * Time: 11:51
 */
public class ClazzRecordServiceClient {

    @Getter
    @ImportService(interfaceClass = ClazzRecordService.class)
    private ClazzRecordService clazzRecordService;

    /**
     * 获取用户自己的作业纪录
     */
    public List<ClazzRecordHwMapper> loadUserHomeworkRecords(Long userId, List<NewHomework.Location> homework, int page, int pageSize) {
        if (userId == null || userId <= 0L || CollectionUtils.isEmpty(homework)) {
            return Collections.emptyList();
        }

        int startIndex = pageSize * (page - 1) >= homework.size() ? homework.size() : pageSize * (page - 1);
        int endIndex = (pageSize * page) >= homework.size() ? homework.size() : (pageSize * page);

        int loadIndex = startIndex > 0 ? startIndex - 1 : startIndex;
        List<NewHomework.Location> pageHomework = homework.subList(loadIndex, endIndex);

        Map<NewHomework.Location, ClazzRecordHwMapper> userHomeworkRecords = ZoneCache.getCache()
                .<NewHomework.Location, ClazzRecordHwMapper>createCacheValueLoader()
                .keyGenerator(source -> ClazzRecordHwMapper.ck_user_homework(userId, source.getId()))
                .externalLoader(missedSources -> clazzRecordService.loadUserHomeworkRecords(userId, missedSources))
                .keys(pageHomework)
                .loads()
                .loadsMissed()
                .expiration(1800)
                .write()
                .getAndResortResult();

        List<ClazzRecordHwMapper> retMapper = new ArrayList<>();

        for (int index = startIndex; index < endIndex; index++) {
            NewHomework.Location curHomework = homework.get(index);
            NewHomework.Location nextHomework = index + 1 >= (homework.size()) ? null : homework.get(index + 1);
            ClazzRecordHwMapper curMapper = userHomeworkRecords.get(curHomework);
            ClazzRecordHwMapper nextMapper = userHomeworkRecords.get(nextHomework);
            if (curMapper == null) continue;

            if (nextMapper != null && curMapper.getScore() != null && nextMapper.getScore() != null
                    && curMapper.getScore() > nextMapper.getScore()) {
                curMapper.setImproved(true);
            }
            retMapper.add(curMapper);
        }

        return retMapper;
    }

    /**
     * 获取当前学期的作业列表 (分group)
     *
     * @param groupId 分组id
     */
    public List<NewHomework.Location> loadHomeworkList(Long groupId) {
        return clazzRecordService.loadHomeworkList(groupId);
    }

    /**
     * 记录语音分享
     *
     * @param mapper 语音对象
     */
    public void recordSoundShare(RecordSoundShareMapper mapper) {
        clazzRecordService.recordSoundShare(mapper);
    }

    /**
     * 获取班级语音分享
     *
     * @param clazzId 班级id
     * @param userId  用户id
     */
    public List<RecordSoundShareMapper> queryRecordSoundShare(Long clazzId, Long userId) {
        return clazzRecordService.queryRecordSoundShare(clazzId, userId);
    }

    /**
     * 获取当前学期的作业列表
     *
     * @param groupIds 分组ids
     */
    public List<NewHomework.Location> loadHomeworkList(Collection<Long> groupIds) {
        return clazzRecordService.loadHomeworkList(groupIds);

    }

    /**
     * 点赞记录
     *
     * @param mapper 点赞对象
     */
    public void like(RecordLikeMapper mapper) {
        clazzRecordService.like(mapper);
    }

    /**
     * 获取班级点赞信息
     *
     * @param mapper 点赞对象
     */
    public List<RecordLikeMapper> queryRecordLike(RecordLikeMapper mapper) {
        return clazzRecordService.queryRecordLike(mapper);
    }

    /**
     * 专注 top3
     *
     * @param homeworkId 作业id
     */
    public List<ClazzRecordCardMapper> queryTop3FocusMapper(String homeworkId) {

        String cacheKey = ClazzRecordCacheKeyGenerator.ck_focus_top3(homeworkId);
        List<ClazzRecordCardMapper> result = ZoneCache.getCache().load(cacheKey);
        if (result == null) {
            result = clazzRecordService.queryTop3FocusMapper(homeworkId);
            ZoneCache.getCache().add(cacheKey, DateUtils.getCurrentToDayEndSecond(), result);
        }
        return result;
    }

    /**
     * 学霸 top3
     *
     * @param homeworkId 作业id
     */
    public List<ClazzRecordCardMapper> queryTop3StudyMasterMapper(String homeworkId) {

        String cacheKey = ClazzRecordCacheKeyGenerator.ck_sm_top3(homeworkId);
        List<ClazzRecordCardMapper> result = ZoneCache.getCache().load(cacheKey);
        if (result == null) {
            result = clazzRecordService.queryTop3StudyMasterMapper(homeworkId);
            ZoneCache.getCache().add(cacheKey, DateUtils.getCurrentToDayEndSecond(), result);
        }
        return result;
    }

    /**
     * 是否已鼓励过
     *
     * @param mapper 鼓励信息
     */
    public boolean recordLiked(RecordLikeMapper mapper) {
        return clazzRecordService.recordLiked(mapper);
    }

    /**
     * 获取当此作业 平均分
     *
     * @param homeworkId 作业id
     */
    public Double getAvgScore(String homeworkId) {
        Double avgScore = ZoneCache.loadHomeworkAvgScore(homeworkId);
        if (avgScore != null) {
            return avgScore;
        }

        avgScore = clazzRecordService.getAvgScore(homeworkId);
        ZoneCache.saveHomeworkAvgScore(homeworkId, avgScore);
        return avgScore;
    }

    /**
     * 满分top3
     *
     * @param groupIds 分组id
     * @param clazzId  班级id
     */
    public List<ClazzRecordCardMapper> queryFullMarksTop3(List<Long> groupIds, Long clazzId) {
        // sort the groupIds and use the hash value as part of the fullmark key
        List<Long> sorted = groupIds.stream().sorted((o1, o2) -> Long.compare(o2, o1)).collect(Collectors.toList());
        String fullMarkKey = StringUtils.join(clazzId, "_", JsonStringSerializer.getInstance().serialize(sorted).hashCode());
        String cacheKey = ClazzRecordCacheKeyGenerator.ck_fullmarks_top3(fullMarkKey);
        List<ClazzRecordCardMapper> result = ZoneCache.getCache().load(cacheKey);
        if (result == null) {
            result = clazzRecordService.queryFullMarksTop3(groupIds, clazzId);
            ZoneCache.getCache().add(cacheKey, DateUtils.getCurrentToDayEndSecond(), result);
        }
        return result;
    }

    /**
     * 友谊top3
     */
    public List<ClazzRecordCardMapper> queryFriendTop3(Long clazzId, List<Long> classmates) {
        // sort the classmates and use the hash value as part of the friendShip key
        List<Long> sorted = classmates.stream().sorted((o1, o2) -> Long.compare(o2, o1)).collect(Collectors.toList());
        String friendShipKey = StringUtils.join(clazzId, "_", JsonStringSerializer.getInstance().serialize(sorted).hashCode());
        String cacheKey = ClazzRecordCacheKeyGenerator.ck_friendship_top3(friendShipKey);
        List<ClazzRecordCardMapper> result = ZoneCache.getCache().load(cacheKey);
        if (result == null) {
            result = clazzRecordService.queryFriendShipTop3(clazzId, classmates);
            ZoneCache.getCache().add(cacheKey, DateUtils.getCurrentToDayEndSecond(), result);
        }
        return result;
    }

    /**
     * 满分周最佳
     *
     * @param groupIds 分组ids
     * @param clazzId  班级id
     */
    public ClazzRecordCardMapper queryWeekTopFullMarks(List<Long> groupIds, Long clazzId) {
        // sort the groupIds and use the hash value as part of the fullmark key
        List<Long> sorted = groupIds.stream().sorted((o1, o2) -> Long.compare(o2, o1)).collect(Collectors.toList());
        String fullMarkWeekKey = StringUtils.join(clazzId, "_", JsonStringSerializer.getInstance().serialize(sorted).hashCode());
        String cacheKey = ClazzRecordCacheKeyGenerator.ck_fullmarks_week(fullMarkWeekKey);
        ClazzRecordCardMapper result = ZoneCache.getCache().load(cacheKey);
        if (result == null) {
            result = clazzRecordService.queryWeekTopFullMarks(groupIds, clazzId);
            ZoneCache.getCache().add(cacheKey, DateUtils.getCurrentToDayEndSecond(), result);
        }
        return result;
    }

    /**
     * 友谊周最佳
     *
     * @param clazzId    班级id
     * @param classmates 同学ids
     */
    public ClazzRecordCardMapper queryWeekTopFriendShip(Long clazzId, List<Long> classmates) {
        // sort the classmates and use the hash value as part of the friendShip key
        List<Long> sorted = classmates.stream().sorted((o1, o2) -> Long.compare(o2, o1)).collect(Collectors.toList());
        String friendShipWeekKey = StringUtils.join(clazzId, "_", JsonStringSerializer.getInstance().serialize(sorted).hashCode());
        String cacheKey = ClazzRecordCacheKeyGenerator.ck_friendship_week(friendShipWeekKey);
        ClazzRecordCardMapper result = ZoneCache.getCache().load(cacheKey);
        if (result == null) {
            result = clazzRecordService.queryWeekTopFriendShip(clazzId, classmates);
            ZoneCache.getCache().add(cacheKey, DateUtils.getCurrentToDayEndSecond(), result);
        }
        return result;
    }

}
