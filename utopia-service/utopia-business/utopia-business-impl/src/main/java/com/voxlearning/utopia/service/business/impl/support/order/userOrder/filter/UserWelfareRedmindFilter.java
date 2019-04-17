package com.voxlearning.utopia.service.business.impl.support.order.userOrder.filter;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.business.impl.support.order.FilterChain;
import com.voxlearning.utopia.service.business.impl.support.order.userOrder.UserOrderFilter;
import com.voxlearning.utopia.service.business.impl.support.order.userOrder.UserOrderFilterContext;
import com.voxlearning.utopia.service.order.api.constants.OrderProductSalesType;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.api.entity.UserOrderProductRef;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 礼劵系统 消费购买赠送天数的优惠卷
 *
 * @author peng.zhang.a
 * @since 17-2-7
 */
@Named
@Slf4j
public class UserWelfareRedmindFilter extends UserOrderFilter {

    @Override
    public void doFilter(UserOrderFilterContext context, FilterChain chain) {
        UserOrder order = context.getOrder();

        StudentDetail student = studentLoaderClient.loadStudentDetail(order.getUserId());
        if (student != null) {
            addExtraDaysIfnc(context, order, student);
        }

        //可以继续后面的处理
        chain.doFilter(context);
    }

    private void addExtraDaysIfnc(UserOrderFilterContext context, UserOrder order, StudentDetail student) {
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
            for (String productId : productList) {
                List<OrderProductItem> itemList = orderItems.get(productId);
                if (CollectionUtils.isEmpty(itemList)) {
                    continue;
                }

                for (OrderProductItem productItem : itemList) {
                    // 道具类的产品不走礼券系统
                    if (productItem.getSalesType() != OrderProductSalesType.TIME_BASED) {
                        continue;
                    }

                    Integer period = productItem.getPeriod();
                    MapMessage consumeUserWelfareRet = wonderlandServiceClient.getWonderlandService()
                            .existAndConsumeUserWelfare(student.getId(), productItem.getProductType(), period);
                    if (consumeUserWelfareRet.isSuccess() && SafeConverter.toInt(consumeUserWelfareRet.get("rewardPeriod")) > 0) {
                        context.addExtraDays(productItem.getProductType(), SafeConverter.toInt(consumeUserWelfareRet.get("rewardPeriod")));
                    }
                }
            }
        } catch (Exception e) {
            log.error("UserWelfareRedmindFilter error: userId={},orderId={}", order.getUserId(), order.getId(), e);
        }
    }
}