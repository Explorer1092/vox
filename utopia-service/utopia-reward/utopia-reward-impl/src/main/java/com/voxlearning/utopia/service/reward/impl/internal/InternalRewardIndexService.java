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

package com.voxlearning.utopia.service.reward.impl.internal;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.reward.api.mapper.VersionedRewardIndexList;
import com.voxlearning.utopia.service.reward.entity.RewardIndex;
import com.voxlearning.utopia.service.reward.impl.dao.RewardIndexDao;
import com.voxlearning.utopia.service.reward.impl.version.RewardIndexVersion;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.stream.Collectors;

@Named
public class InternalRewardIndexService {

    @Inject private RewardIndexDao rewardIndexDao;
    @Inject private RewardIndexVersion rewardIndexVersion;

    public VersionedRewardIndexList loadVersionedRewardIndexList() {
        return new VersionedRewardIndexList(
                rewardIndexVersion.currentVersion(),
                $loadRewardIndices()
        );
    }

    public List<RewardIndex> $loadRewardIndices() {
        return rewardIndexDao.query().stream()
                .sorted((o1, o2) -> {
                    int d1 = SafeConverter.toInt(o1.getDisplayOrder());
                    int d2 = SafeConverter.toInt(o2.getDisplayOrder());
                    return Integer.compare(d1, d2);
                })
                .collect(Collectors.toList());
    }

    public RewardIndex $upsertRewardIndex(RewardIndex document) {
        if (document == null) {
            return null;
        }
        RewardIndex upserted = rewardIndexDao.upsert(document);
        if (upserted != null) {
            rewardIndexVersion.increase();
        }
        return upserted;
    }

    public boolean $removeRewardIndex(Long id) {
        boolean ret = rewardIndexDao.remove(id);
        if (ret) {
            rewardIndexVersion.increase();
        }
        return ret;
    }
}
