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
import com.voxlearning.utopia.service.reward.entity.RewardWishOrder;

import java.util.*;

/**
 * Helper implementation for loading reward wish orders.
 *
 * @author Xiaohai Zhang
 * @since Dec 2, 2014
 */
public class RewardWishOrderLoader {
    private final DataProvider<Collection<Long>, Map<Long, RewardWishOrder>> idDataProvider;
    private final DataProvider<Collection<Long>, Map<Long, List<RewardWishOrder>>> userIdDataProvider;

    public RewardWishOrderLoader(DataProvider<Collection<Long>, Map<Long, RewardWishOrder>> idDataProvider,
                                 DataProvider<Collection<Long>, Map<Long, List<RewardWishOrder>>> userIdDataProvider) {
        this.idDataProvider = Objects.requireNonNull(idDataProvider);
        this.userIdDataProvider = Objects.requireNonNull(userIdDataProvider);
    }

    public Map<Long, RewardWishOrder> loadRewardWishOrders(Collection<Long> ids) {
        return idDataProvider.provide(ids);
    }

    public RewardWishOrder loadRewardWishOrder(Long id) {
        if (id == null) {
            return null;
        }
        return loadRewardWishOrders(Collections.singleton(id)).get(id);
    }

    public Map<Long, List<RewardWishOrder>> loadUserRewardWishOrders(Collection<Long> userIds) {
        return userIdDataProvider.provide(userIds);
    }

    public List<RewardWishOrder> loadUserRewardWishOrders(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        List<RewardWishOrder> result = loadUserRewardWishOrders(Collections.singleton(userId)).get(userId);
        if (result == null) {
            return Collections.emptyList();
        }
        return result;
    }
}
