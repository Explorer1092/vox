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

package com.voxlearning.utopia.service.business.impl.service.teacher.internal.index;

import com.voxlearning.utopia.service.business.impl.service.user.UserTaskService;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.TeacherNewUserTaskMapper;

import javax.inject.Inject;
import javax.inject.Named;

import static com.voxlearning.alps.annotation.meta.AuthenticationState.SUCCESS;

/**
 * 认证相关，负责展示一个未认证教师首页动态中的认证进度
 *
 * @author RuiBao
 * @version 0.1
 * @since 7/14/2015
 */
@Named
public class LoadAuthCard extends AbstractTeacherIndexDataLoader {
    @Inject private UserTaskService userTaskService;

    @Override
    protected TeacherIndexDataContext doProcess(TeacherIndexDataContext context) {
        if (context.isSkipNextAll()) return context;

        Teacher teacher = context.getTeacher();
        if (teacher.fetchCertificationState() == SUCCESS) return context;

        TeacherNewUserTaskMapper mapper = userTaskService.getTeacherNewUserTaskMapper(teacher);
        context.getParam().put("tnut", mapper);
        return context;
    }
}
