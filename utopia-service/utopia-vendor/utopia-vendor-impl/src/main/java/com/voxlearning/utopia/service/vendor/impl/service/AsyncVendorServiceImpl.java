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

package com.voxlearning.utopia.service.vendor.impl.service;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.DigestSignUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.remote.core.support.ValueWrapperFuture;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.core.RuntimeModeLoader;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.api.constant.VendorAppsOrderPayType;
import com.voxlearning.utopia.core.config.CommonConfiguration;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.api.entity.UserOrderProductRef;
import com.voxlearning.utopia.service.order.api.util.AfentiOrderUtil;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.piclisten.api.ParentSelfStudyService;
import com.voxlearning.utopia.service.piclisten.api.PicListenBookOrderService;
import com.voxlearning.utopia.service.piclisten.client.TextBookManagementLoaderClient;
import com.voxlearning.utopia.service.vendor.api.AsyncVendorService;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;
import com.voxlearning.utopia.service.vendor.api.entity.VendorAppsUserRef;
import com.voxlearning.utopia.service.vendor.impl.loader.VendorLoaderImpl;
import com.voxlearning.utopia.service.vendor.impl.support.OpenApiUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Named
@ExposeService(interfaceClass = AsyncVendorService.class)
public class AsyncVendorServiceImpl extends SpringContainerSupport implements AsyncVendorService {

    @ImportService(interfaceClass = ParentSelfStudyService.class)
    private ParentSelfStudyService parentSelfStudyService;

    @Inject
    private VendorLoaderImpl vendorLoader;
    @Inject
    private VendorServiceImpl vendorService;
    @Inject
    private UserOrderLoaderClient userOrderLoaderClient;
    @Inject
    private TextBookManagementLoaderClient textBookManagementLoaderClient;

    @ImportService(interfaceClass = PicListenBookOrderService.class)
    private PicListenBookOrderService picListenBookOrderService;

    @Override
    public AlpsFuture<MapMessage> registerVendorAppUserRef(String appKey, Long userId) {
        return new ValueWrapperFuture<>(internalRegisterVendorAppUserRef(appKey, userId));
    }

    @Override
    public AlpsFuture<Boolean> sendVendorPaymentCallBackNotify(VendorApps app,
                                                               UserOrder order,
                                                               Long productAppId,
                                                               Double payAmount,
                                                               String payMethodGateway,
                                                               String externalTradeNumber) {
        internalSendVendorPaymentCallBackNotify(app, order, productAppId, payAmount, payMethodGateway, externalTradeNumber);
        return new ValueWrapperFuture<>(true);
    }

    private MapMessage internalRegisterVendorAppUserRef(String appKey, Long userId) {
        if (StringUtils.isBlank(appKey) || null == userId) return MapMessage.errorMessage("参数错误");

        VendorAppsUserRef vendorAppsUserRef = vendorLoader.loadVendorAppUserRef(appKey, userId);
        if (null != vendorAppsUserRef) return MapMessage.successMessage().add("ref", vendorAppsUserRef);

        VendorApps app = internalLoadVendorApp(appKey);
        if (null == app) return MapMessage.errorMessage("未查询到应用信息");

        CommonConfiguration commonConfiguration = CommonConfiguration.getInstance();
        String sessionKey = OpenApiUtils.generateSessionKey(commonConfiguration.getSessionEncryptKey(), userId);
        vendorAppsUserRef = new VendorAppsUserRef();
        vendorAppsUserRef.setUserId(userId);
        vendorAppsUserRef.setSessionKey(sessionKey);
        vendorAppsUserRef.setAppId(app.getId());
        vendorAppsUserRef.setAppKey(app.getAppKey());

        MapMessage message = vendorService.persistVenderAppUserRef(vendorAppsUserRef);
        if (!message.isSuccess()) return message;

        vendorAppsUserRef = (VendorAppsUserRef) message.get("ref");

        return MapMessage.successMessage().add("ref", vendorAppsUserRef);
    }

    private VendorApps internalLoadVendorApp(String appKey) {
        if (appKey == null) {
            return null;
        }
        VendorApps result = null;
        for (VendorApps each : vendorLoader.loadVendorAppsIncludeDisabled().values()) {
            if (StringUtils.equals(appKey, each.getAppKey())) {
                result = each;
                break;
            }
        }
        if (result == null || result.isDisabledTrue()) {
            return null;
        }
        if (!result.isVisible(RuntimeModeLoader.getInstance().current().getLevel())) {
            return null;
        }
        return result;
    }

    private MapMessage internalSendHttpNotify(String appKey, String targetUrl, Map<String, Object> params) {
        if (StringUtils.isBlank(appKey) || StringUtils.isBlank(targetUrl) || params == null) {
            return MapMessage.errorMessage();
        }
        try {
            return vendorService.sendHttpNotify(appKey, targetUrl, params);
        } catch (Exception ex) {
            logger.error("Failed to send HTTP notify [appKey={},targetUrl={},params={}]",
                    appKey, targetUrl, params, ex);
            return MapMessage.errorMessage();
        }
    }

    private void internalSendVendorPaymentCallBackNotify(VendorApps app,
                                                         UserOrder order,
                                                         Long productAppId,
                                                         Double payAmount,
                                                         String payMethodGateway,
                                                         String externalTradeNumber) {
        if (app == null || order == null) {
            return;
        }

        //点读机订单同步
        if (OrderProductServiceType.safeParse(order.getOrderProductServiceType()) == OrderProductServiceType.PicListenBook) {
            picListenBookOrderService.synchronizePicListenOrder(order);
            return;
        }
        //阿分题提高版点读机同步
        if (AfentiOrderUtil.isAfentiImpovedOrder(OrderProductServiceType.safeParse(order.getOrderProductServiceType()))) {
            List<UserOrderProductRef> productRefs = userOrderLoaderClient.loadOrderProducts(order.getUserId(), order.getId())
                    .stream().filter(e -> OrderProductServiceType.safeParse(e.getOrderProductServiceType()) == OrderProductServiceType.PicListenBook).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(productRefs)) {
                picListenBookOrderService.synchronizePicListenOrder(order);
            }
            return;
        }

        if(!AfentiOrderUtil.isAfentiImpovedOrder(OrderProductServiceType.safeParse(order.getOrderProductServiceType()))
           && !AfentiOrderUtil.isAfentiCommonOrder(OrderProductServiceType.safeParse(order.getOrderProductServiceType()))
           && !app.getAppKey().equals(OrderProductServiceType.YiQiXue.name())){
            notify(app, order, productAppId, payAmount);
        }
    }


    private void notify(VendorApps app, UserOrder order, Long productAppId, Double payAmount) {
        // 获取appId
        Double realPayAmount = payAmount;
        if (realPayAmount == null) {
            realPayAmount = order.getOrderPrice().doubleValue();
        }

        // 计算SIG
        Map<String, String> sigParams = new HashMap<>();
        sigParams.put("user_id", String.valueOf(order.getUserId()));
        sigParams.put("order_id", order.genUserOrderId());
        if (productAppId != null && productAppId != 0) {
            sigParams.put("product_id", String.valueOf(productAppId));
        } else {
            sigParams.put("product_id", order.getProductId());
        }
        sigParams.put("product_name", order.getProductName());
        if (Objects.equals(order.getPayType(), VendorAppsOrderPayType.BANK.name())) {
            sigParams.put("bank", String.valueOf(realPayAmount));
        } else {
            sigParams.put("hwcoin", String.valueOf(realPayAmount));
        }
        sigParams.put("order_status", StringUtils.lowerCase(order.getPaymentStatus().name()));
        sigParams.put("app_key", order.getOrderProductServiceType());
        VendorAppsUserRef ref = vendorLoader.loadVendorAppUserRef(app.getAppKey(), order.getUserId());
        sigParams.put("session_key", ref.getSessionKey());

        // 返回参数
        Map<String, Object> notifyParams = new HashMap<>();
        notifyParams.put("user_id", String.valueOf(order.getUserId()));
        notifyParams.put("order_id", order.genUserOrderId());
        if (productAppId != null && productAppId != 0) {
            notifyParams.put("product_id", String.valueOf(productAppId));
        } else {
            notifyParams.put("product_id", order.getProductId());
        }
        notifyParams.put("product_name", order.getProductName());
        if (Objects.equals(order.getPayType(), VendorAppsOrderPayType.BANK.name())) {
            notifyParams.put("bank", String.valueOf(realPayAmount));
        } else {
            notifyParams.put("hwcoin", String.valueOf(realPayAmount));
        }
        notifyParams.put("order_status", StringUtils.lowerCase(order.getPaymentStatus().name()));
        notifyParams.put("session_key", ref.getSessionKey());
        notifyParams.put("sig", DigestSignUtils.signMd5(sigParams, app.getSecretKey()));
        // 发送回调
        internalSendHttpNotify(app.getAppKey(), app.getCallBackUrl(), notifyParams);
    }

    private void notifyYiQiXue(VendorApps app, UserOrder order, String payMethodGateway, String externalTradeNumber,Double payAmount) {
        Map<String, String> sigParams = new HashMap<>();
        sigParams.put("order_id", order.genUserOrderId());
        sigParams.put("user_id", String.valueOf(order.getUserId()));
        sigParams.put("product_id", String.valueOf(order.getProductId()));
        sigParams.put("pay_type", payMethodGateway);
        sigParams.put("trade_id", externalTradeNumber);
        sigParams.put("payment_amount",String.valueOf(payAmount));

        Map<String, Object> notifyMap = new HashMap<>();
        notifyMap.put("order_id", order.genUserOrderId());
        notifyMap.put("user_id", String.valueOf(order.getUserId()));
        notifyMap.put("product_id", String.valueOf(order.getProductId()));
        notifyMap.put("pay_type", payMethodGateway);
        notifyMap.put("trade_id", externalTradeNumber);
        notifyMap.put("payment_amount",String.valueOf(payAmount));
        notifyMap.put("sig", DigestSignUtils.signMd5(sigParams, app.getSecretKey()));
        // 发送回调
        // 处理stagingUrl
        String callBackUrl = app.getCallBackUrl();
        if (RuntimeMode.current() == Mode.STAGING) {
            callBackUrl = "https://17xue-student.staging.17zuoye.net/m/pay/jzt/notify.vpage";
        }
        internalSendHttpNotify(app.getAppKey(), callBackUrl, notifyMap);
    }

}
