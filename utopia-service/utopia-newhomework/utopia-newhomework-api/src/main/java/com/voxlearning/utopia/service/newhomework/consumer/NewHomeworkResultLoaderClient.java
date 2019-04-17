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

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.newhomework.api.NewHomeworkResultLoader;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkResultExtendedInfo;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.User;

import java.util.*;

public class NewHomeworkResultLoaderClient implements NewHomeworkResultLoader {

    @ImportService(interfaceClass = NewHomeworkResultLoader.class)
    private NewHomeworkResultLoader remoteReference;

    public Map<String, NewHomeworkResult> loads(Collection<String> ids, boolean needAnswer) {
        return remoteReference.loads(ids, needAnswer);
    }

    @Override
    public NewHomeworkResult loadNewHomeworkResult(NewHomework.Location location, Long userId, boolean needAnswer) {
        if (location == null || Objects.isNull(userId)) {
            return null;
        }
        return remoteReference.loadNewHomeworkResult(location, userId, needAnswer);
    }

    @Override
    public List<String> initSubHomeworkResultAnswerIds(NewHomework newHomework, Long userId) {
        if (newHomework == null || Objects.isNull(userId)) {
            return Collections.emptyList();
        }
        return remoteReference.initSubHomeworkResultAnswerIds(newHomework, userId);
    }

    @Override
    public List<String> fetchSubHomeworkResultAnswerIdsByType(NewHomework newHomework, Long userId, Set<ObjectiveConfigType> type) {
        return remoteReference.fetchSubHomeworkResultAnswerIdsByType(newHomework, userId, type);
    }

    @Override
    public Map<Long, NewHomeworkResult> loadNewHomeworkResult(NewHomework.Location location, Collection<Long> userIds, boolean needAnswer) {
        if (location == null || CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyMap();
        }
        return remoteReference.loadNewHomeworkResult(location, userIds, needAnswer);
    }

    @Override
    public List<NewHomeworkResult> loadNewHomeworkResult(Collection<NewHomework.Location> locations, Long userId) {
        if (userId == null || CollectionUtils.isEmpty(locations)) {
            return Collections.emptyList();
        }
        return remoteReference.loadNewHomeworkResult(locations, userId);
    }

    @Override
    public List<NewHomeworkResult> loadNewHomeworkResult(Collection<NewHomework.Location> locations, Long userId, boolean needAnswer) {
        if (userId == null || CollectionUtils.isEmpty(locations)) {
            return Collections.emptyList();
        }
        return remoteReference.loadNewHomeworkResult(locations, userId, needAnswer);
    }

    @Override
    public Map<String, NewHomeworkResult> findByHomework(NewHomework newHomework) {
        return remoteReference.findByHomework(newHomework);
    }

    @Override
    public Map<String, NewHomeworkResult> findByHomeworkForReport(NewHomework newHomework) {
        return remoteReference.findByHomeworkForReport(newHomework);
    }

    @Override
    public Map<String, Set<NewHomeworkResult>> findByHomeworksForReport(Collection<NewHomework> newHomeworks) {
        return remoteReference.findByHomeworksForReport(newHomeworks);
    }

    @Override
    public Integer homeworkIntegral(boolean repair, NewHomeworkResult newHomeworkResult) {
        return remoteReference.homeworkIntegral(repair, newHomeworkResult);
    }

    @Override
    public Integer generateFinishHomeworkActivityIntegral(Integer integral, NewHomework newHomework, Integer regionCode) {
        return remoteReference.generateFinishHomeworkActivityIntegral(integral, newHomework, regionCode);
    }

    @Override
    public Map<Long, Map<String, Integer>> getCurrentMonthHomeworkRankByGroupId(Long studentId) {
        return remoteReference.getCurrentMonthHomeworkRankByGroupId(studentId);
    }

    public Map<String, List<Map<String, Object>>> homeworkCommentAndIntegralInfo(Map<Long, User> userMap, NewHomework newHomework) {
        Map<Long, NewHomeworkResult> map = loadNewHomeworkResult(newHomework.toLocation(), userMap.keySet(), false);

        List<Map<String, Object>> doUser = new ArrayList<>();
        List<Map<String, Object>> undoUser = new ArrayList<>();
        List<Map<String, Object>> list100 = new ArrayList<>();
        List<Map<String, Object>> list90to100 = new ArrayList<>();
        List<Map<String, Object>> list90to99 = new ArrayList<>();
        List<Map<String, Object>> list80to89 = new ArrayList<>();
        List<Map<String, Object>> list60to79 = new ArrayList<>();
        List<Map<String, Object>> list60 = new ArrayList<>();
        List<Map<String, Object>> unfinisheds = new ArrayList<>();
        List<Map<String, Object>> finisheds = new ArrayList<>();
        for (User user : userMap.values()) {
            NewHomeworkResult result = map.get(user.getId());
            Map<String, Object> info = new HashMap<>();
            info.put("user_id", user.getId());
            info.put("imageUrl", user.fetchImageUrl());
            info.put("user_name", StringUtils.isNotBlank(user.fetchRealname()) ? user.fetchRealname() : user.getId());
            info.put("teacher_comment", null);
            info.put("integral", 0);
            info.put("finished", false);
            if (result != null) {
                info.put("teacher_comment", result.getComment());
                info.put("integral", SafeConverter.toInt(result.getRewardIntegral()));
                if (result.isFinished()) {
                    info.put("finished", true);
                    finisheds.add(info);
                    Integer score = result.processScore();
                    info.put("score", SafeConverter.toInt(score));
                    if (score != null) {
                        if (score == 100) {
                            list100.add(info);
                        }
                        //此處爲了兼容老师app1.4.7之前的版本
                        if (score >= 90 && score <= 100) {
                            list90to100.add(info);
                        }
                        if (score >= 90 && score < 100) {
                            list90to99.add(info);
                        }
                        if (score >= 80 && score <= 89) {
                            list80to89.add(info);
                        }
                        if (score >= 60 && score <= 79) {
                            list60to79.add(info);
                        }
                        if (score < 60) {
                            list60.add(info);
                        }
                    }
                } else {
                    info.put("score", null);
                    unfinisheds.add(info);
                }
                doUser.add(info);
            } else {
                unfinisheds.add(info);
                undoUser.add(info);
            }
        }

        Map<String, List<Map<String, Object>>> result = new HashMap<>();
        result.put("doUser", doUser);
        result.put("undoUser", undoUser);
        result.put("list100", list100);
        result.put("list90to100", list90to100);
        result.put("list90to99", list90to99);
        result.put("list80to89", list80to89);
        result.put("list60to79", list60to79);
        result.put("list60", list60);
        result.put("unfinisheds", unfinisheds);
        result.put("finisheds", finisheds);
        return result;
    }

    public SubHomeworkResultExtendedInfo loadSubHomeworkResultExtentedInfo(String id){
        return remoteReference.loadSubHomeworkResultExtentedInfo(id);
    }
}
