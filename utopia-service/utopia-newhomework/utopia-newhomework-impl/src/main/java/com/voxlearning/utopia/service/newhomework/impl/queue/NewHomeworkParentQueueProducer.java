package com.voxlearning.utopia.service.newhomework.impl.queue;

import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.MessageProducer;
import lombok.Getter;

import javax.inject.Named;

/**
 * @author shiwei.liao
 * @since 2018-3-10
 */
@Named("com.voxlearning.utopia.service.newhomework.impl.queue.NewHomeworkParentQueueProducer")
public class NewHomeworkParentQueueProducer {

    @Getter
    @AlpsQueueProducer(queue = "utopia.group.circle.queue")
    private MessageProducer producer;
}
