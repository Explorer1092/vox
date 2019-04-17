package com.voxlearning.utopia.service.crm.api.service.refund;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;

import java.util.concurrent.TimeUnit;

/**
 * CRM 退款相关调用
 * Created by Yuechen.wang on 2017/1/20.
 */
@ServiceVersion(version = "20170120")
@ServiceTimeout(timeout = 20, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface CrmRefundService extends IPingable {

    /**
     * 财务处理CRM退款请求
     *
     * @param orderId 订单ID
     * @param memo    处理备注
     * @param result  true--批准；false--拒绝
     */
    MapMessage refundResult(String orderId, Long userId, String memo, Boolean result);

    default MapMessage agreeCrmRefund(String orderId, Long userId) {
        return refundResult(orderId, userId, null, true);
    }

    default MapMessage rejectCrmRefund(String orderId, Long userId, String memo) {
        return refundResult(orderId, userId, memo, false);
    }


    /**
     * 财务处理CRM余额提现请求
     *
     * @param userId 用户ID
     * @param amount 提现金额
     * @param memo   处理备注
     * @param result true--批准；false--拒绝
     */
    MapMessage withdrawResult(Long userId, Double amount, String memo, Boolean result);

    default MapMessage agreeCrmWithdraw(Long userId, Double amount) {
        return withdrawResult(userId, amount, null, true);
    }

    default MapMessage rejectCrmWithdraw(Long userId, Double amount, String memo) {
        return withdrawResult(userId, amount, memo, false);
    }

    MapMessage loadCrmFinanceFlow(Long userId, String transactionIds);

}
