package com.voxlearning.utopia.service.business.impl.support.order.userOrder.filter;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.business.impl.support.order.FilterChain;
import com.voxlearning.utopia.service.business.impl.support.order.userOrder.UserOrderFilter;
import com.voxlearning.utopia.service.business.impl.support.order.userOrder.UserOrderFilterContext;
import com.voxlearning.utopia.service.order.api.constants.PaymentStatus;

import javax.inject.Named;
import java.util.Map;

/**
 * Created by Summer on 2017/1/6.
 */
@Named
public class UserOrderAlbumSubscribeFilter extends UserOrderFilter {
    @Override
    public void doFilter(UserOrderFilterContext context, FilterChain chain) {
        if (context.getOrder().getPaymentStatus() == PaymentStatus.Paid && OrderProductServiceType.safeParse(context.getOrder().getOrderProductServiceType()) == OrderProductServiceType.PicListen) {
            Map<String, Object> attributes = JsonUtils.fromJson(context.getOrder().getProductAttributes());
            if (attributes != null && attributes.containsKey("albumId")) {
                Map<String, Object> orderAttr = JsonUtils.fromJson(context.getOrder().getExtAttributes());
                jxtNewsServiceClient.subAlbum(SafeConverter.toLong(orderAttr.get("parentId")), attributes.get("albumId").toString());
            }
        }

        chain.doFilter(context);
    }
}
