package com.voxlearning.utopia.service.business.impl.support.order.userOrder.filter;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.business.impl.support.order.FilterChain;
import com.voxlearning.utopia.service.business.impl.support.order.userOrder.UserOrderFilter;
import com.voxlearning.utopia.service.business.impl.support.order.userOrder.UserOrderFilterContext;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.api.entity.UserOrderProductRef;
import com.voxlearning.utopia.service.order.api.util.AfentiOrderUtil;
import com.voxlearning.utopia.service.piclisten.api.ParentSelfStudyService;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;
import java.util.List;

/**
 *
 * Created by Summer on 2017/1/6.
 */
@Named
@Slf4j
public class UserOrderActiveFilter extends UserOrderFilter {

    @ImportService(interfaceClass = ParentSelfStudyService.class)
    private ParentSelfStudyService parentSelfStudyService;

    @Override
    public void doFilter(UserOrderFilterContext context, FilterChain chain) {

        //激活产品
        MapMessage message = userOrderServiceClient.activeUserOrder(context.getOrder(), context.getExtraDays());
        if (!message.isSuccess()) {
            throw new RuntimeException("activeUserOrder failed, id " + context.getOrder().getId() + ", user " + context.getOrder().getUserId() + ",msg:" + message.getInfo());
        }

        UserOrderProductRef picListenProduct = userOrderLoaderClient.loadOrderProducts(context.getOrder().getUserId(), context.getOrder().getId()).stream()
                .filter(p -> OrderProductServiceType.safeParse(p.getOrderProductServiceType()) == OrderProductServiceType.PicListenBook)
                .filter(p -> p.getRelatedUserId() != null)
                .findFirst()
                .orElse(null);
        if (picListenProduct != null) {
            Long parentId = picListenProduct.getRelatedUserId();
            List<OrderProductItem> orderProductItem = userOrderLoaderClient.loadProductItemsByProductId(picListenProduct.getProductId());
            for (OrderProductItem item : orderProductItem) {
                parentSelfStudyService.addBook2PicListenShelf(parentId, item.getAppItemId());
            }
        }
        chain.doFilter(context); //可以继续后面的处理
    }

}
