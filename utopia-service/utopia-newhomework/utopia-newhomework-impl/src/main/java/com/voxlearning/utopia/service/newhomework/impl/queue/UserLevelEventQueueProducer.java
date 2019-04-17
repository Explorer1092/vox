package com.voxlearning.utopia.service.newhomework.impl.queue;

import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.MessageProducer;
import lombok.Getter;

import javax.inject.Named;

/**
 * @author xinxin
 * @since 4/17/18
 */
@Named
public class UserLevelEventQueueProducer {

    @Getter
    @AlpsQueueProducer(queue = "utopia.userlevel.event.queue")
    private MessageProducer messageProducer;
}
