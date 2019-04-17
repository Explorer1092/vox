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
import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.meta.Gender;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.*;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.api.constant.CouponProductionName;
import com.voxlearning.utopia.service.reward.api.mapper.*;
import com.voxlearning.utopia.service.reward.constant.RewardProductType;
import com.voxlearning.utopia.service.reward.constant.RewardTagLevel;
import com.voxlearning.utopia.service.reward.entity.*;
import com.voxlearning.utopia.service.reward.mapper.RewardProductDetail;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserProfile;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.reward.entity.RewardImage.RelateAttrs.Sex;

/**
 * Reward loader interface definition.
 *
 * @author Xiaopeng Yang
 * @author Xiaohai Zhang
 * @since Dec 1, 2014
 */
@ServiceVersion(version = "20181106")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface RewardLoader extends IPingable {

    // ========================================================================
    // reward category
    // ========================================================================

    List<RewardCategory> loadRewardCategories(RewardProductType productType,
                                              UserType userType);

    List<RewardCategory> loadRewardCategories(UserType userType);

    // ========================================================================
    // reward images
    // ========================================================================

    Map<Long, List<RewardImage>> loadProductRewardImages(Collection<Long> productIds);

    // ========================================================================
    // reward index
    // ========================================================================

    List<RewardIndex> loadRewardIndices();

    // ========================================================================
    // reward product
    // ========================================================================

    Map<Long, RewardProduct> loadRewardProductMap();

    Set<Long> loadProductIdByOneLevelCategoryId(Long oneLevelCategoryId);

    RewardProductDetail loadRewardProductDetail(StudentDetail student, long id);

    default RewardProduct loadRewardProduct(Long id) {
        if (id == null) {
            return null;
        }
        RewardProduct product = loadRewardProductMap().get(id);
        if (product != null && !Boolean.TRUE.equals(product.getOnlined())) {
            product = null;
        }
        return product;
    }

    default List<RewardProduct> loadAllStudentProducts() {
        if (RuntimeMode.isStaging()) {//staging环境全都加载出来方便配置和测试
            return loadRewardProductMap().values().stream()
                    .filter(source -> Boolean.TRUE.equals(source.getStudentVisible()))
                    .collect(Collectors.toList());
        } else {
            return loadRewardProductMap().values().stream()
                    .filter(source -> Boolean.TRUE.equals(source.getOnlined()) && Boolean.TRUE.equals(source.getStudentVisible()))
                    .collect(Collectors.toList());
        }
    }

    default List<RewardProduct> loadAllTeacherProducts() {
        return loadRewardProductMap().values().stream()
                .filter(source -> Boolean.TRUE.equals(source.getOnlined()) && Boolean.TRUE.equals(source.getTeacherVisible()))
                .collect(Collectors.toList());
    }

    default RewardProduct loadProductByProductName(CouponProductionName productionName) {
        if (productionName == null) {
            return null;
        }
        for (RewardProduct product : loadRewardProductMap().values()) {
            // 加入别名判断
            if (Objects.equals(product.getProductName(), productionName.name())
                    || Objects.equals(product.getProductName(),productionName.getAlias())) {
                return product;
            }
        }
        return null;
    }

    default RewardImage pickDisplayImage(User user, List<RewardImage> images) {
        if (CollectionUtils.isEmpty(images))
            return null;

        // 这里要根据图片的属性进行选择
        return images.stream()
                .filter(image -> {
                    if (!StringUtils.isEmpty(image.getRelateAttr())) {
                        // 根据性别挑选图片
                        if (Objects.equals(image.getRelateAttr(), Sex.name())) {
                            UserProfile profile = user.getProfile();
                            if (profile != null) {
                                if (!Objects.equals(profile.getGender(), Gender.NOT_SURE.getCode())) {
                                    return Objects.equals(profile.getGender(), image.getRelateValue());
                                } else {
                                    // 如果性别未知的话，选择男孩属性的图片
                                    return Objects.equals(image.getRelateValue(), Gender.MALE.getCode());
                                }
                            }
                        }
                    }
                    return false;
                })
                .findAny()
                .orElse(images.get(0));
    }

    @CacheMethod(type = RewardProductCategoryRef.class, writeCache = false)
    List<RewardProductCategoryRef> findRewardProductCategoryRefsByCategoryId(@CacheParameter("categoryId") Long categoryId);

    @CacheMethod(type = RewardCategory.class, writeCache = false)
    List<RewardCategory> findRewardProductCategoriesByProductId(@CacheParameter("productId") Long productId);

    @CacheMethod(type = RewardProductTagRef.class, writeCache = false)
    List<RewardProductTagRef> findRewardProductTagRefsByTagId(@CacheParameter(value = "tagId") Long tagId);

    // ========================================================================
    // reward sku
    // ========================================================================

    @CacheMethod(type = RewardSku.class, writeCache = false)
    Map<Long, List<RewardSku>> loadProductRewardSkus(@CacheParameter(value = "productId", multiple = true) Collection<Long> productIds);

    /*@CacheMethod(key = "ALL",type = RewardSku.class)
    Map<Long,List<RewardSku>> loadAllProductSkus();*/

    @CacheMethod(type = RewardSku.class)
    List<RewardSku> loadProductSku(@CacheParameter("productId") Long productId);

    @CacheMethod(type = RewardSku.class, writeCache = false, key = "has_inventory_products")
    List<Long> getHasInventoryProducts();

    // ========================================================================
    // reward tag
    // ========================================================================

    List<RewardTag> loadRewardTags(RewardTagLevel tagLevel, UserType userType);

    List<RewardTag> loadRewardTags(List<Long> idList, UserType userType);

    Map<Long, RewardOrder> loadRewardOrders(Collection<Long> orderIds);

    Map<Long, List<RewardOrder>> loadUserRewardOrders(Collection<Long> userIds);

    Map<Long, RewardWishOrder> loadRewardWishOrders(Collection<Long> orderIds);

    Map<Long, List<RewardWishOrder>> loadUserRewardWishOrders(Collection<Long> userIds);

    Map<Long, List<RewardCouponDetail>> loadProductRewardCouponDetails(Collection<Long> productIds);

    Map<Long, List<RewardCouponDetail>> loadUserRewardCouponDetails(Collection<Long> userIds);

    Map<Long, List<RewardMoonLightBoxHistory>> loadMoonLightBoxHistoryByUserIds(Collection<Long> userIds);

    @CacheMethod(type = RewardLogistics.class, writeCache = false)
    RewardLogistics loadRewardLogistics(@CacheParameter Long id);

    List<RewardLogistics> loadSchoolRewardLogistics(Long schoolId,RewardLogistics.Type type);

    // ========================================================================
    // buffer supported methods
    // ========================================================================

    VersionedRewardCategoryList loadVersionedRewardCategoryList(long version);

    VersionedRewardImageList loadVersionedRewardImageList(long version);

    VersionedRewardIndexList loadVersionedRewardIndexList(long version);

    VersionedRewardProductList loadVersionedRewardProductList(long version);

    VersionedRewardTagList loadVersionedRewardTagList(long version);

    RewardActivityList loadRewardActivitiesList(long version);

    List<RewardActivity> loadRewardActivities();

    List<RewardActivity> loadRewardActivitiesNoBuffer();

    RewardActivity loadRewardActivity(Long activityId);

    RewardActivity loadRewardActivityNoBuffer(Long activityId);

    /*@CacheMethod(type = RewardActivityRecord.class)
    List<RewardActivityRecord> loadRewardActivityRecords(@CacheParameter("ACID") Long activityId, @CacheParameter("UID") Long userId);*/

    /*@CacheMethod(type = RewardActivityRecord.class)
    List<RewardActivityRecord> loadRecentActivityRecords(@CacheParameter("ACID") Long activityId, int limit);*/

    @CacheMethod(type = RewardActivityImage.class)
    List<RewardActivityImage> loadActivityImages(@CacheParameter("ACID") Long activityId);

    /*@CacheMethod(type = RewardActivityRecord.class)
    List<RewardActivityRecord> loadUserRecordsInDay(@CacheParameter("UID") Long userId, Date date);*/

    @CacheMethod(type = RewardActivityRecord.class, expiration = @UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today))
    List<RewardActivityRecord> loadActivityUserRecords(@CacheParameter("USER_ID") Long userId);

    /*@CacheMethod(type = RewardActivityRecord.class)
    List<RewardActivityRecord> loadUserCollectRecords(@CacheParameter("COLLECT_UID") Long userId);*/

    @CacheMethod(type = RewardOrder.class, expiration = @UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today))
    Map<Long,Integer> loadUserCollectOrdersInClazz(@CacheParameter("clazzId") Long clazzId, @CacheParameter("categoryCode") String categoryCode, Date startDate);

    @CacheMethod(type = RewardProductTarget.class)
    Map<Integer,List<RewardProductTarget>> loadRewardTargetGroupByType(@CacheParameter("TARGET_PID") Long productId);

    //@CacheMethod(type = RewardProductTarget.class,key = "ALL_GROUPBY_PID")
    Map<Long,List<RewardProductTarget>> loadAllProductTargets();

    /**
     * 获得奖品中心所有特权类的商品
     * @return
     */
    List<RewardProduct> loadRewardPrivilegeProduct();

    @NoResponseWait(dispatchAll = true, ignoreNoProvider = true)
    void reloadRewardActivityBuffer();

    List<RewardProductTagRef> findRewardProductTagRefsByProductId(Long productId);

    List<RewardProductTagRef> findRewardProductTagRefsByProductIdList(List<Long> productIdList);

    Boolean isGraduate(StudentDetail studentDetail);
}
