package com.voxlearning.utopia.service.business.impl.support.order.userOrder.filter;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.spi.pubsub.AlpsPubsubPublisher;
import com.voxlearning.alps.spi.pubsub.MessagePublisher;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageEncodeMode;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.entity.payment.PaymentVerifiedData;
import com.voxlearning.utopia.service.business.impl.support.order.FilterChain;
import com.voxlearning.utopia.service.business.impl.support.order.userOrder.UserOrderFilter;
import com.voxlearning.utopia.service.business.impl.support.order.userOrder.UserOrderFilterContext;
import com.voxlearning.utopia.service.coupon.api.entities.CouponUserRef;
import com.voxlearning.utopia.service.order.api.entity.*;
import com.voxlearning.utopia.service.order.api.util.UserOrderUtil;
import com.voxlearning.utopia.service.user.api.entities.User;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;

/**
 * 订单支付成功通知
 * Created by alex on 2018/5/3.
 */
@Slf4j
@Named
public class OrderPaymentPubsubFilter extends UserOrderFilter {

    @AlpsPubsubPublisher(topic = "utopia.order.payment.topic", messageEncodeMode = MessageEncodeMode.PLAIN_TXT)
    private MessagePublisher messagePublisher;

    @Override
    public void doFilter(UserOrderFilterContext context, FilterChain chain) {
        UserOrder order = context.getOrder();
        PaymentVerifiedData paymentVerifiedData = context.getCallbackContext().getVerifiedPaymentData();

        User user = userLoaderClient.loadUser(order.getUserId());

        try {
            Set<String> productList = new HashSet<>();
            List<UserOrderProductRef> orderProducts = userOrderLoaderClient.loadOrderProducts(order.getUserId(), order.getId());
            if (CollectionUtils.isNotEmpty(orderProducts)) {
                for (UserOrderProductRef orderProductRef : orderProducts) {
                    productList.add(orderProductRef.getProductId());
                }
            } else {
                productList.add(order.getProductId());
            }

            Map<String, List<OrderProductItem>> orderItems = userOrderLoaderClient.loadProductItemsByProductIds(productList);
            Map<String, BigDecimal> paymentShare = UserOrderUtil.calculateAmortizeAmount(order, orderProducts, orderItems, paymentVerifiedData.getPayAmount());

            CouponUserRef couponUserRef = couponLoaderClient.loadCouponUserRefById(order.getCouponRefId());

            for (String productId : productList) {
                OrderProduct product = userOrderLoaderClient.loadOrderProductById(productId);
                List<OrderProductItem> itemList = orderItems.get(productId);

                // 一起学直播自己的商品
                if (product == null || CollectionUtils.isEmpty(itemList)) {
                    Map<String, Object> event = new HashMap<>();
                    event.put("eventType", "PaySuccess");
                    event.put("orderId", order.genUserOrderId());
                    event.put("userId", order.getUserId());
                    event.put("productId",  order.getProductId());
                    event.put("productName", order.getProductName());
                    event.put("extAttr", order.getExtAttributes());
                    event.put("serviceType", order.getOrderProductServiceType());
                    event.put("status", order.getOrderStatus());
                    event.put("payTime", System.currentTimeMillis());
                    event.put("payType", order.getPayType());
                    event.put("registerOrderTime", order.getCreateDatetime().getTime());
                    event.put("externalTradeNumber", paymentVerifiedData.getExternalTradeNumber());
                    event.put("payMethodGateway", context.getCallbackContext().getPayMethodGateway());
                    event.put("payAmount", paymentVerifiedData.getPayAmount());
                    event.put("orderReferer",order.getOrderReferer());
                    if(Objects.nonNull(couponUserRef)){
                        event.put("couponRefId",couponUserRef.getId());
                        event.put("couponRefReferer",couponUserRef.getChannel());
                    }
                    messagePublisher.publish(Message.newMessage().withPlainTextBody(JsonUtils.toJson(event)));
                    continue;
                }

                // 点读机需要换成家长ID
                Long targetUserId = order.getUserId();
                if (user.isStudent() && OrderProductServiceType.safeParse(product.getProductType()) == OrderProductServiceType.PicListenBook) {
                    UserOrderProductRef picListenProduct = userOrderLoaderClient.loadOrderProducts(order.getUserId(), order.getId()).stream()
                            .filter(p -> Objects.equals(p.getOrderProductServiceType(), product.getProductType()))
                            .filter(p -> p.getRelatedUserId() != null)
                            .findFirst()
                            .orElse(null);
                    if (picListenProduct != null) {
                        targetUserId = picListenProduct.getRelatedUserId();
                    } else {
                        log.warn("no parent user found with piclisten book product, order id:{}", order.getId());
                    }
                }

                // 平台内注册的商品以ITEM为单位向外派发通知
                for (OrderProductItem productItem : itemList) {
                    Map<String, Object> event = new HashMap<>();
                    event.put("eventType", "PaySuccess");
                    event.put("orderId", order.genUserOrderId());
                    event.put("userId", targetUserId);
                    event.put("extAttr", order.getExtAttributes());
                    event.put("productId", product.getId());
                    event.put("productName", product.getName());
                    event.put("serviceType", productItem.getProductType());
                    event.put("status", order.getOrderStatus());
                    event.put("payTime", System.currentTimeMillis());
                    event.put("payType", order.getPayType());
                    event.put("period", productItem.getPeriod());
                    event.put("itemId", productItem.getId());
                    event.put("appItemId", productItem.getAppItemId());
                    event.put("registerOrderTime", order.getCreateDatetime().getTime());
                    event.put("externalTradeNumber", paymentVerifiedData.getExternalTradeNumber());
                    event.put("payMethodGateway", context.getCallbackContext().getPayMethodGateway());
                    event.put("payAmount", paymentVerifiedData.getPayAmount());
                    event.put("shareAmount", paymentShare.get(productItem.getId()));
                    event.put("orderReferer",order.getOrderReferer());
                    if(Objects.nonNull(couponUserRef)){
                        event.put("couponRefId",couponUserRef.getId());
                        event.put("couponRefReferer",couponUserRef.getChannel());
                    }
                    messagePublisher.publish(Message.newMessage().withPlainTextBody(JsonUtils.toJson(event)));
                }
            }

        } catch (Exception e) {
            log.error("send utopia.order.payment.topic failed, orderId={}", order.getId(), e);
        }

        chain.doFilter(context); //可以继续后面的处理
    }
}
