package com.voxlearning.utopia.agent.listener;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.bootstrap.LogCollector;
import com.voxlearning.alps.spi.pubsub.PubsubDestination;
import com.voxlearning.alps.spi.pubsub.PubsubSubscriber;
import com.voxlearning.alps.spi.pubsub.PubsubSystem;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageListener;
import com.voxlearning.utopia.agent.service.honeycomb.HoneycombOrderService;
import com.voxlearning.utopia.agent.utils.QueueMessageUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

@Named
@PubsubSubscriber(
        destinations = {
                @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "honeycomb.order.agent.topic"),
                @PubsubDestination(system = PubsubSystem.KFK, connection = "main-backup", topic = "honeycomb.order.agent.topic"),
        }
)
public class HoneycombOrderListener extends SpringContainerSupport implements MessageListener {

    @Inject
    private HoneycombOrderService orderService;


    @Override
    public void onMessage(Message message) {
        Map<String, Object> dataMap = QueueMessageUtils.decodeMessage(message);
        if(MapUtils.isEmpty(dataMap)){
            return;
        }

        Map<String, String> logMap = MapUtils.map("env", RuntimeMode.getCurrentStage(), "op", this.getClass().getSimpleName() + "-message");
        logMap.put("messageInfo", JsonUtils.toJson(dataMap));
        LogCollector.info("backend-general", logMap);

        String orderType = SafeConverter.toString(dataMap.get("orderType"));
        if(Objects.equals(orderType, "PaySuccess")){
            String orderId = SafeConverter.toString(dataMap.get("orderId"));
            String productId = SafeConverter.toString(dataMap.get("productId"));
            long payTime = SafeConverter.toLong(dataMap.get("paymentTime"));
            Long honeycombId = SafeConverter.toLong(dataMap.get("userId"));

            orderService.handleMessageData(orderId, productId, new Date(payTime), honeycombId);
        }

    }
}
