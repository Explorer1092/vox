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
import com.voxlearning.utopia.agent.service.honeycomb.HoneycombFansService;
import com.voxlearning.utopia.agent.utils.QueueMessageUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.Map;

@Named
@PubsubSubscriber(
        destinations = {
                @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "honeycomb.user.register.topic"),
                @PubsubDestination(system = PubsubSystem.KFK, connection = "main-backup", topic = "honeycomb.user.register.topic"),
        }
)
public class HoneycombFansListener extends SpringContainerSupport implements MessageListener {

    @Inject
    private HoneycombFansService fansService;

    @Override
    public void onMessage(Message message) {
        Map<String, Object> dataMap = QueueMessageUtils.decodeMessage(message);
        if(MapUtils.isEmpty(dataMap)){
            return;
        }

        Map<String, String> logMap = MapUtils.map("env", RuntimeMode.getCurrentStage(), "op", this.getClass().getSimpleName() + "-message");
        logMap.put("messageInfo", JsonUtils.toJson(dataMap));
        LogCollector.info("backend-general", logMap);

        Long honeycombId = SafeConverter.toLong(dataMap.get("inviterId"));
        Long fansId = SafeConverter.toLong(dataMap.get("userId"));
        long time = SafeConverter.toLong(dataMap.get("createTime"));

        fansService.handleMessageData(honeycombId, fansId, new Date(time));

    }
}
