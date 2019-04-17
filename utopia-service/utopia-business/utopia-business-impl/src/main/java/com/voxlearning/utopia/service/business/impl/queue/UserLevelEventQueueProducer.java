package com.voxlearning.utopia.service.business.impl.queue;

import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.MessageProducer;
import lombok.Getter;

import javax.inject.Named;

@Getter
@Named
public class UserLevelEventQueueProducer {

    @AlpsQueueProducer(queue = "utopia.userlevel.event.queue")
    private MessageProducer messageProducer;
}