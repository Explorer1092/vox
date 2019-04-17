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

package com.voxlearning.utopia.service.reward.api;

import com.voxlearning.alps.annotation.remote.ServiceMethod;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.reward.constant.RewardOrderStatus;
import com.voxlearning.utopia.service.reward.entity.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Reward management interface for supporting crm.
 *
 * @author Xiaohai Zhang
 * @since Jan 13, 2015
 */
@ServiceVersion(version = "1.3")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface RewardManagement extends IPingable {

    MapMessage addRewardProduct(RewardProduct rewardProduct, String categoryIds, String tagIds, List<Map<String, Object>> skus);

    MapMessage removeRewardOrder(Long orderId);

    MapMessage updateRewardOrderStatus(Long orderId, String reason, RewardOrderStatus to);

    MapMessage persistRewardCouponDetail(RewardCouponDetail rewardCouponDetail);

    MapMessage couponUsed(RewardCouponDetail couponDetail);

    @ServiceMethod(timeout = 5, unit = TimeUnit.MINUTES, retries = 0)
    List<RewardOrder> loadExportRewardOrdersByParameters(Map<String, Object> parameters);

    RewardCouponDetail loadRewardCouponDetail(Long id);

    @ServiceMethod(timeout = 10, unit = TimeUnit.SECONDS, retries = 0)
    void persistRewardOrderSummary(RewardOrderSummary rewardOrderSummary);

    @ServiceMethod(timeout = 10, unit = TimeUnit.SECONDS, retries = 0)
    void updateRewardOrderSummary(Long id, RewardOrderSummary rewardOrderSummary);

    @ServiceMethod(timeout = 30, unit = TimeUnit.SECONDS, retries = 1)
    List<RewardOrderSummary> loadRewardOrderSummariesByMonth(Integer month);

    @ServiceMethod(timeout = 10, unit = TimeUnit.SECONDS, retries = 0)
    Long persistRewardCompleteOrder(RewardCompleteOrder rewardCompleteOrder);

    @ServiceMethod(timeout = 10, unit = TimeUnit.SECONDS, retries = 0)
    Long upsertRewardCompleteOrder(RewardCompleteOrder rewardCompleteOrder);

    @ServiceMethod(timeout = 10, unit = TimeUnit.SECONDS, retries = 0)
    int updateRewardOrderById(Long orderId, RewardOrderStatus orderStatus, Long completeOrderId);

    int updateRewardOrderLogisticsId(Long orderId, Long logisticsId);

    int updateRewardCompleteOrderLogisticsId(Long completeOrderId, Long logisticsId);

    List<RewardOrder> loadRewardOrderByLogisticId(Long logisticId);

    int updateRewardCompleteOrderStatus(Long orderCompleteId, RewardOrderStatus status);

    MapMessage addRewardCoupon(RewardCoupon coupon);
}
