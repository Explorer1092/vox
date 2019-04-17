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

package com.voxlearning.utopia.business.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.api.constant.ChargeType;
import com.voxlearning.utopia.entity.campaign.WirelessCharging;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author xin.xin
 * @since 14-7-23 上午10:49
 */
@ServiceVersion(version = "1.0")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
@CyclopsMonitor("utopia")
public interface BusinessFinanceService extends IPingable {
//    AfentiOrder createAfentiOrderFromAppsOrder(VendorApps vendorApps, VendorAppsOrder appsOrder, User user, double payAmount, String payMethodGateway);

    MapMessage doVoxAppPayment(Map<String, Object> context);

    MapMessage doHwcoinPaymnet(Map<String, Object> context);

    // TODO: 迁移到DP接口中，准备好DP接口后通知中学端
    // 此方法不要删除，初中在调用
//    @Deprecated
//    MapMessage saveWirelessCharging_junior(Long userId, ChargeType chargeType, Integer amount, String smsMessage, String extraDesc);

    @Deprecated
    MapMessage saveWirelessCharging(Long userId, ChargeType chargeType, String mobile, Integer amount, String smsMessage, String extraDesc);

    @Deprecated
    WirelessCharging loadWirelessCharging(String orderId);

    @Deprecated
    int updateChargingSuccess(String orderId);

    @Deprecated
    void updateChargingFailed(String orderId, String resultCode, String resultStatus);
}
