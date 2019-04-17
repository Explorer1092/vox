package com.voxlearning.utopia.admin.queue;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.MessageProducer;
import lombok.Getter;

import javax.inject.Named;

/**
 * 向Agent发送消息
 */
@Named
public class AgentCommandQueueProducer extends SpringContainerSupport {

    @Getter
    @AlpsQueueProducer(queue = "utopia.agent.command.queue")
    private MessageProducer producer;
}
