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

package com.voxlearning.utopia.service.reward.api;

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.remote.*;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.alps.spi.core.Encoder;
import com.voxlearning.utopia.service.reward.entity.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "2018.04.28")
@ServiceTimeout(timeout = 1, unit = TimeUnit.MINUTES)
@ServiceRetries
public interface CRMRewardService extends IPingable {

    // ========================================================================
    // reward category
    // ========================================================================

    List<RewardCategory> $loadRewardCategories();

    RewardCategory $upsertRewardCategory(RewardCategory document);

    boolean $removeRewardCategory(Long id);

    // ========================================================================
    // reward image
    // ========================================================================

    RewardImage $loadRewardImage(Long id);

    List<RewardImage> $loadRewardImages();

    RewardImage $upsertRewardImage(RewardImage document);

    boolean $removeRewardImage(Long id);

    RewardActivityImage upsertActivityImage(RewardActivityImage image);

    RewardActivityImage loadActivityImage(Long id);

    boolean deleteActivityImage(Long id);

    // ========================================================================
    // reward index
    // ========================================================================

    List<RewardIndex> $loadRewardIndices();

    RewardIndex $upsertRewardIndex(RewardIndex document);

    boolean $removeRewardIndex(Long id);

    // ========================================================================
    // reward product
    // ========================================================================

    RewardProduct $loadRewardProduct(Long id);

    List<RewardProduct> $loadRewardProducts();

    @CacheMethod(type = RewardProductCategoryRef.class, writeCache = false)
    List<RewardProductCategoryRef> $findRewardProductCategoryRefsByCategoryId(@CacheParameter("categoryId") Long categoryId);

    List<RewardProductCategoryRef> $findRewardProductCategoryRefsByProductId(Long productId);

    @CacheMethod(type = RewardProductTagRef.class, writeCache = false)
    List<RewardProductTagRef> $findRewardProductTagRefsByTagId(@CacheParameter(value = "tagId") Long tagId);

    List<RewardProductTagRef> $findRewardProductTagRefsByProductId(Long productId);

    RewardProduct $upsertRewardProduct(RewardProduct document);

    // ========================================================================
    // reward sku
    // ========================================================================

    @CacheMethod(type = RewardSku.class, writeCache = false)
    RewardSku $loadRewardSku(@CacheParameter Long id);

    @CacheMethod(type = RewardSku.class, writeCache = false)
    List<RewardSku> $findRewardSkusByProductId(@CacheParameter("productId") Long productId);

    // ========================================================================
    // reward tag
    // ========================================================================

    List<RewardTag> $loadRewardTags();

    RewardTag $upsertRewardTag(RewardTag document);

    // ========================================================================
    // reward logistics
    // ========================================================================

    @CacheMethod(type = RewardLogistics.class, writeCache = false)
    RewardLogistics $loadRewardLogistics(Long id);

    RewardLogistics $findRewardLogistics(Long schoolId, String month, RewardLogistics.Type type);

    RewardLogistics $findRewardLogistics(Long receiverId, RewardLogistics.Type type, String month);

    List<RewardLogistics> $findRewardLogisticsList(String currentMonth, RewardLogistics.Type type);

    @CacheMethod(type = RewardLogistics.class, writeCache = false)
    List<RewardLogistics> $findRewardLogisticsList(@CacheParameter(value = "UID")Long receiverId, @CacheParameter(value = "TY")RewardLogistics.Type type);

    List<RewardLogistics> $findRewardLogisticsList(String month);

    RewardLogistics $upsertRewardLogistics(RewardLogistics document);

    @Async
    @Codec(Encoder.fst)
    AlpsFuture<Page<RewardLogistics>> getRewardLogisticPage(Pageable pageable,
                                                            Long logisticId,
                                                            String logisticNo,
                                                            String month,
                                                            String isBack);

    RewardCoupon loadRewardCouponByPID(Long productId);

    List<RewardCompleteOrder> findCompleteOrderByLogisticsId(Long logisticsId);

    boolean deleteCompleteOrder(Long id);

}
