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

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.service.business.impl.support.order.FilterChain;
import com.voxlearning.utopia.service.business.impl.support.order.userOrder.UserOrderFilter;
import com.voxlearning.utopia.service.business.impl.support.order.userOrder.UserOrderFilterContext;
import com.voxlearning.utopia.service.order.api.constants.OrderType;
import com.voxlearning.utopia.service.order.api.constants.PaymentStatus;
import com.voxlearning.utopia.service.order.api.util.AfentiOrderUtil;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.utopia.service.vendor.client.AsyncVendorServiceClient;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by Summer on 2016/12/13.
 * 第三方回调的 ， 放到这里来
 */
@Named
public class UserOrderVendorNotifyFilter extends UserOrderFilter {

    @Inject private AsyncVendorServiceClient asyncVendorServiceClient;

    @Override
    public void doFilter(UserOrderFilterContext context, FilterChain chain) {
        if (isTargetOrder(context.getOrder().getOrderType()) &&
                context.getOrder().getPaymentStatus() == PaymentStatus.Paid &&
                OrderProductServiceType.safeParse(context.getOrder().getOrderProductServiceType()).isSendNotify()) {
            OrderProductServiceType orderProductServiceType = OrderProductServiceType.safeParse(context.getOrder().getOrderProductServiceType());
            if (AfentiOrderUtil.isAfentiImpovedOrder(orderProductServiceType)) {
                orderProductServiceType = AfentiOrderUtil.getAfentiBaseOrderTypeByImprovedType(orderProductServiceType);
            }
            MapMessage message = asyncVendorServiceClient.getAsyncVendorService()
                    .registerVendorAppUserRef(orderProductServiceType.name(), context.getOrder().getUserId())
                    .getUninterruptibly();
            if (!message.isSuccess() || null == message.get("ref")) {
                throw new IllegalStateException("No VendorAppsUserRef Found, data is " + JsonUtils.toJson(context));
            }
            // 志龙说这是给第三方对账用的
            VendorApps apps = vendorLoaderClient.getExtension().loadVendorApp(context.getOrder().getOrderProductServiceType());
            asyncVendorServiceClient.getAsyncVendorService().sendVendorPaymentCallBackNotify(apps,
                    context.getOrder(),
                    userOrderLoaderClient.loadAppIdByProductId(context.getOrder().getProductId()),
                    context.getCallbackContext().getVerifiedPaymentData().getPayAmount().doubleValue(),
                    context.getCallbackContext().getPayMethodGateway(),
                    context.getCallbackContext().getVerifiedPaymentData().getExternalTradeNumber()
            ).awaitUninterruptibly();
        }
        //继续下面的处理
        chain.doFilter(context);
    }

    private boolean isTargetOrder(OrderType orderType) {
        return orderType == OrderType.app || orderType == OrderType.pic_listen
                || orderType == OrderType.yi_qi_xue || orderType == OrderType.yi_qi_xue_fz;
    }

}