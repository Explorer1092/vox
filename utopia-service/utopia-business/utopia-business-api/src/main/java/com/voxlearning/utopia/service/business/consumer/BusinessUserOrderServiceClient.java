package com.voxlearning.utopia.service.business.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.business.api.BusinessUserOrderService;
import com.voxlearning.utopia.entity.payment.PaymentCallbackContext;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by Summer on 2016/12/9.
 */
public class BusinessUserOrderServiceClient implements BusinessUserOrderService {

    @ImportService(interfaceClass = BusinessUserOrderService.class)
    private BusinessUserOrderService remoteReference;

    public UserOrder processUserOrderPayment(PaymentCallbackContext context) {
        return remoteReference.processUserOrderPayment(context);
    }

    public MapMessage remindParentForOrder(UserOrder order, List<Long> parentIdList) {
        return remoteReference.remindParentForOrder(order, parentIdList);
    }

    @Override
    public MapMessage processUserOrderPayment(String userOrderId, BigDecimal payAmount, String externalTradeNumber, String externalUserId) {
        return remoteReference.processUserOrderPayment(userOrderId, payAmount, externalTradeNumber, externalUserId);
    }
}
