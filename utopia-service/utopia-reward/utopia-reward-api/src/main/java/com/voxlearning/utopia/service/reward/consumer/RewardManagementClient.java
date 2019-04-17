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

package com.voxlearning.utopia.service.reward.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.annotation.remote.ServiceMethod;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.reward.api.RewardManagement;
import com.voxlearning.utopia.service.reward.constant.RewardOrderStatus;
import com.voxlearning.utopia.service.reward.entity.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Client implementation of {@link RewardManagement}.
 *
 * @author Xiaohai Zhang
 * @since Jan 13, 2015
 */
public class RewardManagementClient implements RewardManagement {

    @ImportService(interfaceClass = RewardManagement.class)
    private RewardManagement remoteReference;

    @Override
    public MapMessage addRewardProduct(RewardProduct rewardProduct, String categoryIds, String tagIds, List<Map<String, Object>> skus) {
        return remoteReference.addRewardProduct(rewardProduct, categoryIds, tagIds, skus);
    }

    @Override
    public MapMessage removeRewardOrder(Long orderId) {
        return remoteReference.removeRewardOrder(orderId);
    }

    @Override
    public MapMessage persistRewardCouponDetail(RewardCouponDetail rewardCouponDetail) {
        return remoteReference.persistRewardCouponDetail(rewardCouponDetail);
    }

    @Override
    public MapMessage couponUsed(RewardCouponDetail couponDetail) {
        return remoteReference.couponUsed(couponDetail);
    }

    @Override
    @ServiceMethod(timeout = 5, unit = TimeUnit.MINUTES, retries = 0)
    public List<RewardOrder> loadExportRewardOrdersByParameters(Map<String, Object> parameters) {
        return remoteReference.loadExportRewardOrdersByParameters(parameters);
    }

    @Override
    public RewardCouponDetail loadRewardCouponDetail(Long id) {
        return remoteReference.loadRewardCouponDetail(id);
    }

    @Override
    @ServiceMethod(timeout = 10, unit = TimeUnit.SECONDS, retries = 0)
    public void persistRewardOrderSummary(RewardOrderSummary rewardOrderSummary) {
        remoteReference.persistRewardOrderSummary(rewardOrderSummary);
    }

    @Override
    @ServiceMethod(timeout = 10, unit = TimeUnit.SECONDS, retries = 0)
    public void updateRewardOrderSummary(Long id, RewardOrderSummary rewardOrderSummary) {
        remoteReference.updateRewardOrderSummary(id, rewardOrderSummary);
    }

    @Override
    @ServiceMethod(timeout = 30, unit = TimeUnit.SECONDS, retries = 1)
    public List<RewardOrderSummary> loadRewardOrderSummariesByMonth(Integer month) {
        return remoteReference.loadRewardOrderSummariesByMonth(month);
    }

    @Override
    @ServiceMethod(timeout = 10, unit = TimeUnit.SECONDS, retries = 0)
    public Long persistRewardCompleteOrder(RewardCompleteOrder rewardCompleteOrder) {
        return remoteReference.persistRewardCompleteOrder(rewardCompleteOrder);
    }

    @Override
    public Long upsertRewardCompleteOrder(RewardCompleteOrder rewardCompleteOrder) {
        return remoteReference.upsertRewardCompleteOrder(rewardCompleteOrder);
    }

    @Override
    @ServiceMethod(timeout = 10, unit = TimeUnit.SECONDS, retries = 0)
    public int updateRewardOrderById(Long orderId, RewardOrderStatus orderStatus, Long completeOrderId) {
        return remoteReference.updateRewardOrderById(orderId, orderStatus, completeOrderId);
    }

    @Override
    public int updateRewardOrderLogisticsId(Long orderId, Long logisticsId) {
        return remoteReference.updateRewardOrderLogisticsId(orderId, logisticsId);
    }

    @Override
    public int updateRewardCompleteOrderLogisticsId(Long completeOrderId, Long logisticsId) {
        return remoteReference.updateRewardCompleteOrderLogisticsId(completeOrderId, logisticsId);
    }

    @Override
    public List<RewardOrder> loadRewardOrderByLogisticId(Long logisticId) {
        return remoteReference.loadRewardOrderByLogisticId(logisticId);
    }

    @Override
    public MapMessage updateRewardOrderStatus(Long orderId, String reason, RewardOrderStatus to) {
        return remoteReference.updateRewardOrderStatus(orderId, reason, to);
    }

    @Override
    public int updateRewardCompleteOrderStatus(Long orderCompleteId, RewardOrderStatus status) {
        return remoteReference.updateRewardCompleteOrderStatus(orderCompleteId, status);
    }

    @Override
    public MapMessage addRewardCoupon(RewardCoupon coupon) {
        return remoteReference.addRewardCoupon(coupon);
    }
}
