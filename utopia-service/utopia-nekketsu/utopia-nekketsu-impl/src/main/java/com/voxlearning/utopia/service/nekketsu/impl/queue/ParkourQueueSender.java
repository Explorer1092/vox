package com.voxlearning.utopia.service.nekketsu.impl.queue;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageProducer;
import com.voxlearning.utopia.service.nekketsu.elf.entity.ElfPlayLog;
import com.voxlearning.utopia.service.nekketsu.parkour.entity.ParkourCoinHistory;
import com.voxlearning.utopia.service.nekketsu.parkour.entity.ParkourPlayLog;
import lombok.Getter;

import javax.inject.Named;
import java.util.*;

/**
 *
 * Created by alex on 2017/7/27.
 */
@Named
public class ParkourQueueSender extends SpringContainerSupport {

    @Getter
    @AlpsQueueProducer(queue = "utopia.nekketsu.parkour.queue")
    private MessageProducer producer;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
    }

    public void saveParkourCoinHistory(ParkourCoinHistory coinHistory) {
        if (coinHistory == null) {
            return;
        }

        final Map<String, Object> input = new LinkedHashMap<>();
        input.put("event", "ParkourCoinHistory");
        input.put("payload", coinHistory);

        String messageText = JsonUtils.toJson(input);
        if (messageText == null) {
            logger.warn("Failed to convert play logs into JSON text");
            return;
        }

        Message message = Message.newMessage();
        message.withPlainTextBody(messageText);
        producer.produce(message);
    }

    public void saveParkourPlayLog(ParkourPlayLog playLog) {
        if (playLog == null) {
            return;
        }

        final Map<String, Object> input = new LinkedHashMap<>();
        input.put("event", "ParkourPlayLog");
        input.put("payload", playLog);

        String messageText = JsonUtils.toJson(input);
        if (messageText == null) {
            logger.warn("Failed to convert play logs into JSON text");
            return;
        }

        Message message = Message.newMessage();
        message.withPlainTextBody(messageText);
        producer.produce(message);
    }
}
