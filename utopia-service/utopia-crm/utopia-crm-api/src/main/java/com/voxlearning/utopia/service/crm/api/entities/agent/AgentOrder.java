/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.crm.api.entities.agent;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentFieldIgnore;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.crm.api.constants.SystemPlatformType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentOrderPaymentMode;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentProductType;
import com.voxlearning.utopia.service.crm.api.entities.AbstractBaseApply;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 * AGENT订单表
 *
 * @author Shuai Huan
 * @author Yuechen Wang
 * 调整表的结构，让这个表能够跟CRM相关上
 * @serial
 * @since 2014-7-14
 * FIXME: java bean 就是 java bean，不要挑战规范去修改getter/setter的类型。你可能会碰到很多不可预知的问题
 * FIXME: 先把被玩坏的两个字段改名了，这样才有干净的getter/setter
 */
@DocumentTable(table = "AGENT_ORDER")
@UtopiaCacheExpiration
@UtopiaCacheRevision("20180919")
@DocumentConnection(configName = "agent")
public class AgentOrder extends AbstractBaseApply {

    private static final long serialVersionUID = 5894565484538456964L;

    @Getter @Setter @UtopiaSqlColumn(name = "CREATOR") String realCreator;                 // 用户ID，CRM里传过来的使用admin.前缀
    @Getter @Setter @UtopiaSqlColumn String creatorName;             // 用户姓名
    @Getter @Setter @UtopiaSqlColumn Long creatorGroup;              // 用户所属组
    @Getter @Setter @UtopiaSqlColumn Integer orderType;              // 订单类型
    @Getter @Setter @UtopiaSqlColumn Float orderAmount;              // 订单金额
    @Getter @Setter @UtopiaSqlColumn Float pointChargeAmount;        // 点数抵现金额
    @Getter @Setter @UtopiaSqlColumn String orderNotes;              // 订单补充说明
    @Getter @Setter @UtopiaSqlColumn Integer orderStatus;            // 订单状态
    @Getter @Setter @UtopiaSqlColumn(name = "LATEST_PROCESSOR") String realLatestProcessor;         // 最近处理人ID，CRM里传过来的使用admin.前缀
    @Getter @Setter @UtopiaSqlColumn String latestProcessorName;     // 最近处理人姓名
    @Getter @Setter @UtopiaSqlColumn Long latestProcessorGroup;      // 最近处理人所属组
    @Getter @Setter @UtopiaSqlColumn String consignee;              // 收货人姓名
    @Getter @Setter @UtopiaSqlColumn String province;              // 收货地址 - 城市
    @Getter @Setter @UtopiaSqlColumn String city;              // 收货地址 - 城市
    @Getter @Setter @UtopiaSqlColumn String county;              // 收货地址 - 城市
    @Getter @Setter @UtopiaSqlColumn String address;              // 订单收货具体位置
    @Getter @Setter @UtopiaSqlColumn String mobile;              // 收货人电话
    @Getter @Setter @UtopiaSqlColumn String logisticsInfo;              // 物流信息
    @Getter @Setter @UtopiaSqlColumn Long invoiceId;              // 发货单Id
    @Getter @Setter @UtopiaSqlColumn Date orderTime;             // 下单时间
    @Getter @Setter @UtopiaSqlColumn Integer paymentMode;           // 支付方式     AgentOrderPaymentMode 默认是物料费用     1：物料费用  2：城市费用  3:自费
    @Getter @Setter @UtopiaSqlColumn String paymentVoucher;         // 支付凭证
    @Getter @Setter @UtopiaSqlColumn Integer cityCostMonth;         // 城市支付月份 例如 201701 201702
    @Getter @Setter @UtopiaSqlColumn (name = "COST_MONTH_STR") String costMonthStr;         // 北京市 201809 1.6 元, 201808 1.2 元
    public static String ck_id(Long id) {
        return CacheKeyGenerator.generateCacheKey(AgentOrder.class, id);
    }

    public static String ck_creator(String creator) {
        return CacheKeyGenerator.generateCacheKey(AgentOrder.class, "creator", creator);
    }

    public static String ck_wid(Long workflowId) {
        return CacheKeyGenerator.generateCacheKey(AgentOrder.class, "wid", workflowId);
    }

    public static String ck_platform_uid(SystemPlatformType userPlatform, String userAccount) {
        return CacheKeyGenerator.generateCacheKey(AgentOrder.class,
                new String[]{"platform", "uid"},
                new Object[]{userPlatform, userAccount});
    }

//    public static String ck_orderNotes(String orderNotes) {
//        return CacheKeyGenerator.generateCacheKey(AgentOrder.class, "notes", orderNotes);
//    }

    @DocumentFieldIgnore
    @Getter @Setter List<AgentOrderProduct> orderProductList;        // 订单中的商品列表

    public static final String CRM_PREFIX = "17admin.";                // CRM里传过来的ID使用的前缀

    public boolean hasProductCard() {
        if (CollectionUtils.isEmpty(orderProductList)) {
            return false;
        }

        for (AgentOrderProduct orderProduct : orderProductList) {
            int productType = orderProduct.getProductType();
            if (productType == AgentProductType.WALKER.getType()
                    || productType == AgentProductType.AFENTI.getType()
                    || productType == AgentProductType.PICARO.getType()) {
                return true;
            }
        }

        return false;
    }

    @JsonIgnore
    public Integer fetchTotalProductCardQuantity() {
        if (CollectionUtils.isEmpty(orderProductList)) {
            return 0;
        }

        int count = 0;
        for (AgentOrderProduct orderProduct : orderProductList) {
            int productType = orderProduct.getProductType();
            if (productType == AgentProductType.WALKER.getType()
                    || productType == AgentProductType.AFENTI.getType()
                    || productType == AgentProductType.PICARO.getType()) {
                count += orderProduct.getProductQuantity();
            }
        }

        return count;
    }

    // CREATOR 字段的 setter/getter 方法
    public void setCreator(Long creatorId) {
        this.realCreator = creatorId.toString();
    }

    public void setCreator(String crmCreator) {
        this.realCreator = CRM_PREFIX.concat(crmCreator);
    }

    public Long getCreator() {
        if (this.realCreator.startsWith(CRM_PREFIX)) {
            return 0L;
        }
        return SafeConverter.toLong(this.realCreator);
    }

    public String getCreator(String crmPrefix) {
        if (CRM_PREFIX.equals(crmPrefix)) {
            return this.realCreator.substring(CRM_PREFIX.length());
        }
        return null;
    }

    public void setLatestProcessor(Long creatorId) {
        this.realLatestProcessor = creatorId.toString();
    }

    public Long getLatestProcessor() {
        if (this.realLatestProcessor.startsWith(CRM_PREFIX)) {
            return 0L;
        }
        return SafeConverter.toLong(this.realLatestProcessor);
    }

    @Override
    public String generateSummary() {
        StringBuilder builder = new StringBuilder(StringUtils.formatMessage("订单编号:{},申请购买：", id));
        if (CollectionUtils.isNotEmpty(orderProductList)) {
            for (AgentOrderProduct product : orderProductList) {
                builder.append(product.getProductName()).append(" * ").append(product.getProductQuantity()).append("<br>");
            }
        }
        builder.append("总金额：").append(orderAmount).append("<br>");
        builder.append("支付方式：").append(AgentOrderPaymentMode.safePayIdToMode(paymentMode, AgentOrderPaymentMode.MATERIAL_COST).getPayDes());
        if(StringUtils.isNotBlank(costMonthStr)){
            builder.append("(").append(costMonthStr).append(")");
        }
        return builder.toString();
    }
}
