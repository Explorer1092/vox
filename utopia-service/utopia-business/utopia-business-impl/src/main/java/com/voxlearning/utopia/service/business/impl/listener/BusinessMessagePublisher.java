package com.voxlearning.utopia.service.business.impl.listener;

import com.voxlearning.alps.spi.pubsub.AlpsPubsubPublisher;
import com.voxlearning.alps.spi.pubsub.MessagePublisher;
import lombok.Getter;

import javax.inject.Named;

@Named
public class BusinessMessagePublisher {

    @Getter
    @AlpsPubsubPublisher(topic = "utopia.business.teacher.resource.topic")
    private MessagePublisher teacherResourcePublisher;

}
