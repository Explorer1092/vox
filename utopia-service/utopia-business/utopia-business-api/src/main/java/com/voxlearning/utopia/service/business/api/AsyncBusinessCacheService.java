package com.voxlearning.utopia.service.business.api;

import com.voxlearning.alps.annotation.remote.Async;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.utopia.business.api.constant.LearningGoalType;
import com.voxlearning.utopia.service.user.api.entities.Clazz;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "2017.03.07")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
@CyclopsMonitor("utopia")
public interface AsyncBusinessCacheService {

    // ========================================================================
    // AppFinishHomeworkCacheManager
    // ========================================================================

    @Async
    AlpsFuture<Boolean> AppFinishHomeworkCacheManager_record(Long teacherId, Clazz clazz, String studentName, Long studentId);

    @Async
    AlpsFuture<Map<Clazz, List<Map<String, Object>>>> AppFinishHomeworkCacheManager_loadByTeacherIdAndClazzIds(Long teacherId, List<Clazz> clazzs);

    // ========================================================================
    // InterestingReportCacheManager
    // ========================================================================

    @Async
    AlpsFuture<Boolean> InterestingReportCacheManager_record(Long userId);

    @Async
    AlpsFuture<Boolean> InterestingReportCacheManager_done(Long userId);

    // ========================================================================
    // MentorCacheManager
    // ========================================================================

    @Async
    AlpsFuture<List<Map<String, Object>>> MentorCacheManager_pureLoad(Long schoolId);

    @Async
    AlpsFuture<Boolean> MentorCacheManager_pureAdd(Long schoolId, List<Map<String, Object>> data);

    @Async
    AlpsFuture<Boolean> MentorCacheManager_clean(Long schoolId);

    // ========================================================================
    // MentorLatestCacheManager
    // ========================================================================

    @Async
    AlpsFuture<Map<String, Object>> MentorLatestCacheManager_pureLoad(Long teacherId);

    @Async
    AlpsFuture<Boolean> MentorLatestCacheManager_pureAdd(Long teacherId, Map<String, Object> data);

    @Async
    AlpsFuture<Boolean> MentorLatestCacheManager_clean(Long teacherId);

    // ========================================================================
    // MentorTermEndCacheManager
    // ========================================================================

    @Async
    AlpsFuture<Boolean> MentorTermEndCacheManager_clean(Long schoolId);

    // ========================================================================
    // StudentMissionNoticeCacheManager
    // ========================================================================

    @Async
    AlpsFuture<Boolean> StudentMissionNoticeCacheManager_record(Long studentId, Long missionId, String wechatNoticeType);

    @Async
    AlpsFuture<Boolean> StudentMissionNoticeCacheManager_sendToday(Long studentId, Long missionId, String wechatNoticeType);

    // ========================================================================
    // StudentParentRewardCacheManager
    // ========================================================================

    @Async
    AlpsFuture<Boolean> StudentParentRewardCacheManager_showCard(Long studentId);

    @Async
    AlpsFuture<Boolean> StudentParentRewardCacheManager_turnOff(Long studentId);

    @Async
    AlpsFuture<Boolean> StudentParentRewardCacheManager_turnOn(Long studentId);

    // ========================================================================
    // StudentWishCreationCacheManager
    // ========================================================================

    @Async
    AlpsFuture<Boolean> StudentWishCreationCacheManager_record(Long studentId);

    @Async
    AlpsFuture<Boolean> StudentWishCreationCacheManager_wishMadeThisWeek(Long studentId);

    // ========================================================================
    // TeacherAdjustClazzRemindCacheManager
    // ========================================================================

    @Async
    AlpsFuture<Boolean> TeacherAdjustClazzRemindCacheManager_record(Long teacherId);

    @Async
    AlpsFuture<Boolean> TeacherAdjustClazzRemindCacheManager_done(Long teacherId);

    // ========================================================================
    // TeacherBatchRewardStudentDayCacheManager
    // ========================================================================

    @Async
    AlpsFuture<Boolean> TeacherBatchRewardStudentDayCacheManager_record(Long teacherId, String homeworkId);

    @Async
    AlpsFuture<Boolean> TeacherBatchRewardStudentDayCacheManager_useToday(Long teacherId, String homeworkId);

    // ========================================================================
    // TeacherClazzAlterationCacheManager
    // ========================================================================

    @Async
    AlpsFuture<Boolean> TeacherClazzAlterationCacheManager_record(Long teacherId);

    @Async
    AlpsFuture<Boolean> TeacherClazzAlterationCacheManager_needPopup(Long teacherId);

    // ========================================================================
    // TeacherFlowerGratitudeCacheManager
    // ========================================================================

    @Async
    AlpsFuture<Boolean> TeacherFlowerGratitudeCacheManager_gratitude(Long teacherId);

    @Async
    AlpsFuture<Boolean> TeacherFlowerGratitudeCacheManager_hasGratitude(Long teacherId);

    // ========================================================================
    // TeacherFlowerRewardManager
    // ========================================================================

    @Async
    AlpsFuture<Boolean> TeacherFlowerRewardManager_receiveReward(Long teacherId);

    @Async
    AlpsFuture<Boolean> TeacherFlowerRewardManager_hasReceivedIntegral(Long teacherId);

    // ========================================================================
    // TeacherRemindParentDownloadAppManager
    // ========================================================================

    @Async
    AlpsFuture<Boolean> TeacherRemindParentDownloadAppManager_record(Long teacherId);

    @Async
    AlpsFuture<Boolean> TeacherRemindParentDownloadAppManager_done(Long teacherId);

    // ========================================================================
    // UsaAdventureActivityCacheManager
    // ========================================================================

    @Async
    AlpsFuture<Boolean> UsaAdventureActivityCacheManager_addRecord(Long userId, LearningGoalType type);

    @Async
    AlpsFuture<LearningGoalType> UsaAdventureActivityCacheManager_loadRecord(Long userId);
}
