package com.voxlearning.utopia.schedule.queue;

import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.MessageProducer;
import lombok.Getter;

import javax.inject.Named;

/**
 * Created by songtao on 2017/11/10.
 */
@Named
public class UserIntegralQueueProducer {

    @Getter
    @AlpsQueueProducer(queue = "utopia.user.integral.expired.queue")
    private MessageProducer expiredQueue;

}
