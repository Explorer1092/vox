package com.voxlearning.utopia.service.business.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.entity.payment.PaymentCallbackContext;
import com.voxlearning.utopia.entity.payment.PaymentVerifiedData;
import com.voxlearning.utopia.payment.constant.PaymentConstants;
import com.voxlearning.utopia.service.business.api.DPBusinessUserOrderService;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;

/**
 * describe:
 *
 * @author yong.liu
 * @date 2019/01/29
 */
@Named
@Service(interfaceClass = DPBusinessUserOrderService.class)
@ExposeService(interfaceClass = DPBusinessUserOrderService.class)
public class DPBusinessUserOrderServiceImpl implements DPBusinessUserOrderService {

    @Inject
    private BusinessUserOrderServiceImpl businessUserOrderService;

    @Override
    public MapMessage processUserOrderPayment(String userOrderId, BigDecimal payAmount, String externalTradeNumber, String externalUserId) {
        return businessUserOrderService.processUserOrderPayment(userOrderId, payAmount, externalTradeNumber, externalUserId);
    }

    @Override
    public MapMessage processVoxPayPaymentForYiQiXue(String userOrderId, BigDecimal payAmount, String externalTradeNumber, Long financeUserId) {
        PaymentCallbackContext context = new PaymentCallbackContext(PaymentConstants.PaymentGatewayName_17Zuoye, "");
        context.setTradeNumber(userOrderId);
        PaymentVerifiedData paymentVerifiedData = new PaymentVerifiedData();
        paymentVerifiedData.setTradeNumber(userOrderId);
        paymentVerifiedData.setPayAmount(payAmount);
        context.setVerifiedPaymentData(paymentVerifiedData);
        context.setFinanceUserId(financeUserId);
        UserOrder order = businessUserOrderService.processUserOrderPayment(context);
        if (null != order) {
            return MapMessage.successMessage("支付成功");
        } else {
            return MapMessage.errorMessage("支付失败");
        }
    }
}
