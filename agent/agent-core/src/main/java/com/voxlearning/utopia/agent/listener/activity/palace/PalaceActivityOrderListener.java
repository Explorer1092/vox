package com.voxlearning.utopia.agent.listener.activity.palace;

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
import com.voxlearning.utopia.agent.service.activity.palace.PalaceActivityService;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Map;
import java.util.Objects;

@Named
//@PubsubSubscriber(
//        destinations = {
//                @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "utopia.order.payment.topic"),
//                @PubsubDestination(system = PubsubSystem.KFK, connection = "main-backup", topic = "utopia.order.payment.topic"),
//        }
//)
public class PalaceActivityOrderListener extends SpringContainerSupport implements MessageListener {

    @Inject
    private PalaceActivityService palaceActivityService;

    @Override
    public void onMessage(Message message) {
        Map<String, Object> dataMap = null;
        Object decoded = message.decodeBody();

        if (decoded instanceof String) {
            String messageText = (String) decoded;
            dataMap = JsonUtils.fromJson(messageText);
        }else if (decoded instanceof Map)
            dataMap = (Map) decoded;

        if(MapUtils.isEmpty(dataMap)){
            return;
        }



        // 过滤出故宫课程推广活动产生的订单
        // 对应 OrderProductServiceType.PalaceMuseum
        String serviceType = SafeConverter.toString(dataMap.get("serviceType"), "");

        if(StringUtils.isBlank(serviceType) || !Objects.equals(serviceType, "PalaceMuseum")){
            return;
        }

        Map<String, String> logMap = MapUtils.map("env", RuntimeMode.getCurrentStage(), "op", "palace_order_message");
        logMap.put("messageInfo", JsonUtils.toJson(dataMap));
        LogCollector.info("backend-general", logMap);

        String orderId = SafeConverter.toString(dataMap.get("orderId"), "");

        long payTime = SafeConverter.toLong(dataMap.get("payTime"));
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(payTime);

        BigDecimal orderPayAmount = new BigDecimal(SafeConverter.toString(dataMap.get("payAmount"), "0"));
        Long orderUserId = SafeConverter.toLong(dataMap.get("userId"));

        palaceActivityService.resolveOrderData(orderId, calendar.getTime(), orderPayAmount, orderUserId);
    }
}
