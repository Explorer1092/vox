package com.voxlearning.utopia.service.ai.impl.service.listener;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.service.ai.impl.service.ChipsActiveServiceImpl;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

/**
 * 毕业证
 */
@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(system = QueueSystem.KFK, config = "primary", queue = "utopia.chips.active.service.remind.message.send.queue"),
                @QueueDestination(system = QueueSystem.KFK, config = "main-backup", queue = "utopia.chips.active.service.remind.message.send.queue")
        },
        maxPermits = 4
)
public class ChipsActiveServiceRemindHandleQueueListener implements MessageListener {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private ChipsActiveServiceImpl chipsActiveService;
    @Override
    public void onMessage(Message message) {
        if (message == null) {
            logger.error("chips active service remind handle share queue no message");
            return;
        }
        Object body = message.decodeBody();

        if (body != null && body instanceof String) {
            String json = (String) body;
            Map<String, Object> param = JsonUtils.fromJson(json);
            if (param == null) {
                logger.error("ChipsActiveServiceRemindHandleQueueListener error. message:{}", body);
                return;
            }
            Long clazzId = SafeConverter.toLong(param.get("clazzId"));
            Long userId = SafeConverter.toLong(param.get("userId"));
            String productId = SafeConverter.toString(param.get("productId"));
            String unitId = SafeConverter.toString(param.get("unitId"));
            chipsActiveService.genChipsRemindRecordByUserId(clazzId, userId,productId, unitId);
        }
    }
}
