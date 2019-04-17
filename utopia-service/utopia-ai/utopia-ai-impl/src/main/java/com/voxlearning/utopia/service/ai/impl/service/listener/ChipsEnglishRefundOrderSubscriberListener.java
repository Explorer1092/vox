package com.voxlearning.utopia.service.ai.impl.service.listener;

import com.voxlearning.alps.core.util.CollectionUtils;
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
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishClass;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishClassUserRef;
import com.voxlearning.utopia.service.ai.entity.ChipsUserCourse;
import com.voxlearning.utopia.service.ai.entity.ChipsUserOrderExt;
import com.voxlearning.utopia.service.ai.impl.persistence.*;
import com.voxlearning.utopia.service.ai.util.StringExtUntil;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.api.entity.UserOrderProductRef;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Named
@PubsubSubscriber(
        destinations = {
                @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "utopia.order.refund.apply.topic"),
                @PubsubDestination(system = PubsubSystem.KFK, connection = "main-backup", topic = "utopia.order.refund.apply.topic")
        },
        maxPermits = 4
)
public class ChipsEnglishRefundOrderSubscriberListener implements MessageListener {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Inject
    private ChipsActivityInvitationPersistence activityInvitationPersistence;

    @Inject
    private ChipsGroupShoppingPersistence chipsGroupShoppingPersistence;

    @Inject
    private UserOrderLoaderClient userOrderLoaderClient;

    @Inject
    private ChipsEnglishClassPersistence chipsEnglishClassPersistence;

    @Inject
    private ChipsEnglishClassUserRefPersistence chipsEnglishClassUserRefPersistence;
    @Inject
    private ChipsActiveServiceRecordDao chipsActiveServiceRecordDao;

    @Inject
    private ChipsUserCoursePersistence chipsUserCoursePersistence;

    @Inject
    private ChipsUserOrderExtDao chipsUserOrderExtDao;

    @Override
    public void onMessage(Message message) {
        if (message == null) {
            logger.error("order refund topic no message");
            return;
        }
        Object body = message.decodeBody();

        if (body != null && body instanceof String) {
            String json = (String) body;
            ChipsOrderRefund orderRefund = JsonUtils.fromJson(json, ChipsOrderRefund.class);
            if (orderRefund == null) {
                logger.error("order refund topic no message");
                return;
            }

            String eventType = SafeConverter.toString(orderRefund.getEventType());
            if (!"OrderRefund".equals(eventType)) {
                return;
            }
            String orderId = orderRefund.getOrderId();
            if (StringUtils.isBlank(orderId)) {
                logger.error("order refund topic no orderId. message:{}", json);
                return;
            }
            UserOrder userOrder = userOrderLoaderClient.loadUserOrderIncludeCanceled(orderId);
            if (userOrder == null || OrderProductServiceType.safeParse(userOrder.getOrderProductServiceType()) != OrderProductServiceType.ChipsEnglish) {
                return;
            }

            processUserClazz(userOrder, orderRefund);

            ChipsUserOrderExt chipsUserOrderExt = chipsUserOrderExtDao.load(orderId);
            if (chipsUserOrderExt == null) {
                return;
            }
            chipsUserOrderExt.setUpdateDate(new Date());
            chipsUserOrderExt.setStatus(ChipsUserOrderExt.OrderStatus.REFUND);
            chipsUserOrderExtDao.upsert(chipsUserOrderExt);

            String code = StringExtUntil.md5(userOrder.getId() + userOrder.getUserId().toString());
            chipsGroupShoppingPersistence.updateNumber(code, 1, 0);

            if (chipsUserOrderExt.getInviter() == null) {
                return;
            }

            activityInvitationPersistence.inserOrUpdate(chipsUserOrderExt.getInviter(), chipsUserOrderExt.getUserId(), userOrder.getProductId(), 3);
        }
    }

    private void processUserClazz(UserOrder userOrder, ChipsOrderRefund orderRefund) {
        try {
            List<ChipsUserCourse> chipsUserCourseList = chipsUserCoursePersistence.loadByUserId(userOrder.getUserId()).stream().filter(e -> e.getOrderId().equals(userOrder.getId())).collect(Collectors.toList());
            Set<String> productIds = new HashSet<>();
            List<UserOrderProductRef> orderProductRefs = userOrderLoaderClient.loadOrderProducts(userOrder.getUserId(), userOrder.getId());
            if (CollectionUtils.isNotEmpty(orderProductRefs)) {
                productIds.addAll(orderProductRefs.stream()
                        .map(UserOrderProductRef::getProductId)
                        .filter(e -> {
                            OrderProductItem productItem = userOrderLoaderClient.loadProductItemsByProductId(e).stream()
                                    .filter(item -> MapUtils.isNotEmpty(orderRefund.getRefundItems()) && orderRefund.getRefundItems().get(item.getId()) != null)
                                    .findFirst().orElse(null);
                            return productItem != null;
                        }).collect(Collectors.toSet()));
            } else {
                productIds.add(userOrder.getProductId());
            }

            for (String productId : productIds) {
                List<ChipsUserCourse> courseList = chipsUserCourseList.stream()
                        .filter(e -> e.getOriginalProductId().equals(productId))
                        .filter(e -> orderRefund.getRefundItems().get(e.getOriginalProductItemId()) != null)
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(courseList)) {
                    courseList.forEach(course -> chipsUserCoursePersistence.disabled(course));
                }

                List<ChipsEnglishClass> chipsEnglishClassList = new ArrayList<>();
                courseList.stream().map(c -> chipsEnglishClassPersistence.loadByProductId(c.getProductId())).forEach(l -> chipsEnglishClassList.addAll(l));

//                List<ChipsEnglishClass> chipsEnglishClassList = chipsEnglishClassPersistence.loadByProductId(productId);
                if (CollectionUtils.isEmpty(chipsEnglishClassList)) {
                    continue;
                }
                List<ChipsEnglishClassUserRef> userRefList = chipsEnglishClassUserRefPersistence.loadByUserId(userOrder.getUserId());
                if (CollectionUtils.isEmpty(userRefList)) {
                    continue;
                }
                Set<Long> chipsClassIds = chipsEnglishClassList.stream().map(ChipsEnglishClass::getId).collect(Collectors.toSet());
                ChipsEnglishClassUserRef userRef = userRefList.stream().filter(e -> chipsClassIds.contains(e.getChipsClassId())).findFirst().orElse(null);
                if (userRef != null) {
                    chipsEnglishClassUserRefPersistence.disabled(userRef);
                }
                chipsClassIds.forEach(clazzId -> {
                    chipsActiveServiceRecordDao.disabled(userOrder.getUserId(), clazzId);
                });
            }
        } catch (Exception e) {
            logger.error("call deleteFromUserChipsClazz error. order:{}, user:{}", userOrder.getId(), userOrder.getUserId(), e);
        }
    }

    @Getter
    @Setter
    private static class ChipsOrderRefund implements Serializable {
        private static final long serialVersionUID = -3416169051938585844L;
        private String orderId;
        private String eventType;
        private Long userId;
        private Map<String, BigDecimal> refundItems;

    }
}
