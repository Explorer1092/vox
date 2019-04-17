package com.voxlearning.utopia.service.campaign.impl.listener;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.cache.AtomicCallback;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.alps.spi.pubsub.*;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageEncodeMode;
import com.voxlearning.alps.spi.queue.MessageListener;
import com.voxlearning.utopia.service.campaign.impl.service.OpenSchoolTestServiceImpl;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Named
@Slf4j
@PubsubSubscriber(
        destinations = {
                @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "utopia.newexam.teacher.topic"),
                @PubsubDestination(system = PubsubSystem.KFK, connection = "main-backup", topic = "utopia.newexam.teacher.topic"),
                @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "utopia.campaign.retry.topic"),
                @PubsubDestination(system = PubsubSystem.KFK, connection = "main-backup", topic = "utopia.campaign.retry.topic")
        }
)
public class OpenSchoolTeacherAssignListener extends SpringContainerSupport implements MessageListener {

    @AlpsPubsubPublisher(topic = "utopia.newexam.teacher.topic", messageEncodeMode = MessageEncodeMode.PLAIN_TXT)
    private MessagePublisher messagePublisher;

    @AlpsPubsubPublisher(topic = "utopia.campaign.retry.topic", messageEncodeMode = MessageEncodeMode.PLAIN_TXT)
    private MessagePublisher retryMessagePublisher;
    @Inject
    private OpenSchoolTestServiceImpl openSchoolTestService;

    @Override
    public void onMessage(Message message) {
        Map<String, Object> msgMap = new HashMap<>();

        Object body = message.decodeBody();
        if (body instanceof String) {
            msgMap = JsonUtils.fromJson((String) body);
        } else if (body instanceof Map) {
            msgMap = (Map) body;
        } else {
            logger.warn("utopia.newexam.teacher.topic msg decode message failed!", JsonUtils.toJson(message.decodeBody()));
        }

        if (RuntimeMode.le(Mode.STAGING)) {
            logger.info("utopia.newexam.teacher.topic msg {}", JsonUtils.toJson(msgMap));
        }

        String newExamId = MapUtils.getString(msgMap, "newExamId");
        if (!OpenSchoolTestServiceImpl.newExamId.contains(newExamId)) {
            return;
        }

        String messageType = MapUtils.getString(msgMap, "messageType");

        if (Objects.equals(messageType, "assignApply")) {
            assignApply(msgMap);
        } else if (Objects.equals(messageType, "shareReport")) {
            shareReport(msgMap);
        }

    }

    private void assignApply(Map<String, Object> msgMap) {
        Long teacherId = MapUtils.getLong(msgMap, "teacherId");
        Long groupId = MapUtils.getLong(msgMap, "groupId");

        AtomicCallback<Void> atomicCallback = () -> {
            Long mainTeacher = openSchoolTestService.getMainTeacher(teacherId);
            openSchoolTestService.addGroupId(mainTeacher, groupId);
            return null;
        };

        try {
            AtomicCallbackBuilderFactory.getInstance()
                    .<Void>newBuilder()
                    .keyPrefix("OpenSchoolTestServiceListener")
                    .keys(teacherId)
                    .callback(atomicCallback)
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            try {
                Thread.sleep(100);
                Integer retry = MapUtils.getInteger(msgMap, "retry", 0);
                msgMap.put("retry", retry + 1);
                retryMessagePublisher.publish(Message.newMessage().withPlainTextBody(JsonUtils.toJson(msgMap)));
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    private void shareReport(Map<String, Object> msgMap) {
        Long teacherId = MapUtils.getLong(msgMap, "teacherId");
        if (teacherId == null || Objects.equals(teacherId, 0L)) {
            return;
        }
        openSchoolTestService.teacherShare(teacherId);
    }

}