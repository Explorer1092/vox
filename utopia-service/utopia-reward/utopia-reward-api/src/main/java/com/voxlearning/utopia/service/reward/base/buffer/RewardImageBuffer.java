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
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.service.reward.api.mapper.VersionedRewardImageList;
import com.voxlearning.utopia.service.reward.entity.RewardImage;

import java.util.*;
import java.util.stream.Collectors;

@ThreadSafe
public class RewardImageBuffer {

    private final ReentrantReadWriteLocker locker = new ReentrantReadWriteLocker();

    private long version;
    private List<RewardImage> rewardImageList;

    public void attach(VersionedRewardImageList data) {
        Objects.requireNonNull(data);
        locker.withinWriteLockWithoutResult(() -> {
            version = data.getVersion();
            rewardImageList = data.getRewardImageList().stream().collect(Collectors.toList());
        });
    }

    public VersionedRewardImageList dump() {
        return locker.withinReadLock(() -> {
            VersionedRewardImageList data = new VersionedRewardImageList();
            data.setVersion(version);
            data.setRewardImageList(rewardImageList.stream().collect(Collectors.toList()));
            return data;
        });
    }

    public long getVersion() {
        return locker.withinReadLock(() -> version);
    }

    public Map<Long, List<RewardImage>> loadProductRewardImages(Collection<Long> productIds) {
        Set<Long> set = CollectionUtils.toLinkedHashSet(productIds);
        if (set.isEmpty()) {
            return Collections.emptyMap();
        }
        return dump().getRewardImageList().stream()
                .map(e -> {
                    // return a new copy instead of original instance
                    RewardImage n = new RewardImage();
                    n.setId(e.getId());
                    n.setCreateDatetime(e.getCreateDatetime());
                    n.setDisplayOrder(e.getDisplayOrder());
                    n.setLocation(e.getLocation());
                    n.setProductId(e.getProductId());
                    n.setRelateAttr(e.getRelateAttr());
                    n.setRelateValue(e.getRelateValue());
                    return n;
                })
                .filter(e -> e.getProductId() != null)
                .filter(e -> set.contains(e.getProductId()))
                .collect(Collectors.groupingBy(RewardImage::getProductId));
    }
}
