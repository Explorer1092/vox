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

package com.voxlearning.utopia.service.reward.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ExposeServices;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.core.hql.Criteria;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.remote.core.support.ValueWrapperFuture;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.service.reward.api.CRMRewardService;
import com.voxlearning.utopia.service.reward.entity.*;
import com.voxlearning.utopia.service.reward.impl.dao.RewardCouponDao;
import com.voxlearning.utopia.service.reward.impl.dao.RewardLogisticsPersistence;
import com.voxlearning.utopia.service.reward.impl.internal.*;
import com.voxlearning.utopia.service.reward.impl.persistence.RewardCompleteOrderPersistence;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.LinkedList;
import java.util.List;

@Named
@Service(interfaceClass = CRMRewardService.class)
@ExposeServices({
        @ExposeService(interfaceClass = CRMRewardService.class, version = @ServiceVersion(version = "2018.04.28")),
        @ExposeService(interfaceClass = CRMRewardService.class, version = @ServiceVersion(version = "2018.04.17"))
})
public class CRMRewardServiceImpl extends SpringContainerSupport implements CRMRewardService {

    @Inject private InternalRewardCategoryService internalRewardCategoryService;
    @Inject private InternalRewardImageService internalRewardImageService;
    @Inject private InternalRewardIndexService internalRewardIndexService;
    @Inject private InternalRewardLogisticsService internalRewardLogisticsService;
    @Inject private InternalRewardProductService internalRewardProductService;
    @Inject private InternalRewardSkuService internalRewardSkuService;
    @Inject private InternalRewardTagService internalRewardTagService;
    @Inject private InternalRewardActivityService internalRewardActivityService;

    @Inject private RewardLogisticsPersistence rewardLogisticsPersistence;
    @Inject private RewardCouponDao rewardCouponDao;
    @Inject private RewardCompleteOrderPersistence rewardCompleteOrderPersistence;

    // ========================================================================
    // reward category
    // ========================================================================

    @Override
    public List<RewardCategory> $loadRewardCategories() {
        return internalRewardCategoryService.$loadRewardCategories();
    }

    @Override
    public RewardCategory $upsertRewardCategory(RewardCategory document) {
        return internalRewardCategoryService.$upsertRewardCategory(document);
    }

    @Override
    public boolean $removeRewardCategory(Long id) {
        return internalRewardCategoryService.$removeRewardCategory(id);
    }

    // ========================================================================
    // reward image
    // ========================================================================

    @Override
    public RewardImage $loadRewardImage(Long id) {
        return internalRewardImageService.$loadRewardImage(id);
    }

    @Override
    public List<RewardImage> $loadRewardImages() {
        return internalRewardImageService.$loadRewardImages();
    }

    @Override
    public RewardImage $upsertRewardImage(RewardImage document) {
        return internalRewardImageService.$upsertRewardImage(document);
    }

    @Override
    public boolean $removeRewardImage(Long id) {
        return internalRewardImageService.$removeRewardImage(id);
    }

    @Override
    public RewardActivityImage upsertActivityImage(RewardActivityImage image) {
        return internalRewardActivityService.upsertActivityImage(image);
    }

    @Override
    public RewardActivityImage loadActivityImage(Long id) {
        return internalRewardActivityService.loadActivityImage(id);
    }

    @Override
    public boolean deleteActivityImage(Long id) {
        return internalRewardActivityService.deleteActivityImage(id);
    }

    // ========================================================================
    // reward index
    // ========================================================================

    @Override
    public List<RewardIndex> $loadRewardIndices() {
        return internalRewardIndexService.$loadRewardIndices();
    }

    @Override
    public RewardIndex $upsertRewardIndex(RewardIndex document) {
        return internalRewardIndexService.$upsertRewardIndex(document);
    }

    @Override
    public boolean $removeRewardIndex(Long id) {
        return internalRewardIndexService.$removeRewardIndex(id);
    }

    // ========================================================================
    // reward product
    // ========================================================================

    @Override
    public RewardProduct $loadRewardProduct(Long id) {
        return internalRewardProductService.$loadRewardProduct(id);
    }

    @Override
    public List<RewardProduct> $loadRewardProducts() {
        return internalRewardProductService.$loadRewardProducts();
    }

    @Override
    public List<RewardProductCategoryRef> $findRewardProductCategoryRefsByCategoryId(Long categoryId) {
        return internalRewardProductService.$findRewardProductCategoryRefsByCategoryId(categoryId);
    }

    @Override
    public List<RewardProductCategoryRef> $findRewardProductCategoryRefsByProductId(Long productId) {
        return internalRewardProductService.$findRewardProductCategoryRefsByProductId(productId);
    }

    @Override
    public List<RewardProductTagRef> $findRewardProductTagRefsByTagId(Long tagId) {
        return internalRewardProductService.$findRewardProductTagRefsByTagId(tagId);
    }

    @Override
    public List<RewardProductTagRef> $findRewardProductTagRefsByProductId(Long productId) {
        return internalRewardProductService.$findRewardProductTagRefsByProductId(productId);
    }

    @Override
    public RewardProduct $upsertRewardProduct(RewardProduct document) {
        return internalRewardProductService.$upsertRewardProduct(document);
    }

    // ========================================================================
    // reward sku
    // ========================================================================

    @Override
    public RewardSku $loadRewardSku(@CacheParameter Long id) {
        return internalRewardSkuService.$loadRewardSku(id);
    }

    @Override
    public List<RewardSku> $findRewardSkusByProductId(Long productId) {
        return internalRewardSkuService.$findRewardSkusByProductId(productId);
    }

    // ========================================================================
    // reward tag
    // ========================================================================

    @Override
    public List<RewardTag> $loadRewardTags() {
        return internalRewardTagService.$loadRewardTags();
    }

    @Override
    public RewardTag $upsertRewardTag(RewardTag document) {
        return internalRewardTagService.$upsertRewardTag(document);
    }

    // ========================================================================
    // reward logistics
    // ========================================================================

    @Override
    public RewardLogistics $loadRewardLogistics(Long id) {
        return internalRewardLogisticsService.$loadRewardLogistics(id);
    }

    @Override
    public RewardLogistics $findRewardLogistics(Long schoolId, String month, RewardLogistics.Type type) {
        return internalRewardLogisticsService.$findRewardLogistics(schoolId, month, type);
    }

    @Override
    public RewardLogistics $findRewardLogistics(Long receiverId, RewardLogistics.Type type, String month) {
        return internalRewardLogisticsService.$findRewardLogistics(receiverId, type, month);
    }

    @Override
    public List<RewardLogistics> $findRewardLogisticsList(String currentMonth, RewardLogistics.Type type) {
        return internalRewardLogisticsService.$findRewardLogisticsList(currentMonth, type);
    }

    @Override
    public List<RewardLogistics> $findRewardLogisticsList(Long receiverId, RewardLogistics.Type type) {
        return internalRewardLogisticsService.$findRewardLogisticsList(receiverId, type);
    }

    @Override
    public List<RewardLogistics> $findRewardLogisticsList(String month) {
        return internalRewardLogisticsService.$findRewardLogisticsList(month);
    }

    @Override
    public RewardLogistics $upsertRewardLogistics(RewardLogistics document) {
        return internalRewardLogisticsService.$upsertRewardLogistics(document);
    }

    @Override
    public AlpsFuture<Page<RewardLogistics>> getRewardLogisticPage(Pageable pageable,
                                                                   Long logisticId,
                                                                   String logisticNo,
                                                                   String month,
                                                                   String isBack) {
        List<Criteria> list = new LinkedList<>();
        if (logisticId != 0) {
            list.add(Criteria.where("ID").is(logisticId));
        }
        if (StringUtils.isNotBlank(logisticNo)) {
            list.add(Criteria.where("LOGISTIC_NO").is(logisticNo));
        }
        if (StringUtils.isNotBlank(month)) {
            list.add(Criteria.where("MONTH").is(month));
        }
        if (StringUtils.isNotBlank(isBack)) {
            list.add(Criteria.where("IS_BACK").is(SafeConverter.toBoolean(isBack)));
        }
        list.add(Criteria.where("DISABLED").is(false));
        Criteria criteria = Criteria.and(list);

        Page<RewardLogistics> logisticsPage = rewardLogisticsPersistence.find(pageable, criteria);

        return new ValueWrapperFuture<>(logisticsPage);
    }

    @Override
    public RewardCoupon loadRewardCouponByPID(Long productId) {
        return rewardCouponDao.loadRewardCouponByPID(productId);
    }

    @Override
    public List<RewardCompleteOrder> findCompleteOrderByLogisticsId(Long logisticsId) {
        return rewardCompleteOrderPersistence.findByLogisticsId(logisticsId);
    }

    @Override
    public boolean deleteCompleteOrder(Long id) {
        return rewardCompleteOrderPersistence.deleteCompleteOrder(id);
    }
}
