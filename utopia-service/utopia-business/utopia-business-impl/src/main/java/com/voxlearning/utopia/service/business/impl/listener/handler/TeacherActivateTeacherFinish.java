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

package com.voxlearning.utopia.service.business.impl.listener.handler;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.business.BusinessEvent;
import com.voxlearning.utopia.business.BusinessEventType;
import com.voxlearning.utopia.service.business.impl.listener.BusinessEventHandler;
import com.voxlearning.utopia.service.business.impl.service.teacher.TeacherActivateTeacherServiceImpl;
import com.voxlearning.utopia.service.invitation.api.TeacherActivateService;
import com.voxlearning.utopia.service.invitation.entity.TeacherActivate;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Named
public class TeacherActivateTeacherFinish implements BusinessEventHandler {

    @Inject
    private TeacherActivateTeacherServiceImpl teacherActivateTeacherService;
    @Inject
    private UserLoaderClient userLoaderClient;

    @ImportService(interfaceClass = TeacherActivateService.class)
    private TeacherActivateService teacherActivateService;

    @Override
    public BusinessEventType getEventType() {
        return BusinessEventType.TEACHER_ACTIVATE_TEACHER_FINISH;
    }

    @Override
    public void handle(BusinessEvent event) {
        if (event == null) return;
        if (event.getAttributes() == null) return;
        long inviteeId = SafeConverter.toLong(event.getAttributes().get("inviteeId"));
        if (inviteeId == 0) return;

        User invitee = userLoaderClient.loadUser(inviteeId);
        if (invitee == null) return;
        teacherActivateTeacherService.teacherActivateTeacherFinish(invitee);

        // 新版激活关系
        List<TeacherActivate> collect = teacherActivateService.loadActivateInitOrIng(Collections.singleton(inviteeId)).stream()
                .filter(i -> Objects.equals(i.getStatus(), TeacherActivate.Status.ING.getCode()))
                .collect(Collectors.toList());
        for (TeacherActivate i : collect) {
            teacherActivateService.success(i.getId(), i.getUserId());
        }
    }
}
