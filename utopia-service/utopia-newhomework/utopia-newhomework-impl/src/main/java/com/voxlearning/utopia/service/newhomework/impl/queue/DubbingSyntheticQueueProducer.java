package com.voxlearning.utopia.service.newhomework.impl.queue;

import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.MessageEncodeMode;
import com.voxlearning.alps.spi.queue.MessageProducer;
import lombok.Getter;

import javax.inject.Named;

@Named("com.voxlearning.utopia.service.newhomework.impl.queue.DubbingSyntheticQueueProducer")
public class DubbingSyntheticQueueProducer {

    @Getter
    @AlpsQueueProducer(queue = "utopia.newhomework.dubbing.synthetic.request.queue", encodeMode = MessageEncodeMode.PLAIN_TXT)
    private MessageProducer producer;
}
