package com.voxlearning.utopia.service.reminder.impl.listener;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.spi.pubsub.PubsubDestination;
import com.voxlearning.alps.spi.pubsub.PubsubSubscriber;
import com.voxlearning.alps.spi.pubsub.PubsubSystem;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageListener;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkPublishMessageType;
import com.voxlearning.utopia.service.reminder.api.ReminderService;
import com.voxlearning.utopia.service.reminder.constant.ReminderPosition;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

/**
 * @author shiwei.liao
 * @since 2017-8-3
 */
@Named
@PubsubSubscriber(destinations = {
        @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "utopia.homework.teacher.topic"),
        @PubsubDestination(system = PubsubSystem.KFK, connection = "main-backup", topic = "utopia.homework.teacher.topic")
})
public class ReminderNewHomeworkSubscribListener implements MessageListener {
    @Inject
    private ReminderService reminderService;

    @Override
    public void onMessage(Message message) {
        Object body = message.decodeBody();
        if (body == null) {
            return;
        }
        Map<String, Object> messageMap = JsonUtils.fromJson((String) body);
        if (MapUtils.isEmpty(messageMap)) {
            return;
        }
        HomeworkPublishMessageType messageType = HomeworkPublishMessageType.of(SafeConverter.toString(messageMap.get("messageType")));
        if (messageType == HomeworkPublishMessageType.UNKNOWN) {
            return;
        }
        Long groupId = SafeConverter.toLong(messageMap.get("groupId"));
        if (groupId <= 0) {
            return;
        }
        if (messageType == HomeworkPublishMessageType.assign) {
            reminderService.addClazzGroupReminder(groupId, ReminderPosition.PARENT_APP_EASEMOB_BOTTOM_MENU_NOTIFY);
        } else if (messageType == HomeworkPublishMessageType.deleted) {
            reminderService.decrClazzGroupReminder(groupId, ReminderPosition.PARENT_APP_EASEMOB_BOTTOM_MENU_NOTIFY);
        }
    }
}
