package com.voxlearning.utopia.service.action.impl.listener;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.service.action.api.event.ActionEvent;
import com.voxlearning.utopia.service.action.api.event.ActionEventType;
import com.voxlearning.utopia.service.action.impl.service.ActionServiceImpl;

import javax.inject.Inject;
import javax.inject.Named;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * @author xinxin
 * @since 1/22/18
 */
@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(system = QueueSystem.KFK, config = "primary", queue = "utopia.action.userlevel.queue"),
                @QueueDestination(system = QueueSystem.KFK, config = "main-backup", queue = "utopia.action.userlevel.queue")
        },
        maxPermits = 64
)
public class ActionUserLevelQueueListener extends SpringContainerSupport implements MessageListener {
    public static final String USER_ID = "userId";
    public static final String LEVEL = "level";
    @Inject
    private ActionServiceImpl actionService;

    @Override
    public void onMessage(Message message) {
        Object obj = message.decodeBody();
        if (obj instanceof Map) {
            Map<String, Object> info = (Map<String, Object>) obj;
            if (info.containsKey(USER_ID) && info.containsKey(LEVEL)) {
                Long userId = SafeConverter.toLong(info.get(USER_ID));
                Integer level = SafeConverter.toInt(info.get(LEVEL));

                ActionEvent event = new ActionEvent();
                event.setUserId(userId);
                event.setType(ActionEventType.StudentUserLevelUpgrade);
                event.setTimestamp(Instant.now().toEpochMilli());

                Map<String, Object> ext = new HashMap<>(1);
                ext.put("level", level);
                event.setAttributes(ext);

                actionService.handleActionEvent(event);
            }
        }
    }
}
