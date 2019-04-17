/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.business.impl.support.order.userOrder.filter;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.spi.pubsub.AlpsPubsubPublisher;
import com.voxlearning.alps.spi.pubsub.MessagePublisher;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageEncodeMode;
import com.voxlearning.utopia.entity.payment.PaymentVerifiedData;
import com.voxlearning.utopia.service.business.impl.support.order.FilterChain;
import com.voxlearning.utopia.service.business.impl.support.order.userOrder.UserOrderFilter;
import com.voxlearning.utopia.service.business.impl.support.order.userOrder.UserOrderFilterContext;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Summer
 * @since 2017/1/6
 */
@Slf4j
@Named
public class AppOrderRemindPubsubFilter extends UserOrderFilter {

    @AlpsPubsubPublisher(topic = "utopia.order.success.topic", messageEncodeMode = MessageEncodeMode.PLAIN_TXT)
    private MessagePublisher messagePublisher;

    @Override
    public void doFilter(UserOrderFilterContext context, FilterChain chain) {
        UserOrder order = context.getOrder();
        PaymentVerifiedData paymentVerifiedData = context.getCallbackContext().getVerifiedPaymentData();
        try {
            List<OrderProductItem> orderProductItems = userOrderLoaderClient.loadProductItemsByProductId(order.getProductId());
            if (CollectionUtils.isNotEmpty(orderProductItems)) {
                Map<String, Object> event = new HashMap<>();
                event.put("eventType", "PaySuccess");
                event.put("orderId", order.genUserOrderId());
                event.put("userId", order.getUserId());
                event.put("productId", order.getProductId());
                event.put("productName", order.getProductName());
                event.put("serviceType", order.getOrderProductServiceType());
                event.put("status", order.getOrderStatus());
                event.put("payTime", System.currentTimeMillis());
                event.put("payType", order.getPayType());
                event.put("period", orderProductItems.get(0).getPeriod());
                event.put("itemId", orderProductItems.get(0).getId());
                event.put("appItemId", orderProductItems.get(0).getAppItemId());
                event.put("registerOrderTime", order.getCreateDatetime().getTime());
                event.put("extAttr", order.getExtAttributes());
                event.put("externalTradeNumber", paymentVerifiedData.getExternalTradeNumber());
                event.put("payMethodGateway", context.getCallbackContext().getPayMethodGateway());
                event.put("payAmount", paymentVerifiedData.getPayAmount());
                messagePublisher.publish(Message.newMessage().withPlainTextBody(JsonUtils.toJson(event)));
            }
        } catch (Exception e) {
            log.error("send wonderland paysuccess error:orderId={}", order.getId());
        }
        chain.doFilter(context); //可以继续后面的处理
    }
}
