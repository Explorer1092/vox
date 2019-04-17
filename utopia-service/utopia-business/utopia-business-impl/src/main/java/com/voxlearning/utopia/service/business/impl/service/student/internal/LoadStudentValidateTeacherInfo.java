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

package com.voxlearning.utopia.service.business.impl.service.student.internal;

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.api.constant.AmbassadorReportStatus;
import com.voxlearning.utopia.api.constant.AmbassadorReportType;
import com.voxlearning.utopia.service.ambassador.api.document.AmbassadorReportInfo;
import com.voxlearning.utopia.service.ambassador.api.document.AmbassadorReportStudentFeedback;
import com.voxlearning.utopia.service.ambassador.client.AmbassadorLoaderClient;
import com.voxlearning.utopia.service.business.impl.service.teacher.DeprecatedAmbassadorService;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.ClazzTeacher;
import com.voxlearning.utopia.service.user.consumer.UserAggregationLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Summer Yang on 2015/12/11.
 * 校园大使举报信息 学生反馈验证
 */
@Named
public class LoadStudentValidateTeacherInfo extends AbstractStudentIndexDataLoader {

    @Inject private AmbassadorLoaderClient ambassadorLoaderClient;

    @Inject private DeprecatedAmbassadorService deprecatedAmbassadorService;
    @Inject private UserAggregationLoaderClient userAggregationLoaderClient;

    @Override
    protected StudentIndexDataContext doProcess(StudentIndexDataContext context) {
        StudentDetail student = context.getStudent();
        List<ClazzTeacher> clazzTeacherList = userAggregationLoaderClient.loadStudentTeachers(student.getId());
        if (CollectionUtils.isEmpty(clazzTeacherList)) {
            return context;
        }
        List<Long> teacherIds = clazzTeacherList.stream().map(e -> e.getTeacher().getId()).collect(Collectors.toList());
        Map<Long, Teacher> teacherMap = teacherLoaderClient.loadTeachers(teacherIds);
        List<Map<String, Object>> reportTeachers = new ArrayList<>();
        for (Long teacherId : teacherIds) {
            Teacher teacher = teacherMap.get(teacherId);
            if (teacher == null) {
                continue;
            }
            if (teacher.fetchCertificationState() != AuthenticationState.SUCCESS) {
                continue;
            }
            List<AmbassadorReportInfo> infos = ambassadorLoaderClient.getAmbassadorLoader().findAmbassadorReportInfos(teacherId);
            AmbassadorReportInfo info = infos.stream().filter(i -> i.getStatus() == AmbassadorReportStatus.REPORTING)
                    .filter(i -> i.getType() == AmbassadorReportType.APPLY_CANCLE_TEACHER_AUTH.getType() ||
                            i.getType() == AmbassadorReportType.APPLY_PENDING_TEACHER.getType())
                    .findFirst().orElse(null);
            if (info != null) {
                AmbassadorReportStudentFeedback feedback = deprecatedAmbassadorService.loadAmbassadorStudentFeedBack(student.getId(), teacherId, info.getCreateDatetime());
                if (feedback != null) {
                    continue;
                }
                Map<String, Object> objectMap = new HashMap<>();
                objectMap.put("subject", teacher.getSubject().name());
                objectMap.put("teacherId", teacher.getId());
                objectMap.put("teacherName", teacher.fetchRealname());
                reportTeachers.add(objectMap);
            }
        }
        context.getParam().put("reportTeachers", reportTeachers);
        return context;
    }
}
