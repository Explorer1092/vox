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
import com.voxlearning.utopia.service.reward.api.mapper.VersionedRewardIndexList;
import com.voxlearning.utopia.service.reward.entity.RewardIndex;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@ThreadSafe
public class RewardIndexBuffer {

    private final ReentrantReadWriteLocker locker = new ReentrantReadWriteLocker();

    private long version;
    private List<RewardIndex> rewardIndexList;

    public void attach(VersionedRewardIndexList data) {
        Objects.requireNonNull(data);
        locker.withinWriteLockWithoutResult(() -> {
            version = data.getVersion();
            rewardIndexList = data.getRewardIndexList().stream().collect(Collectors.toList());
        });
    }

    public VersionedRewardIndexList dump() {
        return locker.withinReadLock(() -> {
            VersionedRewardIndexList data = new VersionedRewardIndexList();
            data.setVersion(version);
            data.setRewardIndexList(rewardIndexList.stream().collect(Collectors.toList()));
            return data;
        });
    }

    public long getVersion() {
        return locker.withinReadLock(() -> version);
    }

    public List<RewardIndex> loadRewardIndices() {
        return dump().getRewardIndexList();
    }
}
