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

package com.voxlearning.utopia.service.reward.base;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.LazyInitializationSupplier;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.DateRange;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.api.constant.EduSystemType;
import com.voxlearning.utopia.service.reward.api.RewardLoader;
import com.voxlearning.utopia.service.reward.base.buffer.*;
import com.voxlearning.utopia.service.reward.base.support.RewardProductDetailGenerator;
import com.voxlearning.utopia.service.reward.constant.RewardProductType;
import com.voxlearning.utopia.service.reward.constant.RewardTagLevel;
import com.voxlearning.utopia.service.reward.entity.*;
import com.voxlearning.utopia.service.reward.mapper.RewardProductDetail;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import org.slf4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

abstract public class AbstractRewardLoader implements RewardLoader {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final LazyInitializationSupplier<RewardCategoryBuffer> rewardCategoryBufferSupplier
            = new LazyInitializationSupplier<>(true, this::createRewardCategoryBuffer);

    private final LazyInitializationSupplier<RewardImageBuffer> rewardImageBufferSupplier
            = new LazyInitializationSupplier<>(true, this::createRewardImageBuffer);

    private final LazyInitializationSupplier<RewardIndexBuffer> rewardIndexBufferSupplier
            = new LazyInitializationSupplier<>(true, this::createRewardIndexBuffer);

    private final LazyInitializationSupplier<RewardProductBuffer> rewardProductBufferSupplier
            = new LazyInitializationSupplier<>(true, this::createRewardProductBuffer);

    private final LazyInitializationSupplier<RewardTagBuffer> rewardTagBufferSupplier
            = new LazyInitializationSupplier<>(true, this::createRewardTagBuffer);

    public RewardImageBuffer getRewardImageBuffer() {
        return rewardImageBufferSupplier.initializeIfNecessary();
    }

    public RewardCategoryBuffer getRewardCategoryBuffer() {
        return rewardCategoryBufferSupplier.initializeIfNecessary();
    }

    public RewardIndexBuffer getRewardIndexBuffer() {
        return rewardIndexBufferSupplier.initializeIfNecessary();
    }

    public RewardProductBuffer getRewardProductBuffer() {
        return rewardProductBufferSupplier.initializeIfNecessary();
    }

    public RewardTagBuffer getRewardTagBuffer() {
        return rewardTagBufferSupplier.initializeIfNecessary();
    }

    abstract protected RewardCategoryBuffer createRewardCategoryBuffer();

    abstract protected RewardImageBuffer createRewardImageBuffer();

    abstract protected RewardIndexBuffer createRewardIndexBuffer();

    abstract protected RewardProductBuffer createRewardProductBuffer();

    abstract protected RewardTagBuffer createRewardTagBuffer();

    public void resetBuffer() {
        rewardCategoryBufferSupplier.reset();
        rewardImageBufferSupplier.reset();
        rewardIndexBufferSupplier.reset();
        rewardProductBufferSupplier.reset();
        rewardTagBufferSupplier.reset();
    }

    @Override
    public List<RewardCategory> loadRewardCategories(RewardProductType productType, UserType userType) {
        return getRewardCategoryBuffer().loadRewardCategories(productType, userType);
    }

    @Override
    public List<RewardCategory> loadRewardCategories(UserType userType) {
        return getRewardCategoryBuffer().loadRewardCategories(userType);
    }

    @Override
    public Map<Long, List<RewardImage>> loadProductRewardImages(Collection<Long> productIds) {
        return getRewardImageBuffer().loadProductRewardImages(productIds);
    }

    @Override
    public List<RewardIndex> loadRewardIndices() {
        return getRewardIndexBuffer().loadRewardIndices();
    }

    @Override
    public Map<Long, RewardProduct> loadRewardProductMap() {
        return getRewardProductBuffer().loadRewardProductMap();
    }

    @Override
    public Set<Long> loadProductIdByOneLevelCategoryId(Long oneLevelCategoryId) {
        return getRewardProductBuffer().loadRewardProductMap().values()
                .stream()
                .filter(product -> Objects.equals(product.getOneLevelCategoryId(), oneLevelCategoryId))
                .map(RewardProduct::getId).collect(Collectors.toSet());
    }

    @Override
    public RewardProductDetail loadRewardProductDetail(StudentDetail student, long id) {
        RewardProduct product = this.loadRewardProduct(id);
        RewardProductDetail productDetail = new RewardProductDetailGenerator(this).generateStudentRewardProductDetail(product, student);
        return productDetail;
    }

    @Override
    public List<RewardTag> loadRewardTags(RewardTagLevel tagLevel, UserType userType) {
        return getRewardTagBuffer().loadRewardTags(tagLevel, userType);
    }

    @Override
    public List<RewardTag> loadRewardTags(List<Long> idList, UserType userType) {
        return getRewardTagBuffer().loadRewardTags(idList, userType);
    }

    /**
     * 判断一个产品是否包含某分类
     *
     * @param productId
     * @param categoryName
     * @return
     */
    public boolean containsCategory(Long productId, String categoryName) {
        return findRewardProductCategoriesByProductId(productId)
                .stream()
                .filter(o -> o != null && !StringUtils.isEmpty(o.getCategoryName()))
                .anyMatch(c -> c.getCategoryName().contains(categoryName));
    }


    // 6月1日 - 8月10日 毕业班不允许兑换
    public Boolean isGraduate(StudentDetail studentDetail) {
        String currentYear = DateUtils.dateToString(new Date(), "yyyy");
        Date startDate = DateUtils.stringToDate(currentYear + "-06-01 00:00:00");
        Date endDate = DateUtils.stringToDate(currentYear + "-08-09 23:59:59");

        if (RuntimeMode.le(Mode.TEST)) {
            startDate = DateUtils.stringToDate(currentYear + "-05-01 00:00:00");
        }

        DateRange range = new DateRange(startDate, endDate);
        Date now = new Date();

        Clazz clazz = studentDetail.getClazz();
        if (clazz == null)
            return false;

        // 已经毕业了的班级也不能兑换
        if (clazz.getClazzLevel() == ClazzLevel.PRIMARY_GRADUATED || clazz.getClazzLevel() == ClazzLevel.MIDDLE_GRADUATED)
            return true;

        if (!range.contains(now))
            return false;

        if (studentDetail.isPrimaryStudent()) {
            // 5年制5年级 或者6年制6年级
            if (((clazz.getEduSystem() == EduSystemType.P5 && clazz.getClazzLevel().getLevel() == 5) ||
                    (clazz.getEduSystem() == EduSystemType.P6 && clazz.getClazzLevel().getLevel() == 6))) {
                return true;
            }
        } else if (studentDetail.isJuniorStudent()) {
            // 如果是9年级，中学毕业
            if (clazz.getClazzLevel() == ClazzLevel.NINTH_GRADE)
                return true;
        }

        return false;
    }
}
