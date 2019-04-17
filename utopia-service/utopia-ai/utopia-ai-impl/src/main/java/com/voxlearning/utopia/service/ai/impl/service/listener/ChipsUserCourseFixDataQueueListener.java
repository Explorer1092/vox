package com.voxlearning.utopia.service.ai.impl.service.listener;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.ai.entity.ChipsUserCourse;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsUserCoursePersistence;
import com.voxlearning.utopia.service.ai.internal.ChipsUserService;
import com.voxlearning.utopia.service.ai.util.CollectionExtUtil;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.order.api.entity.UserActivatedProduct;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.api.entity.UserOrderProductRef;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 修复用户视频数据修复
 */
@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(system = QueueSystem.KFK, config = "primary", queue = "utopia.chips.user.course.fix.data.queue"),
                @QueueDestination(system = QueueSystem.KFK, config = "main-backup", queue = "utopia.chips.user.course.fix.data.queue")
        },
        maxPermits = 4
)
@Deprecated
public class ChipsUserCourseFixDataQueueListener implements MessageListener {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private ChipsUserService chipsUserService;

    @Inject
    private ChipsUserCoursePersistence chipsUserCoursePersistence;

    @Inject
    private UserOrderLoaderClient userOrderLoaderClient;

    @Override
    public void onMessage(Message message) {
        if (message == null) {
            logger.error("user course handle queue no message");
            return;
        }
        Object body = message.decodeBody();

        if (body instanceof String) {
            String json = (String) body;
            Map<String, Object> param = JsonUtils.fromJson(json);
            if (param == null) {
                logger.error("ChipsUserCourseFixDataQueueListener error. message:{}", body);
                return;
            }

            String productId = SafeConverter.toString(param.get("P"));
            long user = SafeConverter.toLong(param.get("U"));
            if (StringUtils.isBlank(productId) || user <= 0L) {
                return;
            }

            List<OrderProductItem> orderProductItemList = userOrderLoaderClient.loadProductItemsByProductId(productId);
            if (CollectionUtils.isEmpty(orderProductItemList)) {
                return;
            }
            Set<String> itemSet = orderProductItemList.stream().map(OrderProductItem::getId).collect(Collectors.toSet());
            Set<String> courseItemSet = null;
            Map<String, List<ChipsUserCourse>> userCourseMap = chipsUserService.loadUserEffectiveCourse(user).stream().collect(Collectors.groupingBy(ChipsUserCourse::getOriginalProductId));
            boolean empty = MapUtils.isEmpty(userCourseMap) || CollectionUtils.isEmpty(userCourseMap.get(productId));
            boolean insert = empty || userCourseMap.get(productId).size() != orderProductItemList.size()
                            || !CollectionExtUtil.equivalent(courseItemSet = userCourseMap.get(productId).stream().map(ChipsUserCourse::getOriginalProductItemId).collect(Collectors.toSet()), itemSet);
            if (!insert) {
                return;
            }

            List<UserActivatedProduct> userActivatedProductList = userOrderLoaderClient.loadUserActivatedProductList(user).stream()
                    .filter(item -> item.getDisabled() != null && !item.getDisabled())
                    .filter(item -> item.getProductServiceType().equals(OrderProductServiceType.ChipsEnglish.name()))
                    .filter(item -> itemSet.contains(item.getProductItemId()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(userActivatedProductList)) {
                return;
            }

            Map<String, UserActivatedProduct> userActivateMap = userActivatedProductList.stream().collect(Collectors.toMap(UserActivatedProduct::getProductItemId, e -> e));

            List<UserOrder> userOrders = userOrderLoaderClient.loadUserPaidOrders(OrderProductServiceType.ChipsEnglish.name(), user);
            if (CollectionUtils.isEmpty(userOrders)) {
                return;
            }
            Set<String> userItemSet = courseItemSet;
            for(UserOrder userOrder : userOrders) {
                if (productId.equals(userOrder.getProductId())) {
                    if (empty) {
                        doSave(userOrder.getId(), productId, itemSet, user, userActivateMap);
                    } else {
                        doSave(userOrder.getId(), productId, itemSet.stream().filter(it -> userItemSet != null && !userItemSet.contains(it)).collect(Collectors.toList()), user, userActivateMap);
                    }
                    break;
                }

                List<UserOrderProductRef> userOrderProductRefList = userOrderLoaderClient.loadOrderProducts(user, userOrder.getId());
                if (CollectionUtils.isEmpty(userActivatedProductList)) {
                    continue;
                }
                UserOrderProductRef orderProductRef = userOrderProductRefList.stream().filter(ref -> productId.equals(ref.getProductId())).findFirst().orElse(null);

                if (orderProductRef != null) {
                    if (empty) {
                        doSave(userOrder.getId(), productId, itemSet, user, userActivateMap);
                    } else {
                        doSave(userOrder.getId(), productId, itemSet.stream().filter(it -> userItemSet != null && !userItemSet.contains(it)).collect(Collectors.toList()), user, userActivateMap);
                    }
                    break;
                }
            }
        }
    }

    private void doSave(String orderId, String productId, Collection<String> productItemIds, Long userId, Map<String, UserActivatedProduct> userActivateMap) {
        for(String productItem : productItemIds) {
            UserActivatedProduct userActivatedProduct = userActivateMap.get(productItem);
            if (userActivatedProduct == null) {
                continue;
            }
            ChipsUserCourse chipsUserCourse = ChipsUserCourse.initNewCourse(userId, orderId, productId, productItem, userActivatedProduct.getServiceStartTime(), userActivatedProduct.getServiceEndTime());
            chipsUserCoursePersistence.insert(chipsUserCourse);
        }
    }
}
