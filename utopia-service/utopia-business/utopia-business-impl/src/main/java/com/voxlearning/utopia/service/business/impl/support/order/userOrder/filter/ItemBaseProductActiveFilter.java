package com.voxlearning.utopia.service.business.impl.support.order.userOrder.filter;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.business.impl.support.order.FilterChain;
import com.voxlearning.utopia.service.business.impl.support.order.userOrder.UserOrderFilter;
import com.voxlearning.utopia.service.business.impl.support.order.userOrder.UserOrderFilterContext;
import com.voxlearning.utopia.service.order.api.constants.OrderProductSalesType;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.wonderland.client.AsyncWonderlandCacheServiceClient;
import com.voxlearning.utopia.service.wonderland.client.WonderlandServiceClient;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.api.constant.OrderProductServiceType.*;

/**
 * @author Summer
 * @since 2017/6/14
 */
@Named
@Slf4j
public class ItemBaseProductActiveFilter extends UserOrderFilter {
    // 道具类 不按有效期的商品处理
    private List<OrderProductServiceType> itemTypes = Arrays.asList(ValueAddedLiveTimesCard, EagletSinologyCard, EagletSinologyExperienceCard);

    @Inject private WonderlandServiceClient wonderlandServiceClient;
    @Inject private AsyncWonderlandCacheServiceClient asyncWonderlandCacheServiceClient;

    @Override
    public void doFilter(UserOrderFilterContext context, FilterChain chain) {
        UserOrder order = context.getOrder();
        if (order.getOrderProductServiceType() != null && itemTypes.contains(order.getOrderProductServiceType())) {
            List<OrderProductItem> itemList = userOrderLoaderClient.loadProductItemsByProductId(order.getProductId());
            itemList = itemList.stream()
                    .filter(i -> i.getSalesType() == OrderProductSalesType.ITEM_BASED)
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(itemList)) {
                // 购买的商品
                OrderProductItem item = itemList.get(0);
                if (OrderProductServiceType.safeParse(item.getProductType()) == ValueAddedLiveTimesCard) {
                    OrderProduct product = userOrderLoaderClient.loadOrderProductById(order.getProductId());
                    if (product != null && StringUtils.equals(product.getAttributes(), "sample_sack")) {
                        asyncWonderlandCacheServiceClient.getAsyncWonderlandCacheService()
                                .eagletSampleSackCacheManager_record(order.getUserId());
                    }
                }
                // 增加上课次数
                MapMessage message = wonderlandServiceClient.getWonderlandService()
                        .changeWonderlandTimesCard(order.getUserId(), item.getProductType(), item.getPeriod());
                if (!message.isSuccess()) {
                    log.error("Add times card failed, oid {}, ak {}", order.genUserOrderId(), item.getProductType());
                }
            }
        }
        chain.doFilter(context); //可以继续后面的处理
    }

}
