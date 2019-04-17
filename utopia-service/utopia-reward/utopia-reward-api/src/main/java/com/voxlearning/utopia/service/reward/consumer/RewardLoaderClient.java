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

package com.voxlearning.utopia.service.reward.consumer;

import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.api.event.AlpsEventContext;
import com.voxlearning.alps.api.event.EventBus;
import com.voxlearning.alps.api.event.dsl.CallbackEvent;
import com.voxlearning.alps.api.event.dsl.MinuteTimerEventListener;
import com.voxlearning.alps.api.event.dsl.TimerEvent;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.concurrent.LazyInitializationSupplier;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.cache.CacheObjectLoader;
import com.voxlearning.alps.spi.cache.ExternalLoader;
import com.voxlearning.alps.spi.cache.KeyGenerator;
import com.voxlearning.alps.spi.core.RuntimeModeLoader;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import com.voxlearning.utopia.service.reward.api.RewardLoader;
import com.voxlearning.utopia.service.reward.api.mapper.*;
import com.voxlearning.utopia.service.reward.api.newversion.NewRewardLoader;
import com.voxlearning.utopia.service.reward.base.AbstractRewardLoader;
import com.voxlearning.utopia.service.reward.base.buffer.*;
import com.voxlearning.utopia.service.reward.base.support.*;
import com.voxlearning.utopia.service.reward.buffer.RewardActivityBuffer;
import com.voxlearning.utopia.service.reward.buffer.internal.JVMRewardActivityBuffer;
import com.voxlearning.utopia.service.reward.cache.RewardCache;
import com.voxlearning.utopia.service.reward.client.newversion.NewRewardBufferLoaderClient;
import com.voxlearning.utopia.service.reward.client.newversion.NewRewardLoaderClient;
import com.voxlearning.utopia.service.reward.constant.RewardOrderStatus;
import com.voxlearning.utopia.service.reward.constant.RewardProductTargetType;
import com.voxlearning.utopia.service.reward.entity.*;
import com.voxlearning.utopia.service.reward.mapper.LoadRewardProductContext;
import com.voxlearning.utopia.service.reward.mapper.RewardOrderMapper;
import com.voxlearning.utopia.service.reward.mapper.RewardProductDetail;
import com.voxlearning.utopia.service.reward.mapper.RewardProductDetailPagination;
import com.voxlearning.utopia.service.reward.util.GrayFuncMngCallback;
import com.voxlearning.utopia.service.reward.util.LoadUserSchoolCallback;
import com.voxlearning.utopia.service.reward.util.RewardProductDetailUtils;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.TeacherExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.ResearchStaffDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.voxlearning.alps.core.util.CacheKeyGenerator.generateCacheKey;
import static com.voxlearning.utopia.service.reward.constant.RewardProductType.*;

/**
 * Client implementation of remote service {@link RewardLoader}.
 *
 * @author Xiaohai Zhang
 * @since Dec 1, 2014
 */
public class RewardLoaderClient extends AbstractRewardLoader implements RewardActivityBuffer.Aware, InitializingBean, DisposableBean {

    @ImportService(interfaceClass = RewardLoader.class)
    private RewardLoader remoteReference;

    @Inject
    private TeacherLoaderClient teacherLoaderClient;
    @Inject
    private NewRewardLoaderClient newRewardLoaderClient;
    @Inject
    private NewRewardBufferLoaderClient newRewardBufferLoaderClient;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (RuntimeMode.isProduction()) {
            EventBus.publish(new CallbackEvent(getRewardActivityBuffer()::loadRewardActivity));
        }
    }

    @Override
    public void destroy() throws Exception {
        EventBus.unsubscribe(reloadRewardActivityBuffer);
    }

    private RewardOrderLoader rewardOrderLoader = null;

    public synchronized RewardOrderLoader getRewardOrderLoader() {
        if (rewardOrderLoader == null) {
            this.rewardOrderLoader = new RewardOrderLoader(
                    orderIds -> {
                        if (CollectionUtils.isEmpty(orderIds)) {
                            return Collections.emptyMap();
                        }
                        CacheObjectLoader cacheObjectLoader = RewardCache.getRewardCache().getCacheObjectLoader();
                        CacheObjectLoader.Loader<Long, RewardOrder> loader = cacheObjectLoader.createLoader(new KeyGenerator<Long>() {
                            @Override
                            public String generate(Long source) {
                                return generateCacheKey(RewardOrder.class, source);
                            }
                        });
                        try {
                            return loader.loads(orderIds).loadsMissed(new ExternalLoader<Long, RewardOrder>() {
                                @Override
                                public Map<Long, RewardOrder> loadFromExternal(Collection<Long> missedSources) {
                                    return remoteReference.loadRewardOrders(missedSources);
                                }
                            }).getResult();
                        } catch (Exception ex) {
                            logger.error("Failed to load reward orders (orderIds={})", StringUtils.join(orderIds, ","), ex);
                            return Collections.emptyMap();
                        }
                    },
                    userIds -> {
                        if (CollectionUtils.isEmpty(userIds)) {
                            return Collections.emptyMap();
                        }
                        CacheObjectLoader cacheObjectLoader = RewardCache.getRewardCache().getCacheObjectLoader();
                        CacheObjectLoader.Loader<Long, List<RewardOrder>> loader = cacheObjectLoader.createLoader(new KeyGenerator<Long>() {
                            @Override
                            public String generate(Long source) {
                                return generateCacheKey(RewardOrder.class, "userId", source);
                            }
                        });
                        try {
                            return loader.loads(userIds).loadsMissed(new ExternalLoader<Long, List<RewardOrder>>() {
                                @Override
                                public Map<Long, List<RewardOrder>> loadFromExternal(Collection<Long> missedSources) {
                                    return remoteReference.loadUserRewardOrders(missedSources);
                                }
                            }).getResult();
                        } catch (Exception ex) {
                            logger.error("Failed to load user reward orders (userIds={})", StringUtils.join(userIds, ","), ex);
                            return Collections.emptyMap();
                        }
                    });
        }
        return rewardOrderLoader;
    }

    private RewardWishOrderLoader rewardWishOrderLoader = null;

    public synchronized RewardWishOrderLoader getRewardWishOrderLoader() {
        if (rewardWishOrderLoader == null) {
            this.rewardWishOrderLoader = new RewardWishOrderLoader(
                    ids -> {
                        if (CollectionUtils.isEmpty(ids)) {
                            return Collections.emptyMap();
                        }
                        CacheObjectLoader cacheObjectLoader = RewardCache.getRewardCache().getCacheObjectLoader();
                        CacheObjectLoader.Loader<Long, RewardWishOrder> loader = cacheObjectLoader.createLoader(new KeyGenerator<Long>() {
                            @Override
                            public String generate(Long source) {
                                return generateCacheKey(RewardWishOrder.class, source);
                            }
                        });
                        try {
                            return loader.loads(ids).loadsMissed(new ExternalLoader<Long, RewardWishOrder>() {
                                @Override
                                public Map<Long, RewardWishOrder> loadFromExternal(Collection<Long> missedSources) {
                                    return remoteReference.loadRewardWishOrders(missedSources);
                                }
                            }).getResult();
                        } catch (Exception ex) {
                            logger.error("Failed to load reward wish orders (orderIds={})", StringUtils.join(ids, ","), ex);
                            return Collections.emptyMap();
                        }
                    },
                    userIds -> {
                        if (CollectionUtils.isEmpty(userIds)) {
                            return Collections.emptyMap();
                        }
                        CacheObjectLoader cacheObjectLoader = RewardCache.getRewardCache().getCacheObjectLoader();
                        CacheObjectLoader.Loader<Long, List<RewardWishOrder>> loader = cacheObjectLoader.createLoader(new KeyGenerator<Long>() {
                            @Override
                            public String generate(Long source) {
                                return generateCacheKey(RewardWishOrder.class, "userId", source);
                            }
                        });
                        try {
                            return loader.loads(userIds).loadsMissed(new ExternalLoader<Long, List<RewardWishOrder>>() {
                                @Override
                                public Map<Long, List<RewardWishOrder>> loadFromExternal(Collection<Long> missedSources) {
                                    return remoteReference.loadUserRewardWishOrders(missedSources);
                                }
                            }).getResult();
                        } catch (Exception ex) {
                            logger.error("Failed to load reward wish orders (userIds={})", StringUtils.join(userIds, ","), ex);
                            return Collections.emptyMap();
                        }
                    }
            );
        }
        return rewardWishOrderLoader;
    }

    private RewardCouponDetailLoader rewardCouponDetailLoader = null;

    public synchronized RewardCouponDetailLoader getRewardCouponDetailLoader() {
        if (rewardCouponDetailLoader == null) {
            this.rewardCouponDetailLoader = new RewardCouponDetailLoader(
                    productIds -> {
                        if (CollectionUtils.isEmpty(productIds)) {
                            return Collections.emptyMap();
                        }
                        CacheObjectLoader cacheObjectLoader = RewardCache.getRewardCache().getCacheObjectLoader();
                        CacheObjectLoader.Loader<Long, List<RewardCouponDetail>> loader = cacheObjectLoader.createLoader(new KeyGenerator<Long>() {
                            @Override
                            public String generate(Long source) {
                                return generateCacheKey(RewardCouponDetail.class, "productId", source);
                            }
                        });
                        try {
                            return loader.loads(productIds).loadsMissed(new ExternalLoader<Long, List<RewardCouponDetail>>() {
                                @Override
                                public Map<Long, List<RewardCouponDetail>> loadFromExternal(Collection<Long> missedSources) {
                                    return remoteReference.loadProductRewardCouponDetails(missedSources);
                                }
                            }).getResult();
                        } catch (Exception ex) {
                            logger.error("Failed to load product reward coupon details (productIds={})", StringUtils.join(productIds, ","), ex);
                            return Collections.emptyMap();
                        }
                    },
                    userIds -> {
                        if (CollectionUtils.isEmpty(userIds)) {
                            return Collections.emptyMap();
                        }
                        CacheObjectLoader cacheObjectLoader = RewardCache.getRewardCache().getCacheObjectLoader();
                        CacheObjectLoader.Loader<Long, List<RewardCouponDetail>> loader = cacheObjectLoader.createLoader(new KeyGenerator<Long>() {
                            @Override
                            public String generate(Long source) {
                                return generateCacheKey(RewardCouponDetail.class, "userId", source);
                            }
                        });
                        try {
                            return loader.loads(userIds).loadsMissed(new ExternalLoader<Long, List<RewardCouponDetail>>() {
                                @Override
                                public Map<Long, List<RewardCouponDetail>> loadFromExternal(Collection<Long> missedSources) {
                                    return remoteReference.loadUserRewardCouponDetails(userIds);
                                }
                            }).getResult();
                        } catch (Exception ex) {
                            logger.error("Failed to load user reward coupon details (userIds={})", StringUtils.join(userIds, ","), ex);
                            return Collections.emptyMap();
                        }
                    }
            );
        }
        return rewardCouponDetailLoader;
    }

    private RewardProductDetailGenerator rewardProductDetailGenerator = null;

    public synchronized RewardProductDetailGenerator getRewardProductDetailGenerator() {
        if (rewardProductDetailGenerator == null) {
            this.rewardProductDetailGenerator = new RewardProductDetailGenerator(this);
        }
        return rewardProductDetailGenerator;
    }

    private RewardOrderMapperGenerator rewardOrderMapperGenerator = null;

    public synchronized RewardOrderMapperGenerator getRewardOrderMapperGenerator() {
        if (rewardOrderMapperGenerator == null) {
            this.rewardOrderMapperGenerator = new RewardOrderMapperGenerator(
                    this,
                    getRewardOrderLoader());
        }
        return rewardOrderMapperGenerator;
    }

    private final MinuteTimerEventListener reloadRewardCategoryBuffer = new MinuteTimerEventListener() {
        @Override
        protected void processEvent(TimerEvent timerEvent, AlpsEventContext alpsEventContext) {
            if (RuntimeModeLoader.getInstance().isUnitTest()) {
                return;
            }
            RewardCategoryBuffer buffer = getRewardCategoryBuffer();
            VersionedRewardCategoryList data = loadVersionedRewardCategoryList(buffer.getVersion());
            if (data != null) {
                buffer.attach(data);
            }
        }
    };

    @Override
    protected RewardCategoryBuffer createRewardCategoryBuffer() {
        VersionedRewardCategoryList data = loadVersionedRewardCategoryList(0);
        RewardCategoryBuffer buffer = new RewardCategoryBuffer();
        buffer.attach(data);
        EventBus.subscribe(reloadRewardCategoryBuffer);
        return buffer;
    }

    private final MinuteTimerEventListener reloadRewardImageBuffer = new MinuteTimerEventListener() {
        @Override
        protected void processEvent(TimerEvent timerEvent, AlpsEventContext alpsEventContext) {
            if (RuntimeModeLoader.getInstance().isUnitTest()) {
                return;
            }
            RewardImageBuffer buffer = getRewardImageBuffer();
            VersionedRewardImageList data = loadVersionedRewardImageList(buffer.getVersion());
            if (data != null) {
                buffer.attach(data);
            }
        }
    };

    @Override
    protected RewardImageBuffer createRewardImageBuffer() {
        VersionedRewardImageList data = loadVersionedRewardImageList(0);
        RewardImageBuffer buffer = new RewardImageBuffer();
        buffer.attach(data);
        EventBus.subscribe(reloadRewardImageBuffer);
        return buffer;
    }

    private final MinuteTimerEventListener reloadRewardIndexBuffer = new MinuteTimerEventListener() {
        @Override
        protected void processEvent(TimerEvent timerEvent, AlpsEventContext alpsEventContext) {
            if (RuntimeModeLoader.getInstance().isUnitTest()) {
                return;
            }
            RewardIndexBuffer buffer = getRewardIndexBuffer();
            VersionedRewardIndexList data = loadVersionedRewardIndexList(buffer.getVersion());
            if (data != null) {
                buffer.attach(data);
            }
        }
    };

    @Override
    protected RewardIndexBuffer createRewardIndexBuffer() {
        VersionedRewardIndexList data = loadVersionedRewardIndexList(0);
        RewardIndexBuffer buffer = new RewardIndexBuffer();
        buffer.attach(data);
        EventBus.subscribe(reloadRewardIndexBuffer);
        return buffer;
    }

    private final MinuteTimerEventListener reloadRewardProductBuffer = new MinuteTimerEventListener() {
        @Override
        protected void processEvent(TimerEvent timerEvent, AlpsEventContext alpsEventContext) {
            if (RuntimeModeLoader.getInstance().isUnitTest()) {
                return;
            }
            RewardProductBuffer buffer = getRewardProductBuffer();
            VersionedRewardProductList data = loadVersionedRewardProductList(buffer.getVersion());
            if (data != null) {
                buffer.attach(data);
            }
        }
    };

    @Override
    protected RewardProductBuffer createRewardProductBuffer() {
        VersionedRewardProductList data = loadVersionedRewardProductList(0);
        RewardProductBuffer buffer = new RewardProductBuffer();
        buffer.attach(data);
        EventBus.subscribe(reloadRewardProductBuffer);
        return buffer;
    }

    private final MinuteTimerEventListener reloadRewardTagBuffer = new MinuteTimerEventListener() {
        @Override
        protected void processEvent(TimerEvent timerEvent, AlpsEventContext alpsEventContext) {
            if (RuntimeModeLoader.getInstance().isUnitTest()) {
                return;
            }
            RewardTagBuffer buffer = getRewardTagBuffer();
            VersionedRewardTagList data = loadVersionedRewardTagList(buffer.getVersion());
            if (data != null) {
                buffer.attach(data);
            }
        }
    };

    @Override
    protected RewardTagBuffer createRewardTagBuffer() {
        VersionedRewardTagList data = loadVersionedRewardTagList(0);
        RewardTagBuffer buffer = new RewardTagBuffer();
        buffer.attach(data);
        EventBus.subscribe(reloadRewardTagBuffer);
        return buffer;
    }

    /**
     * 如果是学生帐户，则User必须是StudentDetail
     * 如果是老师帐户，则User必须是TeacherDetail
     */
    public RewardProductDetail generateUserRewardProductDetail(User user, Long productId) {
        if (user == null || productId == null) {
            return null;
        }
        RewardProduct product = loadRewardProductMap().get(productId);
        if (product == null) {
            return null;
        }
        switch (user.fetchUserType()) {
            case STUDENT:
                if (!(user instanceof StudentDetail)) {
                    throw new IllegalArgumentException("User must be StudentDetail");
                }
                return getRewardProductDetailGenerator().generateStudentRewardProductDetail(product, (StudentDetail) user);
            case TEACHER:
                if (!(user instanceof TeacherDetail)) {
                    throw new IllegalArgumentException("User must be TeacherDetail");
                }
                return getRewardProductDetailGenerator().generateTeacherRewardProductDetail(product, (TeacherDetail) user);
            case RESEARCH_STAFF:
                return getRewardProductDetailGenerator().generateResearchStaffRewardProductDetail(product);
            default:
                return null;
        }
    }

    public List<RewardOrderMapper> generateUserRewardOrderMappers(Long userId) {
        return getRewardOrderMapperGenerator().generateUserRewardOrderMapper(userId);
    }

    /**
     * 如果是学生帐户，则User必须是StudentDetail
     * 如果是老师帐户，则User必须是TeacherDetail
     */
    public Collection<RewardProductDetail> generateUserRewardProductDetails(User user, Collection<Long> productIds) {
        if (user == null || CollectionUtils.isEmpty(productIds)) {
            return Collections.emptyList();
        }
        Map<Long, RewardProduct> map = loadRewardProductMap();
        Collection<RewardProduct> products = new LinkedList<>();
        for (Long productId : productIds) {
            CollectionUtils.addNonNullElement(products, map.get(productId));
        }
        if (CollectionUtils.isEmpty(products)) {
            return Collections.emptyList();
        }
        switch (user.fetchUserType()) {
            case STUDENT:
                if (!(user instanceof StudentDetail)) {
                    throw new IllegalArgumentException("User must be StudentDetail");
                }

                return getRewardProductDetailGenerator().generateStudentRewardProductDetails(products, (StudentDetail) user);
            case TEACHER:
                if (!(user instanceof TeacherDetail)) {
                    throw new IllegalArgumentException("User must be TeacherDetail");
                }
                return getRewardProductDetailGenerator().generateTeacherRewardProductDetails(products,
                        (TeacherDetail) user,
                        false,
                        false,
                        0,
                        false);
            case RESEARCH_STAFF:
                return getRewardProductDetailGenerator().generateResearchStaffRewardProductDetails(products);
            default:
                return Collections.emptyList();
        }
    }

    public RewardProductDetailPagination loadRewardProducts(User user,
                                                            LoadRewardProductContext context,
                                                            LoadUserSchoolCallback userSchoolLoader,
                                                            GrayFuncMngCallback grayMng) {
        return loadRewardProducts(user, context, userSchoolLoader, grayMng, detail -> true);
    }

    /**
     * 如果是老师帐户，则User必须是TeacherDetail
     * 如果是学生帐户，则User必须是StudentDetail
     * 如果是教研员帐户，则User必须是ResearchStaffDetail
     */
    public RewardProductDetailPagination loadRewardProducts(User user,
                                                            LoadRewardProductContext context,
                                                            LoadUserSchoolCallback userSchoolLoader,
                                                            GrayFuncMngCallback grayMng,
                                                            Predicate<RewardProductDetail> filter) {

        List<RewardProductDetail> details = new LinkedList<>();
        List<Integer> regionCodeList = new ArrayList<>();

        AtomicLong usableIntegral = new AtomicLong(0L);
        AtomicBoolean shiwuOffline = new AtomicBoolean(false);

        switch (Objects.requireNonNull(user).fetchUserType()) {
            case TEACHER: {
                if (!(user instanceof TeacherDetail)) {
                    throw new IllegalArgumentException("User must be TeacherDetail");
                }
                List<RewardProduct> products = new LinkedList<>();
                if ("all".equals(context.loadPage)) {
                    if (context.twoLevelTagId == 0L && context.categoryId == 0L) {
                        products.addAll(loadAllTeacherProducts());
                    } else {
                        Set<Long> realProductIds = getRetainProductIdsForAllPage(context.twoLevelTagId, context.categoryId);
                        if (CollectionUtils.isNotEmpty(realProductIds)) {
                            Map<Long, RewardProduct> map = loadRewardProductMap();
                            for (Long productId : realProductIds) {
                                CollectionUtils.addNonNullElement(products, map.get(productId));
                            }
                        }
                    }
                } else if ("tag".equals(context.loadPage)) {
                    Set<Long> realProductIds = getRetainProductIds(context.oneLevelTagId, context.twoLevelTagId, context.categoryId, context.twoLevelTagOnly);
                    if (CollectionUtils.isNotEmpty(realProductIds)) {
                        Map<Long, RewardProduct> map = loadRewardProductMap();
                        for (Long productId : realProductIds) {
                            CollectionUtils.addNonNullElement(products, map.get(productId));
                        }
                    }
                } else {
                    throw new UnsupportedOperationException("Unsupported loadPage: " + context.loadPage);
                }

                details.addAll(getTeacherProductDetail((TeacherDetail) user,
                        products,
                        context.canExchangeFlag,
                        context.teacherLevelFlag,
                        context.ambassadorLevelFlag,
                        context.ambassadorLevel,
                        context.nextLevelFlag));

                TeacherDetail teacherDetail = (TeacherDetail) user;
                regionCodeList.add(teacherDetail.getCityCode());
                regionCodeList.add(teacherDetail.getRootRegionCode());
                regionCodeList.add(teacherDetail.getRegionCode());

                usableIntegral.set(((TeacherDetail) user).getUserIntegral().getUsable());

                break;
            }
            case STUDENT: {
                if (!(user instanceof StudentDetail)) {
                    throw new IllegalArgumentException("User must be StudentDetail");
                }
                School school = Objects.requireNonNull(userSchoolLoader).loadUserSchool(user);
                if (school == null) {
                    return new RewardProductDetailPagination(Collections.<RewardProductDetail>emptyList());
                }
                List<RewardProduct> products = new LinkedList<>();
                if ("all".equals(context.loadPage)) {
                    if (context.twoLevelTagId == 0L && context.categoryId == 0L && CollectionUtils.isEmpty(context.categoryIds)) {
                        // 屏蔽移动端展示的爱心捐赠类型 目前all 只有移动端调用
                        List<RewardProduct> productList = loadAllStudentProducts();
                        productList = productList.stream()
                                .filter(p -> !Objects.equals(p.getProductType(), JPZX_PRESENT.name()))
                                // 只在无分类条件的前提下，才筛选大的商品类型字段
                                .filter(p -> StringUtils.isEmpty(context.productType) ||
                                        Objects.equals(p.getProductType(), context.productType))
                                .collect(Collectors.toList());
                        products.addAll(productList);
                    } else {
                        // 支持同时查询多个类别
                        List<Long> categoryIds;
                        if (CollectionUtils.isNotEmpty(context.categoryIds)) {
                            categoryIds = context.categoryIds;
                        } else
                            categoryIds = Collections.singletonList(context.categoryId);

                        Set<Long> realProductIds;
                        for (Long categoryId : categoryIds) {
                            realProductIds = getRetainProductIdsForAllPage(context.twoLevelTagId, categoryId);
                            if (CollectionUtils.isNotEmpty(realProductIds)) {
                                Map<Long, RewardProduct> map = loadRewardProductMap();
                                for (Long productId : realProductIds) {
                                    CollectionUtils.addNonNullElement(products, map.get(productId));
                                }
                            }
                        }
                    }
                } else if ("tag".equals(context.loadPage)) {
                    Set<Long> realProductIds = new HashSet<>();
                    if (context.tags == null) {
                        realProductIds = getRetainProductIds(context.oneLevelTagId, context.twoLevelTagId, context.categoryId, context.twoLevelTagOnly);
                    } else {
                        realProductIds = getRetainProductIds(context.tags, context.categoryId);
                    }
                    if (CollectionUtils.isNotEmpty(realProductIds)) {
                        Map<Long, RewardProduct> map = loadRewardProductMap();
                        for (Long productId : realProductIds) {
                            CollectionUtils.addNonNullElement(products, map.get(productId));
                        }
                    }
                } else {
                    throw new UnsupportedOperationException("Unsupported loadPage: " + context.loadPage);
                }

                // 放入区域码
                StudentDetail studentDetail = (StudentDetail)user;
                regionCodeList.add(studentDetail.getCityCode());
                regionCodeList.add(studentDetail.getRootRegionCode());
                regionCodeList.add(studentDetail.getStudentSchoolRegionCode());

                details.addAll(getStudentProductDetail( (StudentDetail) user, products, context.canExchangeFlag));

                usableIntegral.set(((StudentDetail) user).getUserIntegral().getUsable());

                if(grayMng != null && grayMng.isGrayAvailable(studentDetail,"Reward","OfflineShiWu"))
                    shiwuOffline.set(true);

                break;
            }
            case RESEARCH_STAFF: {
                if (!(user instanceof ResearchStaffDetail)) {
                    throw new IllegalArgumentException("User must be ResearchStaffDetail");
                }
                List<RewardProduct> products = new LinkedList<>();
                if ("all".equals(context.loadPage)) {
                    if (context.twoLevelTagId == 0L && context.categoryId == 0L) {
                        products.addAll(loadAllTeacherProducts());
                    } else {
                        Set<Long> realProductIds = getRetainProductIdsForAllPage(context.twoLevelTagId, context.categoryId);
                        if (CollectionUtils.isNotEmpty(realProductIds)) {
                            Map<Long, RewardProduct> map = loadRewardProductMap();
                            for (Long productId : realProductIds) {
                                CollectionUtils.addNonNullElement(products, map.get(productId));
                            }
                        }
                    }
                } else if ("tag".equals(context.loadPage)) {
                    Set<Long> realProductIds = getRetainProductIds(context.oneLevelTagId, context.twoLevelTagId, context.categoryId, context.twoLevelTagOnly);
                    if (CollectionUtils.isNotEmpty(realProductIds)) {
                        Map<Long, RewardProduct> map = loadRewardProductMap();
                        for (Long productId : realProductIds) {
                            CollectionUtils.addNonNullElement(products, map.get(productId));
                        }
                    }
                } else {
                    throw new UnsupportedOperationException("Unsupported loadPage: " + context.loadPage);
                }
                details.addAll(getRstaffProductDetail((ResearchStaffDetail) user, products, context.canExchangeFlag));
                break;
            }
            default: {
                // unsupported user type
                return new RewardProductDetailPagination(Collections.<RewardProductDetail>emptyList());
            }
        }

        Map<Long, List<RewardProductTarget>> productTargetsMap = newRewardBufferLoaderClient.getProductTargetBuffer().loadAllProductTargets();
        // 此处过滤0库存的奖品
        // 换种实现
        // 过滤投放区域
        Set<Long> hasInventoryProductIds = new HashSet<>(getHasInventoryProducts());

        // 过滤逻辑
        details = details.stream()
                .filter(filter)
                // 过滤展示端，默认或者是为空的话是都显示
                .filter(p -> StringUtils.isEmpty(p.getDisplayTerminal())
                        || context.terminal == null
                        || p.getDisplayTerminal().contains(context.terminal.name()))
                // 过渡零库存的商品
                .filter(p -> hasInventoryProductIds.contains(p.getId()))
                .filter(p -> {
                    List<RewardProductTarget> productTargets;
                    // 投放区域
                    if ((productTargets = productTargetsMap.get(p.getId())) != null
                            && productTargets.size() > 0) {
                        // 查看是不是全部投放
                        if (productTargets.stream()
                                .filter(pt -> pt.getTargetType() == RewardProductTargetType.TARGET_TYPE_ALL.getType())
                                .anyMatch(pt -> SafeConverter.toBoolean(pt.getTargetStr()))) {
                            return true;
                        }

                        if (!productTargets.stream()
                                .map(pt -> SafeConverter.toInt(pt.getTargetStr()))
                                .anyMatch(regionCodeList::contains)) {
                            return false;
                        }
                    }

                    return true;
                })
                // 过滤affordable选项
                .filter(p -> !context.showAffordable || showAffordableFilter(usableIntegral.get(), p, user))
                // 过滤实物显示
                .filter(p -> JPZX_TIYAN.name().equals(p.getProductType()) || (JPZX_SHIWU.name().equals(p.getProductType()) && !shiwuOffline.get()))
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(details)) {
            return new RewardProductDetailPagination(Collections.emptyList());
        }

        details = RewardProductDetailUtils.orderProducts(details, context.orderBy, context.upDown);
        long total = details.size();
        if (context.pageNumber * context.pageSize > total) {
            // 请正确填写页码
            return new RewardProductDetailPagination(Collections.emptyList());
        }

        int start = context.pageNumber * context.pageSize;
        int end = Math.min((int) total, ((context.pageNumber + 1) * context.pageSize));
        details = new LinkedList<>(details.subList(start, end));

        return new RewardProductDetailPagination(
                details,
                new PageRequest(context.pageNumber, context.pageSize),
                total);
    }

    private Boolean showAffordableFilter(Long integral, RewardProductDetail detail, User user) {
        if (integral < detail.getDiscountPrice()) {
            return false;
        }
        if (user.isTeacher()) {
            return checkExchangePrivilegeProduct(detail, user);
        }
        return true;
    }

    public Boolean checkExchangePrivilegeProduct(RewardProductDetail detail, User user) {
        if (user.isTeacher()) {//老师等级过滤
            TeacherDetail teacherDetail = (TeacherDetail) user;
            if (teacherDetail.isPrimarySchool()) {
                String privilege = getTeacherPrivilege(detail);
                if (privilege != null) {
                    TeacherExtAttribute extAttribute = teacherLoaderClient.loadTeacherExtAttribute(user.getId());
                    if (extAttribute == null || extAttribute.getNewLevel() == null) {
                        return false;
                    } else if (Objects.equals(privilege,"高级") && extAttribute.getNewLevel() < TeacherExtAttribute.NewLevel.SENIOR.getLevel()) {
                        return false;
                    } else if (Objects.equals(privilege,"特级") && extAttribute.getNewLevel() < TeacherExtAttribute.NewLevel.SUPER.getLevel()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public String getTeacherPrivilege(RewardProductDetail detail) {
        String result = null;
        if (detail.getTags().contains(getTeacherPrivilegeOneLevelTag())) {
            if (detail.getTags().contains("高级教师")) {
                result = "高级";
            } else if (detail.getTags().contains("特级教师")) {
                result = "特级";
            }
        }
        return result;
    }

    public String getTeacherPrivilegeOneLevelTag() {
        return "教师等级特权专区";
    }

    public Page<Map<String, Object>> rewardOrdersToPage(List<RewardOrder> orders, Pageable pageable) {
        if (CollectionUtils.isEmpty(orders)) {
            return new PageImpl<>(Collections.<Map<String, Object>>emptyList());
        }
        int fromIndex = Objects.requireNonNull(pageable).getPageNumber() * pageable.getPageSize();
        if (fromIndex >= orders.size()) {
            return new PageImpl<>(Collections.<Map<String, Object>>emptyList());
        }
        int toIndex = Math.min(orders.size(), (pageable.getPageNumber() + 1) * pageable.getPageSize());
        long total = orders.size();
        orders = new LinkedList<>(orders.subList(fromIndex, toIndex));

        List<Map<String, Object>> content = new ArrayList<>();
        Collection<Long> productIds = new LinkedHashSet<>();
        for (RewardOrder order : orders) {
            CollectionUtils.addNonNullElement(productIds, order.getProductId());
        }

        Map<Long, List<RewardImage>> rewardImages = loadProductRewardImages(productIds);
        RewardLogistics logistics;
        for (RewardOrder order : orders) {
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("orderId", order.getId());
            List<RewardImage> images = rewardImages.get(order.getProductId());
            if (CollectionUtils.isNotEmpty(images)) {
                dataMap.put("image", images.get(0).getLocation());
            }
            dataMap.put("productName", order.getProductName());
            dataMap.put("productId", order.getProductId());
            dataMap.put("ext", order.getExtAttributes());
            dataMap.put("skuName", order.getSkuName());
            dataMap.put("price", order.getPrice());
            dataMap.put("quantity", order.getQuantity());
            dataMap.put("unit", order.getUnit());
            dataMap.put("createDatetime", order.getCreateDatetime());
            dataMap.put("totalPrice", order.getTotalPrice());
            dataMap.put("status", order.getStatus());
            dataMap.put("category", newRewardLoaderClient.fetchOnelevelCategoryTypeByOrder(order));
            dataMap.put("discount", order.getDiscount());
            dataMap.put("source", Objects.isNull(order.getSource()) ? StringUtils.EMPTY:order.getSource().name());

            // 已发货的订单，带上物流信息
            if (Objects.equals(order.getStatus(), RewardOrderStatus.DELIVER.name())) {
                logistics = loadRewardLogistics(order.getLogisticsId());

                if (logistics != null) {
                    dataMap.put("logistics", logistics);
                }
            }

            content.add(dataMap);
        }
        return new PageImpl<>(content, pageable, total);
    }

    // ========================================================================
    // PRIVATE METHODS
    // ========================================================================

    private Set<Long> getRetainProductIds(Long oneLevelTagId, Long twoLevelTagId, Long categoryId, Boolean twoLevelTagOnly) {
        //取一级标签 二级标签 对应的奖品ID交集
        Set<Long> tagProductIds = getTagRefProductIds(oneLevelTagId, twoLevelTagId);
        if (CollectionUtils.isEmpty(tagProductIds)) {
            if (SafeConverter.toBoolean(twoLevelTagOnly)) {
                return getTagRefProductIdsByTwoLevelTagId(twoLevelTagId);
            }
            return Collections.emptySet();
        }
        Set<Long> categoryRefProductIds = new HashSet<>();
        //取标签 分类对应的奖品ID 交集
        if (categoryId != 0L) {
            List<RewardProductCategoryRef> categoryRefs = findRewardProductCategoryRefsByCategoryId(categoryId);
            if (categoryRefs == null) {
                categoryRefs = Collections.emptyList();
            }
            for (RewardProductCategoryRef ref : categoryRefs) {
                categoryRefProductIds.add(ref.getProductId());
            }

            tagProductIds.retainAll(categoryRefProductIds);
        }
        return tagProductIds;
    }

    private Set<Long> getRetainProductIds(List<Long> tagId, Long categoryId) {
        //取所有标签的并集
        Set<Long> tagProductIds = getTagRefProductIds(tagId);
        if (CollectionUtils.isEmpty(tagProductIds)) {
            return Collections.emptySet();
        }
        Set<Long> categoryRefProductIds = new HashSet<>();
        //取标签 分类对应的奖品ID 交集
        if (categoryId != 0L) {
            List<RewardProductCategoryRef> categoryRefs = findRewardProductCategoryRefsByCategoryId(categoryId);
            if (categoryRefs == null) {
                categoryRefs = Collections.emptyList();
            }
            for (RewardProductCategoryRef ref : categoryRefs) {
                categoryRefProductIds.add(ref.getProductId());
            }

            tagProductIds.retainAll(categoryRefProductIds);
        }
        return tagProductIds;
    }

    private Set<Long> getRetainProductIdsForAllPage(Long twoLevelTagId, Long categoryId) {

        Set<Long> twoRefProductIds = new HashSet<>();
        List<RewardProductTagRef> twoRefs = findRewardProductTagRefsByTagId(twoLevelTagId);
        for (RewardProductTagRef ref : twoRefs) {
            twoRefProductIds.add(ref.getProductId());
        }
        Set<Long> categoryRefProductIds = new HashSet<>();
        List<RewardProductCategoryRef> categoryRefs = findRewardProductCategoryRefsByCategoryId(categoryId);
        for (RewardProductCategoryRef ref : categoryRefs) {
            categoryRefProductIds.add(ref.getProductId());
        }

        if (twoLevelTagId != 0L && categoryId != 0L) {
            twoRefProductIds.retainAll(categoryRefProductIds);
            return twoRefProductIds;
        }
        if (twoLevelTagId != 0L) {
            return twoRefProductIds;
        }
        if (categoryId != 0L) {
            return categoryRefProductIds;
        }
        return Collections.emptySet();
    }

    private Set<Long> getTagRefProductIds(List<Long> tagIds) {
        Set<Long> tagRefProductIds = new HashSet<>();

        for (Long tagId : tagIds) {
            List<RewardProductTagRef> oneRefs = findRewardProductTagRefsByTagId(tagId);
            if (CollectionUtils.isNotEmpty(oneRefs)) {
                for (RewardProductTagRef ref : oneRefs) {
                    tagRefProductIds.add(ref.getProductId());
                }
            }
        }
        return tagRefProductIds;
    }
    private Set<Long> getTagRefProductIds(Long oneLevelTagId, Long twoLevelTagId) {
        Set<Long> tagRefProductIds = new HashSet<>();
        Set<Long> oneRefProductIds = new HashSet<>();
        Set<Long> twoRefProductIds = new HashSet<>();
        List<RewardProductTagRef> oneRefs = findRewardProductTagRefsByTagId(oneLevelTagId);
        if (CollectionUtils.isEmpty(oneRefs)) {
            return tagRefProductIds;
        }
        for (RewardProductTagRef ref : oneRefs) {
            oneRefProductIds.add(ref.getProductId());
        }
        if (twoLevelTagId != 0L) {
            List<RewardProductTagRef> twoRefs = findRewardProductTagRefsByTagId(twoLevelTagId);
            for (RewardProductTagRef ref : twoRefs) {
                twoRefProductIds.add(ref.getProductId());
            }
            oneRefProductIds.retainAll(twoRefProductIds);
        }
        return oneRefProductIds;
    }

    private Set<Long> getTagRefProductIdsByTwoLevelTagId(Long twoLevelTagId) {
        Set<Long> twoRefProductIds = new HashSet<>();
        if (twoLevelTagId != 0L) {
            List<RewardProductTagRef> twoRefs = findRewardProductTagRefsByTagId(twoLevelTagId);
            for (RewardProductTagRef ref : twoRefs) {
                twoRefProductIds.add(ref.getProductId());
            }
        }
        return twoRefProductIds;
    }

    private List<RewardProductDetail> getTeacherProductDetail(TeacherDetail teacher,
                                                              List<RewardProduct> products,
                                                              boolean canExchangeFlag,
                                                              boolean teacherLevelFlag,
                                                              boolean ambassadorLevelFlag,
                                                              Integer ambassadorLevel,
                                                              boolean nextLevelFlag) {
        Collection<RewardProductDetail> details = getRewardProductDetailGenerator().generateTeacherRewardProductDetails(products,
                teacher,
                teacherLevelFlag,
                ambassadorLevelFlag,
                ambassadorLevel,
                nextLevelFlag);
        List<RewardProductDetail> realData = new ArrayList<>();
        details.stream().sorted(Comparator.comparingInt(RewardProductDetail::getTeacherOrderValue).reversed());
        for (RewardProductDetail productDetail : details) {
            RewardProduct product = (RewardProduct) productDetail.getExtenstionAttributes().get("product");
            double usable = (double) teacher.getUserIntegral().getUsable();
            double price = productDetail.getDiscountPrice();
            if (canExchangeFlag) {
                if (product.getOnlined() && usable >= price) {
                    realData.add(productDetail);
                }
            } else {
                if (product.getOnlined()) {
                    realData.add(productDetail);
                }
            }
        }
        return realData;
    }

    private List<RewardProductDetail> getStudentProductDetail(StudentDetail student, List<RewardProduct> products, boolean canExchangeFlag) {
        Collection<RewardProductDetail> details = getRewardProductDetailGenerator().generateStudentRewardProductDetails(products, student);
        List<RewardProductDetail> realData = new ArrayList<>();
        for (RewardProductDetail productDetail : details) {
            RewardProduct product = (RewardProduct) productDetail.getExtenstionAttributes().get("product");
            double usable = (double) student.getUserIntegral().getUsable();
            double price = productDetail.getDiscountPrice();
            if (canExchangeFlag) {
                if (RuntimeMode.isStaging()) {
                    if (product.getStudentVisible() && usable >= price) {
                        realData.add(productDetail);
                    }
                } else {
                    if (product.getStudentVisible() && product.getOnlined() && usable >= price) {
                        realData.add(productDetail);
                    }
                }
            } else {
                if (RuntimeMode.isStaging()) {
                    if (product.getStudentVisible()) {
                        realData.add(productDetail);
                    }
                } else {
                    if (product.getStudentVisible() && product.getOnlined()) {
                        realData.add(productDetail);
                    }
                }
            }
        }
        return realData;
    }

    private List<RewardProductDetail> getRstaffProductDetail(ResearchStaffDetail researchStaffDetail, List<RewardProduct> products, boolean canExchangeFlag) {
        Collection<RewardProductDetail> details = getRewardProductDetailGenerator().generateResearchStaffRewardProductDetails(products);
        List<RewardProductDetail> realData = new ArrayList<>();
        for (RewardProductDetail productDetail : details) {
            RewardProduct product = (RewardProduct) productDetail.getExtenstionAttributes().get("product");
            double usable = (double) researchStaffDetail.getUserIntegral().getUsable();
            double price = productDetail.getDiscountPrice();
            if (canExchangeFlag) {
                if (product.getTeacherVisible() && product.getOsVisible() && product.getOnlined() && usable >= price) {
                    realData.add(productDetail);
                }
            } else {
                if (product.getTeacherVisible() && product.getOsVisible() && product.getOnlined()) {
                    realData.add(productDetail);
                }
            }
        }
        return realData;
    }

    @Override
    public Map<Long, RewardOrder> loadRewardOrders(Collection<Long> orderIds) {
        return remoteReference.loadRewardOrders(orderIds);
    }

    @Override
    public Map<Long, List<RewardOrder>> loadUserRewardOrders(Collection<Long> userIds) {
        return remoteReference.loadUserRewardOrders(userIds);
    }

    public List<RewardOrder> loadUserRewardOrders(Long userId) {
        Map<Long, List<RewardOrder>> orderMap = loadUserRewardOrders(Collections.singleton(userId));
        if (orderMap != null && orderMap.containsKey(userId)) {
            List<RewardOrder> orders = orderMap.get(userId);
            if (!CollectionUtils.isEmpty(orders)) {
                return orders;
            }
        }

        return Collections.emptyList();
    }

    @Override
    public Map<Long, List<RewardSku>> loadProductRewardSkus(Collection<Long> productIds) {
        return remoteReference.loadProductRewardSkus(productIds);
    }

    /**
     * @Override public Map<Long, List<RewardSku>> loadAllProductSkus() {
     * return remoteReference.loadAllProductSkus();
     * }
     */
    @Override
    public List<Long> getHasInventoryProducts() {
        return remoteReference.getHasInventoryProducts();
    }

    @Override
    public Map<Long, RewardWishOrder> loadRewardWishOrders(Collection<Long> orderIds) {
        return remoteReference.loadRewardWishOrders(orderIds);
    }

    @Override
    public Map<Long, List<RewardWishOrder>> loadUserRewardWishOrders(Collection<Long> userIds) {
        return remoteReference.loadUserRewardWishOrders(userIds);
    }

    @Override
    public Map<Long, List<RewardCouponDetail>> loadProductRewardCouponDetails(Collection<Long> productIds) {
        return remoteReference.loadProductRewardCouponDetails(productIds);
    }

    @Override
    public Map<Long, List<RewardCouponDetail>> loadUserRewardCouponDetails(Collection<Long> userIds) {
        return remoteReference.loadUserRewardCouponDetails(userIds);
    }

    @Override
    public Map<Long, List<RewardMoonLightBoxHistory>> loadMoonLightBoxHistoryByUserIds(Collection<Long> userIds) {
        return remoteReference.loadMoonLightBoxHistoryByUserIds(userIds);
    }

    @Override
    public RewardLogistics loadRewardLogistics(Long id) {
        return remoteReference.loadRewardLogistics(id);
    }

    @Override
    public List<RewardLogistics> loadSchoolRewardLogistics(Long schoolId, RewardLogistics.Type type) {
        return remoteReference.loadSchoolRewardLogistics(schoolId, type);
    }

    @Override
    public List<RewardProductCategoryRef> findRewardProductCategoryRefsByCategoryId(Long categoryId) {
        if (categoryId == null) return Collections.emptyList();
        return remoteReference.findRewardProductCategoryRefsByCategoryId(categoryId);
    }

    @Override
    public List<RewardCategory> findRewardProductCategoriesByProductId(Long productId) {
        if (productId == null)
            return Collections.emptyList();

        return remoteReference.findRewardProductCategoriesByProductId(productId);
    }

    public List<String> findCategoryCode(Long productId) {
        return findRewardProductCategoriesByProductId(productId)
                .stream()
                .filter(Objects::nonNull)
                .map(c -> c.getCategoryCode())
                .collect(Collectors.toList());
    }

    @Override
    public List<RewardProductTagRef> findRewardProductTagRefsByTagId(Long tagId) {
        if (tagId == null) return Collections.emptyList();
        return remoteReference.findRewardProductTagRefsByTagId(tagId);
    }

    @Override
    public List<RewardSku> loadProductSku(Long productId) {
        return remoteReference.loadProductSku(productId);
    }

    // ========================================================================
    // buffer supported methods
    // ========================================================================

    @Override
    public VersionedRewardCategoryList loadVersionedRewardCategoryList(long version) {
        return remoteReference.loadVersionedRewardCategoryList(version);
    }

    @Override
    public VersionedRewardImageList loadVersionedRewardImageList(long version) {
        return remoteReference.loadVersionedRewardImageList(version);
    }

    @Override
    public VersionedRewardIndexList loadVersionedRewardIndexList(long version) {
        return remoteReference.loadVersionedRewardIndexList(version);
    }

    @Override
    public VersionedRewardProductList loadVersionedRewardProductList(long version) {
        return remoteReference.loadVersionedRewardProductList(version);
    }

    @Override
    public VersionedRewardTagList loadVersionedRewardTagList(long version) {
        return remoteReference.loadVersionedRewardTagList(version);
    }

    @Override
    public RewardActivityList loadRewardActivitiesList(long version) {
        return remoteReference.loadRewardActivitiesList(version);
    }

    @Override
    public RewardActivityBuffer getRewardActivityBuffer() {
        return rewardActivityBufferSupplier.initializeIfNecessary();
    }

    @Override
    public void resetRewardActivityBuffer() {
        rewardActivityBufferSupplier.reset();
    }

    private class ReloadRewardActivityBuffer extends MinuteTimerEventListener {
        @Override
        protected void processEvent(TimerEvent event, AlpsEventContext context) {
            if (RuntimeModeLoader.getInstance().isUnitTest()) {
                return;
            }
            RewardActivityBuffer buffer = getRewardActivityBuffer();
            long version = buffer.getVersion();
            RewardActivityList data = remoteReference.loadRewardActivitiesList(version);
            if (null != data) {
                buffer.attach(data);
                logger.info("[RewardActivityBuffer] reloaded");
            }
        }
    }

    private final ReloadRewardActivityBuffer reloadRewardActivityBuffer = new ReloadRewardActivityBuffer();

    private final LazyInitializationSupplier<JVMRewardActivityBuffer> rewardActivityBufferSupplier = new LazyInitializationSupplier<>(() -> {
        RewardActivityList data = remoteReference.loadRewardActivitiesList(-1);
        assert data != null;
        JVMRewardActivityBuffer buffer = new JVMRewardActivityBuffer();
        buffer.attach(data);
        EventBus.subscribe(reloadRewardActivityBuffer);
        logger.info("[RewardActivityBuffer] initialized");
        return buffer;
    });

    @Override
    public List<RewardActivity> loadRewardActivities() {
        return getRewardActivityBuffer().dump().getRewardActivityList();
    }

    @Override
    public List<RewardActivity> loadRewardActivitiesNoBuffer() {
        return remoteReference.loadRewardActivitiesNoBuffer();
    }

    @Override
    public RewardActivity loadRewardActivity(Long activityId) {
        return remoteReference.loadRewardActivity(activityId);
    }

    @Override
    public RewardActivity loadRewardActivityNoBuffer(Long activityId) {
        return remoteReference.loadRewardActivityNoBuffer(activityId);
    }

    public List<RewardActivityRecord> loadRecentActivityRecords(Long activityId) {
        String cacheKey = generateCacheKey("RewardActivityHistory",
                new String[]{"activityId"}, new Object[]{activityId});
        List<RewardActivityRecord> historyRecords = RewardCache.getRewardCache().load(cacheKey);
        if (historyRecords == null)
            return new ArrayList<>();
        else
            return historyRecords;
    }

    @Override
    public List<RewardActivityImage> loadActivityImages(Long activityId) {
        return remoteReference.loadActivityImages(activityId);
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

    public Long loadUserRecordsInDay(Long userId, Date date, Long activityId) {
        return loadUserRecordsInDay(userId, date)
                .stream()
                .filter(r -> Objects.equals(r.getActivityId(), activityId))
                .count();
    }

    /**
     * 判断用户在指定日期是否参加过活动
     *
     * @param date
     * @return
     */
    public boolean hadParticipatedActivityInDate(Long activityId, Long userId, Date date) {
        return loadUserRecordsInDay(userId, date)
                .stream()
                .collect(Collectors.groupingBy(r -> r.getActivityId()))
                .containsKey(activityId);
    }

    @Override
    public List<RewardActivityRecord> loadActivityUserRecords(@CacheParameter("USER_ID") Long userId) {
        return remoteReference.loadActivityUserRecords(userId);
    }

    @Override
    public Map<Long, Integer> loadUserCollectOrdersInClazz(Long clazzId, String categoryCode, Date startDate) {
        return remoteReference.loadUserCollectOrdersInClazz(clazzId, categoryCode, startDate);
    }

    @Override
    public Map<Integer, List<RewardProductTarget>> loadRewardTargetGroupByType(@CacheParameter("TARGET_PID") Long productId) {
        return remoteReference.loadRewardTargetGroupByType(productId);
    }

    /**
     * 此方法暂时没有调用栈, 如果需要调用请走 NewRewardBufferLoaderClient
     */
    @Override
    public Map<Long, List<RewardProductTarget>> loadAllProductTargets() {
        return remoteReference.loadAllProductTargets();
    }

    @Override
    public List<RewardProduct> loadRewardPrivilegeProduct() {
        return remoteReference.loadRewardPrivilegeProduct();
    }

    @Override
    public void reloadRewardActivityBuffer() {
        remoteReference.reloadRewardActivityBuffer();
    }

    @Override
    public List<RewardProductTagRef> findRewardProductTagRefsByProductId(Long productId) {
        return remoteReference.findRewardProductTagRefsByProductId(productId);
    }

    @Override
    public List<RewardProductTagRef> findRewardProductTagRefsByProductIdList(List<Long> productIdList) {
        return remoteReference.findRewardProductTagRefsByProductIdList(productIdList);
    }

    /**
     * 获得用户公益活动的汇总记录，每个活动取汇总的捐赠学豆数，参加次数，以及最近一次捐赠的时间
     *
     * @param userId
     * @return
     */
    public List<RewardActivityRecord> loadUserCollectRecords(Long userId) {
        Map<Long, List<RewardActivityRecord>> activityRecords = loadActivityUserRecords(userId)
                .stream()
                .collect(Collectors.groupingBy(r -> r.getActivityId()));

        List<RewardActivityRecord> result = new ArrayList<>();
        activityRecords.forEach((k, v) -> {
            // 将活动下面的记录做汇总
            RewardActivityRecord entry = new RewardActivityRecord();
            entry.setActivityId(k);
            entry.setCollectNums(v.size());
            // 汇总
            entry.setPrice(v.stream().mapToDouble(r -> r.getPrice()).sum());
            entry.setUserId(v.get(0).getUserId());
            entry.setUserName(v.get(0).getUserName());

            RewardActivityRecord latestRecord = v.stream()
                    .max(Comparator.comparing(AbstractDatabaseEntity::getCreateDatetime))
                    .orElse(null);
            if (latestRecord != null) {
                entry.setCreateDatetime(latestRecord.getCreateDatetime());
            }

            result.add(entry);
        });

        return result;
    }

    /**
     * 根据虚拟奖品的id，反查奖品记录
     *
     * @return
     */
    public RewardProduct loadRewardProductByVirtualItemId(String itemId) {
        return loadRewardProductMap()
                .values()
                .stream()
                // 这个地方不判断上线状态，虚拟类的校验到详情页的时候做
                //.filter(RewardProduct::getOnlined)
                .filter(p -> Objects.equals(p.getRelateVirtualItemId(), itemId))
                .findAny()
                .orElse(null);
    }

    /**
     * 获得用户的兑换券
     *
     * @param userId
     * @param productId
     * @return
     */
    public RewardCouponDetail loadRewardCouponDetail(Long userId, Long productId) {
        return getRewardCouponDetailLoader()
                .loadUserRewardCouponDetails(userId)
                .stream()
                .filter(c -> Objects.equals(c.getProductId(), productId))
                .findFirst()
                .orElse(null);
    }

    /**
     * 获得用户的愿望清单(收藏)的商品列表
     * @param user
     * @return
     */
    public List<Map<String, Object>> getWishDetails(User user) {
        if(user == null)
            return Collections.emptyList();

        ExternalLoader<Long, List<Map<String, Object>>> wishDetailLoader = missedUsrIds -> {
            Long uid = missedUsrIds.stream().findFirst().orElse(null);
            if(uid == null) return null;

            List<RewardWishOrder> wishOrders = getRewardWishOrderLoader().loadUserRewardWishOrders(uid);
            List<Map<String, Object>> data = new ArrayList<>();

            String categoryCode;
            RewardCategory.SubCategory category;
            for (RewardWishOrder wishOrder : wishOrders) {
                RewardProductDetail detail = newRewardLoaderClient.generateRewardProductDetail(user, wishOrder.getProductId());
                Map<String, Object> wishMap = new HashMap<>();
                if (detail != null) {
                    detail.setOneLevelCategoryType(newRewardLoaderClient.getOneLevelCategoryType(detail.getOneLevelCategoryId()));
                    wishMap.put("price", detail.getDiscountPrice());
                    wishMap.put("productName", detail.getProductName());
                    wishMap.put("unit", detail.getUnit());
                    wishMap.put("addTime", wishOrder.getCreateDatetime());
                    wishMap.put("online", detail.getOnline());
                    wishMap.put("wishOrderId", wishOrder.getId());
                    wishMap.put("image", detail.getImage());
                    wishMap.put("oneLevelCategoryType", detail.getOneLevelCategoryType());
                    wishMap.put("productId", detail.getId());
                    wishMap.put("skus", loadProductSku(detail.getId()));

                    categoryCode = findRewardProductCategoriesByProductId(detail.getId()).stream()
                            .map(rpc -> rpc.getCategoryCode())
                            .findFirst()
                            .orElse(null);

                    category = RewardCategory.SubCategory.parseByCode(categoryCode);
                    wishMap.put("isCouponType", category.belongCoupon());
                    wishMap.put("needMobileVerify", category.isNeedVerifyMobile());

                    data.add(wishMap);
                }
            }

            // 按添加时间倒序排列
            data.sort((r1, r2) -> {
                Date addTime1 = (Date) r1.get("addTime");
                Date addTime2 = (Date) r2.get("addTime");

                return addTime2.compareTo(addTime1);
            });

            Map<Long,List<Map<String,Object>>> result = new HashMap<>();
            result.put(uid,data);

            return result;
        };

        return RewardCache.getRewardCache()
                .<Long,List<Map<String,Object>>>createCacheValueLoader()
                .keys(Collections.singletonList(user.getId()))
                .keyGenerator(uId -> generateCacheKey("REWARD_USER_WISH_ORDER_LIST", null, new Object[]{uId}))
                .loads()
                .externalLoader(wishDetailLoader)
                .loadsMissed()
                .expiration(DateUtils.getCurrentToDayEndSecond())
                .writeAsList()
                .getAndResortResult()
                .get(user.getId());
    }

    // 6月1日 - 8月10日 毕业班不允许兑换
    @Override
    public Boolean isGraduate(StudentDetail studentDetail) {
        return remoteReference.isGraduate(studentDetail);
    }

    public Map<Long, RewardProduct> loadProductByIdList(List<Long> idList) {
        Map<Long, RewardProduct> result = new HashMap<>();
        Map<Long, RewardProduct> map = loadRewardProductMap();
        for (Long productId : idList) {
            if (map.containsKey(productId)) {
                result.put(productId, map.get(productId));
            }
        }
        return result;
    }

}
