package com.voxlearning.utopia.service.newhomework.impl.queue;

import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.MessageProducer;
import lombok.Getter;

import javax.inject.Named;

@Named("com.voxlearning.utopia.service.newhomework.impl.queue.BatchRewardIntegralQueueProducer")
public class BatchRewardIntegralQueueProducer {

    @Getter
    @AlpsQueueProducer(queue = "utopia.newhomework.batch.reward.integral.queue")
    private MessageProducer producer;
}
