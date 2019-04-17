package com.voxlearning.utopia.service.nekketsu.impl.listener;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.service.nekketsu.impl.dao.ParkourCoinHistoryDao;
import com.voxlearning.utopia.service.nekketsu.impl.dao.ParkourPlayLogDao;
import com.voxlearning.utopia.service.nekketsu.parkour.entity.ParkourCoinHistory;
import com.voxlearning.utopia.service.nekketsu.parkour.entity.ParkourPlayLog;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

/**
 *
 * Created by alex on 2017/7/27.
 */
@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(system = QueueSystem.KFK, config = "primary", queue = "utopia.nekketsu.parkour.queue"),
                @QueueDestination(system = QueueSystem.KFK, config = "main-backup", queue = "utopia.nekketsu.parkour.queue")
        },
        maxPermits = 4
)
public class ParkourQueueListener extends SpringContainerSupport implements MessageListener {

    @Inject private ParkourCoinHistoryDao parkourCoinHistoryDao;
    @Inject private ParkourPlayLogDao parkourPlayLogDao;

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

        if ("ParkourCoinHistory".equals(event)) {
//            ParkourCoinHistory obj = (ParkourCoinHistory) map.get("payload");
            ParkourCoinHistory obj = JsonUtils.fromJson(JsonUtils.toJson(map.get("payload")), ParkourCoinHistory.class);
            if (obj != null) {
                parkourCoinHistoryDao.insert(obj);
            }
        }

        if ("ParkourPlayLog".equals(event)) {
//            ParkourPlayLog obj = (ParkourPlayLog) map.get("payload");
            ParkourPlayLog obj = JsonUtils.fromJson(JsonUtils.toJson(map.get("payload")), ParkourPlayLog.class);
            if (obj != null) {
                parkourPlayLogDao.insert(obj);
            }
        }

    }
}
