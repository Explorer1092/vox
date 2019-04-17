package com.voxlearning.utopia.service.business.impl.support.order.userOrder.filter;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.payment.constant.PaymentConstants;
import com.voxlearning.utopia.service.business.impl.support.order.FilterChain;
import com.voxlearning.utopia.service.business.impl.support.order.userOrder.UserOrderFilter;
import com.voxlearning.utopia.service.business.impl.support.order.userOrder.UserOrderFilterContext;
import com.voxlearning.utopia.service.order.api.constants.PaymentStatus;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.api.entity.UserOrderPaymentHistory;
import com.voxlearning.utopia.service.user.api.entities.User;

import javax.inject.Named;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xinxin
 * @since 12/18/17
 */
@Named
public class UserActivationFilter extends UserOrderFilter {
    @Override
    public void doFilter(UserOrderFilterContext context, FilterChain chain) {
        UserOrder order = context.getOrder();
        if (order.getPaymentStatus() == PaymentStatus.Paid) {
            List<UserOrderPaymentHistory> userOrderPaymentHistories = userOrderLoaderClient.loadUserOrderPaymentHistoryList(order.getUserId());
            if (CollectionUtils.isNotEmpty(userOrderPaymentHistories)) {
                UserOrderPaymentHistory userOrderPaymentHistory = userOrderPaymentHistories.stream().filter(history -> history.getOrderId().equals(order.getId())).findFirst().orElse(null);
                if (null != userOrderPaymentHistory) {
                    if (userOrderPaymentHistory.getPayMethod().equals(PaymentConstants.PaymentGatewayname_Alipay_ParentApp) ||
                            userOrderPaymentHistory.getPayMethod().equals(PaymentConstants.PaymentGatewayName_ApplePay_ParentApp)
                            || userOrderPaymentHistory.getPayMethod().equals(PaymentConstants.PaymentGatewayName_Wechat_ParentApp)) {
                        User user = userLoaderClient.loadUser(order.getUserId());
                        if (null != user && user.fetchUserType() == UserType.PARENT) {
                            notifyUserLevelPayment(order.getUserId(), userOrderPaymentHistory.getPayAmount(), order.genUserOrderId());
                        } else if (StringUtils.isNotBlank(userOrderPaymentHistory.getComment())) {
                            Long payUserId = SafeConverter.toLong(userOrderPaymentHistory.getComment());
                            if (0 != payUserId) {
                                User payUser = userLoaderClient.loadUser(payUserId);
                                if (null != payUser && payUser.fetchUserType() == UserType.PARENT) {
                                    notifyUserLevelPayment(payUserId, userOrderPaymentHistory.getPayAmount(), order.genUserOrderId());
                                }
                            }
                        }
                    }
                }
            }
        }

        chain.doFilter(context);
    }

    private void notifyUserLevelPayment(Long userId, BigDecimal payAmount, String orderId) {
        Map<String, Object> info = new HashMap<>();
        info.put("type", "ORDER_PAYMENT");
        info.put("userId", userId);
        info.put("amount", payAmount);
        info.put("orderId", orderId);

        userLevelEventQueueProducer.getMessageProducer().produce(Message.newMessage().withPlainTextBody(JsonUtils.toJson(info)));
    }
}
