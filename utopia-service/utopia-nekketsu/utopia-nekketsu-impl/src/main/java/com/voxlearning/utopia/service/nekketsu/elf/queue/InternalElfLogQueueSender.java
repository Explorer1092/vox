package com.voxlearning.utopia.service.nekketsu.elf.queue;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageProducer;
import com.voxlearning.utopia.service.nekketsu.elf.entity.ElfPlayLog;
import lombok.Getter;

import javax.inject.Named;
import java.util.*;

/**
 * Internal Elf Log Queue Sender
 */
@Named
public class InternalElfLogQueueSender extends SpringContainerSupport {

    @Getter
    @AlpsQueueProducer(queue = "utopia.nekketsu.elf.queue")
    private MessageProducer producer;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
    }

    public void saveElfPlayLog(ElfPlayLog playLog) {
        saveElfPlayLog(Collections.singleton(playLog));
    }

    public void saveElfPlayLog(Collection<ElfPlayLog> playLogs) {
        List<ElfPlayLog> logs = CollectionUtils.toLinkedList(playLogs);
        if (CollectionUtils.isEmpty(logs)) {
            return;
        }

        final Map<String, Object> input = new LinkedHashMap<>();
        input.put("event", "ElfPlayLog");
        input.put("payload", logs);

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

