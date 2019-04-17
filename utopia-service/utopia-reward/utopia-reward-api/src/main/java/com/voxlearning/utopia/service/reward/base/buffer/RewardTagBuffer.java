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
import com.voxlearning.utopia.service.reward.api.filter.RewardTagFilter;
import com.voxlearning.utopia.service.reward.api.mapper.VersionedRewardTagList;
import com.voxlearning.utopia.service.reward.constant.RewardTagLevel;
import com.voxlearning.utopia.service.reward.entity.RewardTag;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@ThreadSafe
public class RewardTagBuffer {

    private final ReentrantReadWriteLocker locker = new ReentrantReadWriteLocker();

    private long version;
    private List<RewardTag> rewardTagList;

    public void attach(VersionedRewardTagList data) {
        Objects.requireNonNull(data);
        locker.withinWriteLockWithoutResult(() -> {
            version = data.getVersion();
            rewardTagList = data.getRewardTagList().stream().collect(Collectors.toList());
        });
    }

    public VersionedRewardTagList dump() {
        return locker.withinReadLock(() -> {
            VersionedRewardTagList data = new VersionedRewardTagList();
            data.setVersion(version);
            data.setRewardTagList(rewardTagList.stream().collect(Collectors.toList()));
            return data;
        });
    }

    public long getVersion() {
        return locker.withinReadLock(() -> version);
    }

    public List<RewardTag> loadRewardTags(final RewardTagLevel tagLevel,
                                          final UserType userType) {
        return RewardTagFilter.filter(dump().getRewardTagList(), tagLevel, userType);
    }

    public List<RewardTag> loadRewardTags(final List<Long> idList, final UserType userType) {
        return RewardTagFilter.filter(dump().getRewardTagList(), idList, userType);
    }
}
