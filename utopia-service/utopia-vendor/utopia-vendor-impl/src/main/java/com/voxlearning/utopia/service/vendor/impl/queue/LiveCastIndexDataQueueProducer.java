package com.voxlearning.utopia.service.vendor.impl.queue;

import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.MessageProducer;
import lombok.Getter;

import javax.inject.Named;

/**
 * @author jiangpeng
 * @since 2017-10-18 下午3:41
 **/
@Named
public class LiveCastIndexDataQueueProducer {

    @Getter
    @AlpsQueueProducer(queue = "utopia.jzt.livecast.queue")
    private MessageProducer producer;
}
