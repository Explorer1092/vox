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

package com.voxlearning.utopia.service.business.impl.service;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.remote.core.support.ValueWrapperFuture;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.business.api.constant.LearningGoalType;
import com.voxlearning.utopia.service.business.api.AsyncBusinessCacheService;
import com.voxlearning.utopia.service.business.consumer.cache.*;
import com.voxlearning.utopia.service.user.api.entities.Clazz;

import javax.inject.Named;
import java.util.List;
import java.util.Map;

@Named("com.voxlearning.utopia.service.business.impl.service.AsyncBusinessCacheServiceImpl")
@ExposeService(interfaceClass = AsyncBusinessCacheService.class)
public class AsyncBusinessCacheServiceImpl extends SpringContainerSupport implements AsyncBusinessCacheService {

    // flushable
    private MentorCacheManager mentorCacheManager;
    private MentorLatestCacheManager mentorLatestCacheManager;
    private MentorTermEndCacheManager mentorTermEndCacheManager;
    // unflushable
    private AppFinishHomeworkCacheManager appFinishHomeworkCacheManager;
    private InterestingReportCacheManager interestingReportCacheManager;
    private StudentMissionNoticeCacheManager studentMissionNoticeCacheManager;
    private TeacherAdjustClazzRemindCacheManager teacherAdjustClazzRemindCacheManager;
    private TeacherBatchRewardStudentDayCacheManager teacherBatchRewardStudentDayCacheManager;
    private TeacherClazzAlterationCacheManager teacherClazzAlterationCacheManager;
    private TeacherFlowerGratitudeCacheManager teacherFlowerGratitudeCacheManager;
    private TeacherRemindParentDownloadAppManager teacherRemindParentDownloadAppManager;
    // persistence
    private StudentParentRewardCacheManager studentParentRewardCacheManager;
    private StudentWishCreationCacheManager studentWishCreationCacheManager;
    private TeacherFlowerRewardManager teacherFlowerRewardManager;
    private UsaAdventureActivityCacheManager usaAdventureActivityCacheManager;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();

        UtopiaCache flushable = CacheSystem.CBS.getCache("flushable");
        mentorCacheManager = new MentorCacheManager(flushable);
        mentorLatestCacheManager = new MentorLatestCacheManager(flushable);
        mentorTermEndCacheManager = new MentorTermEndCacheManager(flushable);

        UtopiaCache unflushable = CacheSystem.CBS.getCache("unflushable");
        appFinishHomeworkCacheManager = new AppFinishHomeworkCacheManager(unflushable);
        interestingReportCacheManager = new InterestingReportCacheManager(unflushable);
        studentMissionNoticeCacheManager = new StudentMissionNoticeCacheManager(unflushable);
        teacherAdjustClazzRemindCacheManager = new TeacherAdjustClazzRemindCacheManager(unflushable);
        teacherBatchRewardStudentDayCacheManager = new TeacherBatchRewardStudentDayCacheManager(unflushable);
        teacherClazzAlterationCacheManager = new TeacherClazzAlterationCacheManager(unflushable);
        teacherFlowerGratitudeCacheManager = new TeacherFlowerGratitudeCacheManager(unflushable);
        teacherRemindParentDownloadAppManager = new TeacherRemindParentDownloadAppManager(unflushable);

        UtopiaCache persistence = CacheSystem.CBS.getCache("persistence");
        studentParentRewardCacheManager = new StudentParentRewardCacheManager(persistence);
        studentWishCreationCacheManager = new StudentWishCreationCacheManager(persistence);
        teacherFlowerRewardManager = new TeacherFlowerRewardManager(persistence);
        usaAdventureActivityCacheManager = new UsaAdventureActivityCacheManager(persistence);
    }

    @Override
    public AlpsFuture<Boolean> AppFinishHomeworkCacheManager_record(Long teacherId, Clazz clazz, String studentName, Long studentId) {
        boolean b = appFinishHomeworkCacheManager.record(teacherId, clazz, studentName, studentId);
        return new ValueWrapperFuture<>(b);
    }

    @Override
    public AlpsFuture<Map<Clazz, List<Map<String, Object>>>> AppFinishHomeworkCacheManager_loadByTeacherIdAndClazzIds(Long teacherId, List<Clazz> clazzs) {
        Map<Clazz, List<Map<String, Object>>> m = appFinishHomeworkCacheManager.loadByTeacherIdAndClazzIds(teacherId, clazzs);
        return new ValueWrapperFuture<>(m);
    }

    @Override
    public AlpsFuture<Boolean> InterestingReportCacheManager_record(Long userId) {
        interestingReportCacheManager.record(userId);
        return new ValueWrapperFuture<>(true);
    }

    @Override
    public AlpsFuture<Boolean> InterestingReportCacheManager_done(Long userId) {
        boolean b = interestingReportCacheManager.done(userId);
        return new ValueWrapperFuture<>(b);
    }

    @Override
    public AlpsFuture<List<Map<String, Object>>> MentorCacheManager_pureLoad(Long schoolId) {
        List<Map<String, Object>> l = mentorCacheManager.pureLoad(schoolId);
        return new ValueWrapperFuture<>(l);
    }

    @Override
    public AlpsFuture<Boolean> MentorCacheManager_pureAdd(Long schoolId, List<Map<String, Object>> data) {
        mentorCacheManager.pureAdd(schoolId, data);
        return new ValueWrapperFuture<>(true);
    }

    @Override
    public AlpsFuture<Boolean> MentorCacheManager_clean(Long schoolId) {
        mentorCacheManager.clean(schoolId);
        return new ValueWrapperFuture<>(true);
    }

    @Override
    public AlpsFuture<Map<String, Object>> MentorLatestCacheManager_pureLoad(Long teacherId) {
        Map<String, Object> m = mentorLatestCacheManager.pureLoad(teacherId);
        return new ValueWrapperFuture<>(m);
    }

    @Override
    public AlpsFuture<Boolean> MentorLatestCacheManager_pureAdd(Long teacherId, Map<String, Object> data) {
        mentorLatestCacheManager.pureAdd(teacherId, data);
        return new ValueWrapperFuture<>(true);
    }

    @Override
    public AlpsFuture<Boolean> MentorLatestCacheManager_clean(Long teacherId) {
        mentorLatestCacheManager.clean(teacherId);
        return new ValueWrapperFuture<>(true);
    }

    @Override
    public AlpsFuture<Boolean> MentorTermEndCacheManager_clean(Long schoolId) {
        mentorTermEndCacheManager.clean(schoolId);
        return new ValueWrapperFuture<>(true);
    }

    @Override
    public AlpsFuture<Boolean> StudentMissionNoticeCacheManager_record(Long studentId, Long missionId, String wechatNoticeType) {
        studentMissionNoticeCacheManager.record(studentId, missionId, wechatNoticeType);
        return new ValueWrapperFuture<>(true);
    }

    @Override
    public AlpsFuture<Boolean> StudentMissionNoticeCacheManager_sendToday(Long studentId, Long missionId, String wechatNoticeType) {
        boolean b = studentMissionNoticeCacheManager.sendToday(studentId, missionId, wechatNoticeType);
        return new ValueWrapperFuture<>(b);
    }

    @Override
    public AlpsFuture<Boolean> StudentParentRewardCacheManager_showCard(Long studentId) {
        boolean b = studentParentRewardCacheManager.showCard(studentId);
        return new ValueWrapperFuture<>(b);
    }

    @Override
    public AlpsFuture<Boolean> StudentParentRewardCacheManager_turnOff(Long studentId) {
        studentParentRewardCacheManager.turnOff(studentId);
        return new ValueWrapperFuture<>(true);
    }

    @Override
    public AlpsFuture<Boolean> StudentParentRewardCacheManager_turnOn(Long studentId) {
        studentParentRewardCacheManager.turnOn(studentId);
        return new ValueWrapperFuture<>(true);
    }

    @Override
    public AlpsFuture<Boolean> StudentWishCreationCacheManager_record(Long studentId) {
        studentWishCreationCacheManager.record(studentId);
        return new ValueWrapperFuture<>(true);
    }

    @Override
    public AlpsFuture<Boolean> StudentWishCreationCacheManager_wishMadeThisWeek(Long studentId) {
        boolean b = studentWishCreationCacheManager.wishMadeThisWeek(studentId);
        return new ValueWrapperFuture<>(b);
    }

    @Override
    public AlpsFuture<Boolean> TeacherAdjustClazzRemindCacheManager_record(Long teacherId) {
        teacherAdjustClazzRemindCacheManager.record(teacherId);
        return new ValueWrapperFuture<>(true);
    }

    @Override
    public AlpsFuture<Boolean> TeacherAdjustClazzRemindCacheManager_done(Long teacherId) {
        boolean b = teacherAdjustClazzRemindCacheManager.done(teacherId);
        return new ValueWrapperFuture<>(b);
    }

    @Override
    public AlpsFuture<Boolean> TeacherBatchRewardStudentDayCacheManager_record(Long teacherId, String homeworkId) {
        teacherBatchRewardStudentDayCacheManager.record(teacherId, homeworkId);
        return new ValueWrapperFuture<>(true);
    }

    @Override
    public AlpsFuture<Boolean> TeacherBatchRewardStudentDayCacheManager_useToday(Long teacherId, String homeworkId) {
        boolean b = teacherBatchRewardStudentDayCacheManager.useToday(teacherId, homeworkId);
        return new ValueWrapperFuture<>(b);
    }

    @Override
    public AlpsFuture<Boolean> TeacherClazzAlterationCacheManager_record(Long teacherId) {
        teacherClazzAlterationCacheManager.record(teacherId);
        return new ValueWrapperFuture<>(true);
    }

    @Override
    public AlpsFuture<Boolean> TeacherClazzAlterationCacheManager_needPopup(Long teacherId) {
        boolean b = teacherClazzAlterationCacheManager.needPopup(teacherId);
        return new ValueWrapperFuture<>(b);
    }

    @Override
    public AlpsFuture<Boolean> TeacherFlowerGratitudeCacheManager_gratitude(Long teacherId) {
        teacherFlowerGratitudeCacheManager.gratitude(teacherId);
        return new ValueWrapperFuture<>(true);
    }

    @Override
    public AlpsFuture<Boolean> TeacherFlowerGratitudeCacheManager_hasGratitude(Long teacherId) {
        boolean b = teacherFlowerGratitudeCacheManager.hasGratitude(teacherId);
        return new ValueWrapperFuture<>(b);
    }

    @Override
    public AlpsFuture<Boolean> TeacherFlowerRewardManager_receiveReward(Long teacherId) {
        teacherFlowerRewardManager.receiveReward(teacherId);
        return new ValueWrapperFuture<>(true);
    }

    @Override
    public AlpsFuture<Boolean> TeacherFlowerRewardManager_hasReceivedIntegral(Long teacherId) {
        boolean b = teacherFlowerRewardManager.hasReceivedIntegral(teacherId);
        return new ValueWrapperFuture<>(b);
    }

    @Override
    public AlpsFuture<Boolean> TeacherRemindParentDownloadAppManager_record(Long teacherId) {
        teacherRemindParentDownloadAppManager.record(teacherId);
        return new ValueWrapperFuture<>(true);
    }

    @Override
    public AlpsFuture<Boolean> TeacherRemindParentDownloadAppManager_done(Long teacherId) {
        boolean b = teacherRemindParentDownloadAppManager.done(teacherId);
        return new ValueWrapperFuture<>(b);
    }

    @Override
    public AlpsFuture<Boolean> UsaAdventureActivityCacheManager_addRecord(Long userId, LearningGoalType type) {
        boolean b = usaAdventureActivityCacheManager.addRecord(userId, type);
        return new ValueWrapperFuture<>(b);
    }

    @Override
    public AlpsFuture<LearningGoalType> UsaAdventureActivityCacheManager_loadRecord(Long userId) {
        LearningGoalType t = usaAdventureActivityCacheManager.loadRecord(userId);
        return new ValueWrapperFuture<>(t);
    }
}
