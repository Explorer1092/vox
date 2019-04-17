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
import com.voxlearning.utopia.service.reward.api.mapper.VersionedRewardTagList;
import com.voxlearning.utopia.service.reward.entity.RewardTag;
import com.voxlearning.utopia.service.reward.impl.dao.RewardTagDao;
import com.voxlearning.utopia.service.reward.impl.version.RewardTagVersion;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.stream.Collectors;

@Named
public class InternalRewardTagService {

    @Inject private RewardTagDao rewardTagDao;
    @Inject private RewardTagVersion rewardTagVersion;

    public VersionedRewardTagList loadVersionedRewardTagList() {
        return new VersionedRewardTagList(
                rewardTagVersion.currentVersion(),
                $loadRewardTags()
        );
    }

    public List<RewardTag> $loadRewardTags() {
        return rewardTagDao.query().stream()
                .sorted((o1, o2) -> {
                    int d1 = SafeConverter.toInt(o1.getDisplayOrder());
                    int d2 = SafeConverter.toInt(o2.getDisplayOrder());
                    return Integer.compare(d1, d2);
                })
                .collect(Collectors.toList());
    }

    public RewardTag $upsertRewardTag(RewardTag document) {
        if (document == null) {
            return null;
        }
        RewardTag upserted = rewardTagDao.upsert(document);
        if (upserted != null) {
            rewardTagVersion.increase();
        }
        return upserted;
    }
}
