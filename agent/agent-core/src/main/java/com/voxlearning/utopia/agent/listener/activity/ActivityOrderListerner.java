package com.voxlearning.utopia.agent.listener.activity;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
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
import com.voxlearning.utopia.agent.service.activity.ActivityOrderService;
import com.voxlearning.utopia.agent.service.activity.palace.PalaceActivityService;
import com.voxlearning.utopia.agent.utils.QueueMessageUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;

@Named
@PubsubSubscriber(
        destinations = {
                @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "utopia.order.payment.topic"),
                @PubsubDestination(system = PubsubSystem.KFK, connection = "main-backup", topic = "utopia.order.payment.topic"),
        }
)
public class ActivityOrderListerner extends SpringContainerSupport implements MessageListener {


    private static final List<String> orderServiceType = new ArrayList<>();
    static {
        orderServiceType.add("StudyMates");    // 英语绘本（家长通轻课）
        orderServiceType.add("PalaceMuseum");    // 故宫二十四节气
    }

    @Inject
    private ActivityOrderService activityOrderService;
    @Inject
    private PalaceActivityService palaceActivityService;

    @Override
    public void onMessage(Message message) {

        Map<String, Object> dataMap = QueueMessageUtils.decodeMessage(message);
        if(MapUtils.isEmpty(dataMap)){
            return;
        }



        // 过滤出故宫课程推广活动产生的订单
        // 对应 OrderProductServiceType.EnglishStoryBook
        String serviceType = SafeConverter.toString(dataMap.get("serviceType"), "");

        if(StringUtils.isBlank(serviceType) || !orderServiceType.contains(serviceType)){
            return;
        }

        Map<String, String> logMap = MapUtils.map("env", RuntimeMode.getCurrentStage(), "op", "agent_activity_order_message");
        logMap.put("messageInfo", JsonUtils.toJson(dataMap));
        LogCollector.info("backend-general", logMap);

        String orderId = SafeConverter.toString(dataMap.get("orderId"), "");

        long payTime = SafeConverter.toLong(dataMap.get("payTime"));
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(payTime);

        BigDecimal orderPayAmount = new BigDecimal(SafeConverter.toString(dataMap.get("payAmount"), "0"));
        Long orderUserId = SafeConverter.toLong(dataMap.get("userId"));

        // 故宫立春专用的订单处理， 故宫的其他课程要走通用的订单处理逻辑
        if(Objects.equals(serviceType, "PalaceMuseum")){
            palaceActivityService.resolveOrderData(orderId, calendar.getTime(), orderPayAmount, orderUserId);
        }else {
            // 通用的订单处理
            activityOrderService.handleListenerData("", orderId, calendar.getTime(), orderPayAmount, orderUserId, null);
        }

    }
}
