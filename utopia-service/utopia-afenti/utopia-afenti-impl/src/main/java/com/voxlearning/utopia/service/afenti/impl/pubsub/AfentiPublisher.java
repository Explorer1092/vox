package com.voxlearning.utopia.service.afenti.impl.pubsub;

import com.voxlearning.alps.spi.pubsub.AlpsPubsubPublisher;
import com.voxlearning.alps.spi.pubsub.MessagePublisher;
import com.voxlearning.alps.spi.queue.MessageEncodeMode;
import lombok.Getter;

import javax.inject.Named;

/**
 * @author Ruib
 * @since 2017/7/28
 */
@Named("com.voxlearning.utopia.service.afenti.impl.pubsub.AfentiPublisher")
public class AfentiPublisher {

    @Getter
    @AlpsPubsubPublisher(topic = "utopia.afenti.plain.topic", messageEncodeMode = MessageEncodeMode.PLAIN_TXT)
    private MessagePublisher messagePlainPublisher;
}
