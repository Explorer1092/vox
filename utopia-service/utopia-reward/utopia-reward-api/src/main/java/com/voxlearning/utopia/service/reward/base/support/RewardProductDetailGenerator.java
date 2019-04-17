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

package com.voxlearning.utopia.service.reward.base.support;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.utopia.api.constant.AmbassadorLevel;
import com.voxlearning.utopia.service.reward.api.RewardLoader;
import com.voxlearning.utopia.service.reward.constant.RewardConstants;
import com.voxlearning.utopia.service.reward.constant.RewardCouponResource;
import com.voxlearning.utopia.service.reward.constant.RewardProductPriceUnit;
import com.voxlearning.utopia.service.reward.constant.RewardProductType;
import com.voxlearning.utopia.service.reward.entity.RewardCategory;
import com.voxlearning.utopia.service.reward.entity.RewardImage;
import com.voxlearning.utopia.service.reward.entity.RewardProduct;
import com.voxlearning.utopia.service.reward.entity.RewardProductCategoryRef;
import com.voxlearning.utopia.service.reward.entity.newversion.ProductCategory;
import com.voxlearning.utopia.service.reward.mapper.RewardProductDetail;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * For generating reward product detail.
 *
 *
 * @author Xiaopeng Yang
 * @author Xiaohai Zhang
 * @since Dec 3, 2014
 */
public class RewardProductDetailGenerator {
    private static final Logger logger = LoggerFactory.getLogger(RewardProductDetailGenerator.class);
    public static final String COUPON_REDRICT = "/usermobile/giftMall/coupon/redirect.vpage?productId=";

    private final RewardLoader rewardLoader;

    public RewardProductDetailGenerator(RewardLoader rewardLoader) {
        this.rewardLoader = Objects.requireNonNull(rewardLoader);
    }

    public Collection<RewardProductDetail> generateTeacherRewardProductDetails(Collection<RewardProduct> products,
                                                                               TeacherDetail teacher,
                                                                               boolean teacherLevelFlag,
                                                                               boolean ambassadorLevelFlag,
                                                                               Integer ambassadorLevel,
                                                                               boolean nextLevelFlag) {
        if (CollectionUtils.isEmpty(products)) {
            return Collections.emptyList();
        }
        Collection<RewardProductDetail> details = new LinkedList<>();

        List<RewardProduct> candidates = new LinkedList<>();
        Set<Long> productIds = new LinkedHashSet<>();
        for (RewardProduct product : products) {
            if (!Boolean.TRUE.equals(product.getTeacherVisible())) {
                logger.debug("Product {} invisible for teacher", product.getId());
                continue;
            }

            if (teacher.isPrimarySchool() && !product.getPrimarySchoolVisible()) {
                continue;
            }

            if ((teacher.isJuniorTeacher() || teacher.isSeniorTeacher()) && !product.getJuniorSchoolVisible()) {
                continue;
            }

            // FIXME to do later
//            if (Boolean.TRUE.equals(teacherLevelFlag)) {
//                if (teacher.getLevel() < product.getTeacherLevel()) {
//                    continue;
//                }
//            }
            if (Boolean.TRUE.equals(ambassadorLevelFlag) && teacher.isSchoolAmbassador()) {
                if (ambassadorLevel < product.getAmbassadorLevel()) {
                    continue;
                }
            }

            // FIXME todo later
//            if (Boolean.TRUE.equals(nextLevelFlag)) {
//                if (teacher.getLevel() + 1 < product.getTeacherLevel()) {
//                    continue;
//                }
//            }
            candidates.add(product);
            productIds.add(product.getId());
        }
        Map<Long, List<RewardImage>> rewardImages = rewardLoader.loadProductRewardImages(productIds);
        List<RewardCategory> rewardCategoryList = rewardLoader.loadRewardCategories(RewardProductType.JPZX_TIYAN, UserType.STUDENT)
                .stream().filter(e -> "COUPON".equals(e.getCategoryCode()))
                .collect(Collectors.toList());
        List<RewardProductCategoryRef> categoryRefList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(rewardCategoryList)) {
            for(RewardCategory rewardCategory : rewardCategoryList) {
                List<RewardProductCategoryRef> res = rewardLoader.findRewardProductCategoryRefsByCategoryId(rewardCategory.getId());
                if (CollectionUtils.isEmpty(res)) {
                    continue;
                }
                categoryRefList.addAll(res);
            }
        }

        Set<Long> couponProductIds = categoryRefList.stream().map(RewardProductCategoryRef::getProductId).collect(Collectors.toSet());

        for (RewardProduct product : candidates) {
            RewardProductDetail detail = new RewardProductDetail();
            detail.setRepeatExchanged(product.getRepeatExchanged());
            detail.setExtenstionAttributes(new LinkedHashMap<>());
            detail.getExtenstionAttributes().put("product", product);
            detail.setId(product.getId());
            detail.setProductName(product.getProductName());
            detail.setProductType(product.getProductType());
            detail.setDescription(product.getDescription());
            detail.setSoldQuantity(product.getSoldQuantity());
            detail.setWishQuantity(product.getWishQuantity());
            detail.setSaleGroup(product.getSaleGroup());
            detail.setTags(product.getTags());
            detail.setCreateDatetime(product.getCreateDatetime());
            detail.setUsedUrl(couponProductIds.contains(detail.getId()) && product.getCouponResource() == RewardCouponResource.DUIBA && StringUtils.isNotBlank(product.getUsedUrl()) ? (COUPON_REDRICT +  detail.getId()): product.getUsedUrl());
            detail.setRebated(product.getRebated());
            detail.setTeacherLevel(product.getTeacherLevel());
            detail.setAmbassadorLevel(product.getAmbassadorLevel());
            detail.setStudentOrderValue(product.getStudentOrderValue());
            detail.setTeacherOrderValue(product.getTeacherOrderValue());
            detail.setDisplayTerminal(product.getDisplayTerminal());
            detail.setRelateVirtualItemId(product.getRelateVirtualItemId());
            detail.setRelateVirtualItemContent(product.getRelateVirtualItemContent());
            detail.setMinBuyNums(product.getMinBuyNums());
            detail.setCouponResource(couponProductIds.contains(product.getId()) ? product.getCouponResource() : null);
            detail.setSpendType(product.getSpendType());
            detail.setOneLevelCategoryId(product.getOneLevelCategoryId());
            detail.setTwoLevelCategoryId(product.getTwoLevelCategoryId());
            detail.setIsNewProduct(product.getIsNewProduct());
            if (product.getAmbassadorLevel() > 0) {
                detail.setAmbassadorLevelName(AmbassadorLevel.of(product.getAmbassadorLevel()).getDescription());
            }
//            if (oldSchool) {
            // 小学老师需要区分， 现在对于奖品来说，后台配置的价格全部按照积分配置的。 小学老师要除以10
            double price = product.getPriceOldS();
            double originPrice = product.getPriceS();
            RewardProductPriceUnit unit = RewardProductPriceUnit.中学积分;
            if (teacher.isPrimarySchool()) {
                // 小学老师
                price = new BigDecimal(price).divide(new BigDecimal(10), 0, BigDecimal.ROUND_HALF_UP).doubleValue();
                // 原始积分也要换算单位
                originPrice = new BigDecimal(originPrice).divide(new BigDecimal(10),0,BigDecimal.ROUND_HALF_UP).doubleValue();
                unit = RewardProductPriceUnit.园丁豆;
            }

            detail.setPrice(price);
            detail.setVipPrice(price);
            detail.setDiscount(1.0);
            detail.setDiscountPrice(price);
            detail.setOriginPrice(originPrice);
//            } else {
//            detail.setPrice(product.getPriceT());
//            //新奖品中心改版 去掉了折扣的概念 对于老师来说 没有折扣。 2015-07-23
//            detail.setVipPrice(product.getPriceT());
//            detail.setDiscount(1.0);
//            detail.setDiscountPrice(product.getPriceT());
//            }
            detail.setUnit(unit.name());
            List<RewardImage> images = rewardImages.get(product.getId());
            if (CollectionUtils.isEmpty(images)) {
                RewardImage image = new RewardImage();
                image.setLocation(RewardConstants.DEFAULT_PRODUCT_IMAGE_URL);
                images = Collections.singletonList(image);
            }
            detail.setImage(images.iterator().next().getLocation());
            detail.setImages(images);

            /*List<RewardSku> skus = rewardSkus.get(product.getId());
            if (skus == null) {
                skus = Collections.emptyList();
            }*/
            detail.setSkus(new ArrayList<>());
            detail.setOnline(product.getOnlined());

            details.add(detail);
        }
        return details;
    }

    public RewardProductDetail generateTeacherRewardProductDetail(RewardProduct product, TeacherDetail teacher) {
        if (product == null) {
            return null;
        }
        Collection<RewardProductDetail> details = generateTeacherRewardProductDetails(Collections.singletonList(product),
                teacher,
                false,
                false,
                0,
                false);
        return details.stream().findFirst().orElse(null);
    }

    public Collection<RewardProductDetail> generateStudentRewardProductDetails(Collection<RewardProduct> products, StudentDetail student) {
        if (CollectionUtils.isEmpty(products)) {
            return Collections.emptyList();
        }

        Collection<RewardProductDetail> details = new LinkedList<>();
        Integer grade = student.getClazzLevelAsInteger();

        List<RewardProduct> candidates = new LinkedList<>();
        Set<Long> productIds = new LinkedHashSet<>();
        for (RewardProduct product : products) {
            if (!Boolean.TRUE.equals(product.getStudentVisible())) {
                logger.debug("Product {} invisible for student, ignore", product.getId());
                continue;
            }

            if (student.isPrimaryStudent() && (product.getPrimarySchoolVisible() == null || !product.getPrimarySchoolVisible())) {
                continue;
            }

            if (student.isJuniorStudent() && (product.getJuniorSchoolVisible() == null || !product.getJuniorSchoolVisible())) {
                continue;
            }

            // 过滤年级选项
            if(!product.isTiyan() && StringUtils.isNotEmpty(product.getGradeVisible())){
                boolean match = Arrays.stream(product.getGradeVisible().split(","))
                        .filter(StringUtils::isNumeric)
                        .map(Integer::parseInt)
                        .anyMatch(g -> Objects.equals(grade,g));

                if(!match)
                    continue;
            }

            candidates.add(product);
            productIds.add(product.getId());
        }

        Map<Long, List<RewardImage>> rewardImages = rewardLoader.loadProductRewardImages(productIds);
        List<RewardCategory> rewardCategoryList = rewardLoader.loadRewardCategories(RewardProductType.JPZX_TIYAN, UserType.STUDENT)
                .stream().filter(e -> (student.isJuniorStudent() && Boolean.TRUE.equals(e.getJuniorVisible())) || (student.isPrimaryStudent() && Boolean.TRUE.equals(e.getPrimaryVisible()))
                                   ).filter(e -> "COUPON".equals(e.getCategoryCode()))
                .collect(Collectors.toList());
        List<RewardProductCategoryRef> categoryRefList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(rewardCategoryList)) {
            for(RewardCategory rewardCategory : rewardCategoryList) {
                List<RewardProductCategoryRef> res = rewardLoader.findRewardProductCategoryRefsByCategoryId(rewardCategory.getId());
                if (CollectionUtils.isEmpty(res)) {
                    continue;
                }
                categoryRefList.addAll(res);
            }
        }

        Set<Long> couponProductIds = categoryRefList.stream().map(RewardProductCategoryRef::getProductId).collect(Collectors.toSet());
        for (RewardProduct product : candidates) {
            RewardProductDetail detail = new RewardProductDetail();
            detail.setExtenstionAttributes(new LinkedHashMap<>());
            detail.getExtenstionAttributes().put("product", product);

            detail.setRepeatExchanged(product.getRepeatExchanged());
            detail.setId(product.getId());
            detail.setProductName(product.getProductName());
            detail.setProductType(product.getProductType());
            detail.setDescription(product.getDescription());
            detail.setSoldQuantity(product.getSoldQuantity() == null ? 0 : product.getSoldQuantity());
            detail.setSaleGroup(product.getSaleGroup());
            detail.setWishQuantity(product.getWishQuantity() == null ? 0 : product.getWishQuantity());
            detail.setTags(product.getTags());
            detail.setCreateDatetime(product.getCreateDatetime());
            detail.setUsedUrl(couponProductIds.contains(detail.getId()) && product.getCouponResource() == RewardCouponResource.DUIBA && StringUtils.isNotBlank(product.getUsedUrl()) ? (COUPON_REDRICT +  detail.getId()): product.getUsedUrl());
            detail.setRebated(product.getRebated());
            detail.setTeacherLevel(product.getTeacherLevel());
            detail.setAmbassadorLevel(product.getAmbassadorLevel());
            detail.setStudentOrderValue(product.getStudentOrderValue());
            detail.setTeacherOrderValue(product.getTeacherOrderValue());
            detail.setExpiryDate(product.getExpiryDate());
            detail.setDisplayTerminal(product.getDisplayTerminal());
            detail.setRelateVirtualItemId(product.getRelateVirtualItemId());
            detail.setRelateVirtualItemContent(product.getRelateVirtualItemContent());
            detail.setOriginPrice(product.getPriceS());
            detail.setCouponResource(couponProductIds.contains(detail.getId()) ? product.getCouponResource() : null);
            detail.setSpendType(product.getSpendType());
            detail.setOneLevelCategoryId(product.getOneLevelCategoryId());
            detail.setTwoLevelCategoryId(product.getTwoLevelCategoryId());
            detail.setIsNewProduct(product.getIsNewProduct());

            detail.setPrice(product.getPriceOldS());
            Double vipPrice = new BigDecimal(product.getPriceOldS() * RewardConstants.DISCOUNT_VIP).setScale(0, BigDecimal.ROUND_HALF_UP).doubleValue();
            detail.setVipPrice(vipPrice);

            // 取消vip的九折
            /*if (vipUser) {
                detail.setDiscountPrice(vipPrice);
                detail.setDiscount(RewardConstants.DISCOUNT_VIP);
            } else {*/
            detail.setDiscountPrice(product.getPriceOldS());
            //}

            detail.setUnit(RewardProductPriceUnit.学豆.name());

            List<RewardImage> images = rewardImages.get(product.getId());
            if (CollectionUtils.isEmpty(images)) {
                RewardImage image = new RewardImage();
                image.setLocation(RewardConstants.DEFAULT_PRODUCT_IMAGE_URL);
                images = Collections.singletonList(image);
            }

            // 这里要根据图片的属性进行选择
            RewardImage selectedImage = rewardLoader.pickDisplayImage(student, images);
            if (selectedImage == null) {
                selectedImage = images.stream().findFirst().orElse(null);
            }

            detail.setImage(selectedImage.getLocation());
            detail.setImages(images);

            detail.setSkus(new ArrayList<>());
            // 记录上线状态
            detail.setOnline(product.getOnlined());
            detail.setMinBuyNums(product.getMinBuyNums());

            details.add(detail);
        }
        return details;
    }

    public RewardProductDetail generateStudentRewardProductDetail(RewardProduct product, StudentDetail student) {
        if (product == null) {
            return null;
        }
        Collection<RewardProductDetail> details = generateStudentRewardProductDetails(Collections.singletonList(product), student);
        return details.stream().findFirst().orElse(null);
    }

    public Collection<RewardProductDetail> generateResearchStaffRewardProductDetails(Collection<RewardProduct> products) {
        if (CollectionUtils.isEmpty(products)) {
            return Collections.emptyList();
        }
        Collection<RewardProduct> candidates = new LinkedList<>();
        Collection<Long> productIds = new LinkedHashSet<>();
        for (RewardProduct product : products) {
            if (!Boolean.TRUE.equals(product.getTeacherVisible())) {
                continue;
            }
            if (!Boolean.TRUE.equals(product.getOsVisible())) {
                continue;
            }
            candidates.add(product);
            productIds.add(product.getId());
        }

        Map<Long, List<RewardImage>> rewardImages = rewardLoader.loadProductRewardImages(productIds);

        Collection<RewardProductDetail> details = new LinkedList<>();
        for (RewardProduct product : candidates) {
            RewardProductDetail detail = new RewardProductDetail();
            detail.setExtenstionAttributes(new LinkedHashMap<String, Object>());
            detail.getExtenstionAttributes().put("product", product);

            detail.setRepeatExchanged(product.getRepeatExchanged());
            detail.setId(product.getId());
            detail.setProductName(product.getProductName());
            detail.setProductType(product.getProductType());
            detail.setDescription(product.getDescription());
            detail.setSoldQuantity(product.getSoldQuantity());
            detail.setWishQuantity(product.getWishQuantity());
            detail.setSaleGroup(product.getSaleGroup());
            detail.setTags(product.getTags());
            detail.setCreateDatetime(product.getCreateDatetime());
            detail.setUsedUrl(product.getUsedUrl());
            detail.setRebated(product.getRebated());
            double price = new BigDecimal(product.getPriceOldT()).divide(new BigDecimal(10), 0, BigDecimal.ROUND_HALF_UP).doubleValue();
            detail.setPrice(price);
            detail.setVipPrice(price);
            detail.setDiscountPrice(price);
            detail.setDiscount(1.0);
            detail.setUnit(RewardProductPriceUnit.园丁豆.name());
            detail.setTeacherLevel(product.getTeacherLevel());
            detail.setAmbassadorLevel(product.getAmbassadorLevel());
            detail.setStudentOrderValue(product.getStudentOrderValue());
            detail.setTeacherOrderValue(product.getTeacherOrderValue());
            detail.setCouponResource(detail.getCouponResource());
            List<RewardImage> images = rewardImages.get(product.getId());
            if (CollectionUtils.isEmpty(images)) {
                RewardImage image = new RewardImage();
                image.setLocation(RewardConstants.DEFAULT_PRODUCT_IMAGE_URL);
                images = Collections.singletonList(image);
            }
            detail.setImage(images.iterator().next().getLocation());
            detail.setImages(images);
            detail.setRelateVirtualItemId(product.getRelateVirtualItemId());
            detail.setSpendType(product.getSpendType());
            detail.setOneLevelCategoryId(product.getOneLevelCategoryId());
            detail.setTwoLevelCategoryId(product.getTwoLevelCategoryId());
            detail.setIsNewProduct(product.getIsNewProduct());

            /*List<RewardSku> skus = rewardSkus.get(product.getId());
            if (skus == null) {
                skus = Collections.emptyList();
            }*/
            detail.setSkus(new ArrayList<>());
            details.add(detail);
        }
        return details;
    }

    public RewardProductDetail generateResearchStaffRewardProductDetail(RewardProduct product) {
        if (product == null) {
            return null;
        }
        Collection<RewardProductDetail> details = generateResearchStaffRewardProductDetails(Collections.singletonList(product));
        return details.stream().findFirst().orElse(null);
    }
}
