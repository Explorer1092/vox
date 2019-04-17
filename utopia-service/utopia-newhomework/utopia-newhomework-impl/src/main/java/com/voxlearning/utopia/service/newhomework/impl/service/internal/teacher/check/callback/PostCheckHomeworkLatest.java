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

package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.check.callback;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.homework.api.mapper.CheckHomeworkIntegralDetail;
import com.voxlearning.utopia.service.newhomework.api.client.callback.PostCheckHomework;
import com.voxlearning.utopia.service.newhomework.api.context.CheckHomeworkContext;
import com.voxlearning.utopia.service.user.api.constants.LatestType;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.latest.Latest_CheckHomeworkIntegral;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.client.AsyncTeacherServiceClient;
import com.voxlearning.utopia.service.user.consumer.UserServiceClient;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Ruib
 * @version 0.1
 * @since 2016/1/28
 */
@Named
public class PostCheckHomeworkLatest extends SpringContainerSupport implements PostCheckHomework {

    @Inject private AsyncTeacherServiceClient asyncTeacherServiceClient;

    @Inject private UserServiceClient userServiceClient;

    @Override
    public void afterHomeworkChecked(CheckHomeworkContext context) {
        CheckHomeworkIntegralDetail detail = context.getDetail();
        if (detail == null || detail.getTeacherIntegral() <= 0) return;

        School school = asyncTeacherServiceClient.getAsyncTeacherService()
                .loadTeacherSchool(context.getTeacherId())
                .getUninterruptibly();
        if (school != null) {
            sendSchoolLatest(context.getTeacher(), school, context.getGroup(), context.getClazz(),
                    detail.getFinishCount(), detail.getTeacherIntegral() / 10);
        }
    }

    private void sendSchoolLatest(Teacher teacher, School school, GroupMapper group, Clazz clazz, Integer fc, Integer gold) {
        final Latest_CheckHomeworkIntegral detail = new Latest_CheckHomeworkIntegral();
        detail.setUserId(teacher.getId());
        detail.setUserName(teacher.fetchRealname());
        detail.setUserImg(teacher.fetchImageUrl());
        detail.setClazzName(clazz.formalizeClazzName());
        detail.setGroupName(group.getGroupName());
        detail.setFhwStudentCount(fc);
        detail.setGold(gold);
        userServiceClient.createSchoolLatest(school.getId(), LatestType.CHECK_HOMEWORK_INTEGRAL)
                .withDetail(detail).send();
    }
}
