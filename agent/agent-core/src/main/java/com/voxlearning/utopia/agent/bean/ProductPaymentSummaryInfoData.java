package com.voxlearning.utopia.agent.bean;

import com.voxlearning.utopia.entity.afenti.AfentiOrder;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.voxlearning.utopia.api.constant.AfentiOrderPayStatus.Paid;
import static com.voxlearning.utopia.api.constant.AfentiOrderPayStatus.Refund;

/**
 * Created by Alex on 15-1-21.
 */
@Data
public class ProductPaymentSummaryInfoData implements Serializable {

    private String productName;             // 商品名
    private Integer orderCount;             // 订单数
    private Integer orderUserNum;           // 订单人数
    private Double orderAmount;             // 订单总金额
    private Double cardpayAmount;           // 实物卡支付金额
    private Double refundAmount;            // 退款金额
    private List<Long> orderUserIdList;     // 本统计中用户ID列表

    public ProductPaymentSummaryInfoData(String productName) {
        this.productName = productName;
        this.orderCount = 0;
        this.orderUserNum = 0;
        this.orderAmount = 0d;
        this.cardpayAmount = 0d;
        this.refundAmount = 0d;
        this.orderUserIdList = new ArrayList<>();
    }

    public void append(AfentiOrder order) {
        if (order == null || (order.getPayStatus() != Paid && order.getPayStatus() != Refund)) {
            return;
        }

        if (order.getPayStatus() == Paid) {
            this.orderCount++;
            if (!this.orderUserIdList.contains(order.getUserId())) {
                this.orderUserNum++;
                this.orderUserIdList.add(order.getUserId());
            }
            this.orderAmount += order.getPayAmount();
            if ("productcardpay".equals(order.getPayMethod())) {
                this.cardpayAmount += order.getPayAmount();
            }
        } else {
            this.refundAmount += order.getPayAmount();
        }
    }

    public void append(Integer orderCount, Integer orderUserNum, Double orderAmount, Double cardpayAmount, Double refundAmount) {
        if (orderCount != null) {
            this.orderCount += orderCount;
        }

        if (orderUserNum != null) {
            this.orderUserNum += orderUserNum;
        }

        if (orderAmount != null) {
            this.orderAmount += orderAmount;
        }

        if (cardpayAmount != null) {
            this.cardpayAmount += cardpayAmount;
        }

        if (refundAmount != null) {
            this.refundAmount += refundAmount;
        }
    }

}
