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

package com.voxlearning.utopia.service.business.impl.support.order.userOrder;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.service.business.impl.queue.UserLevelEventQueueProducer;
import com.voxlearning.utopia.service.business.impl.support.order.Filter;
import com.voxlearning.utopia.service.business.impl.support.order.FilterChain;
import com.voxlearning.utopia.service.business.impl.support.order.OrderFilterContext;
import com.voxlearning.utopia.service.coupon.client.CouponLoaderClient;
import com.voxlearning.utopia.service.coupon.client.CouponServiceClient;
import com.voxlearning.utopia.service.mizar.consumer.service.OfficialAccountsServiceClient;
import com.voxlearning.utopia.service.news.client.JxtNewsServiceClient;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.order.consumer.UserOrderServiceClient;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import com.voxlearning.utopia.service.user.consumer.*;
import com.voxlearning.utopia.service.vendor.consumer.VendorLoaderClient;
import com.voxlearning.utopia.service.wonderland.client.WonderlandServiceClient;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;


/**
 * @author Summer
 * @since 2016/12/9
 */
@Slf4j
public abstract class UserOrderFilter implements Filter {
    @Inject protected UserOrderServiceClient userOrderServiceClient;
    @Inject protected UserOrderLoaderClient userOrderLoaderClient;
    @Inject protected OfficialAccountsServiceClient officialAccountsServiceClient;
    @Inject protected SensitiveUserDataServiceClient sensitiveUserDataServiceClient;
    @Inject protected SmsServiceClient smsServiceClient;
    @Inject protected ParentServiceClient parentServiceClient;
    @Inject protected UserLoaderClient userLoaderClient;
    @Inject protected DeprecatedClazzLoaderClient deprecatedClazzLoaderClient;
    @Inject protected DeprecatedGroupLoaderClient groupLoaderClient;
    @Inject protected JxtNewsServiceClient jxtNewsServiceClient;
    @Inject protected StudentLoaderClient studentLoaderClient;
    @Inject protected ParentLoaderClient parentLoaderClient;
    @Inject protected VendorLoaderClient vendorLoaderClient;
    @Inject protected CouponServiceClient couponServiceClient;
    @Inject protected CouponLoaderClient couponLoaderClient;
    @Inject protected UserLevelEventQueueProducer userLevelEventQueueProducer;
    @Inject protected WonderlandServiceClient wonderlandServiceClient;

    @Override
    public void doFilter(OrderFilterContext context, FilterChain chain) {
        if (!(context instanceof UserOrderFilterContext))
            throw new IllegalArgumentException("Context is not instance of UserOrderFilterContext");

        UserOrderFilterContext userOrderFilterContext = (UserOrderFilterContext) context;

        if (null == userOrderFilterContext.getOrder() || null == userOrderFilterContext.getCallbackContext())
            throw new IllegalStateException("Order info or callback info missing");

        try {
            doFilter(userOrderFilterContext, chain);
        } catch (Exception ex) {
            log.error("UserOrderFilter process error, context:{}", JsonUtils.toJson(context), ex);
            chain.doFilter(context);
        }

    }

    public abstract void doFilter(UserOrderFilterContext context, FilterChain chain);
}
