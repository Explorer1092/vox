package com.voxlearning.utopia.admin.queue;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.MessageProducer;
import lombok.Getter;

import javax.inject.Named;

/**
 * @author chunlin.yu
 * @create 2018-05-21 18:13
 **/
@Named
public class AdminCommandQueueProducer extends SpringContainerSupport {
    @Getter
    @AlpsQueueProducer(queue = "utopia.admin.command.queue")
    private MessageProducer producer;
}
