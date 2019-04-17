package com.voxlearning.utopia.service.newhomework.impl.pubsub;

import com.voxlearning.alps.spi.pubsub.AlpsPubsubPublisher;
import com.voxlearning.alps.spi.pubsub.MessagePublisher;
import com.voxlearning.alps.spi.queue.MessageEncodeMode;
import lombok.Getter;
import javax.inject.Named;

@Named
@Getter
public class SelfStudyHomeworkPublisher {
    @AlpsPubsubPublisher(topic = "utopia.17xue.self.study.homework.topic",messageEncodeMode = MessageEncodeMode.PLAIN_TXT)
    private MessagePublisher messagePublisher;
}
