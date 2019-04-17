package com.voxlearning.utopia.service.campaign.impl.listener;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.pubsub.*;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageEncodeMode;
import com.voxlearning.alps.spi.queue.MessageListener;
import com.voxlearning.utopia.service.campaign.impl.service.TeacherWinterPlanServiceImpl;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

@Named
@Slf4j
@PubsubSubscriber(
        destinations = {
                @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "utopia.campaign.planning.msg.topic"),
                @PubsubDestination(system = PubsubSystem.KFK, connection = "main-backup", topic = "utopia.campaign.planning.msg.topic")
        }
)
public class TeacherWinterPlanMsgListener extends SpringContainerSupport implements MessageListener {

    @AlpsPubsubPublisher(topic = "utopia.campaign.planning.msg.topic", messageEncodeMode = MessageEncodeMode.PLAIN_TXT)
    private MessagePublisher messagePublisher;

    @Inject
    private TeacherWinterPlanServiceImpl teacherParentChildService;

    @Override
    @SuppressWarnings("ALL")
    public void onMessage(Message message) {
        Map<String, Object> msgMap = new HashMap<>();

        Object body = message.decodeBody();
        if (body instanceof String) {
            msgMap = JsonUtils.fromJson((String) body);
        } else if (body instanceof Map) {
            msgMap = (Map) body;
        } else {
            logger.warn("utopia.campaign.planning.msg.topic msg decode message failed!", JsonUtils.toJson(message.decodeBody()));
        }

        handler(msgMap);
    }

    private void handler(Map<String, Object> msg) {
        try {
            Long teacherId = MapUtils.getLong(msg, "teacherId");

            teacherParentChildService.sendSmsPush(teacherId);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

}
