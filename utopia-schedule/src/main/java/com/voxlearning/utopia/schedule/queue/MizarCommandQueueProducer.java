package com.voxlearning.utopia.schedule.queue;

import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.MessageProducer;
import com.voxlearning.alps.spi.queue.QueueSystem;
import lombok.Getter;

import javax.inject.Named;

/**
 * MizarCommandQueueProducer
 *
 * @author song.wang
 * @date 2017/6/28
 */
@Named
public class MizarCommandQueueProducer {

    @Getter
    @AlpsQueueProducer(queue = "utopia.mizar.command.queue")
    private MessageProducer producer;
}
