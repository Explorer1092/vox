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

package com.voxlearning.utopia.service.business.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.business.api.MiscService;
import com.voxlearning.utopia.entity.misc.IntegralActivity;
import com.voxlearning.utopia.entity.misc.IntegralActivityRule;
import com.voxlearning.utopia.entity.misc.UgcAnswers;
import com.voxlearning.utopia.entity.questionsurvey.QuestionSurveyResult;
import com.voxlearning.utopia.service.business.api.entity.ActivityData;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class MiscServiceClient implements MiscService {

    @ImportService(interfaceClass = MiscService.class)
    private MiscService remoteReference;

    @Override
    public MapMessage activeStudyCraftCard(Long cardKey, Long userId) {
        return remoteReference.activeStudyCraftCard(cardKey, userId);
    }

    @Override
    public Boolean termBeginHasAdjustClazz(Long teacherId) {
        return remoteReference.termBeginHasAdjustClazz(teacherId);
    }

    @Override
    public void termBeginRecordAdjustClazz(Long teacherId) {
        remoteReference.termBeginRecordAdjustClazz(teacherId);
    }

    @Override
    public void bindInvitedTeacherMobile(Long userId) {
        remoteReference.bindInvitedTeacherMobile(userId);
    }

    @Override
    public void addActivityData(ActivityData activityData) {
        remoteReference.addActivityData(activityData);
    }

    @Override
    public void reCover(Long activityId) {
        remoteReference.reCover(activityId);
    }

    @Override
    public MapMessage saveTeacherSourceCampaignAward(Long userId, String source, int awardStatus) {
        return remoteReference.saveTeacherSourceCampaignAward(userId, source, awardStatus);
    }

    @Override
    @Deprecated
    public Long addIntegralActivity(IntegralActivity activity) {
        return remoteReference.addIntegralActivity(activity);
    }

    @Override
    @Deprecated
    public void updateIntegralActivityStatus(Long activityId, Integer status) {
        remoteReference.updateIntegralActivityStatus(activityId, status);
    }

    @Override
    @Deprecated
    public void updateIntegralActivity(Long activityId, IntegralActivity activity) {
        remoteReference.updateIntegralActivity(activityId, activity);
    }

    @Override
    @Deprecated
    public Long addIntegralActivityRule(IntegralActivityRule rule, int department) {
        return remoteReference.addIntegralActivityRule(rule, department);
    }

    @Override
    @Deprecated
    public void updateIntegralActivityRule(IntegralActivityRule rule) {
        remoteReference.updateIntegralActivityRule(rule);
    }

    @Override
    @Deprecated
    public void disableIntegralActivityRule(Long ruleId) {
        remoteReference.disableIntegralActivityRule(ruleId);
    }

    @Override
    public MapMessage saveUgcAnswer(User user, Long recordId, List<Map<String, Object>> answerMapList, UgcAnswers.Source source) {
        return remoteReference.saveUgcAnswer(user, recordId, answerMapList, source);
    }

    @Override
    @Deprecated
    public String saveQuestionSurveyResult(QuestionSurveyResult questionSurvey) {
        return remoteReference.saveQuestionSurveyResult(questionSurvey);
    }

    @Override
    @Deprecated
    public List<QuestionSurveyResult> loadQuestionSurveyResult(String activityId) {
        return remoteReference.loadQuestionSurveyResult(activityId);
    }

    @Override
    public MapMessage sendFakeAppealMessage(Long teacherId) {
        return remoteReference.sendFakeAppealMessage(teacherId);
    }

    @Override
    public MapMessage sendFakeNoticeMessage(Long teacherId, Collection<Long> receivers) {
        return remoteReference.sendFakeNoticeMessage(teacherId, receivers);
    }

    @Override
    public MapMessage loadLuckyMan() {
        return remoteReference.loadLuckyMan();
    }

    public MapMessage bingo(Long workNo) {
        return remoteReference.bingo(workNo);
    }

    public MapMessage recordNewYearWish(User user, String wishContent) {
        return remoteReference.recordNewYearWish(user, wishContent);
    }

    public MapMessage getInterestingReportAward(User user) {
        return remoteReference.getInterestingReportAward(user);
    }

    public Boolean showScholarshipEnter(Teacher teacher) {
        return remoteReference.showScholarshipEnter(teacher);
    }
}
