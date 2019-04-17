package com.voxlearning.utopia.service.business.impl.queue;

import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.MessageProducer;
import lombok.Getter;

import javax.inject.Named;

/**
 * @author songtao
 * @since 2018/1/18
 */
@Named
public class AIDemoQueueProducer {

    @Getter
    @AlpsQueueProducer(queue = "utopia.business.ai.demo.finished")
    private MessageProducer finishedProducer;
}
