package com.voxlearning.utopia.service.newexam.impl.queue;

import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.MessageProducer;
import lombok.Getter;

import javax.inject.Named;

@Named("com.voxlearning.utopia.service.newexam.impl.queue.NewExamParentQueueProducer")
public class NewExamParentQueueProducer {

    @Getter
    @AlpsQueueProducer(queue = "utopia.group.circle.queue")
    private MessageProducer producer;
}
