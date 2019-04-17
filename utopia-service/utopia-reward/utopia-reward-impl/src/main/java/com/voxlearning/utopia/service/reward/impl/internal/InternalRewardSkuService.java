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

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.service.reward.entity.RewardSku;
import com.voxlearning.utopia.service.reward.impl.dao.RewardSkuDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

@Named
public class InternalRewardSkuService {

    @Inject private RewardSkuDao rewardSkuDao;

    public RewardSku $loadRewardSku(Long id) {
        return rewardSkuDao.load(id);
    }

    public List<RewardSku> $findRewardSkusByProductId(Long productId) {
        if (productId == null) return Collections.emptyList();
        return rewardSkuDao.findByProductId(productId);
    }

    public Map<Long, List<RewardSku>> $findRewardSkusByProductIds(Collection<Long> productIds) {
        Set<Long> set = CollectionUtils.toLinkedHashSet(productIds);
        if (set.isEmpty()) return Collections.emptyMap();
        return rewardSkuDao.findByProductIds(set);
    }

    public boolean $removeRewardSku(Long id) {
        return rewardSkuDao.remove(id);
    }

    public List<Long> getHasInventoryProducts() {
        return rewardSkuDao.findHasInventoryProducts();
    }

}
