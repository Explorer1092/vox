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

package com.voxlearning.utopia.service.reward.base.buffer;

import com.voxlearning.alps.annotation.common.ThreadSafe;
import com.voxlearning.alps.core.concurrent.ReentrantReadWriteLocker;
import com.voxlearning.utopia.service.reward.api.mapper.VersionedRewardProductList;
import com.voxlearning.utopia.service.reward.entity.RewardProduct;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@ThreadSafe
public class RewardProductBuffer {

    private final ReentrantReadWriteLocker locker = new ReentrantReadWriteLocker();

    private long version;
    private List<RewardProduct> rewardProductList;
    private Map<Long, RewardProduct> idMap;

    public void attach(VersionedRewardProductList data) {
        Objects.requireNonNull(data);
        locker.withinWriteLockWithoutResult(() -> {
            version = data.getVersion();
            rewardProductList = data.getRewardProductList().stream().collect(Collectors.toList());
            idMap = new LinkedHashMap<>();
            rewardProductList.forEach(e -> idMap.put(e.getId(), e));
        });
    }

    public VersionedRewardProductList dump() {
        return locker.withinReadLock(() -> {
            VersionedRewardProductList data = new VersionedRewardProductList();
            data.setVersion(version);
            data.setRewardProductList(rewardProductList.stream().collect(Collectors.toList()));
            return data;
        });
    }

    public long getVersion() {
        return locker.withinReadLock(() -> version);
    }

    public Map<Long, RewardProduct> loadRewardProductMap() {
        return locker.withinReadLock(() -> new LinkedHashMap<>(idMap));
    }
}
