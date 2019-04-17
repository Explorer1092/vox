package com.voxlearning.utopia.service.business.impl.support.order.userOrder.filter;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.core.utils.LoggerUtils;
import com.voxlearning.utopia.service.business.impl.support.order.FilterChain;
import com.voxlearning.utopia.service.business.impl.support.order.userOrder.UserOrderFilter;
import com.voxlearning.utopia.service.business.impl.support.order.userOrder.UserOrderFilterContext;
import com.voxlearning.utopia.service.finance.client.FinanceServiceClient;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.user.api.constants.FinanceFlowRefer;
import com.voxlearning.utopia.service.user.api.constants.FinanceFlowState;
import com.voxlearning.utopia.service.user.api.constants.FinanceFlowType;
import com.voxlearning.utopia.service.user.api.mappers.FinanceFlowContext;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * describe:
 * 消费奖学金
 * @author yong.liu
 * @date 2019/03/27
 */
@Named
@Slf4j
public class OrderGiveBalanceFilter  extends UserOrderFilter {
    @Inject
    FinanceServiceClient financeServiceClient;

    @Override
    public void doFilter(UserOrderFilterContext context, FilterChain chain) {
        UserOrder order = context.getOrder();
        if(Objects.isNull(order.getGiveBalance()) || order.getGiveBalance().compareTo(BigDecimal.ZERO) <= 0){
            chain.doFilter(context);
            return;
        }

        BigDecimal userGiveBalance = BigDecimal.ZERO;
        Long userId = 0L;
        String extAttributes = order.getExtAttributes();
        if (StringUtils.isNotBlank(extAttributes)) {
            Map<String, Object> map = JsonUtils.fromJson(extAttributes);
            if (Objects.nonNull(map)) {
                userId = SafeConverter.toLong(map.get("giveBalanceUserId"));
                if(userId != 0L){
                    userGiveBalance = financeServiceClient.getFinanceService().loadUserGiveBalance(userId);
                }
            }
        }

        LoggerUtils.info("orderDeductionGiveBalance", order.genUserOrderId(), extAttributes ,userGiveBalance, order.getGiveBalance());
        if(userGiveBalance.compareTo(order.getGiveBalance()) >= 0){
            try {
                MapMessage mapMessage = financeServiceClient.getFinanceService()
                        .debitGive(userId, order.getGiveBalance(), FinanceFlowRefer.UserOrder.name(), order.genUserOrderId(), "奖学金抵扣订单金额");
            }catch (Exception e){
                log.error("奖学金抵扣异常",e);
            }
            chain.doFilter(context);
        }else{
            //不够扣的情况，暂时先将支付的金额充值到学贝中
            FinanceFlowContext financeFlowContext = FinanceFlowContext.instance()
                    .orderId(order.genUserOrderId())
                    .type(FinanceFlowType.Deposit)
                    .payMethod(context.getCallbackContext().getPayMethodGateway())
                    .transactionId(context.getCallbackContext().getVerifiedPaymentData().getExternalTradeNumber())
                    .state(FinanceFlowState.SUCCESS)
                    .userId(userId!=0L?userId:order.getUserId())
                    .payAmount(context.getCallbackContext().getVerifiedPaymentData().getPayAmount())
                    .amount(context.getCallbackContext().getVerifiedPaymentData().getPayAmount())
                    .refer(FinanceFlowRefer.UserOrder)
                    .memo("奖学金不足临时充值到学贝中");
            financeServiceClient.getFinanceService().deposit(financeFlowContext);
        }
    }
}
