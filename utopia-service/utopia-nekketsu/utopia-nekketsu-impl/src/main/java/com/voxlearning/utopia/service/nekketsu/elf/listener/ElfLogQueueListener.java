package com.voxlearning.utopia.service.nekketsu.elf.listener;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.service.nekketsu.elf.dao.ElfPlayLogDao;
import com.voxlearning.utopia.service.nekketsu.elf.entity.ElfPlayLog;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;

/**
 *
 * Created by alex on 2017/7/27.
 */
@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(system = QueueSystem.KFK, config = "primary", queue = "utopia.nekketsu.elf.queue"),
                @QueueDestination(system = QueueSystem.KFK, config = "main-backup", queue = "utopia.nekketsu.elf.queue")
        },
        maxPermits = 4
)
public class ElfLogQueueListener extends SpringContainerSupport implements MessageListener {

    @Inject
    private ElfPlayLogDao elfPlayLogDao;

    @Override
    public void onMessage(Message message) {

        String messageText = message.getBodyAsString();
        if (logger.isDebugEnabled()) {
            logger.debug("Message received: {}", messageText);
        }

        Map<String, Object> map = JsonUtils.fromJson(messageText);
        if (map == null) {
            logger.error("Failed to parse JSON text: {}", messageText);
            return;
        }

        if (!map.containsKey("event")) {
            logger.error("No 'event' field found");
            return;
        }

        String event = SafeConverter.toString(map.get("event"));
        if ("ElfPlayLog".equals(event)) {
//            List<ElfPlayLog> logs = (List<ElfPlayLog>) map.get("payload");
            List<ElfPlayLog> logs = JsonUtils.fromJsonToList(JsonUtils.toJson(map.get("payload")), ElfPlayLog.class);
            if (CollectionUtils.isNotEmpty(logs)) {
                elfPlayLogDao.inserts(logs);
            }
        }

    }
}