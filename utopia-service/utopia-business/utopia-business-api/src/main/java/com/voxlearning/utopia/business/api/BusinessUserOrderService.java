package com.voxlearning.utopia.business.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.entity.payment.PaymentCallbackContext;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Summer on 2016/12/9.
 */
@ServiceVersion(version = "20180427")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
@CyclopsMonitor("utopia")
public interface BusinessUserOrderService extends IPingable {

    UserOrder processUserOrderPayment(PaymentCallbackContext context);

    MapMessage remindParentForOrder(UserOrder order, List<Long> parentIdList);

    MapMessage processUserOrderPayment(String userOrderId, BigDecimal payAmount, String externalTradeNumber, String externalUserId);

}
