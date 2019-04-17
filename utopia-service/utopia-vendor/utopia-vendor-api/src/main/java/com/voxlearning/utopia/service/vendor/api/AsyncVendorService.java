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

package com.voxlearning.utopia.service.vendor.api;

import com.voxlearning.alps.annotation.remote.Async;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.vendor.api.entity.VendorApps;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "2017.05.27")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface AsyncVendorService {

    String FIELD_APP_ID = "appId";
    String FIELD_BOOK_EDITION_CODE = "bookEditionCode";
    String FIELD_PERIOD = "effectivePeriod";
    String FIELD_USER_NAME = "username";
    String FIELD_ORDER_SN = "orderSn";
    String FIELD_MOBILE = "mobile";
    String FIELD_PHONE = "phone";
    String FIELD_SIGN = "sign";
    String FIELD_SDK_NAME = "sdk";
    String FIELD_SDK_BOOK_ID = "sdk_book_id";
    String FIELD_ORDER_PERIOD = "period";
    String CONFIG_PICLISTEN_WAIYANSHE_APPID = "piclisten.waiyanshe.appid";
    String CONFIG_PICLISTEN_WAIYANSHE_SECRET = "piclisten.waiyanshe.secret";
    String CONFIG_FLTRP_API_GET_MOBILE = "fltrp.api.getmobile";

    @Async
    AlpsFuture<MapMessage> registerVendorAppUserRef(String appKey, Long userId);

    @Async
    AlpsFuture<Boolean> sendVendorPaymentCallBackNotify(VendorApps app, UserOrder order, Long productAppId, Double payAmount, String payMethodGateway, String externalTradeNumber);

}
