package com.voxlearning.utopia.service.business.impl.activity.listener;

import com.voxlearning.alps.spi.pubsub.AlpsPubsubPublisher;
import com.voxlearning.alps.spi.pubsub.MessagePublisher;
import com.voxlearning.alps.spi.queue.MessageEncodeMode;
import lombok.Getter;

import javax.inject.Named;

@Named
public class ActivityReportProducer {

    public static final String ACTIVITY_REPORT_TOPIC = "utopia.schedule.activity.report.topic";
    public static final String ACTIVITY_REPORT_EXPORT_TOPIC = "utopia.schedule.activity.report.export.topic";

    @Getter
    @AlpsPubsubPublisher(topic = ACTIVITY_REPORT_TOPIC, messageEncodeMode = MessageEncodeMode.PLAIN_TXT)
    private MessagePublisher messagePlainPublisher;

    @Getter
    @AlpsPubsubPublisher(topic = ACTIVITY_REPORT_EXPORT_TOPIC, messageEncodeMode = MessageEncodeMode.PLAIN_TXT)
    private MessagePublisher exportPlanPublisher;

}
