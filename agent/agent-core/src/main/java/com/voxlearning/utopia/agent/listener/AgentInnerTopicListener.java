package com.voxlearning.utopia.agent.listener;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.spi.pubsub.PubsubDestination;
import com.voxlearning.alps.spi.pubsub.PubsubSubscriber;
import com.voxlearning.alps.spi.pubsub.PubsubSystem;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageListener;
import com.voxlearning.utopia.agent.constants.AgentConstants;
import com.voxlearning.utopia.agent.persist.entity.honeycomb.HoneycombFans;
import com.voxlearning.utopia.agent.persist.entity.honeycomb.HoneycombOrder;
import com.voxlearning.utopia.agent.service.honeycomb.HoneycombFansStatisticsService;
import com.voxlearning.utopia.agent.service.honeycomb.HoneycombOrderStatisticsService;
import com.voxlearning.utopia.agent.utils.QueueMessageUtils;
import com.voxlearning.utopia.core.utils.LoggerUtils;
import com.voxlearning.utopia.core.utils.MQUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;
import java.util.Objects;

@Named
@PubsubSubscriber(
        destinations = {
                @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "utopia.agent.queue.inner"),
                @PubsubDestination(system = PubsubSystem.KFK, connection = "main-backup", topic = "utopia.agent.queue.inner"),
        }
)
public class AgentInnerTopicListener extends SpringContainerSupport implements MessageListener {

    @Inject
    private HoneycombOrderStatisticsService honeycombOrderStatisticsService;
    @Inject
    private HoneycombFansStatisticsService fansStatisticsService;

    @Override
    public void onMessage(Message message) {


        Map<String, Object> dataMap = QueueMessageUtils.decodeMessage(message);
        if(MapUtils.isEmpty(dataMap)){
            return;
        }

        LoggerUtils.info("utopia.agent.queue.inner", dataMap);

        String type = SafeConverter.toString(dataMap.get("type"), "");
        if(StringUtils.isBlank(type)){
            return;
        }

        try {
            if (Objects.equals(type, "HoneycombOrder")) {
                String data = SafeConverter.toString(dataMap.get("data"), "");
                HoneycombOrder honeycombOrder = JsonUtils.fromJson(data, HoneycombOrder.class);
                honeycombOrderStatisticsService.orderStatistics(honeycombOrder);
            } else if (Objects.equals(type, "HoneycombFans")) {
                String data = SafeConverter.toString(dataMap.get("data"), "");
                HoneycombFans fans = JsonUtils.fromJson(data, HoneycombFans.class);
                fansStatisticsService.fansStatistics(fans);
            }
        }catch (Exception e){
            int sleepTime = RandomUtils.nextInt(100, 400);
            try {
                Thread.sleep(sleepTime);
            }catch (Exception e2){
            }

            int retryTimes = SafeConverter.toInt(dataMap.get("retryTimes"));
            if(retryTimes < 100){
                dataMap.put("retryTimes", ++retryTimes);
                MQUtils.send(AgentConstants.AGENT_INNER_TOPIC, dataMap);
            }else {
                LoggerUtils.error(AgentConstants.AGENT_INNER_TOPIC, e, dataMap);
            }
        }
    }
}
