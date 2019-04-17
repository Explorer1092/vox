package com.voxlearning.utopia.schedule.queue;

import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.MessageProducer;
import lombok.Getter;

import javax.inject.Named;

/**
 * @author malong
 * @since 2018/05/21
 */
@Named
public class TemplateMessageQueueProducer {
    @Getter
    @AlpsQueueProducer(queue = "galaxy.wechat.template.event.queue")
    private MessageProducer producer;
}
