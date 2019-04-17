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

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.MentorCategory;
import com.voxlearning.utopia.service.business.impl.service.BusinessTeacherServiceImpl;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by Summer Yang on 2015/11/4.
 */
@Named
public class LoadMentorInfo extends AbstractTeacherIndexDataLoader {
    @Inject private BusinessTeacherServiceImpl businessTeacherService;

    @Override
    protected TeacherIndexDataContext doProcess(TeacherIndexDataContext context) {
        if (context.isSkipNextAll()) return context;
        // 非认证老师显示 mentor
        if (context.getTeacher().fetchCertificationState() == AuthenticationState.SUCCESS) {
            return context;
        }
        //获取我的Mentor
        MapMessage message = businessTeacherService.findMyMentorOrCandidates(context.getTeacher().getId(), MentorCategory.MENTOR_AUTHENTICATION);
        context.getParam().put("showMentor", false);
        if (message.isSuccess() && message.get("mentor") != null) {
            context.getParam().put("showMentor", true);
            context.getParam().put("mentor", message.get("mentor"));
        }
        return context;
    }
}
