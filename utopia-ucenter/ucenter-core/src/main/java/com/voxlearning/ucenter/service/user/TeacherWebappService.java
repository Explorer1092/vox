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

package com.voxlearning.ucenter.service.user;

import com.voxlearning.alps.api.event.EventBus;
import com.voxlearning.utopia.service.invitation.client.AsyncInvitationServiceClient;
import com.voxlearning.utopia.service.user.api.entities.InviteHistory;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.consumer.*;
import com.voxlearning.utopia.service.wechat.client.WechatServiceClient;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * teacher related services for user center webapp
 *
 * @author changyuan.liu
 * @since 2015.12.08
 */
@Named
public class TeacherWebappService {

    @Inject private AsyncInvitationServiceClient asyncInvitationServiceClient;

    @Inject
    UserLoaderClient userLoaderClient;
    @Inject
    TeacherServiceClient teacherServiceClient;
    @Inject
    InvitationLoaderClient invitationLoaderClient;
    @Inject
    WechatServiceClient wechatServiceClient;
    @Inject
    TeacherLoaderClient teacherLoaderClient;
    @Inject
    DeprecatedClazzLoaderClient deprecatedClazzLoaderClient;
    @Inject
    SensitiveUserDataServiceClient sensitiveUserDataServiceClient;

    /**
     * bind invited teacher mobile
     *
     * @param teacherId
     */
    public void bindInvitedTeacherMobile(final Long teacherId) {
        EventBus.execute(() -> {
            InviteHistory inviteHistory = asyncInvitationServiceClient.loadByInvitee(teacherId)
                    .enabled()
                    .findFirst();
            if (inviteHistory == null) return false;

            teacherServiceClient.bindInvitedTeacherMobile(teacherId);

            Teacher invitee = teacherLoaderClient.loadTeacher(teacherId);
            Teacher inviter = teacherLoaderClient.loadTeacher(inviteHistory.getUserId());
            if (invitee == null || inviter == null) return false;
            if (!invitee.isPrimarySchool() || !inviter.isPrimarySchool()) return false;

            // // FIXME 邀请活动没有了。。。如果有再加
//            if (invitee.getLoginCount() <= 1) {
//                //第一次登陆就发个微信消息给邀请者
//                Map<String, Object> extension = new HashMap<>();
//                extension.put("teacherId", inviteHistory.getInviteeUserId());
//                extension.put("createDate", new Date());
//                extension.put("mobile", sensitiveUserDataServiceClient.loadUserMobileObscured(inviteHistory.getInviteeUserId()));
//                wechatServiceClient.processWechatNotice(
//                        WechatNoticeProcessorType.TeacherAcceptInviteNotice,
//                        inviteHistory.getUserId(),
//                        extension,
//                        WechatType.TEACHER);
//            }
            return true;
        });
    }
}
