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

package com.voxlearning.utopia.business.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
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
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "3.0.STABLE")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
@CyclopsMonitor("utopia")
public interface MiscService extends IPingable {

    // ========================================================================
    // MiscVendorService
    // ========================================================================

    // 激活口袋学社
    MapMessage activeStudyCraftCard(Long cardKey, Long userId);

//    void persistUserCampaignAward(User user, CampaignAward campaignAward, String appKey);

    void bindInvitedTeacherMobile(Long userId);

    void addActivityData(ActivityData activityData);

    void reCover(Long activityId);

    // 此方法中学在调用
    Boolean termBeginHasAdjustClazz(Long teacherId);

    // 此方法中学在调用
    void termBeginRecordAdjustClazz(Long teacherId);

    MapMessage saveTeacherSourceCampaignAward(Long userId, String source, int awardStatus);

    // ========================================================================
    // 积分活动相关的方法 By Wyc 2016-01-18
    // ========================================================================

    /**
     * 新增一条积分活动记录
     *
     * @param activity 积分活动实体
     */
    @Deprecated
    Long addIntegralActivity(IntegralActivity activity);

    /**
     * 变更积分活动状态
     *
     * @param activityId 积分活动ID
     * @param status     变更后的状态
     */
    @Deprecated
    void updateIntegralActivityStatus(Long activityId, Integer status);

    /**
     * 更新积分活动信息
     *
     * @param activityId 积分活动的ID
     * @param activity   积分活动实体
     */
    @Deprecated
    void updateIntegralActivity(Long activityId, IntegralActivity activity);

    /**
     * 增加一条积分活动规则
     *
     * @param rule 积分活动规则实体
     */
    @Deprecated
    Long addIntegralActivityRule(IntegralActivityRule rule, int department);

    /**
     * 更新一条积分活动规则
     *
     * @param rule 积分活动规则实体
     */
    @Deprecated
    void updateIntegralActivityRule(IntegralActivityRule rule);


    /**
     * 仅停用一条规则
     *
     * @param ruleId 规则实体ID
     */
    @Deprecated
    void disableIntegralActivityRule(Long ruleId);

    /**
     * 新版UGC收集 用户答题记录保存
     *
     * @param user          用户
     * @param recordId      活动ID
     * @param answerMapList 答题详情
     * @return MapMessage
     */
    MapMessage saveUgcAnswer(User user, Long recordId, List<Map<String, Object>> answerMapList, UgcAnswers.Source source);

    /**
     * 保存问卷星回调函数的结果
     *
     * @param questionSurvey 问卷星实体
     */
    @Deprecated
    String saveQuestionSurveyResult(QuestionSurveyResult questionSurvey);

    /**
     * 查询问卷的数据
     *
     * @param activityId 问卷id
     * @return 问卷的结果
     */
    @Deprecated
    List<QuestionSurveyResult> loadQuestionSurveyResult(String activityId);

    // 发送老师判假申诉消息
    MapMessage sendFakeAppealMessage(Long teacherId);

    /**
     * 老师判假后通知换班申请的相关人
     *
     * @param receivers 接收老师ID
     */
    MapMessage sendFakeNoticeMessage(Long teacherId, Collection<Long> receivers);

    // 年会获取抽奖接口
    MapMessage loadLuckyMan();

    MapMessage bingo(Long workNo);

    MapMessage recordNewYearWish(User user, String wishContent);

    MapMessage getInterestingReportAward(User user);

    Boolean showScholarshipEnter(Teacher teacher);
}
