package com.voxlearning.utopia.service.campaign.impl.listener;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.pubsub.PubsubDestination;
import com.voxlearning.alps.spi.pubsub.PubsubSubscriber;
import com.voxlearning.alps.spi.pubsub.PubsubSystem;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageListener;
import com.voxlearning.utopia.service.campaign.impl.service.TeacherNewTermPlanListenerService;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;

@Named
@Slf4j
@PubsubSubscriber(
        destinations = {
                @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "utopia.campaign.new_term_plan_msg.topic"),
                @PubsubDestination(system = PubsubSystem.KFK, connection = "main-backup", topic = "utopia.campaign.new_term_plan_msg.topic")
        },
        maxPermits = 2
)
public class NewTermPlanMsgListener extends SpringContainerSupport implements MessageListener {

    @Inject
    private TeacherNewTermPlanListenerService teacherNewTermPlanListenerService;

    @Override
    public void onMessage(Message message) {

        // 用户 ID
        Object body = message.decodeBody();

        try {
            if (body instanceof String) {
                teacherNewTermPlanListenerService.handle(SafeConverter.toLong(body));
            }
        } catch (Exception e) {
            if (RuntimeMode.lt(Mode.STAGING)) {
                log.error(e.getMessage(), e);
            }
        }
    }
}