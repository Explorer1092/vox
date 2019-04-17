package com.voxlearning.utopia.service.newhomework.impl.pubsub;

import com.voxlearning.alps.spi.pubsub.AlpsPubsubPublisher;
import com.voxlearning.alps.spi.pubsub.MessagePublisher;
import com.voxlearning.alps.spi.queue.MessageEncodeMode;
import lombok.Getter;

import javax.inject.Named;

/**
 * @Auther: majianxin
 * @Date: 2018/5/3
 * @Description: 诊断干预打点上报
 */
@Named
@Getter
public class InterventionPublisher {

    /**
     * 即时干预-上报数据
     */
    @AlpsPubsubPublisher(topic = "utopia.homework.intervention.topic", messageEncodeMode = MessageEncodeMode.PLAIN_TXT)
    private MessagePublisher interventionProducer;

    /**
     * 干预课程-打点上报数据
     */
    @AlpsPubsubPublisher(topic = "utopia.homework.course.intervention.topic", messageEncodeMode = MessageEncodeMode.PLAIN_TXT)
    private MessagePublisher courseInterventionProducer;
}
