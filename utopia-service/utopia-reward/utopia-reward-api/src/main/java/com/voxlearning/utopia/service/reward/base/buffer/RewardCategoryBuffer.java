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
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.concurrent.ReentrantReadWriteLocker;
import com.voxlearning.utopia.service.reward.api.filter.RewardCategoryFilter;
import com.voxlearning.utopia.service.reward.api.mapper.VersionedRewardCategoryList;
import com.voxlearning.utopia.service.reward.constant.RewardProductType;
import com.voxlearning.utopia.service.reward.entity.RewardCategory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@ThreadSafe
public class RewardCategoryBuffer {

    private final ReentrantReadWriteLocker locker = new ReentrantReadWriteLocker();

    private long version;
    private List<RewardCategory> rewardCategoryList;
    private Map<Long, RewardCategory> idMap;

    public void attach(VersionedRewardCategoryList data) {
        Objects.requireNonNull(data);
        locker.withinWriteLockWithoutResult(() -> {
            version = data.getVersion();
            rewardCategoryList = data.getRewardCategoryList().stream().collect(Collectors.toList());

            this.idMap = rewardCategoryList.stream()
                    .collect(Collectors.toMap(k -> k.getId(), v -> v));
        });
    }

    public VersionedRewardCategoryList dump() {
        return locker.withinReadLock(() -> {
            VersionedRewardCategoryList data = new VersionedRewardCategoryList();
            data.setVersion(version);
            data.setRewardCategoryList(rewardCategoryList.stream().collect(Collectors.toList()));
            return data;
        });
    }

    public long getVersion() {
        return locker.withinReadLock(() -> version);
    }

    public List<RewardCategory> loadRewardCategories(final RewardProductType productType,
                                                     final UserType userType) {
        return RewardCategoryFilter.filter(dump().getRewardCategoryList(), productType, userType);
    }

    public List<RewardCategory> loadRewardCategories(final UserType userType) {
        return RewardCategoryFilter.filter(dump().getRewardCategoryList(), userType);
    }

    public Map<Long, RewardCategory> loadRewardCategoryMap() {
        return locker.withinReadLock(() -> new LinkedHashMap<>(idMap));
    }

}
