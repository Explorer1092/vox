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

package com.voxlearning.utopia.service.business.impl.support.order.userOrder.filter;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.entity.afenti.AfentiOrder;
import com.voxlearning.utopia.entity.payment.PaymentVerifiedData;
import com.voxlearning.utopia.service.business.impl.support.order.FilterChain;
import com.voxlearning.utopia.service.business.impl.support.order.userOrder.UserOrderFilter;
import com.voxlearning.utopia.service.business.impl.support.order.userOrder.UserOrderFilterContext;
import com.voxlearning.utopia.service.order.api.constants.OrderStatus;
import com.voxlearning.utopia.service.order.api.constants.OrderType;
import com.voxlearning.utopia.service.order.api.constants.PaymentStatus;
import com.voxlearning.utopia.service.order.api.entity.UserOrder;
import com.voxlearning.utopia.service.order.api.entity.UserOrderPaymentHistory;
import com.voxlearning.utopia.service.order.consumer.UserOrderServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

/**
 *
 * Created by Summer on 2016/12/9.
 */
@Named
@Slf4j
public class UserPaymentHistoryFilter extends UserOrderFilter {

    @Inject private UserOrderServiceClient userOrderServiceClient;
    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void doFilter(UserOrderFilterContext context, FilterChain chain) {
        PaymentVerifiedData paymentVerifiedData = context.getCallbackContext().getVerifiedPaymentData();
        UserOrder order = context.getOrder();
        if (order.getOrderStatus() == OrderStatus.Confirmed && order.getPaymentStatus() == PaymentStatus.Paid) {
            // 记录流水历史
            UserOrderPaymentHistory history = new UserOrderPaymentHistory();
            history.setPaymentStatus(PaymentStatus.Paid);
            history.setUserId(order.getUserId());
            history.setOrderId(order.getId());
            history.setOuterTradeId(paymentVerifiedData.getExternalTradeNumber());
            history.setPayDatetime(new Date());
            history.setPayAmount(paymentVerifiedData.getPayAmount());
            history.setPayMethod(context.getCallbackContext().getPayMethodGateway());
            history.setPayMethodName(context.getCallbackContext().getPayMethodGateway());
            // 处理支付人信息
            Map<String, String> result = context.getCallbackContext().getParams();
            String payUser = "";
            if (MapUtils.isNotEmpty(result)) {
                // 判断支付方式
                if (context.getCallbackContext().getPayMethodGateway().contains("wechat")) {
                    payUser = result.get("attach");
                } else if (context.getCallbackContext().getPayMethodGateway().contains("alipay")) {
                    if (StringUtils.isNotBlank(result.get("body"))) {
                        String[] body = StringUtils.split(result.get("body"), "|");
                        if (body.length > 1) {
                            payUser = body[1];
                        }
                    }
                }
            }
            history.setComment(payUser); // 支付人信息
            if (order.getOrderType() == OrderType.app || order.getOrderType() == OrderType.pic_listen || order.getOrderType() == OrderType.yi_qi_xue) {
                Map<String, Date> validTimeMap = userOrderServiceClient.getOrderValidityPeriodIfNecessary(order.getUserId(), order.getProductId());
                history.setServiceStartTime(validTimeMap.get("serviceStartTime"));
                // 赠送天数也一起记录
                Date endDate = validTimeMap.get("serviceEndTime");
                if (endDate != null && context.getRewardPeriod() != null && context.getRewardPeriod() > 0) {
                    endDate = DateUtils.nextDay(endDate, context.getRewardPeriod());
                }
                history.setServiceEndTime(endDate);
            }
            userOrderServiceClient.saveUserOrderPaymentHistory(history);
            //如果有奖学金支付补上奖学金的支付历史
            if(Objects.nonNull(order.getGiveBalance()) && order.getGiveBalance().compareTo(BigDecimal.ZERO) > 0){
                UserOrderPaymentHistory giveHistory = new UserOrderPaymentHistory();
                giveHistory.setPaymentStatus(PaymentStatus.Paid);
                history.setUserId(order.getUserId());
                history.setOrderId(order.getId());
                history.setPayDatetime(new Date());
                history.setPayAmount(order.getGiveBalance());
                history.setPayMethod("givepay");
                history.setPayMethodName("givepay");
                String extAttributes = order.getExtAttributes();
                if (StringUtils.isNotBlank(extAttributes)) {
                    Map<String, Object> map = JsonUtils.fromJson(extAttributes);
                    if (Objects.nonNull(map) && Objects.nonNull(map.get("giveBalanceUserId"))) {
                        history.setComment(SafeConverter.toString(map.get("giveBalanceUserId")));
                    }
                }
                userOrderServiceClient.saveUserOrderPaymentHistory(history);
            }
        }
        chain.doFilter(context);
    }
}
