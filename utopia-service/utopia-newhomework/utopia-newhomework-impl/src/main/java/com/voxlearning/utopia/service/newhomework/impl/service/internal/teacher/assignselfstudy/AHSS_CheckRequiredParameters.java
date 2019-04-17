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

package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assignselfstudy;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newhomework.api.context.AssignSelfStudyHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.HomeworkSource;
import com.voxlearning.utopia.service.user.api.entities.Group;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * @author xuesong.zhang
 * @since 2017/1/22
 */
@Named
public class AHSS_CheckRequiredParameters extends SpringContainerSupport implements AssignSelfStudyHomeworkTask {

    @Inject private RaikouSystem raikouSystem;

    @Override
    public void execute(AssignSelfStudyHomeworkContext context) {
        HomeworkSource homeworkSource = context.getSource();
        if (homeworkSource == null) {
            context.errorResponse("作业内容错误");
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_HOMEWORK_CONTENT_IS_NULL);
            context.setTerminateTask(true);
            return;
        }
        Long duration = SafeConverter.toLong(homeworkSource.get("duration"));
        if (duration == 0) {
            context.errorResponse("作业预计时间错误");
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_DURATION);
            context.setTerminateTask(true);
            return;
        }
        Long studentId = SafeConverter.toLong(homeworkSource.get("studentId"));
        if (studentId == 0) {
            context.errorResponse("学生信息错误");
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_STUDENT_NOT_EXIST);
            context.setTerminateTask(true);
            return;
        }
        if (context.getSource().get("subject") == null) {
            context.errorResponse("学科属性不存在");
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_SUBJECT);
            context.setTerminateTask(true);
            return;
        }
        String sourceHomeworkId = SafeConverter.toString(homeworkSource.get("sourceHomeworkId"), "");

        context.setDuration(duration);
        context.setSourceHomeworkId(sourceHomeworkId);
//        context.setGroupId(clazzGroupId);
        context.setStudentId(studentId);
        context.setSubject(Subject.valueOf((String) context.getSource().get("subject")));

        List<Group> groupMappers = raikouSystem.loadStudentGroups(context.getStudentId());
        //取出對應學科的班組
        Group group = groupMappers.stream()
                .filter(o -> StringUtils.equalsIgnoreCase(o.getSubject().name(), context.getSubject().name()))
                .findFirst()
                .orElse(null);
        if (group == null) {
            context.errorResponse("不存在学科班組");
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_CLAZZ_GROUP_NOT_EXIST);
            context.setTerminateTask(true);
            return;
        }
        context.setGroupId(group.getId());
    }
}
