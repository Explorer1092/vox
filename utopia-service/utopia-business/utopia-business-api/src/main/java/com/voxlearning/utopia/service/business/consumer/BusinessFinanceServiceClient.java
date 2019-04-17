/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.business.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.ChargeType;
import com.voxlearning.utopia.business.api.BusinessFinanceService;
import com.voxlearning.utopia.entity.campaign.WirelessCharging;

import java.util.Map;

/**
 * @author xin
 * @since 14-7-23 下午5:15
 */
public class BusinessFinanceServiceClient implements BusinessFinanceService {

    @ImportService(interfaceClass = BusinessFinanceService.class)
    private BusinessFinanceService remoteReference;

//    @Override
//    public AfentiOrder createAfentiOrderFromAppsOrder(VendorApps vendorApps, VendorAppsOrder appsOrder, User user, double payAmount, String payMethodGateway) {
//        return remoteReference.createAfentiOrderFromAppsOrder(vendorApps, appsOrder, user, payAmount, payMethodGateway);
//    }

    @Override
    public MapMessage doVoxAppPayment(Map<String, Object> context) {
        return remoteReference.doVoxAppPayment(context);
    }

    @Override
    public MapMessage doHwcoinPaymnet(Map<String, Object> context) {
        return remoteReference.doHwcoinPaymnet(context);
    }

//    @Override
//    @Deprecated
//    public MapMessage saveWirelessCharging_junior(Long userId, ChargeType chargeType, Integer amount, String smsMessage, String extraDesc) {
//        return remoteReference.saveWirelessCharging_junior(userId, chargeType, amount, smsMessage, extraDesc);
//    }

    @Override
    @Deprecated
    public MapMessage saveWirelessCharging(Long userId, ChargeType chargeType, String mobile, Integer amount, String smsMessage, String extraDesc) {
        return remoteReference.saveWirelessCharging(userId, chargeType, mobile, amount, smsMessage, extraDesc);
    }

    @Override
    @Deprecated
    public WirelessCharging loadWirelessCharging(String orderId) {
        return remoteReference.loadWirelessCharging(orderId);
    }

    @Override
    @Deprecated
    public int updateChargingSuccess(String orderId) {
        return remoteReference.updateChargingSuccess(orderId);
    }

    @Override
    @Deprecated
    public void updateChargingFailed(String orderId, String resultCode, String resultStatus) {
        remoteReference.updateChargingFailed(orderId, resultCode, resultStatus);
    }
}
