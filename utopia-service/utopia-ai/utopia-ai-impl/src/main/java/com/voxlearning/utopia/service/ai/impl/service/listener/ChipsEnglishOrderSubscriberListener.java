package com.voxlearning.utopia.service.ai.impl.service.listener;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.spi.pubsub.PubsubDestination;
import com.voxlearning.alps.spi.pubsub.PubsubSubscriber;
import com.voxlearning.alps.spi.pubsub.PubsubSystem;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageListener;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.ai.impl.context.ChipsOrderPostContext;
import com.voxlearning.utopia.service.ai.impl.service.processor.order.ChipsOrderPostHandleProcessor;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

@Named
@PubsubSubscriber(
        destinations = {
                @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "utopia.order.success.topic"),
                @PubsubDestination(system = PubsubSystem.KFK, connection = "main-backup", topic = "utopia.order.success.topic")
        },
        maxPermits = 4
)
public class ChipsEnglishOrderSubscriberListener implements MessageListener {

    private static final Logger logger = LoggerFactory.getLogger(ChipsEnglishOrderSubscriberListener.class);

    @Inject
    private ChipsOrderPostHandleProcessor chipsOrderPostHandleProcessor;

    @Override
    public void onMessage(Message message) {
        if (message == null) {
            logger.error("order success topic no message");
            return;
        }
        Object body = message.decodeBody();

        if (body != null && body instanceof String) {
            String json = (String) body;
            Map<String, Object> param = JsonUtils.fromJson(json);
            if (MapUtils.isEmpty(param)) {
                logger.error("order success topic no message");
                return;
            }

            if (!OrderProductServiceType.ChipsEnglish.name().equals(SafeConverter.toString(param.get("serviceType")))) {
                return;
            }

            String eventType = SafeConverter.toString(param.get("eventType"));
            if (!"PaySuccess".equalsIgnoreCase(eventType)) {
                logger.warn("order success topic pay is not success. message:{}", json);
                return;
            }

            long userId = SafeConverter.toLong(param.get("userId"), 0L);
            if (Long.compare(userId, 0L) <= 0) {
                logger.error("order success topic userId is null, message:{}", json);
                return;
            }

            String orderId = SafeConverter.toString(param.get("orderId"), "");
            if (StringUtils.isBlank(orderId)) {
                logger.error("orderId is not exists, message {}", json);
                return;
            }

            ChipsOrderPostContext context = new ChipsOrderPostContext();
            context.setOrderId(orderId);
            context.setUserId(userId);
            context.setParam(param);
            chipsOrderPostHandleProcessor.process(context);
        }
    }
}
