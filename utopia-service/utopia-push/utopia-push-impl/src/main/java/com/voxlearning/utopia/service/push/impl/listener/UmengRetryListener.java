package com.voxlearning.utopia.service.push.impl.listener;

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.service.push.impl.handler.UmengRetryHandler;
import com.voxlearning.utopia.service.push.api.support.PushRetryContext;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author xinxin
 * @since 14/11/2016.
 */
@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(
                        system = QueueSystem.KFK,
                        config = "primary",
                        queue = "utopia.umeng.retry.queue"
                ),
                @QueueDestination(
                        system = QueueSystem.KFK,
                        config = "main-backup",
                        queue = "utopia.umeng.retry.queue"
                )
        }
)
public class UmengRetryListener extends SpringContainerSupport implements MessageListener {
    @Inject
    private UmengRetryHandler umengHandler;

    @Override
    public void onMessage(Message message) {
        Object body = message.decodeBody();
        if (logger.isDebugEnabled()) {
            logger.debug("Message received: {}", JsonUtils.toJson(body));
        }

        if (body instanceof PushRetryContext) {
            umengHandler.handle((PushRetryContext) body);
        } else {
            throw new IllegalArgumentException("Not support msg type," + JsonUtils.toJson(body));
        }

    }
}
