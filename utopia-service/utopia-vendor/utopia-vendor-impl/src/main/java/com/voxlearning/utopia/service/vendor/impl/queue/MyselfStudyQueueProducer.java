package com.voxlearning.utopia.service.vendor.impl.queue;

import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.MessageProducer;
import lombok.Getter;

import javax.inject.Named;

@Named("com.voxlearning.utopia.service.vendor.impl.queue.MyselfStudyQueueProducer")
public class MyselfStudyQueueProducer {

    @Getter
    @AlpsQueueProducer(queue = "utopia.vendor.myselfstudy.queue")
    private MessageProducer producer;
}
