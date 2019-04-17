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

package com.voxlearning.utopia.service.afenti.impl.service;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.remote.core.support.ValueWrapperFuture;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.service.afenti.api.AsyncAfentiCacheService;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiPromptType;
import com.voxlearning.utopia.service.afenti.api.constant.AfentiRankType;
import com.voxlearning.utopia.service.afenti.api.constant.PurchaseType;
import com.voxlearning.utopia.service.afenti.base.cache.managers.*;
import com.voxlearning.utopia.service.afenti.base.cache.managers.activity.AfentiUserLoginRewardCacheManager;
import com.voxlearning.utopia.service.afenti.cache.AfentiCache;
import com.voxlearning.utopia.service.parentreward.api.constant.ParentRewardType;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import lombok.Getter;

import javax.inject.Named;
import java.util.*;

@Named("com.voxlearning.utopia.service.afenti.impl.service.AsyncAfentiCacheServiceImpl")
@ExposeService(interfaceClass = AsyncAfentiCacheService.class)
public class AsyncAfentiCacheServiceImpl extends SpringContainerSupport implements AsyncAfentiCacheService {

    // flushable
    private LearningRankListCacheManager learningRankListCacheManager;
    private UserLearningRankCacheManager userLearningRankCacheManager;

    // persistence\
    private AfentiUserLoginRewardCacheManager afentiUserLoginRewardCacheManager;
    @Getter private AfentiCourseBuyerCountManager afentiCourseBuyerCountManager;
    @Getter private AfentiReviewRankFootprintCacheManager afentiReviewRankFootprintCacheManager;
    @Getter private AfentiReviewParrentRewardCacheManager afentiReviewParrentRewardCacheManager;
    @Getter private AfentiReviewFamilyJoinCacheManager afentiReviewFamilyJoinCacheManager;

    // unflushable
    private AfentiClickLikedCacheManager afentiClickLikedCacheManager;
    private AfentiInviteUserRecordCacheManager afentiInviteUserRecordCacheManager;
    private AfentiKnowledgePointCacheManager afentiKnowledgePointCacheManager;
    private AfentiLastWeekUsedCacheManager afentiLastWeekUsedCacheManager;
    private AfentiLoginCacheManager afentiLoginCacheManager;
    private AfentiPaidSuccessClassmatesCacheManager afentiPaidSuccessClassmatesCacheManager;
    private AfentiPromptCacheManager afentiPromptCacheManager;
    private AfentiRankLikedSummaryCacheManager afentiRankLikedSummaryCacheManager;
    private AfentiSuccessInviteRecordCacheManager afentiSuccessInviteRecordCacheManager;
    private SelfLearningActionCacheManager selfLearningActionCacheManager;
    private SubmitResultActionCacheManager submitResultActionCacheManager;
    private AfentiParentRewardCacheManager afentiParentRewardCacheManager;

    // storage
    private AfentiLoginCountCacheManager afentiLoginCountCacheManager;
    private AfentiPurchaseInfosCacheManager afentiPurchaseInfosCacheManager;
    private AfentiRewardInfosCacheManager afentiRewardInfosCacheManager;
    @Getter
    private AfentiAchievementMaxLevelCacheManager afentiAchievementMaxLevelCacheManager;
    @Getter
    private AfentiVideoCourseViewRecordCacheManager afentiVideoCourseViewRecordCacheManager;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();

        learningRankListCacheManager = new LearningRankListCacheManager(AfentiCache.getAfentiCache());
        userLearningRankCacheManager = new UserLearningRankCacheManager(AfentiCache.getAfentiCache());

        UtopiaCache persistence = CacheSystem.CBS.getCache("persistence");
        afentiUserLoginRewardCacheManager = new AfentiUserLoginRewardCacheManager(persistence);
        afentiCourseBuyerCountManager = new AfentiCourseBuyerCountManager(persistence);
        afentiReviewRankFootprintCacheManager = new AfentiReviewRankFootprintCacheManager(persistence);
        afentiReviewParrentRewardCacheManager = new AfentiReviewParrentRewardCacheManager(persistence);
        afentiReviewFamilyJoinCacheManager = new AfentiReviewFamilyJoinCacheManager(persistence);

        UtopiaCache unflushable = CacheSystem.CBS.getCache("unflushable");
        afentiClickLikedCacheManager = new AfentiClickLikedCacheManager(unflushable);
        afentiInviteUserRecordCacheManager = new AfentiInviteUserRecordCacheManager(unflushable);
        afentiKnowledgePointCacheManager = new AfentiKnowledgePointCacheManager(unflushable);
        afentiLastWeekUsedCacheManager = new AfentiLastWeekUsedCacheManager(unflushable);
        afentiLoginCacheManager = new AfentiLoginCacheManager(unflushable);
        afentiPaidSuccessClassmatesCacheManager = new AfentiPaidSuccessClassmatesCacheManager(unflushable);
        afentiPromptCacheManager = new AfentiPromptCacheManager(unflushable);
        afentiRankLikedSummaryCacheManager = new AfentiRankLikedSummaryCacheManager(unflushable);
        afentiSuccessInviteRecordCacheManager = new AfentiSuccessInviteRecordCacheManager(unflushable);
        selfLearningActionCacheManager = new SelfLearningActionCacheManager(unflushable);
        submitResultActionCacheManager = new SubmitResultActionCacheManager(unflushable);
        afentiParentRewardCacheManager = new AfentiParentRewardCacheManager(unflushable);

        UtopiaCache storage = CacheSystem.CBS.getCache("storage");
        afentiLoginCountCacheManager = new AfentiLoginCountCacheManager(storage);
        afentiPurchaseInfosCacheManager = new AfentiPurchaseInfosCacheManager(storage);
        afentiRewardInfosCacheManager = new AfentiRewardInfosCacheManager(storage);
        afentiAchievementMaxLevelCacheManager = new AfentiAchievementMaxLevelCacheManager(storage);
        afentiVideoCourseViewRecordCacheManager = new AfentiVideoCourseViewRecordCacheManager(storage);
    }

    @Override
    public AlpsFuture<Boolean> AfentiUserLoginRewardCacheManager_addRecord(StudentDetail studentDetail, Subject subject) {
        boolean b = afentiUserLoginRewardCacheManager.addRecord(studentDetail, subject);
        return new ValueWrapperFuture<>(b);
    }

    @Override
    public AlpsFuture<Boolean> AfentiUserLoginRewardCacheManager_existRecord(StudentDetail studentDetail, Subject subject) {
        boolean b = afentiUserLoginRewardCacheManager.existRecord(studentDetail, subject);
        return new ValueWrapperFuture<>(b);
    }

    @Override
    public AlpsFuture<Boolean> AfentiParentRewardCacheManager_addRecord(Long studentId, ParentRewardType rewardType) {
        boolean b = afentiParentRewardCacheManager.addRecord(studentId, rewardType);
        return new ValueWrapperFuture<>(b);
    }

    @Override
    public AlpsFuture<Boolean> AfentiParentRewardCacheManager_existRecord(Long studentId, ParentRewardType rewardType) {
        boolean b = afentiParentRewardCacheManager.existRecord(studentId, rewardType);
        return new ValueWrapperFuture<>(b);
    }

    @Override
    public AlpsFuture<Set<Integer>> AfentiUserLoginRewardCacheManager_loadRecords(StudentDetail studentDetail, Subject subject) {
        Set<Integer> s = afentiUserLoginRewardCacheManager.loadRecords(studentDetail, subject);
        return new ValueWrapperFuture<>(s);
    }

    @Override
    public AlpsFuture<Boolean> AfentiClickLikedCacheManager_clickLiked(StudentDetail clickUser, StudentDetail likedUser, Subject subject, AfentiRankType afentiRankType) {
        afentiClickLikedCacheManager.clickLiked(clickUser, likedUser, subject, afentiRankType);
        return new ValueWrapperFuture<>(true);
    }

    @Override
    public AlpsFuture<Set<Long>> AfentiClickLikedCacheManager_loadTodayClickLikedSet(StudentDetail clickUser, Subject subject, AfentiRankType afentiRankType) {
        Set<Long> s = afentiClickLikedCacheManager.loadTodayClickLikedSet(clickUser, subject, afentiRankType);
        return new ValueWrapperFuture<>(s);
    }

    @Override
    public AlpsFuture<Boolean> AfentiInviteUserRecordCacheManager_setRecord(Long sendInvitationUserId, Long invitedUserId, Subject subject) {
        afentiInviteUserRecordCacheManager.setRecord(sendInvitationUserId, invitedUserId, subject);
        return new ValueWrapperFuture<>(true);
    }

    @Override
    public AlpsFuture<Set<Long>> AfentiInviteUserRecordCacheManager_loadRecord(Long sendInvitationUserId, Subject subject) {
        Set<Long> s = afentiInviteUserRecordCacheManager.loadRecord(sendInvitationUserId, subject);
        return new ValueWrapperFuture<>(s);
    }

    @Override
    public AlpsFuture<Boolean> AfentiKnowledgePointCacheManager_sended(Long studentId, Subject subject, String kp) {
        boolean b = afentiKnowledgePointCacheManager.sended(studentId, subject, kp);
        return new ValueWrapperFuture<>(b);
    }

    @Override
    public AlpsFuture<Boolean> AfentiKnowledgePointCacheManager_record(Long studentId, Subject subject, String kp) {
        afentiKnowledgePointCacheManager.record(studentId, subject, kp);
        return new ValueWrapperFuture<>(true);
    }

    @Override
    public AlpsFuture<Boolean> AfentiLastWeekUsedCacheManager_record(Long userId, Subject subject) {
        afentiLastWeekUsedCacheManager.record(userId, subject);
        return new ValueWrapperFuture<>(true);
    }

    @Override
    public AlpsFuture<Boolean> AfentiLastWeekUsedCacheManager_fetch(Long userId, Subject subject) {
        boolean b = afentiLastWeekUsedCacheManager.fetch(userId, subject);
        return new ValueWrapperFuture<>(b);
    }

    @Override
    public AlpsFuture<Boolean> AfentiLoginCacheManager_notified(Long studentId, Subject subject) {
        boolean b = afentiLoginCacheManager.notified(studentId, subject);
        return new ValueWrapperFuture<>(b);
    }

    @Override
    public AlpsFuture<Integer> AfentiLoginCountCacheManager_fetchCurrentCount(Long studentId, Subject subject) {
        int i = afentiLoginCountCacheManager.fetchCurrentCount(studentId, subject);
        return new ValueWrapperFuture<>(i);
    }

    @Override
    public AlpsFuture<Boolean> AfentiLoginCountCacheManager_updateCurrentCount(Long studentId, Subject subject, int count) {
        afentiLoginCountCacheManager.updateCurrentCount(studentId, subject, count);
        return new ValueWrapperFuture<>(true);
    }

    @Override
    public AlpsFuture<Boolean> AfentiPaidSuccessClassmatesCacheManager_addPaidSuccessMsg(Long paySuccessUserId, Collection<Long> classmateIds, Subject subject) {
        afentiPaidSuccessClassmatesCacheManager.addPaidSuccessMsg(paySuccessUserId, classmateIds, subject);
        return new ValueWrapperFuture<>(true);
    }

    @Override
    public AlpsFuture<Set<Long>> AfentiPaidSuccessClassmatesCacheManager_loadPaidClassmateUserIds(Long userId, Subject subject) {
        Set<Long> s = afentiPaidSuccessClassmatesCacheManager.loadPaidClassmateUserIds(userId, subject);
        return new ValueWrapperFuture<>(s);
    }

    @Override
    public AlpsFuture<Boolean> AfentiPromptCacheManager_record(Long studentId, Subject subject, AfentiPromptType type) {
        afentiPromptCacheManager.record(studentId, subject, type);
        return new ValueWrapperFuture<>(true);
    }

    @Override
    public AlpsFuture<Boolean> AfentiPromptCacheManager_record(Collection<Long> userIds, Subject subject, AfentiPromptType type) {
        afentiPromptCacheManager.record(userIds, subject, type);
        return new ValueWrapperFuture<>(true);
    }

    @Override
    public AlpsFuture<Boolean> AfentiPromptCacheManager_reset(Long studentId, Subject subject, AfentiPromptType type) {
        afentiPromptCacheManager.reset(studentId, subject, type);
        return new ValueWrapperFuture<>(true);
    }

    @Override
    public AlpsFuture<Map<AfentiPromptType, Boolean>> AfentiPromptCacheManager_fetch(Long studentId, Subject subject) {
        Map<AfentiPromptType, Boolean> m = afentiPromptCacheManager.fetch(studentId, subject);
        return new ValueWrapperFuture<>(m);
    }

    @Override
    public AlpsFuture<Boolean> AfentiPurchaseInfosCacheManager_addRecord(StudentDetail studentDetail, PurchaseType purchaseType, Date createDate) {
        afentiPurchaseInfosCacheManager.addRecord(studentDetail, purchaseType, createDate);
        return new ValueWrapperFuture<>(true);
    }

    @Override
    public AlpsFuture<List<Map<String, Object>>> AfentiPurchaseInfosCacheManager_getRecords(StudentDetail studentDetail) {
        List<Map<String, Object>> l = afentiPurchaseInfosCacheManager.getRecords(studentDetail);
        return new ValueWrapperFuture<>(l);
    }

    @Override
    public AlpsFuture<Boolean> AfentiRankLikedSummaryCacheManager_addLiked(AfentiRankType afentiRankType, StudentDetail likedUser) {
        afentiRankLikedSummaryCacheManager.addLiked(afentiRankType, likedUser);
        return new ValueWrapperFuture<>(true);
    }

    @Override
    public AlpsFuture<Map<Long, Integer>> AfentiRankLikedSummaryCacheManager_loadSchoolRank(StudentDetail studentDetail, Date date) {
        Map<Long, Integer> m = afentiRankLikedSummaryCacheManager.loadSchoolRank(studentDetail, date);
        return new ValueWrapperFuture<>(m);
    }

    @Override
    public AlpsFuture<Map<Long, Integer>> AfentiRankLikedSummaryCacheManager_loadNationRank(Date date) {
        Map<Long, Integer> m = afentiRankLikedSummaryCacheManager.loadNationRank(date);
        return new ValueWrapperFuture<>(m);
    }

    @Override
    public AlpsFuture<Boolean> AfentiRewardInfosCacheManager_addRecord(StudentDetail studentDetail, Integer integral) {
        afentiRewardInfosCacheManager.addRecord(studentDetail, integral);
        return new ValueWrapperFuture<>(true);
    }

    @Override
    public AlpsFuture<List<Map<String, Object>>> AfentiRewardInfosCacheManager_getRecords(StudentDetail studentDetail) {
        List<Map<String, Object>> l = afentiRewardInfosCacheManager.getRecords(studentDetail);
        return new ValueWrapperFuture<>(l);
    }

    @Override
    public AlpsFuture<Boolean> AfentiSuccessInviteRecordCacheManager_addRecords(List<Long> sendInvitationUserId, Long invitedUserId, Subject subject) {
        afentiSuccessInviteRecordCacheManager.addRecords(sendInvitationUserId, invitedUserId, subject);
        return new ValueWrapperFuture<>(true);
    }

    @Override
    public AlpsFuture<Set<Long>> AfentiSuccessInviteRecordCacheManager_loadAndReset(Long sendInvitationUserId, Subject subject) {
        Set<Long> s = afentiSuccessInviteRecordCacheManager.loadAndReset(sendInvitationUserId, subject);
        return new ValueWrapperFuture<>(s);
    }

    @Override
    public AlpsFuture<Boolean> LearningRankListCacheManager_addNationalRank(List<Map<String, Object>> rankList, Subject subject, Date calculateDate) {
        learningRankListCacheManager.addNationalRank(rankList, subject, calculateDate);
        return new ValueWrapperFuture<>(true);
    }

    @Override
    public AlpsFuture<Boolean> LearningRankListCacheManager_addSchoolRank(List<Map<String, Object>> rankList, Long schoolId, Subject subject, Date calculateDate) {
        learningRankListCacheManager.addSchoolRank(rankList, schoolId, subject, calculateDate);
        return new ValueWrapperFuture<>(true);
    }

    @Override
    public AlpsFuture<String> LearningRankListCacheManager_generateKey(AfentiRankType afentiRankType, Long schoolId, Subject subject, Date date) {
        String s = learningRankListCacheManager.generateKey(afentiRankType, schoolId, subject, date);
        return new ValueWrapperFuture<>(s);
    }

    @Override
    public AlpsFuture<Boolean> SelfLearningActionCacheManager_sended(Long studentId) {
        boolean b = selfLearningActionCacheManager.sended(studentId);
        return new ValueWrapperFuture<>(b);
    }

    @Override
    public AlpsFuture<Boolean> SubmitResultActionCacheManager_sended(Long studentId, Subject subject) {
        boolean b = submitResultActionCacheManager.sended(studentId, subject);
        return new ValueWrapperFuture<>(b);
    }

    @Override
    public AlpsFuture<Boolean> UserLearningRankCacheManager_setNationalRank(Subject subject, Date date, Map<Long, Integer> list) {
        userLearningRankCacheManager.setNationalRank(subject, date, list);
        return new ValueWrapperFuture<>(true);
    }

    @Override
    public AlpsFuture<Boolean> UserLearningRankCacheManager_setSchoolRank(Subject subject, Long schoolId, Date date, Map<Long, Integer> list) {
        userLearningRankCacheManager.setSchoolRank(subject, schoolId, date, list);
        return new ValueWrapperFuture<>(true);
    }

    @Override
    public AlpsFuture<Date> UserLearningRankCacheManager_lastWeekCalculateDate() {
        Date d = userLearningRankCacheManager.lastWeekCalculateDate();
        return new ValueWrapperFuture<>(d);
    }

    @Override
    public AlpsFuture<String> UserLearningRankCacheManager_generateKey(AfentiRankType afentiRankType, Long schoolId, Subject subject, Date date) {
        String s = userLearningRankCacheManager.generateKey(afentiRankType, schoolId, subject, date);
        return new ValueWrapperFuture<>(s);
    }

    @Override
    public AlpsFuture<Boolean> AfentiCourseBuyerCountManager_upsertBuyerCount() {
        afentiCourseBuyerCountManager.upsertBuyerCount();
        return new ValueWrapperFuture<>(true);
    }
}
