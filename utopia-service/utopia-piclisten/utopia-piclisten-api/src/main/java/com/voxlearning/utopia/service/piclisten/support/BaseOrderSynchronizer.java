package com.voxlearning.utopia.service.piclisten.support;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.api.entity.UserOrderProductRef;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;

import javax.inject.Inject;
import java.util.List;

/**
 *
 * Created by alex on 2018/2/2.
 */
public abstract class BaseOrderSynchronizer implements OrderSynchronizer {

    @Inject  private UserOrderLoaderClient userOrderLoaderClient;

    public Long getRealNotifyUserId(UserOrder order) {
        List<UserOrderProductRef> orderProducts = userOrderLoaderClient.loadOrderProducts(order.getUserId(), order.getId());
        if (CollectionUtils.isNotEmpty(orderProducts)) {
            UserOrderProductRef orderProduct = orderProducts.stream().filter(p -> OrderProductServiceType.safeParse(p.getOrderProductServiceType()) == OrderProductServiceType.PicListenBook).findFirst().orElse(null);
            if (orderProduct != null) {
                return orderProduct.getRelatedUserId();
            }
        }

        return order.getUserId();
    }
}
