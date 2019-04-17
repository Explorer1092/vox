/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
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
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DateRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.api.constant.ActivationType;
import com.voxlearning.utopia.mapper.ActivateInfoMapper;
import com.voxlearning.utopia.service.business.api.entity.TeacherActivateTeacherHistory;
import com.voxlearning.utopia.service.business.impl.dao.TeacherActivateTeacherHistoryDao;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.api.constant.ActivationType.*;

/**
 * @author xin.xin
 * @since 2014-04-02
 */
@Named
@NoArgsConstructor
@Slf4j
public class ActivatedTeacherFinder extends SpringContainerSupport {

    @Inject private TeacherActivateTeacherHistoryDao teacherActivateTeacherHistoryDao;
    @Inject private TeacherLoaderClient teacherLoaderClient;

    public List<ActivateInfoMapper> find(Long teacherId) {
        // 获取激活记录
        List<TeacherActivateTeacherHistory> histories = teacherActivateTeacherHistoryDao
                .findByInviterIds(Collections.singleton(teacherId))
                .values()
                .stream()
                .flatMap(List::stream)
                .filter(t -> SafeConverter.toBoolean(t.getSuccess()))
                .filter(t -> SafeConverter.toBoolean(t.getOver()))
                .collect(Collectors.toList());
        if (histories.size() == 0) {
            return Collections.emptyList();
        }
        List<ActivateInfoMapper> result = new ArrayList<>();
        for (TeacherActivateTeacherHistory history : histories) {
            Teacher teacher = teacherLoaderClient.loadTeacher(history.getInviteeId());
            if (null == teacher) {
                throw new IllegalArgumentException("invalid teacher id " + history.getInviteeId());
            }

            ActivateInfoMapper mapper = build(teacher, history.getActivationType());
            mapper.setActivateSuccessDays(DateUtils.dayDiff(new Date(), history.getUpdateTime())); // 已唤醒成功的天数
            mapper.setActivateSpendDays(DateUtils.dayDiff(history.getUpdateTime(), history.getCreateTime())); // 从唤醒到唤醒成功用的天数
            mapper.setActivateSuccessDate(history.getUpdateTime());

            result.add(mapper);
        }
        return result;
    }

    private ActivateInfoMapper build(Teacher teacher, ActivationType type) {
        ActivateInfoMapper mapper = new ActivateInfoMapper();
        mapper.setUserId(teacher.getId());
        mapper.setUserName(StringUtils.defaultString(teacher.getProfile().getRealname()));
        mapper.setUserAvatar(teacher.fetchImageUrl());
        mapper.setType(type);
        mapper.setSubject(teacher.getSubject());
        // todo 从20141119下午开始，教师激活教师加话费，活动到20141231结束，回复到原来的加金币
        DateRange range = new DateRange(DateUtils.stringToDate("2014-11-20 00:00:00"), DateUtils.stringToDate("2014-12-31 23:59:59"));
        boolean b = range.contains(new Date());
        if (type == SCHOOL_AMBASSADOR_ACTIVATE_TEACHER_LEVEL_ONE || type == TEACHER_ACTIVATE_TEACHER_LEVEL_ONE) {
            mapper.setActivateIntegral(b ? "10元话费" : "50园丁豆");
            mapper.setOppoIntegral(b ? "10元话费" : "50园丁豆");
        } else if (type == SCHOOL_AMBASSADOR_ACTIVATE_TEACHER_LEVEL_TWO || type == TEACHER_ACTIVATE_TEACHER_LEVEL_TWO) {
            mapper.setActivateIntegral(b ? "10元话费" : "100园丁豆");
            mapper.setOppoIntegral(b ? "30元话费" : "50园丁豆");
        } else if (type == SCHOOL_AMBASSADOR_ACTIVATE_TEACHER_LEVEL_THREE || type == TEACHER_ACTIVATE_TEACHER_LEVEL_THREE) {
            mapper.setActivateIntegral(b ? "10元话费" : "150园丁豆");
            mapper.setOppoIntegral(b ? "50元话费" : "50园丁豆");
        }
        return mapper;
    }
}
