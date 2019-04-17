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

package com.voxlearning.utopia.service.reward.impl.loader;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ExposeServices;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.LazyInitializationSupplier;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.StringUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.monitor.FlightController;
import com.voxlearning.alps.spi.monitor.FlightRecorder;
import com.voxlearning.utopia.service.action.api.document.Privilege;
import com.voxlearning.utopia.service.privilege.client.PrivilegeBufferServiceClient;
import com.voxlearning.utopia.service.reward.api.RewardLoader;
import com.voxlearning.utopia.service.reward.api.mapper.*;
import com.voxlearning.utopia.service.reward.base.AbstractRewardLoader;
import com.voxlearning.utopia.service.reward.base.buffer.*;
import com.voxlearning.utopia.service.reward.buffer.ProductTargetBuffer;
import com.voxlearning.utopia.service.reward.buffer.RewardActivityBuffer;
import com.voxlearning.utopia.service.reward.buffer.internal.JVMRewardActivityBuffer;
import com.voxlearning.utopia.service.reward.entity.*;
import com.voxlearning.utopia.service.reward.impl.dao.RewardActivityVersion;
import com.voxlearning.utopia.service.reward.impl.dao.RewardOrderPersistence;
import com.voxlearning.utopia.service.reward.impl.dao.RewardProductTargetDao;
import com.voxlearning.utopia.service.reward.impl.internal.*;
import com.voxlearning.utopia.service.reward.impl.service.newversion.NewRewardLoaderImpl;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Default {@link RewardLoader} implementation.
 *
 * @author Xiaopeng Yang
 * @author Xiaohai Zhang
 */
@Named
@ExposeService(interfaceClass = RewardLoader.class)
public class RewardLoaderImpl extends AbstractRewardLoader implements InitializingBean, JVMRewardActivityBuffer.Aware {

    @Inject private InternalRewardCategoryService internalRewardCategoryService;
    @Inject private InternalRewardImageService internalRewardImageService;
    @Inject private InternalRewardIndexService internalRewardIndexService;
    @Inject private InternalRewardLogisticsService internalRewardLogisticsService;
    @Inject private InternalRewardProductService internalRewardProductService;
    @Inject private InternalRewardSkuService internalRewardSkuService;
    @Inject private InternalRewardTagService internalRewardTagService;
    @Inject private InternalRewardActivityService internalRewardActivityService;

    @Inject private RewardOrderPersistence rewardOrderPersistence;
    @Inject private RewardHelpers rewardHelpers;
    @Inject private RewardProductTargetDao rewardProductTargetDao;
    @Inject private PrivilegeBufferServiceClient privilegeBufferServiceClient;

    @Inject private RewardActivityVersion rewardActivityVersion;
    @Inject private NewRewardLoaderImpl newRewardLoader;

    @Override
    protected RewardCategoryBuffer createRewardCategoryBuffer() {
        RewardCategoryBuffer buffer = new RewardCategoryBuffer();
        buffer.attach(internalRewardCategoryService.loadVersionedRewardCategoryList());
        return buffer;
    }

    @Override
    protected RewardImageBuffer createRewardImageBuffer() {
        RewardImageBuffer buffer = new RewardImageBuffer();
        buffer.attach(internalRewardImageService.loadVersionedRewardImageList());
        return buffer;
    }

    @Override
    protected RewardIndexBuffer createRewardIndexBuffer() {
        RewardIndexBuffer buffer = new RewardIndexBuffer();
        buffer.attach(internalRewardIndexService.loadVersionedRewardIndexList());
        return buffer;
    }

    @Override
    protected RewardProductBuffer createRewardProductBuffer() {
        RewardProductBuffer buffer = new RewardProductBuffer();
        buffer.attach(internalRewardProductService.loadVersionedRewardProductList());
        return buffer;
    }

    @Override
    protected RewardTagBuffer createRewardTagBuffer() {
        RewardTagBuffer buffer = new RewardTagBuffer();
        buffer.attach(internalRewardTagService.loadVersionedRewardTagList());
        return buffer;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (!RuntimeMode.isUnitTest()) {
            long version = getRewardCategoryBuffer().getVersion();
            logger.info("Buffer [RewardCategory] loaded, current version is {}.", version);
            version = getRewardImageBuffer().getVersion();
            logger.info("Buffer [RewardImage] loaded, current version is {}.", version);
            version = getRewardIndexBuffer().getVersion();
            logger.info("Buffer [RewardIndex] loaded, current version is {}.", version);
            version = getRewardProductBuffer().getVersion();
            logger.info("Buffer [RewardProduct] loaded, current version is {}.", version);
            version = getRewardTagBuffer().getVersion();
            logger.info("Buffer [RewardTag] loaded, current version is {}.", version);
        }
    }

    @Override
    public List<RewardProductCategoryRef> findRewardProductCategoryRefsByCategoryId(Long categoryId) {
        return internalRewardProductService.$findRewardProductCategoryRefsByCategoryId(categoryId);
    }

    @Override
    public List<RewardCategory> findRewardProductCategoriesByProductId(Long productId) {
        return internalRewardProductService
                .$findRewardProductCategoryRefsByProductId(productId)
                .stream()
                .map(p -> getRewardCategoryBuffer().loadRewardCategoryMap().get(p.getCategoryId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<RewardProductTagRef> findRewardProductTagRefsByTagId(Long tagId) {
        return internalRewardProductService.$findRewardProductTagRefsByTagId(tagId);
    }

    @Override
    public Map<Long, RewardOrder> loadRewardOrders(Collection<Long> orderIds) {
        return rewardHelpers.getRewardOrderLoader().loadRewardOrders(orderIds);
    }

    @Override
    public Map<Long, List<RewardOrder>> loadUserRewardOrders(Collection<Long> userIds) {
        return rewardHelpers.getRewardOrderLoader().loadUserRewardOrders(userIds);
    }

    @Override
    public Map<Long, List<RewardSku>> loadProductRewardSkus(Collection<Long> productIds) {
        return internalRewardSkuService.$findRewardSkusByProductIds(productIds);
    }

    public List<RewardSku> loadProductSku(Long productId) {
        return internalRewardSkuService.$findRewardSkusByProductId(productId);
    }

    @Override
    public List<Long> getHasInventoryProducts() {
        return internalRewardSkuService.getHasInventoryProducts();
    }

    @Override
    public Map<Long, RewardWishOrder> loadRewardWishOrders(Collection<Long> orderIds) {
        return rewardHelpers.getRewardWishOrderLoader().loadRewardWishOrders(orderIds);
    }

    @Override
    public Map<Long, List<RewardWishOrder>> loadUserRewardWishOrders(Collection<Long> userIds) {
        return rewardHelpers.getRewardWishOrderLoader().loadUserRewardWishOrders(userIds);
    }

    @Override
    public Map<Long, List<RewardCouponDetail>> loadProductRewardCouponDetails(Collection<Long> productIds) {
        return rewardHelpers.getRewardCouponDetailLoader().loadProductRewardCouponDetails(productIds);
    }

    @Override
    public Map<Long, List<RewardCouponDetail>> loadUserRewardCouponDetails(Collection<Long> userIds) {
        return rewardHelpers.getRewardCouponDetailLoader().loadUserRewardCouponDetails(userIds);
    }

    @Override
    public Map<Long, List<RewardMoonLightBoxHistory>> loadMoonLightBoxHistoryByUserIds(Collection<Long> userIds) {
        return rewardHelpers.getRewardMoonLightBoxLoader().loadUsersRewardMoonlightBoxHistorys(userIds);
    }

    @Override
    public RewardLogistics loadRewardLogistics(Long id) {
        return internalRewardLogisticsService.$loadRewardLogistics(id);
    }

    @Override
    public List<RewardLogistics> loadSchoolRewardLogistics(Long schoolId, RewardLogistics.Type type) {
        return internalRewardLogisticsService.$findRewardLogistics(schoolId, type);
    }

    // ========================================================================
    // buffer supported methods
    // ========================================================================

    @Override
    public VersionedRewardCategoryList loadVersionedRewardCategoryList(long version) {
        FlightRecorder.closeLog();
        VersionedRewardCategoryList data = getRewardCategoryBuffer().dump();
        return (version == 0 || version < data.getVersion()) ? data : null;
    }

    @Override
    public VersionedRewardImageList loadVersionedRewardImageList(long version) {
        FlightRecorder.closeLog();
        VersionedRewardImageList data = getRewardImageBuffer().dump();
        return (version == 0 || version < data.getVersion()) ? data : null;
    }

    @Override
    public VersionedRewardIndexList loadVersionedRewardIndexList(long version) {
        FlightRecorder.closeLog();
        VersionedRewardIndexList data = getRewardIndexBuffer().dump();
        return (version == 0 || version < data.getVersion()) ? data : null;
    }

    @Override
    public VersionedRewardProductList loadVersionedRewardProductList(long version) {
        FlightRecorder.closeLog();
        VersionedRewardProductList data = getRewardProductBuffer().dump();
        return (version == 0 || version < data.getVersion()) ? data : null;
    }

    @Override
    public VersionedRewardTagList loadVersionedRewardTagList(long version) {
        FlightRecorder.closeLog();
        VersionedRewardTagList data = getRewardTagBuffer().dump();
        return (version == 0 || version < data.getVersion()) ? data : null;
    }


    public RewardActivityList loadRewardActivitiesList(long version) {

        FlightController.disableLog();
        RewardActivityBuffer buffer = getRewardActivityBuffer();
        if (version < buffer.getVersion()) {
            return buffer.dump();
        } else {
            return null;
        }
    }

    @Override
    public List<RewardActivity> loadRewardActivities() {
        return getRewardActivityBuffer().loadRewardActivity();
    }

    @Override
    public List<RewardActivity> loadRewardActivitiesNoBuffer() {
        return internalRewardActivityService.loadRewardActivities();
    }

    @Override
    public RewardActivity loadRewardActivity(Long activityId) {
        return internalRewardActivityService.loadRewardActivity(activityId);
    }

    @Override
    public RewardActivity loadRewardActivityNoBuffer(Long activityId) {
        return internalRewardActivityService.loadRewardActivity(activityId);
    }

/*    @Override
    public List<RewardActivityRecord> loadRewardActivityRecords(Long activityId, Long userId) {
        return internalRewardActivityService.loadRewardActivityRecords(activityId, userId);
    }*/

/*    @Override
    public List<RewardActivityRecord> loadRecentActivityRecords(Long activityId, int limit) {
        return internalRewardActivityService.loadRecentActivityRecords(activityId, limit);
    }*/

    @Override
    public List<RewardActivityImage> loadActivityImages(Long activityId) {
        return internalRewardActivityService.loadActivityImages(activityId);
    }

/*    @Override
    public List<RewardActivityRecord> loadUserRecordsInDay(Long userId, Date date) {
        return internalRewardActivityService.loadUserRecordsInDay(userId, date);
    }*/

    @Override
    public List<RewardActivityRecord> loadActivityUserRecords(@CacheParameter("USER_ID") Long userId) {
        return internalRewardActivityService.loadUserRecords(userId);
    }

    @Override
    public Map<Long, Integer> loadUserCollectOrdersInClazz(Long clazzId, String categoryCode, Date startDate) {
        if (clazzId == null)
            return Collections.emptyMap();

        return rewardOrderPersistence.loadUserCollectOrdersInClazz(clazzId, categoryCode, startDate);
    }

    @Override
    public Map<Integer, List<RewardProductTarget>> loadRewardTargetGroupByType(@CacheParameter("TARGET_PID") Long productId) {
        if (productId == null || productId == 0L)
            return Collections.emptyMap();

        return rewardProductTargetDao.findByProductId(productId)
                .stream()
                .collect(Collectors.groupingBy(
                        RewardProductTarget::getTargetType,
                        Collectors.toList()));
    }

    @Override
    public Map<Long, List<RewardProductTarget>> loadAllProductTargets() {
        //return rewardProductTargetDao.findAllGrupByProductId();
        // 从 buffer 取数据
        ProductTargetBuffer productTargetBuffer = newRewardLoader.getProductTargetBuffer();
        return productTargetBuffer.loadAllProductTargets();
    }

    @Override
    public List<RewardProduct> loadRewardPrivilegeProduct() {
        Map<String, Privilege> privilegeMap = privilegeBufferServiceClient.getPrivilegeBuffer()
                .dump()
                .getData()
                .stream()
                .collect(Collectors.toMap(Privilege::getId, p -> p));
        return loadRewardProductMap()
                .values()
                .stream()
                .filter(p -> StringUtils.isNotBlank(p.getRelateVirtualItemId()))
                .filter(p -> privilegeMap.containsKey(p.getRelateVirtualItemId()))
                .collect(Collectors.toList());
    }

    @Override
    public synchronized void reloadRewardActivityBuffer() {
        long actualVersion = rewardActivityVersion.current();
        RewardActivityBuffer buffer = getRewardActivityBuffer();
        long bufferVersion = buffer.getVersion();
        if (bufferVersion != actualVersion) {
            RewardActivityList data = new RewardActivityList();
            data.setVersion(actualVersion);
            data.setRewardActivityList(loadRewardActivitiesNoBuffer());
            buffer.attach(data);
            logger.info("[RewardActivityBuffer] reloaded: [{}] -> [{}]", bufferVersion, actualVersion);
        }
    }

    @Override
    public List<RewardProductTagRef> findRewardProductTagRefsByProductId(Long productId) {
        return internalRewardProductService.$findRewardProductTagRefsByProductId(productId);
    }

    @Override
    public List<RewardProductTagRef> findRewardProductTagRefsByProductIdList(List<Long> productIdList) {
        return internalRewardProductService.$findRewardProductTagRefsByProductIdList(productIdList);
    }

    public List<RewardActivityRecord> loadUserRecordsInDay(Long userId, Date date) {
        Date startTime = DateUtils.getDayStart(date);
        Date endTime = DateUtils.getDayEnd(date);

        return loadActivityUserRecords(userId)
                .stream()
                .filter(r -> r.getCreateDatetime().before(endTime))
                .filter(r -> r.getCreateDatetime().after(startTime))
                .collect(Collectors.toList());
    }

    private final LazyInitializationSupplier<JVMRewardActivityBuffer> rewardActivityBufferSupplier = new LazyInitializationSupplier<>(() -> {
        RewardActivityList data = new RewardActivityList();
        data.setVersion(rewardActivityVersion.current());
        data.setRewardActivityList(loadRewardActivitiesNoBuffer());

        JVMRewardActivityBuffer buffer = new JVMRewardActivityBuffer();
        buffer.attach(data);
        return buffer;
    });

    @Override
    public RewardActivityBuffer getRewardActivityBuffer() {
        return rewardActivityBufferSupplier.initializeIfNecessary();
    }

    @Override
    public void resetRewardActivityBuffer() {
        rewardActivityBufferSupplier.reset();
    }
}
