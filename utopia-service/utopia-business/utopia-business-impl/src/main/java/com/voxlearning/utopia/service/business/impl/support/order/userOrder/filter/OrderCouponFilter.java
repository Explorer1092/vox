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
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.business.impl.support.order.FilterChain;
import com.voxlearning.utopia.service.business.impl.support.order.userOrder.UserOrderFilter;
import com.voxlearning.utopia.service.business.impl.support.order.userOrder.UserOrderFilterContext;
import com.voxlearning.utopia.service.coupon.api.constants.CouponType;
import com.voxlearning.utopia.service.coupon.api.entities.Coupon;
import com.voxlearning.utopia.service.coupon.api.entities.CouponProductRef;
import com.voxlearning.utopia.service.coupon.api.entities.CouponUserRef;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;
import java.util.List;

/**
 * Created by Summer on 2017/3/17.
 * 优惠劵系统处理Filter 以后如果有新类型的优惠劵需要在后处理处理的话， 请在这里完成。
 */
@Named
@Slf4j
public class OrderCouponFilter extends UserOrderFilter {

    @Override
    public void doFilter(UserOrderFilterContext context, FilterChain chain) {
        UserOrder order = context.getOrder();
        try {
            if (StringUtils.isNotBlank(order.getCouponRefId())) {
                // 修改优惠劵状态
                CouponUserRef userRef = couponLoaderClient.loadCouponUserRefById(order.getCouponRefId());
                if (userRef != null) {
                    couponServiceClient.usedCoupon(userRef, order.getUserId());
                    // 处理Period类型的优惠劵
                    Coupon coupon = couponLoaderClient.loadCouponById(userRef.getCouponId());
                    if (coupon.getCouponType() == CouponType.Period && coupon.getTypeValue() != null) {
                        Integer dayCount = coupon.getTypeValue().intValue();
                        context.addRewardPeriod(dayCount);

                        List<CouponProductRef> productRefList = couponLoaderClient.loadCouponProductRefs(coupon.getId());
                        if (CollectionUtils.isNotEmpty(productRefList)) {
                            for (CouponProductRef couponProductRef : productRefList) {
                                context.addExtraDays(couponProductRef.getProductValue(), dayCount);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            log.error("deal order coupon error, order {},  error {}", order.genUserOrderId(), ex.getMessage());
        }
        chain.doFilter(context); //可以继续后面的处理
    }
}
