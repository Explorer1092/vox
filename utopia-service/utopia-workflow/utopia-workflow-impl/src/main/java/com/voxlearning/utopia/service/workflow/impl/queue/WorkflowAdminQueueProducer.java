package com.voxlearning.utopia.service.workflow.impl.queue;

import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.MessageProducer;
import lombok.Getter;

import javax.inject.Named;

@Named("com.voxlearning.utopia.service.workflow.impl.queue.WorkflowAdminQueueProducer")
public class WorkflowAdminQueueProducer {

    @Getter
    @AlpsQueueProducer(queue = "utopia.workflow.admin.queue")
    private MessageProducer producer;
}
