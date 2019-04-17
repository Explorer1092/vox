package com.voxlearning.utopia.service.workflow.impl.queue;

import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.MessageProducer;
import lombok.Getter;

import javax.inject.Named;

@Named("com.voxlearning.utopia.service.workflow.impl.queue.WorkflowMizarQueueProducer")
public class WorkflowMizarQueueProducer {

    @Getter
    @AlpsQueueProducer(queue = "utopia.workflow.mizar.queue")
    private MessageProducer producer;
}
