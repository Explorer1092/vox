package com.voxlearning.utopia.service.business.impl.support.order.userOrder.filter;

import com.voxlearning.utopia.payment.constant.PaymentConstants;
import com.voxlearning.utopia.service.business.impl.support.order.FilterChain;
import com.voxlearning.utopia.service.business.impl.support.order.userOrder.UserOrderFilter;
import com.voxlearning.utopia.service.business.impl.support.order.userOrder.UserOrderFilterContext;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageTag;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageType;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Summer on 2017/4/24.
 */
@Slf4j
@Named
public class FinanceDebitRemindFilter extends UserOrderFilter {
    @Inject private MessageCommandServiceClient messageCommandServiceClient;

    @Override
    public void doFilter(UserOrderFilterContext context, FilterChain chain) {
        if (context.getCallbackContext().getPayMethodGateway().startsWith(PaymentConstants.PaymentGatewayName_17Zuoye)
                && context.getCallbackContext().getFinanceUserId() != null) {

            try {
                // 作业币支付的发送家长站内信
                BigDecimal payAmount = context.getCallbackContext().getVerifiedPaymentData().getPayAmount();
                payAmount = payAmount.setScale(2, BigDecimal.ROUND_HALF_UP);
                String content = "购买" + context.getOrder().getProductName() + "成功， 消费" + payAmount + "学贝。";
                AppMessage message = new AppMessage();
                message.setUserId(context.getCallbackContext().getFinanceUserId());
                message.setMessageType(ParentMessageType.REMINDER.type);
                message.setTitle("通知");
                message.setContent(content);
                Map<String, Object> extInfo = new HashMap<>();
                extInfo.put("tag", ParentMessageTag.订单.name());
                message.setExtInfo(extInfo);
                messageCommandServiceClient.getMessageCommandService().createAppMessage(message);
            } catch (Exception e) {
                log.error("failed to send voxpay notify. order id {}", context.getOrder().getId(), e);
            }

        }
        chain.doFilter(context); // go on
    }
}
