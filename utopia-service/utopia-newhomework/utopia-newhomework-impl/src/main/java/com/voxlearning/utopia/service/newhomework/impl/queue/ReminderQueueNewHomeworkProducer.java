package com.voxlearning.utopia.service.newhomework.impl.queue;

import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.MessageProducer;
import lombok.Getter;

import javax.inject.Named;

/**
 * @author shiwei.liao
 * @since 2017-7-29
 */
@Named
public class ReminderQueueNewHomeworkProducer {

    @Getter
    @AlpsQueueProducer(queue = "utopia.reminder.queue")
    private MessageProducer producer;
}
