package com.voxlearning.utopia.schedule.pubsub;

import com.voxlearning.alps.spi.pubsub.AlpsPubsubPublisher;
import com.voxlearning.alps.spi.pubsub.MessagePublisher;
import com.voxlearning.alps.spi.queue.MessageEncodeMode;
import lombok.Getter;

import javax.inject.Named;

@Named
public class ActivityReportProducer {

    @Getter
    @AlpsPubsubPublisher(topic = "utopia.schedule.activity.report.topic", messageEncodeMode = MessageEncodeMode.PLAIN_TXT)
    private MessagePublisher messagePlainPublisher;

}
