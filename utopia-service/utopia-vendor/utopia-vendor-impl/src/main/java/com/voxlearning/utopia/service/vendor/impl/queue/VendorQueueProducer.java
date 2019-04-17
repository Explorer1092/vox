package com.voxlearning.utopia.service.vendor.impl.queue;

import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.MessageProducer;
import lombok.Getter;

import javax.inject.Named;

@Named("com.voxlearning.utopia.service.vendor.impl.queue.VendorQueueProducer")
public class VendorQueueProducer {

    @Getter
    @AlpsQueueProducer(queue = "utopia.vendor.queue")
    private MessageProducer producer;
}
