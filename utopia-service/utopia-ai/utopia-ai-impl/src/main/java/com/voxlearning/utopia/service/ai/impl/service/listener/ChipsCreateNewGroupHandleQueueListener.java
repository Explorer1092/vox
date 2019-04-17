package com.voxlearning.utopia.service.ai.impl.service.listener;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsGroupShoppingPersistence;
import com.voxlearning.utopia.service.ai.util.StringExtUntil;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;


@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(system = QueueSystem.KFK, config = "primary", queue = "utopia.chips.create.group.queue"),
                @QueueDestination(system = QueueSystem.KFK, config = "main-backup", queue = "utopia.chips.create.group.queue")
        },
        maxPermits = 4
)
public class ChipsCreateNewGroupHandleQueueListener implements MessageListener {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static String ORDER_TEMP = "temp_";

    @Inject
    private ChipsGroupShoppingPersistence chipsGroupShoppingPersistence;

    @Override
    public void onMessage(Message message) {
        if (message == null) {
            logger.error("course begin notify handle share queue no message");
            return;
        }
        Object body = message.decodeBody();

        if (body != null && body instanceof String) {
            String json = (String) body;
            Map<String, Object> param = JsonUtils.fromJson(json);
            if (param == null) {
                logger.error("ChipsGroupToSuccessHandleQueueListener error. message:{}", body);
                return;
            }
            Long user = SafeConverter.toLong(param.get("U"));
            int number = SafeConverter.toInt(param.get("N"));

            for(int i = 0; i < number; i ++) {
                String orderId = ORDER_TEMP + RandomUtils.nextObjectId();
                String newGroupCode = StringExtUntil.md5(orderId + user);
                chipsGroupShoppingPersistence.insertOrUpdate(user, orderId, newGroupCode, 1);
            }
        }
    }
}
