package com.voxlearning.utopia.service.campaign.impl.listener;

import com.alibaba.fastjson.JSON;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.pubsub.*;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageEncodeMode;
import com.voxlearning.alps.spi.queue.MessageListener;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.invitation.client.AsyncInvitationServiceClient;
import com.voxlearning.utopia.service.user.api.entities.InviteHistory;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.client.UserIntegralServiceClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.integral.api.constants.IntegralType.TEACHER_GROWTH_REWARD_TASK_JUNIOR_INVITATION;

@Named
@Slf4j
@PubsubSubscriber(
        destinations = {
                @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "utopia.user.teacher.auth.state.topic"),
                @PubsubDestination(system = PubsubSystem.KFK, connection = "main-backup", topic = "utopia.user.teacher.auth.state.topic")
        }
)
public class TeacherAuthListener extends SpringContainerSupport implements MessageListener {

    @AlpsPubsubPublisher(topic = "utopia.user.teacher.auth.state.topic", messageEncodeMode = MessageEncodeMode.PLAIN_TXT)
    private MessagePublisher messagePublisher;

    @Inject
    private AsyncInvitationServiceClient invitationServiceClient;
    @Inject
    private TeacherLoaderClient teacherLoaderClient;
    @Inject
    UserIntegralServiceClient userIntegralServiceClient;
    @Inject
    private TeacherInviAuthService teacherInviAuthService;

    private static final int NO_SEND_REWARD = 8;        // 未发奖励
    private static final int ALREADY_SEND_REWARD = 9;   // 已发奖励

    @Override
    public void onMessage(Message message) {
        Map<String, Object> msgMap = new HashMap<>();

        Object body = message.decodeBody();
        if (body instanceof String) {
            msgMap = JsonUtils.fromJson((String) body);
        } else if (body instanceof Map) {
            msgMap = (Map) body;
        } else {
            logger.warn("utopia.user.teacher.auth.state.topic msg decode message failed!", JsonUtils.toJson(message.decodeBody()));
        }

        if (RuntimeMode.le(Mode.STAGING)) {
            logger.info("utopia.user.teacher.auth.state.topic msg {}", JSON.toJSONString(msgMap));
        }

        try {
            teacherInviAuthService.handle(msgMap);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        // 产品说寒假邀请的老师不再等待认证,所以这里不再处理了
        //handler2019Invitation(msgMap);
    }

    private void handler2019Invitation(Map<String, Object> msgMap) {
        Long userId = MapUtils.getLong(msgMap, "userId");
        Integer auth = MapUtils.getInteger(msgMap, "authenticationState");
        AuthenticationState authenticationState = AuthenticationState.safeParse(auth, AuthenticationState.WAITING);

        if (userId == null || (authenticationState != AuthenticationState.SUCCESS)) return;

        try {
            executeInviter(userId);
        } catch (Exception e) {
            logger.error("邀请者奖励处理异常, teacherId is" + userId, e);
        }
        try {
            executeInvitees(userId);
        } catch (Exception e) {
            logger.error("被邀请者奖励处理异常, teacherId is" + userId, e);
        }
    }

    /**
     * 作为邀请者
     */
    private void executeInviter(Long userId) {
        List<InviteHistory> inviteHistoryList = invitationServiceClient.getAsyncInvitationService().findByUserId2019(userId);
        inviteHistoryList = inviteHistoryList.stream().filter(i -> Objects.equals(i.getIsChecked(), NO_SEND_REWARD)).collect(Collectors.toList());

        for (InviteHistory history : inviteHistoryList) {
            Long inviteeUserId = history.getInviteeUserId();
            TeacherDetail inviteeTeacher = teacherLoaderClient.loadTeacherDetail(inviteeUserId);
            if (Objects.equals(AuthenticationState.SUCCESS.getState(), inviteeTeacher.getAuthenticationState())) {
                history.setIsChecked(ALREADY_SEND_REWARD);
                invitationServiceClient.getAsyncInvitationService().updateHistory(history);

                sendReward(userId, inviteeTeacher, true);
            }
        }
    }

    /**
     * 作为被邀请者
     */
    private void executeInvitees(Long userId) {
        InviteHistory history = invitationServiceClient.getAsyncInvitationService().findByInviteId2019(userId);
        if (history == null) return;

        if (Objects.equals(history.getIsChecked(), NO_SEND_REWARD)) {
            TeacherDetail userDetail = teacherLoaderClient.loadTeacherDetail(history.getUserId());
            if (Objects.equals(AuthenticationState.SUCCESS.getState(), userDetail.getAuthenticationState())) {
                history.setIsChecked(ALREADY_SEND_REWARD);
                invitationServiceClient.getAsyncInvitationService().updateHistory(history);

                sendReward(userId, userDetail, false);
            }
        }
    }

    private void sendReward(Long firstUserId, TeacherDetail secondUser, boolean firstIsInvi) {
        int firstNum = 10000; //  小学发 1000 园丁豆 中学发 10000 学豆 发豆接口以学豆为单位(所以都传10000) 园丁豆:学豆 = 1:10
        IntegralType firstIntegralType = IntegralType.TEACHER_GROWTH_REWARD_TASK_PRIMARY_INVITATION;
        TeacherDetail firstTeacher = teacherLoaderClient.loadTeacherDetail(firstUserId);
        if (!firstTeacher.isPrimarySchool()) {
            firstIntegralType = TEACHER_GROWTH_REWARD_TASK_JUNIOR_INVITATION;
        }
        sendIntegral(firstUserId, firstNum, firstIntegralType, firstIsInvi ? "邀请新老师奖励" : "接受邀请奖励");

        int secondNum = 10000;
        IntegralType secondIntegralType = IntegralType.TEACHER_GROWTH_REWARD_TASK_PRIMARY_INVITATION;
        if (!secondUser.isPrimarySchool()) {
            secondIntegralType = TEACHER_GROWTH_REWARD_TASK_JUNIOR_INVITATION;
        }
        sendIntegral(secondUser.getId(), secondNum, secondIntegralType, firstIsInvi ? "接受邀请奖励" : "邀请新老师奖励");
    }

    private void sendIntegral(Long userId, int num, IntegralType integralType, String comment) {
        IntegralHistory integralHistory = new IntegralHistory(userId, integralType, num);
        integralHistory.setComment(comment);
        MapMessage mapMessage = userIntegralServiceClient.getUserIntegralService().changeIntegral(integralHistory);
        if (!mapMessage.isSuccess()) {
            logger.error("老师邀请活动学豆发放失败：userId:{}, info:{}", userId, mapMessage.getInfo());
        }
    }

}