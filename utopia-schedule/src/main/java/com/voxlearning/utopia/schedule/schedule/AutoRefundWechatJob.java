package com.voxlearning.utopia.schedule.schedule;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.schedule.progress.ISimpleProgressMonitor;
import com.voxlearning.alps.spi.schedule.ScheduledJobDefinition;
import com.voxlearning.utopia.payment.PaymentGateway;
import com.voxlearning.utopia.payment.PaymentGatewayManager;
import com.voxlearning.utopia.payment.RefundRequest;
import com.voxlearning.utopia.payment.WechatAbstractPaymentGateway;
import com.voxlearning.utopia.payment.constant.PaymentConstants;
import com.voxlearning.utopia.schedule.journal.JobJournalLogger;
import com.voxlearning.utopia.schedule.support.ScheduledJobWithJournalSupport;
import com.voxlearning.utopia.service.order.api.constants.RefundHistoryStatus;
import com.voxlearning.utopia.service.order.api.entity.OrderRefundHistory;
import com.voxlearning.utopia.service.order.api.entity.UserOrderPaymentHistory;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.order.consumer.UserOrderServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * Created by Summer on 2017/11/6.
 */
@Named
@ScheduledJobDefinition(
        jobName = "微信自动退款任务",
        jobDescription = "微信自动退款任务,每20分钟运行一次",
        disabled = {Mode.DEVELOPMENT, Mode.UNIT_TEST, Mode.STAGING, Mode.TEST},
        cronExpression = "0 */15 * * * ?"
)
public class AutoRefundWechatJob extends ScheduledJobWithJournalSupport {
    @Inject UserOrderLoaderClient userOrderLoaderClient;
    @Inject PaymentGatewayManager paymentGatewayManager;
    @Inject private UserOrderServiceClient userOrderServiceClient;

    @Override
    protected void executeScheduledJob(JobJournalLogger jobJournalLogger,
                                       long startTimestamp,
                                       Map<String, Object> parameters,
                                       ISimpleProgressMonitor progressMonitor) throws Exception {
        List<String> payMethodList = Arrays.asList(PaymentConstants.PaymentGatewayName_Wechat,
                PaymentConstants.PaymentGatewayName_Wechat_ParentApp,
                PaymentConstants.PaymentGatewayName_Wechat_StudentApp,
                PaymentConstants.PaymentGatewayName_Wechat_PcNative,
                PaymentConstants.PaymentGatewayName_Wechat_H5_StudentApp,
                PaymentConstants.PaymentGatewayName_Wechat_H5_ParentApp,
                PaymentConstants.PaymentGatewayName_Wechat_Chips,
                PaymentConstants.PaymentGatewayName_Wechat_Piclisten,
                PaymentConstants.PaymentGatewayName_Wechat_StudyTogether,
                PaymentConstants.PaymentGatewayName_Wechat_StudentApp_Junior);
        List<OrderRefundHistory> historyList = userOrderLoaderClient.loadWechatRefundHistoryLimit50(payMethodList, RefundHistoryStatus.REFUNDING);
        if (CollectionUtils.isNotEmpty(historyList)) {
            // 循环退款
            for (OrderRefundHistory history : historyList) {
                try {
                    PaymentGateway paymentGateway = paymentGatewayManager.getPaymentGateway(history.getPayMethod());
                    if (paymentGateway == null) {
                        logger.error("auto refund wechat order error, payMethod not fund, {}", history.getId());
                        continue;
                    }
                    List<UserOrderPaymentHistory> paymentHistoryList = userOrderLoaderClient.loadUserOrderPaymentHistoryList(history.getUserId());
                    String paymentOrderId = history.getOrderId().split("_")[0];
                    UserOrderPaymentHistory paymentHistory = paymentHistoryList.stream().filter(h -> StringUtils.equals(paymentOrderId, h.getOrderId()))
                            .findFirst().orElse(null);

                    Double totalFee = paymentHistory == null ? history.getRefundFee().doubleValue() : paymentHistory.getPayAmount().doubleValue();
                    RefundRequest refundRequest = new RefundRequest();
                    refundRequest.setTransactionId(history.getId());
                    refundRequest.setPayMethod(history.getPayMethod());
                    refundRequest.setUserId(history.getUserId());
                    refundRequest.setOrderId(history.getOrderId()+"_"+System.currentTimeMillis());
                    refundRequest.setTotalFee(totalFee);
                    refundRequest.setRefundFee(history.getRefundFee().doubleValue());
                    WechatAbstractPaymentGateway wechatAbstractPaymentGateway = (WechatAbstractPaymentGateway) paymentGateway;
                    MapMessage message = wechatAbstractPaymentGateway.refundOrder(refundRequest);
                    Map<String, String> result = new HashMap<>();
                    if (message.isSuccess()) {
                        result.put("code", "SUCCESS");
                        result.put("transactionId", history.getId());
                    } else {
                        result.put("code", message.getInfo());
                        result.put("transactionId", history.getId());
                    }
                    userOrderServiceClient.dealAliRefundResults(Collections.singletonList(result));
                } catch (Exception ex) {
                    logger.error("auto refund wechat error, {}", history.getId(), ex);
                }
                // 10秒请求一次
                Thread.sleep(10000);
            }
        }


    }
}
