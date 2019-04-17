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

package com.voxlearning.utopia.service.reward.base.support;

import com.voxlearning.alps.spi.common.DataProvider;
import com.voxlearning.utopia.service.reward.entity.RewardCouponDetail;

import java.util.*;

/**
 * For loading reward coupon details.
 *
 * @author Xiaohai Zhang
 * @since Dec 5, 2014
 */
public class RewardCouponDetailLoader {
    private final DataProvider<Collection<Long>, Map<Long, List<RewardCouponDetail>>> productIdDataProvider;
    private final DataProvider<Collection<Long>, Map<Long, List<RewardCouponDetail>>> userIdDataProvider;

    public RewardCouponDetailLoader(DataProvider<Collection<Long>, Map<Long, List<RewardCouponDetail>>> productIdDataProvider,
                                    DataProvider<Collection<Long>, Map<Long, List<RewardCouponDetail>>> userIdDataProvider) {
        this.productIdDataProvider = Objects.requireNonNull(productIdDataProvider);
        this.userIdDataProvider = Objects.requireNonNull(userIdDataProvider);
    }

    public Map<Long, List<RewardCouponDetail>> loadProductRewardCouponDetails(Collection<Long> productIds) {
        return productIdDataProvider.provide(productIds);
    }

    public List<RewardCouponDetail> loadProductRewardCouponDetails(Long productId) {
        if (productId == null) {
            return Collections.emptyList();
        }
        List<RewardCouponDetail> result = loadProductRewardCouponDetails(Collections.singleton(productId)).get(productId);
        if (result == null) {
            return Collections.emptyList();
        }
        return result;
    }

    public Map<Long, List<RewardCouponDetail>> loadUserRewardCouponDetails(Collection<Long> userIds) {
        return userIdDataProvider.provide(userIds);
    }

    public List<RewardCouponDetail> loadUserRewardCouponDetails(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        List<RewardCouponDetail> result = loadUserRewardCouponDetails(Collections.singleton(userId)).get(userId);
        if (result == null) {
            return Collections.emptyList();
        }
        return result;
    }
}
