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

package com.voxlearning.utopia.service.business.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.dao.jdbc.exception.UtopiaSqlRollbackTransactionException;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.utopia.api.constant.ChargeType;
import com.voxlearning.utopia.business.api.BusinessFinanceService;
import com.voxlearning.utopia.entity.campaign.WirelessCharging;
import com.voxlearning.utopia.payment.constant.PaymentConstants;
import com.voxlearning.utopia.service.business.impl.support.BusinessServiceSpringBean;
import com.voxlearning.utopia.service.finance.client.FinanceServiceClient;
import com.voxlearning.utopia.service.finance.client.WirelessChargingServiceClient;
import com.voxlearning.utopia.service.order.api.constants.OrderStatus;
import com.voxlearning.utopia.service.order.api.constants.PaymentStatus;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.api.entity.UserOrderPaymentHistory;
import com.voxlearning.utopia.service.order.api.mapper.AppPayMapper;
import com.voxlearning.utopia.service.user.api.constants.FinanceFlowRefer;
import com.voxlearning.utopia.service.user.api.constants.FinanceFlowState;
import com.voxlearning.utopia.service.user.api.constants.FinanceFlowType;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.mappers.FinanceFlowContext;
import com.voxlearning.utopia.service.user.api.service.financial.Finance;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.voxlearning.utopia.api.constant.OrderProductServiceType.A17ZYSPG;

/**
 * @author xin
 * @since 14-7-23 上午11:05
 */
@Named
@Service(interfaceClass = BusinessFinanceService.class)
@ExposeService(interfaceClass = BusinessFinanceService.class)
public class BusinessFinanceServiceImpl extends BusinessServiceSpringBean implements BusinessFinanceService {

    private static final Map<String, Integer> VIP_AWARD_MAP = new HashMap<>();

    static {
        VIP_AWARD_MAP.put(A17ZYSPG.name() + "_7", 50);
        VIP_AWARD_MAP.put(A17ZYSPG.name() + "_30", 500);
        VIP_AWARD_MAP.put(A17ZYSPG.name() + "_90", 1250);
        VIP_AWARD_MAP.put(A17ZYSPG.name() + "_365", 4000);
    }

    @Inject private FinanceServiceClient financeServiceClient;
    @Inject private WirelessChargingServiceClient wirelessChargingServiceClient;

    @Override
    public MapMessage doVoxAppPayment(Map<String, Object> context) {
        VendorApps vendorApps = (VendorApps) context.get("vendorApps");
        UserOrder order = (UserOrder) context.get("order");
        User user = (User) context.get("user");
        return doVoxAppPayment(vendorApps, order, user);
    }

    @Override
    public MapMessage doHwcoinPaymnet(Map<String, Object> context) {
        VendorApps vendorApps = (VendorApps) context.get("vendorApps");
        UserOrder userOrder = (UserOrder) context.get("order");
        User user = (User) context.get("user");
        return doHwcoinPaymnet(vendorApps, userOrder, user);
    }

    /**
     * 第三方APP小额支付处理
     *
     * @param vendorApps 第三方APP信息
     * @param userOrder  第三方APP产生的订单信息
     * @param user       用户
     * @return 支付结果
     */
    public synchronized MapMessage doVoxAppPayment(final VendorApps vendorApps, final UserOrder userOrder, final User user) {
        // 判断参数是否有效
        if (vendorApps == null || userOrder == null || user == null) {
            return MapMessage.errorMessage("订单数据错误，无法完成支付!");
        }

        // 判断作业币是否足够
        Finance userFinance = financeServiceClient.getFinanceService()
                .createUserFinanceIfAbsent(user.getId())
                .getUninterruptibly();
        if (userFinance == null || userFinance.getBalance().doubleValue() < userOrder.getOrderPrice().doubleValue()) {
            return MapMessage.errorMessage("对不起，您的作业币不足！");
        }

        // 支付处理, 修正用户帐户余额
        voxAppPayment_UpdateBalance(user.getId(), vendorApps.getAppKey(), userOrder);

        // 支付处理，新订单中心
        doOrderPaid(userOrder);
        return MapMessage.successMessage();
    }

    private UserOrder doOrderPaid(UserOrder order) {
        order.setOrderStatus(OrderStatus.Confirmed);
        order.setPaymentStatus(PaymentStatus.Paid);
        order.setUpdateDatetime(new Date());
        userOrderServiceClient.updateUserOrderStatus(order, PaymentStatus.Paid, OrderStatus.Confirmed);

        // 记录 payment history
        UserOrderPaymentHistory history = UserOrderPaymentHistory.fromOrder(order);
        history.setPayMethod(PaymentConstants.PaymentGatewayName_17Zuoye);
        history.setPayDatetime(new Date());
        userOrderServiceClient.saveUserOrderPaymentHistory(history);

        MapMessage orderResult = MapMessage.successMessage();
        Integer validPeriod = getValidPeriod(order.getProductId());
        if (null != validPeriod) {
            orderResult = userOrderServiceClient.activeUserOrder(order, new HashMap<>());
        }
        if (!orderResult.isSuccess()) {
            logger.error("用户支付完成，但是处理UserOrder出现错误，错误信息:" + orderResult.getInfo());
            throw new UtopiaSqlRollbackTransactionException();
        }
        return order;

    }

    private void voxAppPayment_UpdateBalance(Long userId, String appKey, UserOrder userOrder) {
        Map<String, String> attrs = MiscUtils.map("app_key", appKey,
                "product_id", userOrder.getProductId(),
                "product_name", userOrder.getProductName(),
                "order_id", userOrder.genUserOrderId());

        FinanceFlowContext context = FinanceFlowContext.instance()
                .userId(userId)
                .amount(userOrder.getOrderPrice())
                .payAmount(userOrder.getOrderPrice())
                .type(FinanceFlowType.Debit)
                .state(FinanceFlowState.SUCCESS)
                .refer(FinanceFlowRefer.UserOrder)
                .orderId(userOrder.genUserOrderId())
                .payMethod(PaymentConstants.PaymentGatewayName_17Zuoye)
                .attribute(JsonUtils.toJson(attrs));
        boolean success = financeServiceClient.getFinanceService()
                .debit(context)
                .getUninterruptibly();
        if (!success) throw new UtopiaSqlRollbackTransactionException();
    }

    private Integer getValidPeriod(String productId) {
        OrderProduct product = userOrderLoaderClient.loadOrderProductById(productId);
        if (product != null) {
            // 非捆绑模式
            List<OrderProductItem> itemList = userOrderLoaderClient.loadProductItemsByProductId(product.getId());
            if (CollectionUtils.isNotEmpty(itemList) && itemList.size() == 1) {
                return itemList.get(0).getPeriod();
            }
        }
        return null;
    }

    // 作业币支付
    public MapMessage doHwcoinPaymnet(VendorApps vendorApps, UserOrder order, User user) {

        String appKey = vendorApps.getAppKey();

        // 重新获取下订单数据,防止重复支付
        UserOrder userOrder = userOrderLoaderClient.loadUserOrderIncludeCanceled(order.genUserOrderId());
        if (userOrder.getPaymentStatus() == PaymentStatus.Paid) {
            return MapMessage.errorMessage("这个订单已经被支付过了！");
        }

        //判断用户在所购买的产品中当前的vip剩余时间。>=365则不让购买
        AppPayMapper payStatus = userOrderLoaderClient.getUserAppPaidStatus(appKey, user.getId());
        if (payStatus != null && payStatus.getDayToExpire() != null && payStatus.getDayToExpire() > 365) {
            return MapMessage.errorMessage("您已经开通了 " + order.getProductName() + "，可以直接学习使用哦！");
        }

        Map<String, Object> context = MiscUtils.m(
                "vendorApps", vendorApps,
                "order", order,
                "user", user);
        return doVoxAppPayment(context);
    }

//    @Override
//    @Deprecated
//    public MapMessage saveWirelessCharging_junior(Long userId, ChargeType chargeType, Integer amount, String smsMessage, String extraDesc) {
//        boolean ret = wirelessChargingServiceClient.getWirelessChargingService()
//                .saveWirelessCharging_junior(userId, chargeType, amount, smsMessage, extraDesc)
//                .getUninterruptibly();
//        if (ret) {
//            return MapMessage.successMessage();
//        } else {
//            return MapMessage.errorMessage("Unknown user id:" + userId);
//        }
//    }

    @Override
    @Deprecated
    public MapMessage saveWirelessCharging(Long userId, ChargeType chargeType, String mobile, Integer amount, String smsMessage, String extraDesc) {
        boolean ret = wirelessChargingServiceClient.getWirelessChargingService()
                .saveWirelessCharging(userId, chargeType, mobile, amount, smsMessage, extraDesc)
                .getUninterruptibly();
        return new MapMessage().setSuccess(ret);
    }

    @Override
    @Deprecated
    public WirelessCharging loadWirelessCharging(String orderId) {
        return wirelessChargingServiceClient.getWirelessChargingService()
                .loadWirelessCharging(orderId)
                .getUninterruptibly();
    }

    @Override
    @Deprecated
    public int updateChargingSuccess(String orderId) {
        boolean ret = wirelessChargingServiceClient.getWirelessChargingService()
                .updateChargingSuccess(orderId)
                .getUninterruptibly();
        return ret ? 1 : 0;
    }

    @Override
    @Deprecated
    public void updateChargingFailed(String orderId, String resultCode, String resultStatus) {
        wirelessChargingServiceClient.getWirelessChargingService()
                .updateChargingFailed(orderId, resultCode, resultStatus)
                .awaitUninterruptibly();
    }
}
