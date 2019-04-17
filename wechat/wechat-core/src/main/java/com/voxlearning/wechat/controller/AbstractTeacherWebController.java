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

package com.voxlearning.wechat.controller;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.business.consumer.BusinessTeacherServiceClient;
import com.voxlearning.utopia.service.content.consumer.*;
import com.voxlearning.utopia.service.newhomework.consumer.*;
import com.voxlearning.utopia.service.question.consumer.TeachingObjectiveLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.consumer.UserTagLoaderClient;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by xinxin on 18/1/2016.
 */
public class AbstractTeacherWebController extends AbstractController {
    @Inject
    protected NewHomeworkServiceClient newHomeworkServiceClient;
    @Inject
    protected NewHomeworkContentServiceClient newHomeworkContentServiceClient;
    @Inject
    protected ClazzBookLoaderClient clazzBookLoaderClient;
    @Inject
    protected MathContentLoaderClient mathContentLoaderClient;
    @Inject
    protected TeachingObjectiveLoaderClient teachingObjectiveLoaderClient;
    @Inject
    protected NewKnowledgePointLoaderClient newKnowledgePointLoaderClient;
    @Inject
    protected ContentServiceClient contentServiceClient;
    @Inject
    protected NewContentLoaderClient newContentLoaderClient;
    @Inject
    protected NewContentServiceClient newContentServiceClient;
    @Inject
    protected NewClazzBookLoaderClient newClazzBookLoaderClient;
    @Inject
    protected NewClazzBookServiceClient newClazzBookServiceClient;
    @Inject
    protected NewHomeworkLoaderClient newHomeworkLoaderClient;
    @Inject
    protected NewRegionBookLoaderClient newRegionBookLoaderClient;
    @Inject
    protected NewHomeworkResultLoaderClient newHomeworkResultLoaderClient;
    @Inject
    protected NewHomeworkProcessResultLoaderClient newHomeworkProcessResultLoaderClient;
    @Inject
    protected BusinessTeacherServiceClient businessTeacherServiceClient;
    @Inject
    protected UserTagLoaderClient userTagLoaderClient;
    @Inject
    protected OfflineHomeworkServiceClient offlineHomeworkServiceClient;
    @Inject
    protected OfflineHomeworkLoaderClient offlineHomeworkLoaderClient;

    protected List<Clazz> getTeacherClazzs() {
        Long teacherId = getTeacherIdBySubject();
        if (null == teacherId) return new ArrayList<>();

        List<Clazz> clazzs = deprecatedClazzLoaderClient.getRemoteReference().loadTeacherClazzs(teacherId);
        if (CollectionUtils.isEmpty(clazzs)) return new ArrayList<>();

        return clazzs.stream().filter(c -> c.isPublicClazz() && !c.isTerminalClazz()).collect(Collectors.toList());
    }

    protected List<GroupMapper> getTeacherGroups() {
        Long teacherId = getRequestContext().getUserId();
        if (null == teacherId) return new ArrayList<>();

        List<Clazz> clazzs = getTeacherClazzs();
        if (CollectionUtils.isEmpty(clazzs)) return new ArrayList<>();

        List<GroupMapper> groups = groupLoaderClient.loadTeacherGroupsByTeacherId(teacherId, false);
        if (CollectionUtils.isEmpty(groups)) return new ArrayList<>();

        List<Long> clazzIds = clazzs.stream().map(Clazz::getId).collect(Collectors.toList());
        return groups.stream().filter(group -> clazzIds.contains(group.getClazzId())).collect(Collectors.toList());
    }

    /* ======================================================================================
       包班制支持
       一个老师多学科情况,拿到指定学科的老师
       ====================================================================================== */

    protected Long getTeacherIdBySubject() {
        return getTeacherIdBySubject(currentSubject());
    }

    protected Long getTeacherIdBySubject(Subject subject) {
        Long teacherId = getRequestContext().getUserId();
        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
        if (subject == null) {
            return teacher != null ? teacher.getId() : null;
        }

        // 多学科支持
        if (teacher != null) {
            if (subject != teacher.getSubject()) {// 此时需要根据学科切换当前老师的班级信息
                Long t = teacherLoaderClient.loadRelTeacherIdBySubject(teacher.getId(), subject);
                if (t != null) {
                    teacherId = t;
                }
            }
        }
        return teacherId;
    }

    protected Teacher getTeacherBySubject() {
        return getTeacherBySubject(currentSubject());
    }

    protected Teacher getTeacherBySubject(Subject subject) {
        Long teacherId = getRequestContext().getUserId();
        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
        if (subject == null) {
            return teacher;
        }

        // 多学科支持
        if (teacher != null) {
            if (subject != teacher.getSubject()) {// 此时需要根据学科切换当前老师的班级信息
                Long id = teacherLoaderClient.loadRelTeacherIdBySubject(teacher.getId(), subject);
                Teacher t = teacherLoaderClient.loadTeacher(id);
                if (t != null) {
                    teacher = t;
                }
            }
        }
        return teacher;
    }

    protected Subject currentSubject() {
        String subjectStr = getRequestString("subject");
        return StringUtils.isNotEmpty(subjectStr) ? Subject.of(subjectStr) : null;
    }

}
