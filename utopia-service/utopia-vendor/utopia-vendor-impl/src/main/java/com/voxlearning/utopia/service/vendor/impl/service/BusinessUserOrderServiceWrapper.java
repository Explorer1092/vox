package com.voxlearning.utopia.service.vendor.impl.service;

import com.voxlearning.alps.annotation.remote.GenericService;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20180427")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
@GenericService(value = "com.voxlearning.utopia.business.api.BusinessUserOrderService")
public interface BusinessUserOrderServiceWrapper {

    MapMessage processUserOrderPayment(String userOrderId, BigDecimal payAmount, String externalTradeNumber, String externalUserId);
}
