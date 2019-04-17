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

package com.voxlearning.utopia.service.business.impl.service.user.internal;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.api.constant.ActivationType;
import com.voxlearning.utopia.mapper.ActivateInfoMapper;
import com.voxlearning.utopia.service.business.api.entity.TeacherActivateTeacherHistory;
import com.voxlearning.utopia.service.business.impl.dao.TeacherActivateTeacherHistoryDao;
import com.voxlearning.utopia.service.footprint.client.UserActivityServiceClient;
import com.voxlearning.utopia.service.user.api.constants.UserActivityType;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.UserActivity;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.client.AsyncTeacherServiceClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.alps.annotation.meta.AuthenticationState.SUCCESS;
import static com.voxlearning.utopia.api.constant.ActivationType.*;

/**
 * @author RuiBao
 * @version 0.1
 * @since 13-11-26
 */
@Named
@Slf4j
@NoArgsConstructor
public class ActivatableTeacherFinder extends SpringContainerSupport {

    @Inject private AsyncTeacherServiceClient asyncTeacherServiceClient;
    @Inject private UserActivityServiceClient userActivityServiceClient;
    @Inject private TeacherActivateTeacherHistoryDao teacherActivateTeacherHistoryDao;
    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private UserLoaderClient userLoaderClient;

    public List<ActivateInfoMapper> find(final TeacherDetail teacherDetail) {
        School school = asyncTeacherServiceClient.getAsyncTeacherService()
                .loadTeacherSchool(teacherDetail.getId())
                .getUninterruptibly();
        if (null == school) {
            return Collections.emptyList();
        }

        // 找到本校所有除我之外的认证教师
        List<Teacher> teachers = teacherLoaderClient.loadSchoolTeachers(school.getId());
        teachers = teachers.stream()
                .filter(source -> !source.getId().equals(teacherDetail.getId()) && source.fetchCertificationState() == SUCCESS)
                .filter(t -> !teacherLoaderClient.isFakeTeacher(t.getId()))
                .collect(Collectors.toList());

        // 包班制支持
        // 过滤掉副账号老师
        Set<Long> teacherIds = teachers.stream().map(Teacher::getId).collect(Collectors.toSet());
        Map<Long, Long> mainTeacherIds = teacherLoaderClient.loadMainTeacherIds(teacherIds);
        teachers = teachers.stream().filter(e -> mainTeacherIds.get(e.getId()) == null).collect(Collectors.toList());

        if (teachers.isEmpty()) {
            return Collections.emptyList();
        }

        // 找出正在被我激活的老师Ids
        Set<Long> iActivatingTeacherIds = new HashSet<>();
        List<TeacherActivateTeacherHistory> histories = teacherActivateTeacherHistoryDao
                .findByInviterIds(Collections.singleton(teacherDetail.getId()))
                .values()
                .stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
        for (TeacherActivateTeacherHistory history : histories) {
            if (SafeConverter.toBoolean(history.getSuccess()) && !SafeConverter.toBoolean(history.getOver())) {
                iActivatingTeacherIds.add(history.getInviteeId());
            }
        }

        // 找出正在被别的老师激活的老师Ids
        Set<Long> otherActivatingTeacherIds = new HashSet<>();
        List<Long> teacherIdList = teachers.stream()
                .filter(e -> e != null && e.getId() != null)
                .map(Teacher::getId)
                .collect(Collectors.toList());
        histories = teacherActivateTeacherHistoryDao.findByInviteeIds(teacherIdList)
                .values()
                .stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
        for (TeacherActivateTeacherHistory history : histories) {
            if (SafeConverter.toBoolean(history.getSuccess()) && !SafeConverter.toBoolean(history.getOver())) {
                otherActivatingTeacherIds.add(history.getInviteeId());
            }
        }

        List<ActivateInfoMapper> result = new LinkedList<>();
        TreeMap<Long, List<ActivateInfoMapper>> sortedResultMap = new TreeMap<>();

        for (Teacher teacher : teachers) {
            if (iActivatingTeacherIds.contains(teacher.getId()) || otherActivatingTeacherIds.contains(teacher.getId())) {
                continue;
            }
            fillSortedResultMap(teacherDetail, teacher, sortedResultMap);
        }
        for (long key : sortedResultMap.keySet()) {
            result.addAll(sortedResultMap.get(key));
        }

        return result;
    }

    private void fillSortedResultMap(TeacherDetail source, Teacher target, TreeMap<Long, List<ActivateInfoMapper>> sortedResultMap) {
        List<Long> allTeacherIds = new ArrayList<>();
        allTeacherIds.add(target.getId());
        List<Long> subIds = teacherLoaderClient.loadSubTeacherIds(target.getId());
        if (CollectionUtils.isNotEmpty(subIds)) {
            allTeacherIds.addAll(subIds);
        }

        // 查询目标老师最后一次检查作业或者测验的时间，如果为空，表示该老师从来没有检查过作业或者测验，则用老师的创建时间
        // 考虑包班制，看所有老师的
        UserActivity ua = userActivityServiceClient.getUserActivityService()
                .findUserActivities(allTeacherIds)
                .getUninterruptibly()
                .values()
                .stream()
                .flatMap(Collection::stream)
                .filter(t -> t.getActivityType() == UserActivityType.LAST_HOMEWORK_TIME)
                .max(Comparator.comparing(UserActivity::getActivityTime))
                .orElse(null);
        Date date = (ua == null ? null : ua.getActivityTime());
        if (null == date) {
            date = target.getCreateTime();
        }
        long dayDiff = DateUtils.dayDiff(new Date(), date);

        ActivationType type = dayDiff > 28 ? TEACHER_ACTIVATE_TEACHER_LEVEL_ONE : null;
        if (null != type) {
            ActivateInfoMapper mapper = build(target, type);
            mapper.setLastHomeworkDays(String.valueOf(dayDiff) + "天前");
            List<ActivateInfoMapper> lst;
            if (null == sortedResultMap.get(dayDiff)) {
                lst = new ArrayList<>();
            } else {
                lst = sortedResultMap.get(dayDiff);
            }
            lst.add(mapper);
            sortedResultMap.put(dayDiff, lst);
        }
    }

    private ActivateInfoMapper build(Teacher teacher, ActivationType type) {
        ActivateInfoMapper mapper = new ActivateInfoMapper();
        mapper.setUserId(teacher.getId());
        mapper.setUserName(StringUtils.defaultString(teacher.getProfile().getRealname()));
        mapper.setUserAvatar(teacher.fetchImageUrl());
        mapper.setType(type);
        mapper.setSubject(teacher.getSubject());
        mapper.setActivateIntegral("200");
        return mapper;
    }
}
