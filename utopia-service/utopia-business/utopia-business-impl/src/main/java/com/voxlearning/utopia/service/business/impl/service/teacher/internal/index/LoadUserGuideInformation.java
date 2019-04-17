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

import com.voxlearning.alps.annotation.meta.PasswordState;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.spi.monitor.FlightRecorder;
import com.voxlearning.utopia.api.constant.AppAuditAccounts;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.invitation.client.AsyncInvitationServiceClient;
import com.voxlearning.utopia.service.user.api.constants.InvitationType;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashSet;
import java.util.Set;

import static com.voxlearning.utopia.service.user.api.constants.InvitationType.TEACHER_INVITE_TEACHER_SMS;
import static com.voxlearning.utopia.service.user.api.constants.InvitationType.TEACHER_INVITE_TEACHER_SMS_BY_WECHAT;

/**
 * 教师首页判断是否走引导流程
 *
 * @author RuiBao
 * @version 0.1
 * @since 13-8-6
 */
@Named
public class LoadUserGuideInformation extends AbstractTeacherIndexDataLoader {

    @Inject private AsyncInvitationServiceClient asyncInvitationServiceClient;

    @Override
    protected TeacherIndexDataContext doProcess(TeacherIndexDataContext context) {
        FlightRecorder.dot("LUGI_START");
        Teacher teacher = context.getTeacher();

        // 课题教师是有subject和ktwelve的，所以如果subject和ktwelve没有设置，为普通教师。
        if (!teacher.hasValidSubject()) {
            context.setSkipNextAll(true);
            context.getParam().put("userGuideFlow", generateSelectSchoolUrl());
            context.getParam().put("isInvited", invitedBySmsOrWechat(teacher));
            return context;
        }

        if (null == context.getSchool()) {
            // 如果当前教师是课题教师并且没有填写过名字
            if (User.KETI_IDENTIFICATION.equals(teacher.getWebSource()) && StringUtils.isBlank(teacher.getProfile().getRealname())) {
                context.getParam().put("userGuideFlow", "teacherv3/guide/ketiname");
                context.getParam().put("isInvited", invitedBySmsOrWechat(teacher));
                context.setSkipNextAll(true);
                return context;
            } else {
                context.getParam().put("userGuideFlow", generateSelectSchoolUrl());
                context.getParam().put("isInvited", invitedBySmsOrWechat(teacher));
                context.setSkipNextAll(true);
                return context;
            }
        }

        // 强制修改密码
        UserAuthentication ua = userLoaderClient.loadUserAuthentication(teacher.getId());
        if (teacher.isBatchUser() && ua.fetchPasswordState() == PasswordState.AUTO_GEN) {
            context.getParam().put("userGuideFlow", "teacherv3/guide/modifypwd");
            context.setSkipNextAll(true);
            return context;
        }

        // 如果没有班级
        if (context.getClazzList().isEmpty()) {
            context.getParam().put("userGuideFlow", "redirect:/teacher/showtip.vpage");
            context.setSkipNextAll(true);
            return context;
        }

        FlightRecorder.dot("LUGI_END");
        return context;
    }

    private boolean invitedBySmsOrWechat(Teacher teacher) {
        Set<InvitationType> types = new HashSet<>();
        types.add(TEACHER_INVITE_TEACHER_SMS);
        types.add(TEACHER_INVITE_TEACHER_SMS_BY_WECHAT);

        return SafeConverter.toBoolean(teacher.getIsInvite()) &&
                asyncInvitationServiceClient.loadByInvitee(teacher.getId()).enabled()
                        .filter(t -> types.contains(t.getType()))
                        .count() > 0;
    }

    private String generateSelectSchoolUrl() {
        return "redirect:" + ProductConfig.getUcenterUrl() + "/teacher/selectschool.vpage";
    }
}
