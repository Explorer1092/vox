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
import com.voxlearning.utopia.service.reward.api.mapper.VersionedRewardProductList;
import com.voxlearning.utopia.service.reward.entity.RewardProduct;
import com.voxlearning.utopia.service.reward.entity.RewardProductCategoryRef;
import com.voxlearning.utopia.service.reward.entity.RewardProductTagRef;
import com.voxlearning.utopia.service.reward.impl.dao.RewardProductCategoryRefDao;
import com.voxlearning.utopia.service.reward.impl.dao.RewardProductDao;
import com.voxlearning.utopia.service.reward.impl.dao.RewardProductTagRefDao;
import com.voxlearning.utopia.service.reward.impl.version.RewardProductVersion;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Named
public class InternalRewardProductService {

    @Inject private RewardProductCategoryRefDao rewardProductCategoryRefDao;
    @Inject private RewardProductDao rewardProductDao;
    @Inject private RewardProductTagRefDao rewardProductTagRefDao;
    @Inject private RewardProductVersion rewardProductVersion;

    public VersionedRewardProductList loadVersionedRewardProductList() {
        return new VersionedRewardProductList(
                rewardProductVersion.currentVersion(),
                $loadRewardProducts()
        );
    }

    public RewardProduct $loadRewardProduct(Long id) {
        return rewardProductDao.load(id);
    }

    public List<RewardProduct> $loadRewardProducts() {
        return rewardProductDao.query();
    }

    public List<RewardProductCategoryRef> $findRewardProductCategoryRefsByCategoryId(Long categoryId) {
        if (categoryId == null) {
            return Collections.emptyList();
        }
        return rewardProductCategoryRefDao.findByCategoryId(categoryId);
    }

    public List<RewardProductCategoryRef> $findRewardProductCategoryRefsByProductId(Long productId) {
        if (productId == null) {
            return Collections.emptyList();
        }
        return rewardProductCategoryRefDao.findByProductId(productId);
    }

    public List<RewardProductTagRef> $findRewardProductTagRefsByTagId(Long tagId) {
        if (tagId == null) {
            return Collections.emptyList();
        }
        return rewardProductTagRefDao.findByTagId(tagId);
    }

    public List<RewardProductTagRef> $findRewardProductTagRefsByProductId(Long productId) {
        if (productId == null) {
            return Collections.emptyList();
        }
        return rewardProductTagRefDao.findByProductId(productId);
    }

    public List<RewardProductTagRef> $findRewardProductTagRefsByProductIdList(List<Long> productIdList) {
        if (CollectionUtils.isEmpty(productIdList)) {
            return Collections.emptyList();
        }
        List<RewardProductTagRef> result = new ArrayList<>();
        Map<Long, List<RewardProductTagRef>> map = rewardProductTagRefDao.findByProductIdList(productIdList);
        if (map != null && !map.isEmpty()) {
            for (Map.Entry<Long, List<RewardProductTagRef>> entry : map.entrySet()) {
                result.addAll(entry.getValue());
            }
        }
        return result;
    }

    public RewardProduct $upsertRewardProduct(RewardProduct document) {
        if (document == null) {
            return null;
        }
        RewardProduct upserted = rewardProductDao.upsert(document);
        if (upserted != null) {
            rewardProductVersion.increase();
        }
        return upserted;
    }
}
