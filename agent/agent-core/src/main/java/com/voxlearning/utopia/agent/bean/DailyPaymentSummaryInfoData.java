package com.voxlearning.utopia.agent.bean;

import com.voxlearning.utopia.service.crm.api.constants.ProductType;
import com.voxlearning.utopia.agent.persist.entity.statistics.PaymentDataSummary;
import com.voxlearning.utopia.agent.persist.entity.statistics.ProductPaymentInfo;
import com.voxlearning.utopia.api.constant.OrderProductServiceType;
import com.voxlearning.utopia.entity.afenti.AfentiOrder;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Alex on 15-1-22.
 */
@Data
public class DailyPaymentSummaryInfoData implements Serializable {

    private static final Map<OrderProductServiceType, String> PRODUCT_NAME_MAP = new HashMap<>();
    static {
        PRODUCT_NAME_MAP.put(ProductType.CARD_AFENTI.getCardType(), ProductType.CARD_AFENTI.getCardName());
        PRODUCT_NAME_MAP.put(ProductType.CARD_WALKER.getCardType(), ProductType.CARD_WALKER.getCardName());
        PRODUCT_NAME_MAP.put(ProductType.CARD_SANGUODMZ.getCardType(), ProductType.CARD_SANGUODMZ.getCardName());
        PRODUCT_NAME_MAP.put(ProductType.CARD_TRAVEL_AMERICA.getCardType(), ProductType.CARD_TRAVEL_AMERICA.getCardName());
        PRODUCT_NAME_MAP.put(ProductType.CARD_SPG.getCardType(), ProductType.CARD_SPG.getCardName());
        PRODUCT_NAME_MAP.put(ProductType.CARD_PETSWAR.getCardType(), ProductType.CARD_PETSWAR.getCardName());
        PRODUCT_NAME_MAP.put(ProductType.CARD_WALKERELF.getCardType(), ProductType.CARD_WALKERELF.getCardName());
    }

    private String dateRange;        // could be a date or a date range
    private Boolean prodcutSplit;
    Map<String, ProductPaymentSummaryInfoData> productSummaryData;

    public DailyPaymentSummaryInfoData(String dateRange, Boolean productSplit) {
        this.dateRange = dateRange;
        this.prodcutSplit = productSplit;
        productSummaryData = new LinkedHashMap<>();
    }

    public void append(AfentiOrder order) {
        if (order == null) {
            return;
        }

        OrderProductServiceType productServiceType = OrderProductServiceType.safeParse(order.getProductServiceType());
        if (!PRODUCT_NAME_MAP.containsKey(productServiceType)) {
            return;
        }

        // 计算所有产品信息
        String productName = "所有";
        ProductPaymentSummaryInfoData productData = productSummaryData.get(productName);
        if (productData == null) {
            productData = new ProductPaymentSummaryInfoData(productName);
            productSummaryData.put(productName, productData);
        }

        productData.append(order);

        // 判断是否需要分产品统计
        if (!prodcutSplit) {
            return;
        }

        productName = PRODUCT_NAME_MAP.get(productServiceType);
        productData = productSummaryData.get(productName);
        if (productData == null) {
            productData = new ProductPaymentSummaryInfoData(productName);
            productSummaryData.put(productName, productData);
        }

        productData.append(order);
    }

    public void append(PaymentDataSummary order) {
        if (order == null) {
            return;
        }

        // 计算所有产品信息
        appendProductData("所有",
                order.getTotal_order_num(),
                order.getTotal_student_num(),
                order.getTotal_order_amount(),
                order.getTotal_cardpay_amount(),
                order.getTotal_refund_amount());

        // 判断是否需要分产品统计
        if (!prodcutSplit || order.getProductPaymentData() == null) {
            return;
        }

        List<ProductType> sharableProducts = ProductType.getSharePaymentProducts();
        for (ProductType productType : sharableProducts) {
            String productName = productType.getCardType().name();
            if (order.getProductPaymentData().containsKey(productName)) {
                ProductPaymentInfo payment = order.getProductPaymentData().get(productName);
                appendProductData(productType.getCardName(),
                        payment.getOrder_count(),
                        payment.getPay_user_count(),
                        payment.getOrder_amount(),
                        payment.getCard_pay_amount(),
                        payment.getRefund_amount());
            }
        }
    }

    private void appendProductData(String productName, Integer orderCount, Integer orderUserNum, Double orderAmount, Double cardpayAmount, Double refundAmount) {
        if (orderCount == null || orderCount == 0) {
            return;
        }

        ProductPaymentSummaryInfoData productData = productSummaryData.get(productName);
        if (productData == null) {
            productData = new ProductPaymentSummaryInfoData(productName);
            productSummaryData.put(productName, productData);
        }

        productData.append(orderCount, orderUserNum, orderAmount, cardpayAmount, refundAmount);
    }

    public int getDeepSize() {
        return productSummaryData.size();
    }

    public static boolean isValidProduct(OrderProductServiceType productServiceType) {
        return productServiceType != null && PRODUCT_NAME_MAP.containsKey(productServiceType);
    }

}
