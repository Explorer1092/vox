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

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.dao.jdbc.policy.RoutingPolicyExecutorBuilder;
import com.voxlearning.alps.dao.jdbc.policy.UtopiaRoutingDataSourcePolicy;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.alps.spi.common.DataProvider;
import com.voxlearning.utopia.api.legacy.MemcachedKeyConstants;
import com.voxlearning.utopia.service.reward.cache.RewardCache;
import com.voxlearning.utopia.service.reward.constant.RewardOrderStatus;
import com.voxlearning.utopia.service.reward.entity.RewardOrder;
import lombok.Getter;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * For loading reward orders.
 *
 * @author Xiaohai Zhang
 * @since Dec 4, 2014
 */
public class RewardOrderLoader {

    @Getter private final DataProvider<Collection<Long>, Map<Long, RewardOrder>> idDataProvider;
    @Getter private final DataProvider<Collection<Long>, Map<Long, List<RewardOrder>>> userIdDataProvider;

    public RewardOrderLoader(DataProvider<Collection<Long>, Map<Long, RewardOrder>> idDataProvider,
                             DataProvider<Collection<Long>, Map<Long, List<RewardOrder>>> userIdDataProvider) {

        this.idDataProvider = Objects.requireNonNull(idDataProvider);
        this.userIdDataProvider = Objects.requireNonNull(userIdDataProvider);
    }

    public Map<Long, RewardOrder> loadRewardOrders(Collection<Long> orderIds) {
        Map<Long, RewardOrder> result = new LinkedHashMap<>();
        result.putAll(idDataProvider.provide(orderIds));
        for (Iterator<Map.Entry<Long, RewardOrder>> it = result.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Long, RewardOrder> entry = it.next();
            if (Boolean.TRUE.equals(entry.getValue().getDisabled())) {
                it.remove();
            }
        }
        return result;
    }

    public RewardOrder loadRewardOrder(Long orderId) {
        if (orderId == null) {
            return null;
        }
        return loadRewardOrders(Collections.singleton(orderId)).get(orderId);
    }

    public Map<Long, List<RewardOrder>> loadUserRewardOrders(Collection<Long> userIds) {
        return userIdDataProvider.provide(userIds);
    }

    public List<RewardOrder> loadUserRewardOrders(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        List<RewardOrder> result = loadUserRewardOrders(Collections.singleton(userId)).get(userId);
        if (result == null) {
            return Collections.emptyList();
        }
        return result;
    }

    public int loadUserSubmitRewardOrderCount(Long userId) {
        if (userId == null) {
            return 0;
        }
        String key = CacheKeyGenerator.generateCacheKey(MemcachedKeyConstants.REWARD_USER_ORDER_COUNT, null, new Object[]{userId});
        CacheObject<Integer> cacheObject = RewardCache.getRewardCache().get(key);
        if (cacheObject != null && cacheObject.getValue() != null) {
            return cacheObject.getValue();
        }
        List<RewardOrder> orders = loadUserRewardOrders(userId).stream()
                .filter(source -> RewardOrderStatus.SUBMIT.name().equals(source.getStatus()))
                .collect(Collectors.toList());
        RewardCache.getRewardCache().add(key, DateUtils.getCurrentToDayEndSecond(), orders.size());
        return orders.size();
    }

}
