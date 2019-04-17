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

package com.voxlearning.utopia.service.newhomework.consumer;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.homework.api.mapper.WechatHomeworkMapper;
import com.voxlearning.utopia.service.newhomework.api.NewHomeworkPartLoader;
import com.voxlearning.utopia.service.newhomework.api.entity.NewHomeworkFinishRewardInParentApp;
import com.voxlearning.utopia.service.newhomework.api.entity.NewHomeworkStudyMaster;
import com.voxlearning.utopia.service.newhomework.api.entity.OfflineListenPaper;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkApp;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;

import javax.inject.Inject;
import java.util.*;

/**
 * @author xuesong.zhang
 * @since 2016/8/3
 */
public class NewHomeworkPartLoaderClient implements NewHomeworkPartLoader {

    @ImportService(interfaceClass = NewHomeworkPartLoader.class)
    private NewHomeworkPartLoader remoteReference;

    @Inject private NewHomeworkLoaderClient newHomeworkLoaderClient;
    @Inject private NewHomeworkReportServiceClient newHomeworkReportServiceClient;
    @Inject private NewHomeworkResultLoaderClient newHomeworkResultLoaderClient;

    /**
     * 学生某次作业中基础应用练习的报告
     *
     * @param homeworkId 作业Id
     * @param userId     学生Id
     * @return Map <String, Map<String, Object>>
     */
    public Map<String, Map<String, Object>> getBasicAppData(String homeworkId, Long userId) {
        if (StringUtils.isBlank(homeworkId) || userId == null) {
            return Collections.emptyMap();
        }

        NewHomework newHomework = newHomeworkLoaderClient.load(homeworkId);
        NewHomeworkResult newHomeworkResult = newHomeworkResultLoaderClient.loadNewHomeworkResult(newHomework.toLocation(), userId, true);

        NewHomeworkPracticeContent newHomeworkPracticeContent = newHomework.findPracticeContents().get(ObjectiveConfigType.BASIC_APP);
        if (Objects.isNull(newHomeworkPracticeContent) || CollectionUtils.isEmpty(newHomeworkPracticeContent.getApps())) {
            return Collections.emptyMap();
        }
        List<NewHomeworkApp> apps = newHomeworkPracticeContent.getApps();
        boolean flag = MapUtils.isEmpty(newHomeworkResult.getPractices()) || Objects.isNull(newHomeworkResult.getPractices().get(ObjectiveConfigType.BASIC_APP));
        return newHomeworkReportServiceClient.lessonDataForBasicApp(newHomeworkResult, apps, flag, ObjectiveConfigType.BASIC_APP);
    }

    @Override
    public Map<String, List<String>> getBasicAppVoiceUrl(NewHomework newHomework, Long userId) {
        return remoteReference.getBasicAppVoiceUrl(newHomework, userId);
    }

    @Override
    public NewHomeworkFinishRewardInParentApp getRewardInParentApp(Long userId) {
        NewHomeworkFinishRewardInParentApp rewardInParentApp = remoteReference.getRewardInParentApp(userId);
        if (rewardInParentApp == null || MapUtils.isEmpty(rewardInParentApp.getNotReceivedRewardMap())) {
            return rewardInParentApp;
        }
        boolean needReload = false;
        //把过期的学豆奖励处理掉
        Map<String, NewHomeworkFinishRewardInParentApp.RewardDetail> notReceivedRewardMap = rewardInParentApp.getNotReceivedRewardMap();
        for (String homeworkId : notReceivedRewardMap.keySet()) {
            NewHomeworkFinishRewardInParentApp.RewardDetail detail = notReceivedRewardMap.get(homeworkId);
            if (detail.getExpire().before(new Date())) {
                needReload = true;
                updateTimeoutInteger(userId, homeworkId);
            }
        }
        if (needReload) {
            return remoteReference.getRewardInParentApp(userId);
        } else {
            return rewardInParentApp;
        }
    }

    @Override
    public MapMessage updateTimeoutInteger(Long userId, String homeworkId) {
        return remoteReference.updateTimeoutInteger(userId, homeworkId);
    }

    @Override
    public MapMessage updateBeforeReceivedInteger(Long userId, String homeworkId) {
        return remoteReference.updateBeforeReceivedInteger(userId, homeworkId);
    }

    @Override
    public MapMessage getStudentHomeworkProgress(Long studentId, Subject subject, String homeworkId) {
        return remoteReference.getStudentHomeworkProgress(studentId, subject, homeworkId);
    }

    public Map<String, Integer> getRewardCountInParentApp(Long userId) {
        if (userId == null) {
            return Collections.emptyMap();
        }
        NewHomeworkFinishRewardInParentApp rewardInParentApp = getRewardInParentApp(userId);
        if (rewardInParentApp == null || rewardInParentApp.getNotReceivedRewardMap() == null) {
            return Collections.emptyMap();
        }
        Map<String, NewHomeworkFinishRewardInParentApp.RewardDetail> notReceivedRewardMap = rewardInParentApp.getNotReceivedRewardMap();
        Map<String, Integer> result = new HashMap<>();
        for (String homeworkId : notReceivedRewardMap.keySet()) {
            NewHomeworkFinishRewardInParentApp.RewardDetail detail = notReceivedRewardMap.get(homeworkId);
            if (detail.getExpire().before(new Date())) {
                //把过期的学豆奖励处理掉
                updateTimeoutInteger(userId, homeworkId);
            } else {
                result.put(homeworkId, detail.getRewardCount());
            }
        }
        return result;
    }

    public Map<String, NewHomeworkStudyMaster> getNewHomeworkStudyMasterMap(Collection<String> newHomeworkIds) {
        if (CollectionUtils.isEmpty(newHomeworkIds)) {
            return Collections.emptyMap();
        }
        return remoteReference.getNewHomeworkStudyMasterMap(newHomeworkIds);
    }

    /**
     * 用于查询指定时间段内的组下面的作业
     *
     * @param groupIds  班组ids
     * @param startDate 起始时间
     * @param endDate   结束时间
     * @return List
     */
    public List<NewHomework.Location> loadNewHomeworkByClazzGroupId(Collection<Long> groupIds, Date startDate, Date endDate) {
        if (CollectionUtils.isEmpty(groupIds)) {
            return Collections.emptyList();
        }
        return remoteReference.loadNewHomeworkByClazzGroupId(groupIds, startDate, endDate);
    }

    public Map<String, OfflineListenPaper> findOfflineListenPaperByIds(Collection<String> ids) {
        ids = CollectionUtils.toLinkedHashSet(ids);
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        return remoteReference.findOfflineListenPaperByIds(ids);
    }

    public MapMessage getTeacherHomeworkProgress(Long teacherId, Subject subject) {
        return remoteReference.getTeacherHomeworkProgress(teacherId, subject);
    }

    @Override
    public List<WechatHomeworkMapper> getAllHomeworkMapper(List<NewHomework.Location> newHomeworkList, Long studentId) {
        return null;
    }
}