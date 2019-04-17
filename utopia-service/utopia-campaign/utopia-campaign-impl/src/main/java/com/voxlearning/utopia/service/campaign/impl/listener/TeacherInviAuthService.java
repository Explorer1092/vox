package com.voxlearning.utopia.service.campaign.impl.listener;

import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.invitation.api.InviteRewardHistoryService;
import com.voxlearning.utopia.service.invitation.client.AsyncInvitationServiceClient;
import com.voxlearning.utopia.service.user.api.entities.InviteHistory;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.client.UserIntegralServiceClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

@Named
@Slf4j
class TeacherInviAuthService extends SpringContainerSupport {

    @Inject
    private AsyncInvitationServiceClient invitationServiceClient;
    @Inject
    private TeacherLoaderClient teacherLoaderClient;
    @Inject
    UserIntegralServiceClient userIntegralServiceClient;

    @ImportService(interfaceClass = InviteRewardHistoryService.class)
    private InviteRewardHistoryService inviteRewardHistoryService;

    void handle(Map<String, Object> msgMap) {
        Long userId = MapUtils.getLong(msgMap, "userId");
        Integer auth = MapUtils.getInteger(msgMap, "authenticationState");
        AuthenticationState authenticationState = AuthenticationState.safeParse(auth, AuthenticationState.WAITING);

        if (userId == null || (authenticationState != AuthenticationState.SUCCESS)) return;

        // 只处理中学的
        TeacherDetail detail = teacherLoaderClient.loadTeacherDetail(userId);
        if (detail == null || (!detail.isJuniorTeacher())) {
            return;
        }

        // 只处理 30 天内的
        if (DateUtils.dayDiff(new Date(), detail.getCreateTime()) > 30) {
            return;
        }

        // 只处理有邀请关系的
        InviteHistory inviteHistory = invitationServiceClient.getAsyncInvitationService().queryByInviteId2019First(userId);
        if (inviteHistory == null) {
            return;
        }

        // 中英老师
        int integralNum = 10000;

        if (CollectionUtils.isNotEmpty(detail.getSubjects())
                && (detail.getSubjects().contains(Subject.ENGLISH)
                || detail.getSubjects().contains(Subject.JENGLISH))) {
            sendIntegral(detail.getId(), integralNum, IntegralType.TEACHER_DAY_TASK_INVITATION, "中学老师达成认证奖励");
        }

        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(inviteHistory.getUserId());
        if (allowSendReward(teacherDetail)) {
            if (Objects.equals(teacherDetail.getAuthenticationState(), AuthenticationState.SUCCESS.getState())) {
                sendIntegral(inviteHistory.getUserId(), integralNum, IntegralType.TEACHER_DAY_TASK_INVITATION, "邀请中学老师且达成认证奖励");

                inviteRewardHistoryService.incrReward(inviteHistory.getUserId(), inviteHistory.getInviteeUserId(), integralNum);
            }
        }
    }


    private boolean allowSendReward(TeacherDetail detail) {
        if (detail == null) return false;
        return detail.isPrimarySchool() ||
                (
                        CollectionUtils.isNotEmpty(detail.getSubjects())
                                &&
                                (detail.getSubjects().contains(Subject.ENGLISH) || detail.getSubjects().contains(Subject.JENGLISH))
                );
    }

    private void sendIntegral(Long userId, int num, IntegralType integralType, String comment) {
        IntegralHistory integralHistory = new IntegralHistory(userId, integralType, num);
        integralHistory.setComment(comment);
        MapMessage mapMessage = userIntegralServiceClient.getUserIntegralService().changeIntegral(integralHistory);
        if (!mapMessage.isSuccess()) {
            logger.error(comment + "发放失败：userId:{}, info:{}", userId, mapMessage.getInfo());
        }
    }
}