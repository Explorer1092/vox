package com.voxlearning.utopia.service.afenti.api;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.Async;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiPromptType;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiRankType;
import com.voxlearning.utopia.service.afenti.api.constant.PurchaseType;
import com.voxlearning.utopia.service.parentreward.api.constant.ParentRewardType;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;

import java.util.*;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "2017.03.05")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface AsyncAfentiCacheService {

    // ========================================================================
    // AfentiUserLoginRewardCacheManager
    // ========================================================================

    @Async
    AlpsFuture<Boolean> AfentiUserLoginRewardCacheManager_addRecord(StudentDetail studentDetail, Subject subject);

    @Async
    AlpsFuture<Boolean> AfentiUserLoginRewardCacheManager_existRecord(StudentDetail studentDetail, Subject subject);

    @Async
    AlpsFuture<Boolean> AfentiParentRewardCacheManager_addRecord(Long studentId, ParentRewardType rewardType);

    @Async
    AlpsFuture<Boolean> AfentiParentRewardCacheManager_existRecord(Long studentId, ParentRewardType rewardType);

    @Async
    AlpsFuture<Set<Integer>> AfentiUserLoginRewardCacheManager_loadRecords(StudentDetail studentDetail, Subject subject);

    // ========================================================================
    // AfentiClickLikedCacheManager
    // ========================================================================

    @Async
    AlpsFuture<Boolean> AfentiClickLikedCacheManager_clickLiked(StudentDetail clickUser, StudentDetail likedUser, Subject subject, AfentiRankType afentiRankType);

    @Async
    AlpsFuture<Set<Long>> AfentiClickLikedCacheManager_loadTodayClickLikedSet(StudentDetail clickUser, Subject subject, AfentiRankType afentiRankType);

    // ========================================================================
    // AfentiInviteUserRecordCacheManager
    // ========================================================================

    @Async
    AlpsFuture<Boolean> AfentiInviteUserRecordCacheManager_setRecord(Long sendInvitationUserId, Long invitedUserId, Subject subject);

    @Async
    AlpsFuture<Set<Long>> AfentiInviteUserRecordCacheManager_loadRecord(Long sendInvitationUserId, Subject subject);

    // ========================================================================
    // AfentiKnowledgePointCacheManager
    // ========================================================================

    @Async
    AlpsFuture<Boolean> AfentiKnowledgePointCacheManager_sended(Long studentId, Subject subject, String kp);

    @Async
    AlpsFuture<Boolean> AfentiKnowledgePointCacheManager_record(Long studentId, Subject subject, String kp);

    // ========================================================================
    // AfentiLastWeekUsedCacheManager
    // ========================================================================

    @Async
    AlpsFuture<Boolean> AfentiLastWeekUsedCacheManager_record(Long userId, Subject subject);

    @Async
    AlpsFuture<Boolean> AfentiLastWeekUsedCacheManager_fetch(Long userId, Subject subject);

    // ========================================================================
    // AfentiLoginCacheManager
    // ========================================================================

    @Async
    AlpsFuture<Boolean> AfentiLoginCacheManager_notified(Long studentId, Subject subject);

    // ========================================================================
    // AfentiLoginCountCacheManager
    // ========================================================================

    @Async
    AlpsFuture<Integer> AfentiLoginCountCacheManager_fetchCurrentCount(Long studentId, Subject subject);

    @Async
    AlpsFuture<Boolean> AfentiLoginCountCacheManager_updateCurrentCount(Long studentId, Subject subject, int count);

    // ========================================================================
    // AfentiPaidSuccessClassmatesCacheManager
    // ========================================================================

    @Async
    AlpsFuture<Boolean> AfentiPaidSuccessClassmatesCacheManager_addPaidSuccessMsg(Long paySuccessUserId, Collection<Long> classmateIds, Subject subject);

    @Async
    AlpsFuture<Set<Long>> AfentiPaidSuccessClassmatesCacheManager_loadPaidClassmateUserIds(Long userId, Subject subject);

    // ========================================================================
    // AfentiPromptCacheManager
    // ========================================================================

    @Async
    AlpsFuture<Boolean> AfentiPromptCacheManager_record(Long studentId, Subject subject, AfentiPromptType type);

    @Async
    AlpsFuture<Boolean> AfentiPromptCacheManager_record(Collection<Long> userIds, Subject subject, AfentiPromptType type);

    @Async
    AlpsFuture<Boolean> AfentiPromptCacheManager_reset(Long studentId, Subject subject, AfentiPromptType type);

    @Async
    AlpsFuture<Map<AfentiPromptType, Boolean>> AfentiPromptCacheManager_fetch(Long studentId, Subject subject);

    // ========================================================================
    // AfentiPurchaseInfosCacheManager
    // ========================================================================

    @Async
    AlpsFuture<Boolean> AfentiPurchaseInfosCacheManager_addRecord(StudentDetail studentDetail, PurchaseType purchaseType, Date createDate);

    @Async
    AlpsFuture<List<Map<String, Object>>> AfentiPurchaseInfosCacheManager_getRecords(StudentDetail studentDetail);

    // ========================================================================
    // AfentiRankLikedSummaryCacheManager
    // ========================================================================

    @Async
    AlpsFuture<Boolean> AfentiRankLikedSummaryCacheManager_addLiked(AfentiRankType afentiRankType, StudentDetail likedUser);

    @Async
    AlpsFuture<Map<Long, Integer>> AfentiRankLikedSummaryCacheManager_loadSchoolRank(StudentDetail studentDetail, Date date);

    @Async
    AlpsFuture<Map<Long, Integer>> AfentiRankLikedSummaryCacheManager_loadNationRank(Date date);

    // ========================================================================
    // AfentiRewardInfosCacheManager
    // ========================================================================

    @Async
    AlpsFuture<Boolean> AfentiRewardInfosCacheManager_addRecord(StudentDetail studentDetail, Integer integral);

    @Async
    AlpsFuture<List<Map<String, Object>>> AfentiRewardInfosCacheManager_getRecords(StudentDetail studentDetail);

    // ========================================================================
    // AfentiSuccessInviteRecordCacheManager
    // ========================================================================

    @Async
    AlpsFuture<Boolean> AfentiSuccessInviteRecordCacheManager_addRecords(List<Long> sendInvitationUserId, Long invitedUserId, Subject subject);

    @Async
    AlpsFuture<Set<Long>> AfentiSuccessInviteRecordCacheManager_loadAndReset(Long sendInvitationUserId, Subject subject);

    // ========================================================================
    // LearningRankListCacheManager
    // ========================================================================

    @Async
    AlpsFuture<Boolean> LearningRankListCacheManager_addNationalRank(List<Map<String, Object>> rankList, Subject subject, Date calculateDate);

    @Async
    AlpsFuture<Boolean> LearningRankListCacheManager_addSchoolRank(List<Map<String, Object>> rankList, Long schoolId, Subject subject, Date calculateDate);

    @Async
    AlpsFuture<String> LearningRankListCacheManager_generateKey(AfentiRankType afentiRankType, Long schoolId, Subject subject, Date date);

    // ========================================================================
    // SelfLearningActionCacheManager
    // ========================================================================

    @Async
    AlpsFuture<Boolean> SelfLearningActionCacheManager_sended(Long studentId);

    // ========================================================================
    // SubmitResultActionCacheManager
    // ========================================================================

    @Async
    AlpsFuture<Boolean> SubmitResultActionCacheManager_sended(Long studentId, Subject subject);

    // ========================================================================
    // UserLearningRankCacheManager
    // ========================================================================

    @Async
    AlpsFuture<Boolean> UserLearningRankCacheManager_setNationalRank(Subject subject, Date date, Map<Long, Integer> list);

    @Async
    AlpsFuture<Boolean> UserLearningRankCacheManager_setSchoolRank(Subject subject, Long schoolId, Date date, Map<Long, Integer> list);

    @Async
    AlpsFuture<Date> UserLearningRankCacheManager_lastWeekCalculateDate();

    @Async
    AlpsFuture<String> UserLearningRankCacheManager_generateKey(AfentiRankType afentiRankType, Long schoolId, Subject subject, Date date);

    // ========================================================================
    // AfentiCourseBuyerCountManager
    // ========================================================================

    @Async
    AlpsFuture<Boolean> AfentiCourseBuyerCountManager_upsertBuyerCount();
}
