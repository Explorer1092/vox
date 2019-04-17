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

package com.voxlearning.utopia.service.reward.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ExposeServices;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.reward.api.RewardManagement;
import com.voxlearning.utopia.service.reward.constant.RewardOrderStatus;
import com.voxlearning.utopia.service.reward.entity.*;
import com.voxlearning.utopia.service.reward.impl.dao.*;
import com.voxlearning.utopia.service.reward.impl.internal.InternalRewardSkuService;
import com.voxlearning.utopia.service.reward.impl.persistence.RewardCompleteOrderPersistence;
import com.voxlearning.utopia.service.reward.impl.version.RewardProductVersion;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Default {@link RewardManagement} implementation.
 *
 * @author Xiaohai Zhang
 * @since Jan 13, 2015
 */
@Named
@Service(interfaceClass = RewardManagement.class)
@ExposeServices({
        @ExposeService(interfaceClass = RewardManagement.class, version = @ServiceVersion(version = "1.2")),
        @ExposeService(interfaceClass = RewardManagement.class, version = @ServiceVersion(version = "1.3"))
})
public class RewardManagementImpl extends SpringContainerSupport implements RewardManagement {

    @Inject private InternalRewardSkuService internalRewardSkuService;
    @Inject private RewardCompleteOrderPersistence rewardCompleteOrderPersistence;
    @Inject private RewardCouponDetailPersistence rewardCouponDetailPersistence;
    @Inject private RewardLogisticsPersistence rewardLogisticsPersistence;
    @Inject private RewardOrderPersistence rewardOrderPersistence;
    @Inject private RewardOrderSummaryPersistence rewardOrderSummaryPersistence;
    @Inject private RewardProductCategoryRefDao rewardProductCategoryRefDao;
    @Inject private RewardProductDao rewardProductDao;
    @Inject private RewardProductTagRefDao rewardProductTagRefDao;
    @Inject private RewardProductVersion rewardProductVersion;
    @Inject private RewardSkuDao rewardSkuDao;
    @Inject private UtopiaSqlFactory utopiaSqlFactory;
    @Inject private RewardCouponDao rewardCouponDao;

    private UtopiaSql utopiaSql;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        utopiaSql = utopiaSqlFactory.getDefaultUtopiaSql();
    }

    @Override
    public MapMessage addRewardProduct(final RewardProduct rewardProduct, final String categoryIds, final String tagIds, final List<Map<String, Object>> skus) {
        if (rewardProduct == null || skus == null) {
            return MapMessage.errorMessage("参数错误！");
        }
        if (StringUtils.isBlank(rewardProduct.getProductName())) {
            return MapMessage.errorMessage("参数错误！");
        }
        if (StringUtils.isBlank(rewardProduct.getProductType())) {
            return MapMessage.errorMessage("参数错误！");
        }
        if (StringUtils.isBlank(rewardProduct.getTags())) {
            return MapMessage.errorMessage("参数错误！");
        }
        if (Math.abs(rewardProduct.getPriceS() - 0) < 0.00001) {
            return MapMessage.errorMessage("学生原始积分不能为零！");
        }
        if (Math.abs(rewardProduct.getPriceT() - 0) < 0.00001) {
            return MapMessage.errorMessage("老师原始积分不能为零！");
        }
        if (StringUtils.isBlank(categoryIds)) {
            return MapMessage.errorMessage("参数错误！");
        }


        try {
            final MapMessage resultMsg = MapMessage.successMessage();
            utopiaSql.withTransaction(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                    Long productId;
                    if (rewardProduct.getId() != null) {
                        productId = rewardProduct.getId();
                        rewardProductCategoryRefDao.deleteByProductId(productId);
                        rewardProductTagRefDao.deleteByProductId(productId);
                        rewardProductDao.replace(rewardProduct);
                    } else {
                        rewardProductDao.insert(rewardProduct);
                        productId = rewardProduct.getId();
                    }

                    // 放上商品id，供后面兑换券生成的时候关联用
                    resultMsg.add("productId",productId);
                    //分类 标签

                    String[] tagIdArray = StringUtils.split(tagIds, ",");
                    String[] categoryIdArray = StringUtils.split(categoryIds, ",");
                    for (String tagId : tagIdArray) {
                        RewardProductTagRef tagRef = new RewardProductTagRef();
                        tagRef.setTagId(ConversionUtils.toLong(tagId));
                        tagRef.setProductId(productId);
                        rewardProductTagRefDao.insert(tagRef);
                    }

                    for (String categoryId : categoryIdArray) {
                        RewardProductCategoryRef categoryRef = new RewardProductCategoryRef();
                        categoryRef.setCategoryId(ConversionUtils.toLong(categoryId));
                        categoryRef.setProductId(productId);
                        rewardProductCategoryRefDao.insert(categoryRef);
                    }

                    //单品
                    List<RewardSku> skuList = internalRewardSkuService.$findRewardSkusByProductId(productId)
                            .stream().collect(Collectors.toList());
                    List<RewardSku> delList = new ArrayList<>();
                    for (Map<String, Object> skuMap : skus) {
                        if (skuMap.get("skuId") != null && StringUtils.isNotBlank(skuMap.get("skuId").toString())) {
                            for (RewardSku sku : skuList) {
                                if (conversionService.convert(skuMap.get("skuId").toString(), Long.class).equals(sku.getId())) {
                                    delList.add(sku);
                                }
                            }
                            long skuId = SafeConverter.toLong(skuMap.get("skuId"));
                            RewardSku sku = internalRewardSkuService.$loadRewardSku(skuId);
                            sku.setSkuName(skuMap.get("skuName").toString());
                            sku.setInventorySellable(ConversionUtils.toInt(skuMap.get("skuQuantity")));
                            rewardSkuDao.replace(sku);
                        } else {
                            RewardSku sku = new RewardSku();
                            sku.setProductId(productId);
                            sku.setSkuName(skuMap.get("skuName").toString());
                            sku.setInventorySellable(ConversionUtils.toInt(skuMap.get("skuQuantity")));
                            rewardSkuDao.insert(sku);
                        }
                    }
                    skuList.removeAll(delList);
                    for (RewardSku sku : skuList) {
                        internalRewardSkuService.$removeRewardSku(sku.getId());
                    }
                }
            });
            rewardProductVersion.increase();
            return resultMsg.setInfo("编辑成功");
        } catch (Exception ex) {
            logger.error("Failed to add reward product", ex);
            return MapMessage.errorMessage("编辑失败！");
        }
    }

    @Override
    public MapMessage persistRewardCouponDetail(RewardCouponDetail rewardCouponDetail) {
        try {
            rewardCouponDetailPersistence.insert(rewardCouponDetail);
            return MapMessage.successMessage();
        } catch (Exception ex) {
            // logger.error("Failed to persist reward coupon detail", ex);
            return MapMessage.errorMessage();
        }
    }

    @Override
    public MapMessage couponUsed(RewardCouponDetail couponDetail) {
        MapMessage message;
        try {
            boolean ret = rewardCouponDetailPersistence.couponUsed(couponDetail);
            message = new MapMessage();
            message.setSuccess(ret);
        } catch (Exception ex) {
            logger.error("Failed to use coupon", ex);
            message = MapMessage.errorMessage();
        }
        return message;
    }

    @Override
    public MapMessage removeRewardOrder(Long orderId) {
        try {
            rewardOrderPersistence.removeOrder(orderId);
            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error("Failed to remove reward order", ex);
            return MapMessage.errorMessage();
        }
    }

    @Override
    public MapMessage updateRewardOrderStatus(Long orderId, String reason, RewardOrderStatus to) {
        try {
            rewardOrderPersistence.updateOrderStatus(orderId, reason, to);
            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error("Failed to update reward order status", ex);
            return MapMessage.errorMessage();
        }
    }

    @Override
    public List<RewardOrder> loadExportRewardOrdersByParameters(Map<String, Object> parameters) {
        return rewardOrderPersistence.loadExportOrderByParam(parameters);
    }

    @Override
    public RewardCouponDetail loadRewardCouponDetail(Long id) {
        return rewardCouponDetailPersistence.load(id);
    }

    @Override
    public void persistRewardOrderSummary(RewardOrderSummary rewardOrderSummary) {
        rewardOrderSummaryPersistence.insert(rewardOrderSummary);
    }

    @Override
    public void updateRewardOrderSummary(Long id, RewardOrderSummary rewardOrderSummary) {
        rewardOrderSummary.setId(id);
        rewardOrderSummaryPersistence.replace(rewardOrderSummary);
    }

    @Override
    public List<RewardOrderSummary> loadRewardOrderSummariesByMonth(Integer month) {
        return rewardOrderSummaryPersistence.loadByMonth(month);
    }

    @Override
    public Long persistRewardCompleteOrder(RewardCompleteOrder rewardCompleteOrder) {
        if (rewardCompleteOrder == null) {
            return null;
        }
        rewardCompleteOrderPersistence.insert(rewardCompleteOrder);
        return rewardCompleteOrder.getId();
    }

    @Override
    public Long upsertRewardCompleteOrder(RewardCompleteOrder rewardCompleteOrder) {
        if (rewardCompleteOrder == null) {
            return null;
        }
        rewardCompleteOrderPersistence.upsert(rewardCompleteOrder);
        return rewardCompleteOrder.getId();
    }

    @Override
    public int updateRewardOrderById(Long orderId, RewardOrderStatus orderStatus, Long completeOrderId) {
        return rewardOrderPersistence.updateOrderById(orderId, orderStatus, completeOrderId);
    }

    @Override
    public int updateRewardOrderLogisticsId(Long orderId, Long logisticsId) {
        return rewardOrderPersistence.updateRewardOrderLogisticsId(orderId, logisticsId);
    }

    @Override
    public int updateRewardCompleteOrderLogisticsId(Long completeOrderId, Long logisticsId) {
        return rewardCompleteOrderPersistence.updateRewardCompleteOrderLogisticsId(completeOrderId, logisticsId);
    }

    @Override
    public int updateRewardCompleteOrderStatus(Long id, RewardOrderStatus status) {
        return rewardCompleteOrderPersistence.updateCompleteOrderStatus(id, status);
    }

    @Override
    public MapMessage addRewardCoupon(RewardCoupon coupon) {
        if(coupon == null || coupon.getProductId() == null || StringUtils.isEmpty(coupon.getName()))
            return MapMessage.errorMessage("参数错误!");

        Long relatePId = coupon.getProductId();
        RewardProduct relateProduct = rewardProductDao.load(relatePId);
        if(relateProduct == null)
            return MapMessage.errorMessage("未关联商品!");

        try {
            rewardCouponDao.upsert(coupon);
            return MapMessage.successMessage();
        }catch(Exception e){
            return MapMessage.errorMessage("保存兑换券失败!原因:" + e.getMessage());
        }
    }

    // CRM使用
    @Override
    public List<RewardOrder> loadRewardOrderByLogisticId(Long logisticId) {
        return rewardOrderPersistence.loadByLogisticId(logisticId);
    }
}
