package com.voxlearning.utopia.service.campaign.impl.listener;

import com.alibaba.fastjson.JSON;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.pubsub.PubsubDestination;
import com.voxlearning.alps.spi.pubsub.PubsubSubscriber;
import com.voxlearning.alps.spi.pubsub.PubsubSystem;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageListener;
import com.voxlearning.utopia.service.campaign.impl.service.TeacherActivityCardServiceImpl;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

@Named
@Slf4j
@PubsubSubscriber(
        destinations = {
                @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "utopia.invitation.user.topic"),
                @PubsubDestination(system = PubsubSystem.KFK, connection = "main-backup", topic = "utopia.invitation.user.topic")
        },
        maxPermits = 2
)
public class InvitationListener extends SpringContainerSupport implements MessageListener {

    @Inject
    TeacherActivityCardServiceImpl teacherActivityCardService;

    @Override
    public void onMessage(Message message) {
        Map<String, Object> msgMap = new HashMap<>();

        Object body = message.decodeBody();
        if (body instanceof String) {
            msgMap = JsonUtils.fromJson((String) body);
        } else if (body instanceof Map) {
            msgMap = (Map) body;
        } else {
            logger.warn("utopia.invitation.user.topic msg decode message failed!", JsonUtils.toJson(message.decodeBody()));
        }

        handler(msgMap);
    }

    private void handler(Map<String, Object> msg) {
        try {
            if (RuntimeMode.lt(Mode.STAGING)) {
                log.info("utopia-invitation-user-topic-listener:{}", JSON.toJSONString(msg));
            }

            Long userId = MapUtils.getLong(msg, "userId"); // 邀请者
            //Long inviteeUserId = MapUtils.getLong(msg, "inviteeUserId"); // 被邀请者

            //teacherActivityCardService.addOpportunity(userId, OpportunityReasonEnum.邀请老师.name());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}