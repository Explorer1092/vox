package com.voxlearning.utopia.service.business.impl.support.order.userOrder.filter;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.business.impl.support.order.FilterChain;
import com.voxlearning.utopia.service.business.impl.support.order.userOrder.UserOrderFilter;
import com.voxlearning.utopia.service.business.impl.support.order.userOrder.UserOrderFilterContext;
import com.voxlearning.utopia.service.order.api.constants.OrderStatus;
import com.voxlearning.utopia.service.order.api.constants.PaymentStatus;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.api.entity.UserOrderPaymentHistory;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 *
 * Created by Summer on 2016/12/9.
 */
@Named
@Slf4j
public class MarkUserOrderPaidFilter extends UserOrderFilter {
    @Override
    public void doFilter(UserOrderFilterContext context, FilterChain chain) {
        // 未支付的单子
        if (context.getOrder().canBePaidOrCanceled()) {
            // FIXME 处理特殊情况，有PaymentHistory，但是Order不是Paid的只更新Order状态
            List<UserOrderPaymentHistory> paymentHistoryList = userOrderLoaderClient.loadUserOrderPaymentHistoryList(context.getOrder().getUserId());
            UserOrderPaymentHistory paymentHistory = paymentHistoryList.stream()
                    .filter(p -> Objects.equals(p.getOrderId(), context.getOrder().getId()))
                    .findAny().orElse(null);
            if (paymentHistory == null) {
                context.setOrder(markOrderToPaid(context.getOrder()));
                chain.doFilter(context); //可以继续后面的处理
            } else {
                context.setOrder(markOrderToPaid(context.getOrder()));
                log.info("has payment history but order is unpaid data found, order id:{}", context.getOrder().getId());
            }
        }
    }

    private UserOrder markOrderToPaid(UserOrder order) {
        order.setOrderStatus(OrderStatus.Confirmed);
        order.setPaymentStatus(PaymentStatus.Paid);
        order.setUpdateDatetime(new Date());
        // 修改订单状态
        MapMessage message = userOrderServiceClient.updateUserOrderStatus(order, PaymentStatus.Paid, OrderStatus.Confirmed);
        if (!message.isSuccess()) {
            throw new RuntimeException("Update User order status fail, id " + order.getId() + ", user " + order.getUserId());
        }
        return order;
    }
}
