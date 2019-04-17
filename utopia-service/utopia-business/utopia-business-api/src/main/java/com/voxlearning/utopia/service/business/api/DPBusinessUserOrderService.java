package com.voxlearning.utopia.service.business.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

/**
 * describe:
 *
 * @author yong.liu
 * @date 2019/01/29
 */
@ServiceVersion(version = "1.0.0")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface DPBusinessUserOrderService {

    MapMessage processUserOrderPayment(String userOrderId, BigDecimal payAmount, String externalTradeNumber, String externalUserId);

    MapMessage processVoxPayPaymentForYiQiXue(String userOrderId, BigDecimal payAmount, String externalTradeNumber, Long financeUserId);

}
