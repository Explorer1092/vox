package com.voxlearning.utopia.service.afenti.impl.listener;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.spi.pubsub.PubsubDestination;
import com.voxlearning.alps.spi.pubsub.PubsubSubscriber;
import com.voxlearning.alps.spi.pubsub.PubsubSystem;
import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageListener;
import com.voxlearning.alps.spi.queue.MessageProducer;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.afenti.api.context.OrderPaySuccessContext;
import com.voxlearning.utopia.service.afenti.impl.service.UserPicBookServiceImpl;
import com.voxlearning.utopia.service.afenti.impl.service.processor.order.AfentiOrderPaySuccessProcessor;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.api.entity.UserOrderProductRef;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.api.constant.OrderProductServiceType.*;

/**
 * @author Ruib
 * @since 2017/10/10
 */
@Named
@PubsubSubscriber(
        destinations = {
                @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "utopia.order.success.topic"),
                @PubsubDestination(system = PubsubSystem.KFK, connection = "main-backup", topic = "utopia.order.success.topic")
        }
)
public class AfentiOrderSubscriberListener implements MessageListener {
    public static final List<OrderProductServiceType> afenti = Arrays.asList(AfentiExam, AfentiMath, AfentiChinese);
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Inject AfentiOrderPaySuccessProcessor processor;
    @Inject UserPicBookServiceImpl userPicBookService;
    @Inject UserOrderLoaderClient usrOrderLoaderCli;

    @AlpsQueueProducer(queue = "utopia.wonderland.queue") private MessageProducer messageProducer;


    @Override
    public void onMessage(Message message) {
        Map<String, Object> map = null;
        Object decoded = message.decodeBody();
        if (decoded instanceof String) {
            String messageText = (String) decoded;
            map = JsonUtils.fromJson(messageText);
        }
        if (decoded instanceof Map) map = (Map) decoded;

        if (map == null) {
            logger.error("AfentiOrderSubscriberListener error message {}", JsonUtils.toJson(message.decodeBody()));
            return;
        }

        Long studentId = MapUtils.getLong(map, "userId");
        String eventType = SafeConverter.toString(map.get("eventType"));
        switch (eventType) {
            case "PaySuccess": {
                OrderProductServiceType type = OrderProductServiceType.safeParse(SafeConverter.toString(map.get("serviceType")));
                if (afenti.contains(type)) {

                    int period = SafeConverter.toInt(map.get("period"));
                    if (studentId != 0L && period != 0)
                        processor.process(new OrderPaySuccessContext(studentId, type.name(), period));
                } else if (type == OrderProductServiceType.ELevelReading) {
                    String userOrderId = SafeConverter.toString(map.get("orderId"));
                    // 按照格式解析orderId
                    String orderId = Arrays.stream(userOrderId.split(UserOrder.SEP))
                            .findFirst()
                            .orElse(null);

                    if (orderId == null)
                        return;

                    // 一个订单可能对应多个绘本
                    List<String> productIds = usrOrderLoaderCli.loadOrderProducts(studentId, orderId)
                            .stream()
                            .map(UserOrderProductRef::getProductId)
                            .collect(Collectors.toList());

                    if (CollectionUtils.isEmpty(productIds)) {
                        productIds.add(SafeConverter.toString(map.get("productId")));
                    }

                    List<String> picBookIds = new ArrayList<>();
                    usrOrderLoaderCli.loadProductItemsByProductIds(productIds)
                            .values()
                            .stream()
                            .flatMap(Collection::stream)
                            .map(OrderProductItem::getAppItemId)
                            .forEach(item -> {
                                picBookIds.add(item);
                                MapMessage msg = userPicBookService.createUserPicBook(studentId, item, type.name());
                                if (!msg.isSuccess())
                                    logger.error("AfentiOrderSubscriberListener:Create user pic book error!stuId:{},bookId:{},orderId:{},detail:{}"
                                            , studentId, item, orderId, msg.getInfo());
                            });

                    // 发奖励  根据绘本系列
//                    Map<String, List<PicBookPurchaseProp>> rewardMap = userPicBookService.getRewardListByBookIds(picBookIds);
//                    List<Map<String, Object>> totalList = new ArrayList<>();
//                    for (Map.Entry<String, List<PicBookPurchaseProp>> entry : rewardMap.entrySet()) {
//                        List<PicBookPurchaseProp> rewardList = entry.getValue();
//                        if (CollectionUtils.isNotEmpty(rewardList)) {
//                            for (PicBookPurchaseProp prop : rewardList) {
//                                Map<String, Object> exist = totalList.stream().filter(p -> p != null && p.get("id") != null
//                                        && StringUtils.equals(prop.getId(), SafeConverter.toString(p.get("id"))))
//                                        .findFirst().orElse(null);
//                                if (exist == null) {
//                                    exist = new HashMap<>();
//                                    exist.put("id", prop.getId());
//                                    exist.put("num", prop.getNum());
//                                    totalList.add(exist);
//                                } else {
//                                    int addAfter = SafeConverter.toInt(exist.get("num")) + prop.getNum();
//                                    exist.put("num", addAfter);
//                                }
//                            }
//                        }
//                    }
//                    WonderlandEvent event = new WonderlandEvent();
//                    event.setType(WonderlandEventType.PicBookReward);
//                    event.getAttributes().put("rewardList", totalList);
//                    event.getAttributes().put("studentId", studentId);
//                    messageProducer.produce(event.toMessage());
                }
                break;
            }
        }
    }
}
