package com.voxlearning.utopia.agent.listener.activity;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.pubsub.PubsubDestination;
import com.voxlearning.alps.spi.pubsub.PubsubSubscriber;
import com.voxlearning.alps.spi.pubsub.PubsubSystem;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageListener;
import com.voxlearning.utopia.agent.service.activity.ActivityOrderRefService;
import com.voxlearning.utopia.agent.utils.QueueMessageUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Calendar;
import java.util.Map;

@Named
@PubsubSubscriber(
        destinations = {
                @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "equator.promote.products.plain.topic"),
                @PubsubDestination(system = PubsubSystem.KFK, connection = "main-backup", topic = "equator.promote.products.plain.topic"),
        }
)
public class ActivityOrderRefListener extends SpringContainerSupport implements MessageListener {

    @Inject
    private ActivityOrderRefService activityOrderRefService;

    @Override
    public void onMessage(Message message) {
        Map<String, Object> dataMap = QueueMessageUtils.decodeMessage(message);
        if(MapUtils.isEmpty(dataMap)){
            return;
        }

        String activityId = SafeConverter.toString(dataMap.get("activityId"), "");
        String orderId = SafeConverter.toString(dataMap.get("orderId"), "");
        Long orderUserId = SafeConverter.toLong(dataMap.get("parentId"));

        Integer layer = SafeConverter.toInt(dataMap.get("layer"));

        Long userId = SafeConverter.toLong(dataMap.get("marketId"));

        long payTime = SafeConverter.toLong(dataMap.get("orderTime"));
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(payTime);


        activityOrderRefService.handleListenerData(activityId, orderId, calendar.getTime(), orderUserId, layer, userId);
    }
}
