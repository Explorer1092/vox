package com.voxlearning.utopia.admin.listener;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.admin.listener.handler.SmsSaveHandler;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;


@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(
                        system = QueueSystem.KFK,
                        config = "primary",
                        queue = "utopia.admin.command.queue"
                ),
                @QueueDestination(
                        system = QueueSystem.KFK,
                        config = "main-backup",
                        queue = "utopia.admin.command.queue"
                )
        }
)
public class AdminCommandQueueListener extends SpringContainerSupport implements MessageListener {

    @Inject private SmsSaveHandler smsSaveHandler;

    @Override
    public void onMessage(Message message) {
        String text = message.getBodyAsString();

        Map<String, Object> command = JsonUtils.fromJson(text);

        if (command == null || !command.containsKey("command")) {
            return;
        }
        String commandName = String.valueOf(command.get("command"));
        if ("admin_sms_bach_save".equals(commandName)) {
            String targets = String.valueOf(command.get("targets"));
            long taskId = SafeConverter.toLong(command.get("taskId"));
            smsSaveHandler.handle(taskId, targets);
        }
    }

}
