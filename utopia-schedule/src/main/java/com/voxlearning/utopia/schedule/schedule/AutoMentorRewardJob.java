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

package com.voxlearning.utopia.schedule.schedule;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ProgressTotalWork;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.api.constant.MentorCategory;
import com.voxlearning.utopia.api.constant.MentorLevel;
import com.voxlearning.utopia.entity.ucenter.MentorHistory;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.business.api.entity.MentorRewardHistory;
import com.voxlearning.utopia.service.business.cache.BusinessCache;
import com.voxlearning.utopia.service.business.client.AsyncBusinessCacheServiceClient;
import com.voxlearning.utopia.service.business.consumer.BusinessTeacherServiceClient;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.mentor.client.MentorHistoryServiceClient;
import com.voxlearning.utopia.service.mentor.client.MentorServiceClient;
import com.voxlearning.utopia.service.user.api.UserIntegralService;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.client.AsyncTeacherServiceClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.api.constant.MentorCategory.MENTOR_NEW_ST_COUNT;
import static com.voxlearning.utopia.api.constant.MentorCategory.MENTOR_TERM_END;
import static com.voxlearning.utopia.api.legacy.MemcachedKeyConstants.MENTOR_SYSTEM_TEACHER_MENTOR_LIST;

/**
 * @author xiaopeng.yang
 * @since 2015/5/28
 */
@Named
@ScheduledJobDefinition(
        jobName = "MENTOR奖励任务",
        jobDescription = "每天5:30运行一次",
        disabled = {Mode.UNIT_TEST, Mode.STAGING},
        cronExpression = "0 30 5 * * ?"
)
@ProgressTotalWork(100)
public class AutoMentorRewardJob extends ScheduledJobWithJournalSupport {

    @Inject private AsyncBusinessCacheServiceClient asyncBusinessCacheServiceClient;
    @Inject private AsyncTeacherServiceClient asyncTeacherServiceClient;
    @Inject private MentorHistoryServiceClient mentorHistoryServiceClient;
    @Inject private BusinessTeacherServiceClient businessTeacherServiceClient;
    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private UserLoaderClient userLoaderClient;
    @Inject private MentorServiceClient mentorServiceClient;

    @ImportService(interfaceClass = UserIntegralService.class) private UserIntegralService userIntegralService;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {
        //查10天内 所有非认证的帮助类型
        List<MentorHistory> histories = mentorHistoryServiceClient.getMentorHistoryService()
                .loadAutoMentorRewardJobData()
                .getUninterruptibly();
        if (CollectionUtils.isEmpty(histories)) {
            jobJournalLogger.log("无记录");
            return;
        }

        progressMonitor.worked(5);

        ISimpleProgressMonitor monitor = progressMonitor.subTask(95, histories.size());
        //执行扫描 并确定是否发奖
        for (MentorHistory history : histories) {
            try {
                //30天之内认证的老师  看新增学生数
                Teacher mentee = teacherLoaderClient.loadTeacher(history.getMenteeId());
                if (mentee == null || AuthenticationState.SUCCESS != mentee.fetchCertificationState()) {
                    jobJournalLogger.log("mentee is not auth, mentee is {}", history.getMenteeId());
                    continue;
                }
                Teacher mentor = teacherLoaderClient.loadTeacher(history.getMentorId());
                if (mentor == null || AuthenticationState.SUCCESS != mentor.fetchCertificationState()) {
                    jobJournalLogger.log("mentor is not auth, mentor is {}", history.getMentorId());
                    continue;
                }
                switch (MentorCategory.valueOf(history.getMentorCategory())) {
                    case MENTOR_NEW_ST_COUNT:
                        //确立mentor关系时的级别
                        MentorLevel agoLevel = MentorLevel.valueOf(history.getMentorLevel());
                        int currentCount = businessTeacherServiceClient
                                .studentsFinishedHomeworkCount(mentee.getId(), mentee.getCreateTime());
                        //分别取对应的级别
                        int currentLevel = getLevel(currentCount);
                        if (currentLevel <= agoLevel.getLevel()) {
                            continue;
                        }
                        int cha = currentLevel - agoLevel.getLevel();
                        if (cha > 0) {
                            if (!validate300(mentor.getId(), mentee.getId(), MENTOR_NEW_ST_COUNT)) {
                                jobJournalLogger.log("same mentor reward amount gte 300, mentor is {}, mentee is {}, category is {}", mentor.getId(), mentee.getId(), MENTOR_NEW_ST_COUNT.name());
                                continue;
                            }
                            //发奖
                            IntegralHistory integralHistory = new IntegralHistory(history.getMentorId(), IntegralType.老师帮助老师邀请学生奖励_产品平台, cha * 100 * 10);
                            integralHistory.setComment("老师帮助老师邀请学生奖励园丁豆");
                            MapMessage message = userIntegralService.changeIntegral(userLoaderClient.loadUser(history.getMentorId()), integralHistory);
                            if (!message.isSuccess()) {
                                jobJournalLogger.log("add integral error, mentor is {}", history.getMentorId());
                                continue;
                            }
                            if (currentLevel == 4) {
                                //超过90人 结束帮助关系
                                mentorServiceClient.getRemoteReference()
                                        .setMentorHistorySuccess(history.getId())
                                        .awaitUninterruptibly();
                            } else {
                                //更改level
                                mentorServiceClient.getRemoteReference()
                                        .changeMentorHistoryLevel(history.getId(), getMentorLevel(currentLevel, MENTOR_NEW_ST_COUNT))
                                        .awaitUninterruptibly();
                            }
                            //记录奖励历史
                            MentorRewardHistory rewardHistory = MentorRewardHistory.newInstance(mentor.getId());
                            rewardHistory.setMenteeId(mentee.getId());
                            rewardHistory.setMentorCategory(MENTOR_NEW_ST_COUNT.name());
                            rewardHistory.setAmount(cha * 100);
                            rewardHistory.setRewardCategory("GOLD");
                            mentorServiceClient.getRemoteReference().persistMentorRewardHistory(rewardHistory).awaitUninterruptibly();
                            //清除缓存
                            asyncBusinessCacheServiceClient.getAsyncBusinessCacheService()
                                    .MentorLatestCacheManager_clean(history.getMentorId())
                                    .awaitUninterruptibly();
                            School menteeSchool = asyncTeacherServiceClient.getAsyncTeacherService()
                                    .loadTeacherSchool(mentee.getId())
                                    .getUninterruptibly();
                            if (menteeSchool != null) {
                                asyncBusinessCacheServiceClient.getAsyncBusinessCacheService()
                                        .MentorCacheManager_clean(menteeSchool.getId())
                                        .awaitUninterruptibly();
                            }
                            String key = CacheKeyGenerator.generateCacheKey(MENTOR_SYSTEM_TEACHER_MENTOR_LIST, null, new Object[]{history.getMentorId()});
                            BusinessCache.getBusinessCache().delete(key);
                        }
                        break;
                    case MENTOR_TERM_END://期末回馈计划奖励
                        //确立mentor关系时的级别
                        MentorLevel agoTeLevel = MentorLevel.valueOf(history.getMentorLevel());
                        int currentTeCount = businessTeacherServiceClient
                                .studentsFinishedHomeworkCount(mentee.getId(), DateUtils.stringToDate("2015-05-26 00:00:00"));
                        //分别取对应的级别
                        int currentTeLevel = getLevel(currentTeCount);
                        if (currentTeLevel <= agoTeLevel.getLevel()) {
                            continue;
                        }
                        int teCha = currentTeLevel - agoTeLevel.getLevel();
                        if (teCha > 0) {
                            if (!validate300(mentor.getId(), mentee.getId(), MENTOR_TERM_END)) {
                                jobJournalLogger.log("same mentor reward amount gte 300, mentor is {}, mentee is {}, category is {}", mentor.getId(), mentee.getId(), MENTOR_NEW_ST_COUNT.name());
                                continue;
                            }
                            //发奖
                            IntegralHistory integralHistory = new IntegralHistory(history.getMentorId(), IntegralType.老师帮助老师邀请学生奖励_产品平台, teCha * 100 * 10);
                            integralHistory.setComment("老师帮助老师邀请学生奖励园丁豆");
                            MapMessage message = userIntegralService.changeIntegral(userLoaderClient.loadUser(history.getMentorId()), integralHistory);
                            if (!message.isSuccess()) {
                                jobJournalLogger.log("add integral error, mentor is {}", history.getMentorId());
                                continue;
                            }
                            if (currentTeLevel == 4) {
                                //超过90人 结束帮助关系
                                mentorServiceClient.getRemoteReference()
                                        .setMentorHistorySuccess(history.getId())
                                        .awaitUninterruptibly();
                            } else {
                                //更改level
                                mentorServiceClient.getRemoteReference()
                                        .changeMentorHistoryLevel(history.getId(), getMentorLevel(currentTeLevel, MENTOR_TERM_END))
                                        .awaitUninterruptibly();
                            }
                            //记录奖励历史
                            MentorRewardHistory rewardHistory = MentorRewardHistory.newInstance(mentor.getId());
                            rewardHistory.setMenteeId(mentee.getId());
                            rewardHistory.setMentorCategory(MENTOR_TERM_END.name());
                            rewardHistory.setAmount(teCha * 100);
                            rewardHistory.setRewardCategory("GOLD");
                            mentorServiceClient.getRemoteReference().persistMentorRewardHistory(rewardHistory).awaitUninterruptibly();
                            //清除缓存
                            asyncBusinessCacheServiceClient.getAsyncBusinessCacheService()
                                    .MentorLatestCacheManager_clean(history.getMentorId())
                                    .awaitUninterruptibly();
                            School menteeSchool = asyncTeacherServiceClient.getAsyncTeacherService()
                                    .loadTeacherSchool(mentee.getId())
                                    .getUninterruptibly();
                            if (menteeSchool != null) {
                                asyncBusinessCacheServiceClient.getAsyncBusinessCacheService()
                                        .MentorTermEndCacheManager_clean(menteeSchool.getId())
                                        .awaitUninterruptibly();
                            }
                            String key = CacheKeyGenerator.generateCacheKey(MENTOR_SYSTEM_TEACHER_MENTOR_LIST, null, new Object[]{history.getMentorId()});
                            BusinessCache.getBusinessCache().delete(key);
                        }
                        break;
                    default:
                        jobJournalLogger.log("error mentor category, category is {}", history.getMentorCategory());
                }
            } finally {
                monitor.worked(1);
            }
        }
        progressMonitor.done();
    }

    private MentorLevel getMentorLevel(int currentLevel, MentorCategory category) {
        if (category == MENTOR_NEW_ST_COUNT) {
            if (currentLevel == 1) {
                return MentorLevel.MENTOR_NEW_ST_COUNT_LEVEL_ONE;
            } else if (currentLevel == 2) {
                return MentorLevel.MENTOR_NEW_ST_COUNT_LEVEL_TWO;
            } else if (currentLevel == 3) {
                return MentorLevel.MENTOR_NEW_ST_COUNT_LEVEL_THREE;
            }
        } else if (category == MENTOR_TERM_END) {
            if (currentLevel == 1) {
                return MentorLevel.MENTOR_TERM_END_LEVEL_ONE;
            } else if (currentLevel == 2) {
                return MentorLevel.MENTOR_TERM_END_LEVEL_TWO;
            } else if (currentLevel == 3) {
                return MentorLevel.MENTOR_TERM_END_LEVEL_THREE;
            }
        }
        return null;
    }

    private int getLevel(int count) {
        int level = 1;
        if (count >= 30 && count < 60) {
            level = 2;
        } else if (count >= 60 && count < 90) {
            level = 3;
        } else if (count >= 90) {
            level = 4;
        }
        return level;
    }

    private boolean validate300(Long mentorId, Long menteeId, MentorCategory category) {
        //这里有一个300金币的限制  一个导师对同一个被帮助的老师最多得300
        List<MentorRewardHistory> historyList = mentorServiceClient.getRemoteReference()
                .findMentorRewardHistoriesByMentorId(mentorId)
                .getUninterruptibly();
        if (CollectionUtils.isEmpty(historyList)) {
            return true;
        }
        List<MentorRewardHistory> sameList = historyList.stream()
                .filter(h -> Objects.equals(h.getMenteeId(), menteeId) && category.name().equals(h.getMentorCategory()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(sameList)) {
            return true;
        }
        int rewardAmount = 0;
        for (MentorRewardHistory his : sameList) {
            rewardAmount = rewardAmount + his.getAmount();
        }
        if (rewardAmount >= 300) {
            return false;
        }
        return true;
    }
}