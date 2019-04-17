package com.voxlearning.utopia.service.action.impl.queue;

import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.MessageProducer;
import lombok.Getter;

import javax.inject.Named;

@Named("com.voxlearning.utopia.service.action.impl.queue.ActionQueueProducer")
public class ActionQueueProducer {

    @Getter
    @AlpsQueueProducer(queue = "utopia.queue.action")
    private MessageProducer producer;
}
