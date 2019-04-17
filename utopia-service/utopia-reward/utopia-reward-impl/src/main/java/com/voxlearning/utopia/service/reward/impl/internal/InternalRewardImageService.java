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

import com.voxlearning.utopia.service.reward.api.mapper.VersionedRewardImageList;
import com.voxlearning.utopia.service.reward.entity.RewardImage;
import com.voxlearning.utopia.service.reward.impl.dao.RewardImageDao;
import com.voxlearning.utopia.service.reward.impl.version.RewardImageVersion;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Named
public class InternalRewardImageService {

    @Inject private RewardImageDao rewardImageDao;
    @Inject private RewardImageVersion rewardImageVersion;

    public VersionedRewardImageList loadVersionedRewardImageList() {
        return new VersionedRewardImageList(
                rewardImageVersion.currentVersion(),
                $loadRewardImages()
        );
    }

    public RewardImage $loadRewardImage(Long id) {
        return rewardImageDao.load(id);
    }

    public List<RewardImage> $loadRewardImages() {
        return rewardImageDao.query();
    }

    public RewardImage $upsertRewardImage(RewardImage document) {
        if (document == null) {
            return null;
        }
        RewardImage upserted = rewardImageDao.upsert(document);
        if (upserted != null) {
            rewardImageVersion.increase();
        }
        return upserted;
    }

    public boolean $removeRewardImage(Long id) {
        boolean ret = rewardImageDao.remove(id);
        if (ret) {
            rewardImageVersion.increase();
        }
        return ret;
    }

}
