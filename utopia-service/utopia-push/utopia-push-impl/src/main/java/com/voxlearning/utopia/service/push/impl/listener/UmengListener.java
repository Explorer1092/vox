package com.voxlearning.utopia.service.push.impl.listener;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.service.push.impl.handler.UmengHandler;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author xinxin
 * @since 10/11/2016
 */
@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(system = QueueSystem.KFK, config = "primary", queue = "utopia.umeng.queue"),
                @QueueDestination(system = QueueSystem.KFK, config = "main-backup", queue = "utopia.umeng.queue")
        },
        maxPermits = 256
)
public class UmengListener extends SpringContainerSupport implements MessageListener {

    @Inject
    private UmengHandler umengHandler;

    @Override
    public void onMessage(Message message) {
//        消费掉积压的消息
//        FlightRecorder.closeLog();
//
//        Object body = message.decodeBody();
//        if (body instanceof PushContext) {
//            PushContext context = (PushContext) body;
//
//            umengHandler.handle(context);
//        } else {
//            throw new IllegalStateException("Not support message," + JsonUtils.toJson(message.decodeBody()));
//        }
    }
}
