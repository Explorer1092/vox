/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
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

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.*;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.math.NumberUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.cache.CacheObject;
import com.voxlearning.utopia.api.constant.CouponProductionName;
import com.voxlearning.utopia.api.exception.NoEnoughBalanceException;
import com.voxlearning.utopia.api.legacy.MemcachedKeyConstants;
import com.voxlearning.utopia.service.integral.api.constants.IntegralType;
import com.voxlearning.utopia.service.integral.api.entities.IntegralHistory;
import com.voxlearning.utopia.service.integral.api.mapper.UserIntegral;
import com.voxlearning.utopia.service.integral.client.IntegralLoaderClient;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.privilege.client.PrivilegeLoaderClient;
import com.voxlearning.utopia.service.reward.api.RewardService;
import com.voxlearning.utopia.service.reward.api.enums.RewardTagEnum;
import com.voxlearning.utopia.service.reward.api.mapper.newversion.teacher.entity.TeacherCouponEntity;
import com.voxlearning.utopia.service.reward.cache.RewardCache;
import com.voxlearning.utopia.service.reward.constant.*;
import com.voxlearning.utopia.service.reward.entity.*;
import com.voxlearning.utopia.service.reward.impl.CouponService;
import com.voxlearning.utopia.service.reward.impl.RewardManagementImpl;
import com.voxlearning.utopia.service.reward.impl.dao.*;
import com.voxlearning.utopia.service.reward.impl.internal.InternalRewardCenterService;
import com.voxlearning.utopia.service.reward.impl.internal.InternalRewardTipService;
import com.voxlearning.utopia.service.reward.impl.internal.RewardHelpers;
import com.voxlearning.utopia.service.reward.impl.loader.RewardLoaderImpl;
import com.voxlearning.utopia.service.reward.impl.persistence.RewardCompleteOrderPersistence;
import com.voxlearning.utopia.service.reward.impl.service.newversion.NewRewardLoaderImpl;
import com.voxlearning.utopia.service.reward.impl.service.newversion.NewRewardServiceImpl;
import com.voxlearning.utopia.service.reward.impl.version.RewardProductVersion;
import com.voxlearning.utopia.service.reward.mapper.RewardProductDetail;
import com.voxlearning.utopia.service.sms.api.constant.SmsType;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import com.voxlearning.utopia.service.user.api.UserIntegralService;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.ResearchStaffDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.consumer.*;
import org.jsoup.helper.Validate;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.reward.entity.RewardCategory.SubCategory;


/**
 * Default {@link RewardService} implementation.
 *
 * @author Xiaopeng Yang
 * @author Xiaohai Zhang
 * @since Dec 2, 2014
 */
@Named
@ExposeService(interfaceClass = RewardService.class)
public class RewardServiceImpl extends SpringContainerSupport implements RewardService {

    @Inject private CouponService couponService;
    @Inject private ResearchStaffLoaderClient researchStaffLoaderClient;
    @Inject private RewardCompleteOrderPersistence rewardCompleteOrderPersistence;
    @Inject private RewardHelpers rewardHelpers;
    @Inject private RewardMoonLightBoxHistoryPersistence rewardMoonLightBoxHistoryPersistence;
    @Inject private RewardOrderPersistence rewardOrderPersistence;
    @Inject private RewardProductDao rewardProductDao;
    @Inject private RewardProductVersion rewardProductVersion;
    @Inject private RewardSkuDao rewardSkuDao;
    @Inject private RewardWishOrderPersistence rewardWishOrderPersistence;
    @Inject private StudentLoaderClient studentLoaderClient;
    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private UserLoaderClient userLoaderClient;
    @Inject private RewardLoaderImpl rewardLoader;
    @Inject private RewardActivityRecordDao rewardActivityRecordDao;
    @Inject private RewardActivityDao rewardActivityDao;
    @Inject private DeprecatedClazzLoaderClient deprecatedClazzLoaderClient;
    @Inject private RewardProductTargetDao rewardProductTargetDao;
    @Inject private RewardManagementImpl rewardManagement;
    @Inject private RewardCouponDao rewardCouponDao;

    @Inject private IntegralLoaderClient integralLoaderClient;
    @Inject private MessageCommandServiceClient messageCommandServiceClient;
    @Inject private PrivilegeLoaderClient privilegeLoaderClient;
    @Inject private UserOrderLoaderClient userOrderLoaderClient;
    @Inject private SmsServiceClient smsServiceClient;
    @Inject private SensitiveUserDataServiceClient sensitiveUserDataServiceClient;

    @Inject private InternalRewardTipService internalRewardTipService;
    @Inject private DebrisServiceImpl debrisService;
    @Inject private InternalRewardCenterService internalRewardCenterService;
    @Inject private NewRewardLoaderImpl newRewardLoader;
    @Inject private NewRewardServiceImpl newRewardService;

    @ImportService(interfaceClass = UserIntegralService.class) private UserIntegralService userIntegralService;

    // 公益活动需要参加三个公益活动才可领取
    private static final Set<Long> PUBLIC_GOOD_TAGS = new HashSet<>();

    static {
        PUBLIC_GOOD_TAGS.add(RewardTagEnum.公益专区.getId());
        PUBLIC_GOOD_TAGS.add(RewardTagEnum.托比公益.getId());
    }

    /**
     * 如果wishOrder不为空，说明是从愿望盒兑换的
     */
    @Override
    public MapMessage createRewardOrder(final User user,
                                        final RewardProductDetail productDetail,
                                        final RewardSku sku,
                                        final int quantity,
                                        final RewardWishOrder wishOrder,
                                        RewardOrder.Source source) {
        return createRewardOrder(user, productDetail, sku, quantity, wishOrder, source, null);
    }

    @Override
    public MapMessage createRewardOrder(User user, RewardProductDetail productDetail, RewardSku sku, int quantity, RewardWishOrder wishOrder, RewardOrder.Source source, TeacherCouponEntity coupon) {

        if (user == null || productDetail == null || sku == null) {
            return MapMessage.errorMessage();
        }

        List<Long> productTagSet = rewardLoader.findRewardProductTagRefsByProductId(productDetail.getId()).stream().map(RewardProductTagRef::getTagId).collect(Collectors.toList());
        if (user.isStudent() && CollectionUtils.containsAny(productTagSet, PUBLIC_GOOD_TAGS)) {
            StudentDetail studentDetail = (StudentDetail)user;
            if (studentDetail.isPrimaryStudent()) {//只限制小学
                Long donationCount = internalRewardCenterService.getDonationCount(user.getId());
                if (donationCount < 5) {
                    return MapMessage.errorMessage("至少参加过五次公益活动才可兑换！");
                }
            }
        }

        try {
            // 这里先修改库存 防止并发的情况
            if (rewardSkuDao.decreaseInventorySellable(sku.getId(), quantity) == 0) {
                logger.warn("Failed to descrease reward sku {} inventory, rollback", sku.getId());
                return MapMessage.errorMessage("奖品数量不足！");
            }

            // 如果是虚拟头饰，校验不能重复购买
            MapMessage validateResult = validateTopknot(user, productDetail);
            if (!validateResult.isSuccess()) {
                return validateResult;
            }

            int cost = getDiscountPrice(quantity, productDetail, coupon);
            int totalCost = cost;
            if (user.fetchUserType() == UserType.TEACHER) {
                TeacherDetail teacher = (TeacherDetail) user;
                if (teacher.isPrimarySchool()) {
                    cost = cost * 10;
                }
            }
            if (user.fetchUserType() == UserType.RESEARCH_STAFF) {
                cost = cost * 10;
            }

            String comment = "兑换奖品";
            if (user.isStudent()) {
                comment = RewardConstants.STUSENT_REWARD_NAME + comment;
            } else {
                comment = RewardConstants.TEACHER_REWARD_NAME + comment;
            }
            if (productDetail.getSpendType() != null && productDetail.getSpendType() == RewardProduct.SpendType.FRAGMENT.intValue()) {
                MapMessage result = debrisService.changeDebris(user.getId(), DebrisType.TOBY.getType(), -cost *1L, comment);
                if (!result.isSuccess()) {
                    throw new NoEnoughBalanceException();
                }
            } else {
                // 赠品来源的不扣学豆  |  ??? 这注释过期了吧
                // 区分学豆类型,方便后期针对不同类型累加能量值
                IntegralType integralType = newRewardService.fetchIntegralByExchangProduct(productDetail.getOneLevelCategoryId());

                IntegralHistory integralHistory = new IntegralHistory(user.getId(), integralType, -cost);
                integralHistory.setComment(newRewardService.fetchRewardIntegralComment(integralHistory.toIntegralType()));

                if (!userIntegralService.changeIntegral(integralHistory).isSuccess()) {
                    throw new NoEnoughBalanceException();
                }
            }

            RewardOrder order = new RewardOrder();
            order.setSkuName(sku.getSkuName());
            order.setSkuId(sku.getId());
            order.setCode("");//订单号暂时没用， 先留着
            order.setProductId(productDetail.getId());
            order.setProductName(productDetail.getProductName());
            order.setSaleGroup(productDetail.getSaleGroup());
            order.setUnit(productDetail.getUnit());
            order.setTotalPrice(SafeConverter.toDouble(totalCost)); // 取四舍五入后的值
            order.setBuyerId(user.getId());
            order.setBuyerType(user.getUserType());
            order.setBuyerName(user.fetchRealname());
            order.setPrice(productDetail.getDiscountPrice());
            order.setQuantity(quantity);
            order.setProductType(productDetail.getProductType());

            // 设置班级ID
            Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(user.getId());
            if (user.getUserType() == UserType.STUDENT.getType()
                    && clazz != null) {
                order.setClazzId(clazz.getId());
            }

            // 设置子类别编码字段
            RewardCategory category = rewardLoader.findRewardProductCategoriesByProductId(productDetail.getId())
                    .stream()
                    .findFirst()
                    .orElse(null);
            if (category != null) {
                order.setProductCategory(category.getCategoryCode());
            }

            // 如果是体验类的商品，没有发货流程，直接是结束状态
            if (Objects.equals(productDetail.getProductType(), RewardProductType.JPZX_TIYAN.name())) {
                order.setStatus(RewardOrderStatus.DELIVER.name());
            } else
                order.setStatus(RewardOrderStatus.SUBMIT.name());

            order.setDiscount(this.getDiscount(productDetail, coupon));
            order.setSource(source);
            order.setSpendType(productDetail.getSpendType());
            rewardOrderPersistence.insert(order);
            // 修改产品卖出量
            if (rewardProductDao.increaseSoldQuantity(productDetail.getId(), quantity) == 0) {
                logger.warn("Failed to increase reward product {} sold quantity, rollback", productDetail.getId());
                throw new RuntimeException();
            }
            rewardProductVersion.increase();

            Collection<String> keys = new LinkedHashSet<>();
            keys.add(CacheKeyGenerator.generateCacheKey(MemcachedKeyConstants.REWARD_USER_ORDER_COUNT, null, new Object[]{user.getId()}));
            if (wishOrder != null) {
                if (rewardWishOrderPersistence.achievedWishOrderById(wishOrder.getId()) > 0) {
                    keys.add(CacheKeyGenerator.generateCacheKey(MemcachedKeyConstants.REWARD_USER_WISH_ORDER, null, new Object[]{user.getId()}));
                }
            }
            RewardCache.getRewardCache().delete(keys);
            return MapMessage.successMessage().add("orderId",order.getId());
        } catch (Exception ex) {
            String errorMessage;
            if (ex instanceof NoEnoughBalanceException) {
                errorMessage = "Failed to create reward order (userId={},productId={},skuId={},quantity={}): no enough balance";
            } else {
                errorMessage = "Failed to create reward order (userId={},productId={},skuId={},quantity={})";
            }
            logger.error(errorMessage, user.getId(), productDetail.getId(), sku.getId(), quantity);
            return MapMessage.errorMessage("兑换失败，请重试");
        }
    }

    private Integer getDiscountPrice(int quantity, RewardProductDetail productDetail, TeacherCouponEntity coupon) {
        Double price = coupon==null || coupon.getDiscount()==null ? productDetail.getDiscountPrice():productDetail.getDiscountPrice() * coupon.getDiscount();
        BigDecimal total = new BigDecimal(price).multiply(new BigDecimal(quantity));

        //托比装扮两件9折
        if (Objects.equals(quantity, 2) && newRewardLoader.isTobyWear(productDetail.getOneLevelCategoryId())) {
            total = total.multiply(new BigDecimal(0.9));
        }
        return total.setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
    }

    private Double getDiscount(RewardProductDetail productDetail, TeacherCouponEntity coupon) {
        Double result = null;
        if (productDetail.getDiscount()!=null && coupon!=null && coupon.getDiscount()!=null) {
            result = productDetail.getDiscount() * coupon.getDiscount();
        } else if (productDetail.getDiscount()!=null) {
            result = productDetail.getDiscount();
        } else if (coupon!=null && coupon.getDiscount()!=null) {
            result = coupon.getDiscount();
        }
        return result;
    }

    /**
     * 如果是虚拟头饰，校验不能重复购买
     * @param user
     * @param productDetail
     * @return
     */
    private MapMessage validateTopknot(final User user,
                                       final RewardProductDetail productDetail) {
        if (Objects.equals(productDetail.getProductType(), RewardProductType.JPZX_TIYAN.name())) {
            List<String> categoryCodes = rewardLoader.loadRewardCategories(RewardProductType.JPZX_TIYAN,user.fetchUserType())
                    .stream()
                    .map(r -> r.getCategoryCode())
                    .collect(Collectors.toList());

            if (categoryCodes.contains(SubCategory.HEAD_WEAR.name())) {
                // 取现在拥有的在有效期以内的头饰
                if (privilegeLoaderClient.existValidPrivilege(user.getId(), productDetail.getRelateVirtualItemId())) {
                    // logger.warn("Failed to create order,the headdress already exists!");
                    return MapMessage.errorMessage("不能重复购买头饰!");
                }
            } else if (categoryCodes.contains(SubCategory.MINI_COURSE.name()) ||
                    categoryCodes.contains(SubCategory.CHOICEST_ARTICLE.name())) {
                // 微课和精品文章分类的话，查询历史的订单记录
                Map<Long, List<RewardOrder>> historyOrderMap = rewardLoader.loadUserRewardOrders(Collections.singleton(user.getId()));
                if (historyOrderMap != null) {

                    List<RewardOrder> historyOrders = historyOrderMap.get(user.getId());
                    if (CollectionUtils.isNotEmpty(historyOrders)) {
                        // 买过的不能重复购买
                        RewardOrder boughtOrder = historyOrders
                                .stream()
                                .filter(o -> Objects.equals(o.getProductId(), productDetail.getId()))
                                .filter(o -> Objects.equals(o.getStatus(), RewardOrderStatus.DELIVER.name()))
                                .findAny()
                                .orElse(null);
                        if (boughtOrder != null) {
                            return MapMessage.errorMessage("不能重复购买课程!");
                        }
                    }
                }
            }
        }
        return MapMessage.successMessage();
    }

    /**
     * 上面创建订单方法，包含了积分扣除等逻辑，能量柱的订单不包含，
     * 所以独立出一个方法
     * @return
     */
    public MapMessage createLagacyOrder(final User user,
                                       final RewardProduct rewardProduct,
                                       final RewardSku sku,
                                       final int quantity,
                                       RewardOrder.Source source) {

        if (user == null || rewardProduct == null) {
            return MapMessage.errorMessage();
        }

        try {
            // 这里先修改库存 防止并发的情况
            if (sku != null) {
                int rows = rewardSkuDao.decreaseInventorySellable(sku.getId(), quantity);
                if (rows == 0) {
                    logger.warn("Failed to descrease reward sku {} inventory, rollback", sku.getId());
                    return MapMessage.errorMessage("奖品数量不足！");
                }
            }

            final BigDecimal total = new BigDecimal(rewardProduct.fetchProductPrice(user)).multiply(new BigDecimal(quantity));
            int cost = total.setScale(0, BigDecimal.ROUND_HALF_UP).intValue();

            RewardOrder order = new RewardOrder();
            if (sku != null) {
                order.setSkuName(sku.getSkuName());
                order.setSkuId(sku.getId());
            } else {
                order.setSkuId(0L);
                order.setSkuName("");
            }
            order.setCode("");//订单号暂时没用， 先留着
            order.setProductId(rewardProduct.getId());
            order.setProductName(rewardProduct.getProductName());
            order.setSaleGroup(rewardProduct.getSaleGroup());
            order.setBuyerId(user.getId());
            order.setBuyerType(user.getUserType());
            order.setBuyerName(user.fetchRealname());

            order.setPrice(rewardProduct.fetchProductPrice(user));
            order.setQuantity(quantity);
            order.setUnit(fetchOrderUnit(user).name());
            if (Objects.equals(source.name(), RewardOrder.Source.power_pillar.name())) {
                order.setTotalPrice(5d); // 能量柱来源默认5学豆
            }

            order.setProductType(String.valueOf(newRewardLoader.getOneLevelCategoryType(rewardProduct.getOneLevelCategoryId())));
            order.setProductCategory(String.valueOf(rewardProduct.getOneLevelCategoryId()));

            // 设置班级ID
            if (user.isStudent()) {
                Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(user.getId());
                if (clazz != null) {
                    order.setClazzId(clazz.getId());
                }
            }

            // 如果是体验类的商品，没有发货流程，直接是结束状态
            if (rewardProduct.isShiwu()) {
                order.setStatus(RewardOrderStatus.SUBMIT.name());
            } else {
                order.setStatus(RewardOrderStatus.DELIVER.name());
            }

            order.setDiscount(1d); // 折扣，目前没有
            order.setSource(source);
            rewardOrderPersistence.insert(order);

            // 修改产品卖出量
            if (rewardProductDao.increaseSoldQuantity(rewardProduct.getId(), quantity) == 0) {
                logger.warn("Failed to increase reward product {} sold quantity, rollback", rewardProduct.getId());
            }
            rewardProductVersion.increase();

            Collection<String> keys = new LinkedHashSet<>();
            keys.add(CacheKeyGenerator.generateCacheKey(MemcachedKeyConstants.REWARD_USER_ORDER_COUNT, null, new Object[]{user.getId()}));
            RewardCache.getRewardCache().delete(keys);
            return MapMessage.successMessage().add("orderId",order.getId());
        } catch (Exception ex) {
            logger.error("createLagacyOrder failed, user:{}, product:{}", user.getId(), rewardProduct.getId(), ex);
            return MapMessage.errorMessage("兑换失败，请重试");
        }
    }

    @Override
    public MapMessage updateRewardOrder(final User user, final RewardOrder order, final RewardProduct product, final RewardSku sku, final int quantity) {
        if (user == null) {
            return MapMessage.errorMessage();
        }
        if (order == null) {
            return MapMessage.errorMessage("订单不存在");
        }
        if (product == null) {
            return MapMessage.errorMessage("对不起！因厂商原因，你兑换的奖品已经下架，请去挑选其他奖品哦！");
        }
        final int cha = quantity - (order.getQuantity() == null ? 0 : order.getQuantity());
        final double totalPrice = quantity * order.getPrice();
        if (cha > 0) {
            if (sku == null) {
                return MapMessage.errorMessage("单品不存在");
            }
            if (sku.getInventorySellable() < cha) {
                return MapMessage.errorMessage("奖品数量不足！");
            }
            switch (user.fetchUserType()) {
                case TEACHER: {
                    if (!(user instanceof TeacherDetail)) {
                        throw new IllegalArgumentException("User must be TeacherDetail");
                    }
                    TeacherDetail teacher = (TeacherDetail) user;
                    // 判断余额
                    UserIntegral integral = teacher.getUserIntegral();
                    if (integral == null || integral.getUsable() < cha * order.getPrice()) {
                        String unit = teacher.isPrimarySchool() ? "园丁豆" : "学豆";
                        return MapMessage.errorMessage("你的" + unit + "数量不足，请检查一下价格和数量吧！");
                    }
                    break;
                }
                case STUDENT: {
                    if (!(user instanceof StudentDetail)) {
                        throw new IllegalArgumentException("User must be StudentDetail");
                    }
                    // 判断余额
                    UserIntegral integral = ((StudentDetail) user).getUserIntegral();
                    if (integral == null || integral.getUsable() < cha * order.getPrice()) {
                        return MapMessage.errorMessage("你的学豆数量不足，请检查一下价格和数量吧！");
                    }
                    break;
                }
                case RESEARCH_STAFF: {
                    if (!(user instanceof ResearchStaffDetail)) {
                        throw new IllegalArgumentException("User must be ResearchStaffDetail");
                    }
                    // 判断余额
                    UserIntegral integral = ((ResearchStaffDetail) user).getUserIntegral();
                    if (integral == null || integral.getUsable() < cha * order.getPrice()) {
                        return MapMessage.errorMessage("你的园丁豆数量不足，请检查一下价格和数量吧！");
                    }
                    break;
                }
                default: {
                    return MapMessage.errorMessage("角色错误");
                }
            }
        }
        try {
            // 这里先修改库存 防止并发的情况
            if (rewardSkuDao.decreaseInventorySellable(sku.getId(), cha) == 0) {
                logger.warn("Failed to descrease reward sku {} inventory, rollback", sku.getId());
                return MapMessage.errorMessage("奖品数量不足！");
            }

            int amount = (int) (cha * order.getPrice());
            if (order.getUnit().equals(RewardProductPriceUnit.园丁豆.name())) {
                amount = (int) (cha * order.getPrice() * 10);
            }
            IntegralHistory integralHistory = new IntegralHistory(user.getId(), newRewardService.fetchIntegralByExchangProduct(product.getOneLevelCategoryId()), -amount);
            integralHistory.setComment("奖品中心修改兑换奖品数量," + newRewardService.fetchRewardIntegralComment(integralHistory.toIntegralType()));
            if (!userIntegralService.changeIntegral(integralHistory).isSuccess()) {
                throw new NoEnoughBalanceException();
            }

            order.setTotalPrice(totalPrice);
            order.setQuantity(quantity);
            order.setUpdateDatetime(new Date());
            rewardOrderPersistence.replace(order);
            // 修改已兑换数量
            rewardProductDao.increaseSoldQuantity(product.getId(), cha);
            rewardProductVersion.increase();
            return MapMessage.successMessage();
        } catch (Exception ex) {
            String errorMessage;
            if (ex instanceof NoEnoughBalanceException) {
                errorMessage = "Failed to update reward order quantity, no enough balance, uid {}";
            } else {
                errorMessage = "Failed to update reward order, uid {}";
            }
            logger.error(errorMessage, user.getId());
            return MapMessage.errorMessage("修改失败");
        }
    }

    @Override
    public MapMessage deleteRewardOrder(final User user, final RewardOrder order) {
        if (user == null || order == null) {
            return MapMessage.errorMessage();
        }
        if (!Objects.equals(user.getId(), order.getBuyerId())) {
            return MapMessage.errorMessage("订单与用户不一致");
        }
        try {
            int amount = order.getTotalPrice().intValue();
            if (order.getUnit().equals(RewardProductPriceUnit.园丁豆.name())) {
                amount = order.getTotalPrice().intValue() * 10;
            }
            IntegralType integralType = newRewardService.fetchIntegralByCancelProduct(NumberUtils.toLong(order.getProductCategory()));
            if (Objects.equals(integralType, integralType.UNKNOWN)) {// fixme 为了兼容老的ProductCategory，当前商品全发货之后可去掉（18.11.27）
                integralType = IntegralType.奖品相关;
            }
            IntegralHistory integralHistory = new IntegralHistory(order.getBuyerId(), integralType, amount);
            integralHistory.setComment(newRewardService.fetchRewardIntegralComment(integralHistory.toIntegralType()));

            if (!userIntegralService.changeIntegral(integralHistory).isSuccess()) {
                throw new RuntimeException();
            }

            //修改库存
            int quantity = order.getQuantity() == null ? 0 : order.getQuantity();
            if (quantity > 0) {
                rewardSkuDao.increaseInventorySellable(order.getSkuId(), order.getQuantity());
            }
            //删除订单
            rewardOrderPersistence.removeOrder(order.getId());
            //清缓存
            String key = CacheKeyGenerator.generateCacheKey(MemcachedKeyConstants.REWARD_USER_ORDER_COUNT, null, new Object[]{user.getId()});
            RewardCache.getRewardCache().delete(key);
            return MapMessage.successMessage("删除成功");
        } catch (Exception ex) {
            logger.error("Failed to delete reward order (user={},order={})", user.getId(), order.getId(), ex);
            return MapMessage.errorMessage("删除失败");
        }
    }

    @Override
    public MapMessage createRewardWishOrder(final User user, final RewardProductDetail productDetail) {
        if (user == null || productDetail == null) {
            return MapMessage.errorMessage();
        }
        try {
            List<RewardWishOrder> wishOrders = rewardHelpers.getRewardWishOrderLoader().loadUserRewardWishOrders(user.getId());
            // 如果已经存在了，不能重复加入
            boolean exist = wishOrders.stream().anyMatch(o -> Objects.equals(productDetail.getId(),o.getProductId()));
            if(exist){
                return MapMessage.errorMessage("愿望盒中已经存在该商品，不能重复加入!");
            }

            RewardWishOrder wishOrder = RewardWishOrder.newInstance(productDetail.getId(), user.getId());
            wishOrder.setProductName(productDetail.getProductName());
            rewardWishOrderPersistence.insert(wishOrder);

            //修改奖品加入愿望盒数量
            rewardProductDao.increaseWishQuantity(productDetail.getId(), 1);
            rewardProductVersion.increase();
            String key = CacheKeyGenerator.generateCacheKey("REWARD_USER_WISH_ORDER_LIST", null, new Object[]{user.getId()});
            RewardCache.getRewardCache().delete(key);
            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error("Failed to create reward wish order (user={},product={})",
                    user.getId(), productDetail.getId(), ex);
            return MapMessage.errorMessage("添加愿望盒失败");
        }
    }

    @Override
    public MapMessage deleteRewardWishOrder(final Long userId, final Long wishOrderId) {
        if (userId == null || wishOrderId == null) {
            return MapMessage.errorMessage();
        }
        try {
            int rows = rewardWishOrderPersistence.deleteWishOrderById(wishOrderId, userId);
            if (rows > 0) {
                String key = CacheKeyGenerator.generateCacheKey("REWARD_USER_WISH_ORDER_LIST", null, new Object[]{userId});
                RewardCache.getRewardCache().delete(key);
            }
            return MapMessage.successMessage();
        } catch (Exception ex) {
            logger.error("Failed to delete reward wish order");
            return MapMessage.errorMessage();
        }
    }

    // 批量修改用户订单状态
    @Override
    public MapMessage batchUpdateUserOrder(String[] userIds, String reason, RewardOrderStatus orderStatus) {
        List<Map<String, Object>> dealData = new ArrayList<>();
        for (String id : userIds) {
            Map<String, Object> dataMap = new HashMap<>();
            String realId = id.replaceAll(" ", "");
            try {
                if (realId != null && realId.length() > 0) {
                    Long userId = Long.parseLong(id);
                    // 修改订单状态  返回积分
                    List<RewardOrder> userOrderList = rewardHelpers.getRewardOrderLoader().loadUserRewardOrders(userId);
                    if (CollectionUtils.isEmpty(userOrderList)) {
                        dataMap.put("userId", realId);
                        dataMap.put("info", "用户订单不存在");
                        dealData.add(dataMap);
                        continue;
                    }
                    // 过滤出上个发货段的订单
                    int month = MonthRange.current().getMonth();
                    Date startDate = MonthRange.current().previous().getStartDate();
                    Date endDate = MonthRange.current().previous().getEndDate();
                    if (month == 3) {
                        startDate = MonthRange.current().previous().previous().previous().getStartDate();//去年12月1号开始的订单
                    }
                    if (month == 9) {
                        startDate = MonthRange.current().previous().previous().previous().getStartDate();//6月1号开始的订单
                    }
                    final Date finalStartDate = startDate;
                    userOrderList = userOrderList.stream()
                            .filter(o -> {
                                return Objects.equals(o.getProductType(), RewardProductType.JPZX_SHIWU.name())
                                        || Objects.equals(o.getProductType(), OneLevelCategoryType.JPZX_SHIWU.intType().toString());
                            })
                            .filter(o -> o.getCreateDatetime().after(finalStartDate) && o.getCreateDatetime().before(endDate))
                            .collect(Collectors.toList());
                    if (CollectionUtils.isEmpty(userOrderList)) {
                        dataMap.put("userId", realId);
                        dataMap.put("info", "用户订单不存在");
                        dealData.add(dataMap);
                        continue;
                    }

                    // 如果用户订单中有待审核 或者 已发货的订单 则 不允许修改
                    // 还有多判断一次商品类型，只能修改实物类别
                    List<RewardOrder> orderList = userOrderList.stream()
                            .filter(o -> RewardOrderStatus.SUBMIT.name().equals(o.getStatus()) ||
                                    RewardOrderStatus.DELIVER.name().equals(o.getStatus()))
                            .collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(orderList)) {
                        dataMap.put("userId", realId);
                        dataMap.put("info", "对不起，只能修改状态为配货中或用户信息异常状态的订单");
                        dealData.add(dataMap);
                        continue;
                    }
                    // 增加积分并取消订单
                    if (orderStatus == RewardOrderStatus.EXCEPTION) {
                        // 开始修改
                        for (RewardOrder order : userOrderList) {
                            if (order == null) {
                                continue;
                            }
                            if (RewardOrderStatus.EXCEPTION.name().equals(order.getStatus())) {
                                continue;
                            }
                            int amount = order.getTotalPrice().intValue();
                            if (order.getUnit().equals(RewardProductPriceUnit.园丁豆.name())) {
                                amount = order.getTotalPrice().intValue() * 10;
                            }
                            IntegralType integralType = newRewardService.fetchIntegralByCancelProduct(NumberUtils.toLong(order.getProductCategory()));
                            if (Objects.equals(integralType, integralType.UNKNOWN)) {// fixme 为了兼容老的ProductCategory，当前商品全发货之后可去掉（18.11.27）
                                integralType = IntegralType.兑换优惠劵;
                            }
                            IntegralHistory integralHistory = new IntegralHistory(order.getBuyerId(), integralType, amount);
                            integralHistory.setComment("奖品中心用户信息异常" + newRewardService.fetchRewardIntegralComment(integralHistory.toIntegralType()));

                            MapMessage msg = userIntegralService.changeIntegral(integralHistory);
                            if (!msg.isSuccess()) {
                                logger.warn("给用户{}补加积分失败", order.getBuyerId());
                            }

                            // 修改库存
                            int quantity = order.getQuantity() == null ? 0 : order.getQuantity();
                            if (quantity > 0) {
                                rewardSkuDao.increaseInventorySellable(order.getSkuId(), order.getQuantity());
                            }
                            // 修改订单状态
                            rewardOrderPersistence.updateOrderStatus(order.getId(), reason, RewardOrderStatus.EXCEPTION);
                            rewardCompleteOrderPersistence.updateCompleteOrderStatus(order.getCompleteId(), RewardOrderStatus.EXCEPTION);
                        }
                    } else if (orderStatus == RewardOrderStatus.PREPARE) {
                        // 改为配货中 修改订单状态 扣除积分
                        // 计算用户此次需要扣除的总积分
                        int totalAmount = 0;
                        for (RewardOrder order : userOrderList) {
                            if (order == null) {
                                continue;
                            }
                            if (RewardOrderStatus.PREPARE.name().equals(order.getStatus())) {
                                continue;
                            }

                            int amount = order.getTotalPrice().intValue();
                            totalAmount = totalAmount + amount;
                        }
                        // 判断余额
                        User user = userLoaderClient.loadUser(userId);
                        UserIntegral integral;
                        if (user.isTeacher()) {
                            integral = teacherLoaderClient.loadMainSubTeacherUserIntegral(user.getId(), null);
                        } else {
                            integral = integralLoaderClient.getIntegralLoader().loadUserIntegral(user.toSimpleUser());
                        }
                        if (integral == null || integral.getUsable() < totalAmount) {
                            dataMap.put("userId", realId);
                            dataMap.put("info", "用户积分不足");
                            dealData.add(dataMap);
                            continue;
                        }
                        // 开始修改
                        for (RewardOrder order : userOrderList) {
                            if (order == null) {
                                continue;
                            }
                            if (RewardOrderStatus.PREPARE.name().equals(order.getStatus())) {
                                continue;
                            }
                            int amount = order.getTotalPrice().intValue();
                            if (order.getUnit().equals(RewardProductPriceUnit.园丁豆.name())) {
                                amount = order.getTotalPrice().intValue() * 10;
                            }
                            IntegralHistory integralHistory = new IntegralHistory(order.getBuyerId(), newRewardService.fetchIntegralByExchangProduct(NumberUtils.toLong(order.getProductCategory())), -amount);
                            integralHistory.setComment("奖品中心订单恢复" + newRewardService.fetchRewardIntegralComment(integralHistory.toIntegralType()));

                            MapMessage msg = userIntegralService.changeIntegral(integralHistory);
                            if (!msg.isSuccess()) {
                                logger.warn("给用户{}扣除积分失败", order.getBuyerId());
                            }

                            // 修改库存
                            int quantity = order.getQuantity() == null ? 0 : order.getQuantity();
                            if (quantity > 0) {
                                rewardSkuDao.decreaseInventorySellable(order.getSkuId(), order.getQuantity());
                            }
                            // 修改订单状态
                            rewardOrderPersistence.updateOrderStatus(order.getId(), reason, RewardOrderStatus.PREPARE);
                            rewardCompleteOrderPersistence.updateCompleteOrderStatus(order.getCompleteId(), RewardOrderStatus.PREPARE);
                        }
                    }
                }
            } catch (Exception ex) {
                dataMap.put("userId", realId);
                dataMap.put("info", "处理异常");
                dealData.add(dataMap);
            }
        }
        return MapMessage.successMessage("操作成功").add("errorList", dealData);
    }

    @Override
    public MapMessage deleteRewardOrder(RewardOrder order) {
        try {
            //增加对应的学豆金币并修改订单状态
            if (order == null) {
                return MapMessage.errorMessage("参数错误");
            }
            int amount = order.getTotalPrice().intValue();
            if (order.getUnit().equals(RewardProductPriceUnit.园丁豆.name())) {
                amount = order.getTotalPrice().intValue() * 10;
            }

            IntegralType integralType = newRewardService.fetchIntegralByCancelProduct(NumberUtils.toLong(order.getProductCategory()));
            if (Objects.equals(integralType, integralType.UNKNOWN)) {// fixme 为了兼容老的ProductCategory，当前商品全发货之后可去掉（18.11.27）
                integralType = IntegralType.奖品相关;
            }
            IntegralHistory integralHistory = new IntegralHistory(order.getBuyerId(), integralType, amount);
            integralHistory.setComment("奖品订单失效，" + newRewardService.fetchRewardIntegralComment(integralHistory.toIntegralType()));

            MapMessage msg = userIntegralService.changeIntegral(integralHistory);
            if (msg.isSuccess()) {
                logger.debug("Add integral:");
                logger.debug("  integral      -> {}", amount);
                logger.debug("  integral type -> {}", IntegralType.奖品相关);
                logger.debug("  user id       -> {}", order.getBuyerId());
            } else {
                logger.warn("给用户{}补加积分失败", order.getBuyerId());
            }
            //发系统消息

            if (order.getUnit().equals(RewardProductPriceUnit.学豆.name())) {
                String content = "同学你好，因为你的老师没有填写收货地址，奖品无法寄送，订单被取消，学豆已返还到你的账号上。";
                messageCommandServiceClient.getMessageCommandService().sendUserMessage(order.getBuyerId(), content);
            } else {
                String content = "老师您好，因为您的收货地址无效，奖品无法寄送，订单被取消，园丁豆已返还到您的账号上。";
                teacherLoaderClient.sendTeacherMessage(order.getBuyerId(), content);
            }

            // 修改库存
            int quantity = order.getQuantity() == null ? 0 : order.getQuantity();
            if (quantity > 0) {
                rewardSkuDao.increaseInventorySellable(order.getSkuId(), order.getQuantity());
            }
            // 删除订单
            rewardOrderPersistence.removeOrder(order.getId());
        } catch (Exception ex) {
            logger.error("删除奖品中心订单异常，exception[{}]", ex.getMessage());
            return MapMessage.errorMessage("删除失败");
        }
        return MapMessage.successMessage("删除成功");
    }

    @Override
    public MapMessage deleteRewardOrder(Long orderId) {
        RewardOrder rewardOrder = rewardOrderPersistence.load(orderId);
        return deleteRewardOrder(rewardOrder);
    }

    @Override
    @Deprecated
    public MapMessage exchangedCoupon(User user, String mobile, CouponProductionName couponProductionName) {
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage exchangedCoupon(Long productId, User user, String mobile, TeacherCouponEntity coupon) {
        switch (user.fetchUserType()) {
            case STUDENT:
                StudentDetail studentDetail;
                if (user instanceof StudentDetail) {
                    studentDetail = (StudentDetail) user;
                } else {
                    studentDetail = studentLoaderClient.loadStudentDetail(user.getId());
                }
                return couponService.exchangedCouponForStudent(productId,studentDetail, mobile);
            case TEACHER:
                TeacherDetail teacherDetail;
                if (user instanceof TeacherDetail) {
                    teacherDetail = (TeacherDetail) user;
                } else {
                    teacherDetail = teacherLoaderClient.loadTeacherDetail(user.getId());
                }
                return couponService.exchangedCouponForTeacher(productId,teacherDetail, mobile, coupon);
            case RESEARCH_STAFF:
                ResearchStaffDetail researchStaffDetail;
                if (user instanceof ResearchStaffDetail) {
                    researchStaffDetail = (ResearchStaffDetail) user;
                } else {
                    researchStaffDetail = researchStaffLoaderClient.loadResearchStaffDetail(user.getId());
                }
                return couponService.exchangedCouponForRstaff(productId,researchStaffDetail, mobile);
            default:
                return MapMessage.errorMessage("角色错误");
        }
    }

    @Override
    public MapMessage exchangedDuibaCoupon(DuibaCoupon duibaCoupon, User user, RewardProductDetail productDetail) {
        //扣积分
        Integer deductIntegral = -duibaCoupon.getCredits().intValue();
        if (user.isTeacher()) {
            TeacherDetail teacherDetail = user instanceof TeacherDetail ? (TeacherDetail) user : teacherLoaderClient.loadTeacherDetail(user.getId());
            deductIntegral = teacherDetail.isPrimarySchool() ? deductIntegral * 10 : deductIntegral;
        }
        IntegralHistory integralHistory = new IntegralHistory(user.getId(), newRewardService.fetchIntegralByExchangProduct(productDetail.getOneLevelCategoryId()), deductIntegral);
        integralHistory.setComment(newRewardService.fetchRewardIntegralComment(integralHistory.toIntegralType()));

        if (!userIntegralService.changeIntegral(integralHistory).isSuccess()) {
            return MapMessage.errorMessage().add("status", "fail").add("errorMessage", "扣除积分失败");
        }

        //增加兑换记录
        RewardSku sku = rewardSkuDao.findByProductId(productDetail.getId()).stream().findFirst().orElse(null);
        RewardOrder order = new RewardOrder();
        order.setSkuName(sku.getSkuName());
        order.setSkuId(sku.getId());
        order.setCode(duibaCoupon.getOrderNum());
        order.setProductId(productDetail.getId());
        order.setProductName(productDetail.getProductName());
        order.setSaleGroup(productDetail.getSaleGroup());
        order.setUnit(productDetail.getUnit());
        order.setTotalPrice(Double.valueOf(duibaCoupon.getCredits()));
        order.setBuyerId(user.getId());
        order.setBuyerType(user.getUserType());
        order.setBuyerName(user.fetchRealname());
        order.setPrice(productDetail.getDiscountPrice());
        order.setQuantity(1);
        order.setProductType(String.valueOf(newRewardLoader.getOneLevelCategoryType(productDetail.getOneLevelCategoryId())));
        order.setProductCategory(String.valueOf(productDetail.getOneLevelCategoryId()));
        String url = "https://trade.m.duiba.com.cn/crecord/recordDetail?orderId=" + duibaCoupon.getOrderNum().replaceAll("[a-zA-Z]+", "") + "&dbnewopen&after=1&dpm= " + duibaCoupon.getDuibaAppId() + ".23.1.0&fromSource=1";
        Map<String, Object> extMap = new HashMap<>();
        extMap.put("couponUrl", url);
        order.setExtAttributes(JsonUtils.toJson(extMap));
        // 设置班级ID
        Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(user.getId());
        if (user.getUserType() == UserType.STUDENT.getType()
                && clazz != null) {
            order.setClazzId(clazz.getId());
        }
        order.setStatus(RewardOrderStatus.SUBMIT.name());
        order.setDiscount(productDetail.getDiscount());
        order.setSource(RewardOrder.Source.app);
        rewardOrderPersistence.insert(order);
        //发消息
        RewardCoupon coupon = rewardCouponDao.loadRewardCouponByPID(productDetail.getId());
        if (coupon.getSendSms() && StringUtils.isNotBlank(coupon.getSmsTpl())) {
            UserAuthentication userAuthentication = userLoaderClient.loadUserAuthentication(user.getId());
            if (userAuthentication != null) {
                String mobile = sensitiveUserDataServiceClient.loadUserMobile(user.getId());
                if ( MobileRule.isMobile(mobile)) {
                    SmsType type = user.isStudent() ? SmsType.EXCHANGE_COUPON_STUDENT : SmsType.EXCHANGE_COUPON_TEACHER;
                    String notifyMsg = StringUtils.isNotBlank(coupon.getSmsTpl()) ? coupon.getSmsTpl() : ("兑换兑吧优惠券:" + duibaCoupon.getName());
                    // 发短信
                    smsServiceClient.createSmsMessage(mobile)
                            .content(notifyMsg)
                            .type(type.name())
                            .send();
                }
            }
        }

        // 系统消息
        if (Boolean.TRUE.equals(coupon.getSendMsg())) {
            String systemMsg = StringUtils.isNotBlank(coupon.getSmsTpl()) ? coupon.getSmsTpl() : ("兑换兑吧优惠券:" + duibaCoupon.getName());
            messageCommandServiceClient.getMessageCommandService().sendUserMessage(user.getId(), systemMsg);
        }

        return MapMessage.successMessage().add("status", "ok").add("bizId", order.getId().toString());
    }

    @Override
    public MapMessage confirmDuibaCoupon(User user, Boolean success, Long id, String orderNum) {
        RewardOrder rewardOrder = rewardOrderPersistence.load(id);
        if (rewardOrder == null || !user.getId().equals(rewardOrder.getBuyerId()) || !orderNum.equals(rewardOrder.getCode())) {
            return MapMessage.errorMessage("订单不存在");
        }

        if (Boolean.FALSE.equals(success)) {
            int amount = rewardOrder.getTotalPrice().intValue();
            if (rewardOrder.getUnit().equals(RewardProductPriceUnit.园丁豆.name())) {
                amount = rewardOrder.getTotalPrice().intValue() * 10;
            }

            IntegralType integralType = newRewardService.fetchIntegralByCancelProduct(NumberUtils.toLong(rewardOrder.getProductCategory()));
            if (Objects.equals(integralType, integralType.UNKNOWN)) {// fixme 为了兼容老的ProductCategory，当前商品全发货之后可去掉（18.11.27）
                integralType = IntegralType.兑换优惠劵;
            }
            IntegralHistory integralHistory = new IntegralHistory(rewardOrder.getBuyerId(), integralType, amount);
            integralHistory.setComment(newRewardService.fetchRewardIntegralComment(integralHistory.toIntegralType()));

            MapMessage msg = userIntegralService.changeIntegral(integralHistory);
            if (msg.isSuccess()) {
                // 删除订单
                rewardOrderPersistence.removeOrder(rewardOrder.getId());
                switch (user.fetchUserType()) {
                    case STUDENT:
                        messageCommandServiceClient.getMessageCommandService().sendUserMessage(user.getId(), "兑换"+ rewardOrder.getProductName() + "失败，学豆已经返还");
                        break;
                    case TEACHER:
                        teacherLoaderClient.sendTeacherMessage(user.getId(), "兑换"+ rewardOrder.getProductName() + "失败，"+ rewardOrder.getUnit() + "已经返还");
                        break;
                }
            } else {
                logger.warn("给用户{}补加积分失败", user.getId());
                return MapMessage.errorMessage("补加积分失败");
            }
        } else {
            rewardOrderPersistence.updateOrderStatus(rewardOrder.getId(), "", RewardOrderStatus.DELIVER);
            rewardProductDao.increaseSoldQuantity(rewardOrder.getProductId(), 1);
            rewardProductVersion.increase();
        }
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage couponRebate(Long userId, Long couponDetailId, CouponProductionName couponProductionName) {
        return couponService.couponRebate(userId, couponDetailId, couponProductionName);
    }

    @Override
    public MapMessage openMoonLightBox(TeacherDetail teacherDetail, RewardProductDetail productDetail, RewardSku sku) {
        try {
            // 获取结果
            Map<String, Object> box = openBox(productDetail,teacherDetail);
            if (MapUtils.isEmpty(box)) {
                return MapMessage.errorMessage("开启宝箱失败");
            }
            // 中了奖品
            if (SafeConverter.toInt(box.get("awardId")) == 1) {
                // 校验是否达到上限
                if (productDetail.getDiscountPrice().intValue() >= 1000) {
                    if (boxMonthMax(productDetail.getDiscountPrice().intValue())) {
                        box = getEmptyBox();
                    } else {
                        recordBoxMonthCount(productDetail.getDiscountPrice().intValue(), teacherDetail.getId());
                    }
                }
                // 先进行库存操作 防止并发情况产生超出库存的现象
                if (Objects.nonNull(sku)) {
                    if (rewardSkuDao.decreaseInventorySellable(sku.getId(), 1) == 0) {
                        logger.warn("Failed to descrease reward sku {} inventory, rollback", sku.getId());
                        box = getEmptyBox();
                    }
                }
            }
            // 扣钱
            IntegralHistory integralHistory = new IntegralHistory(teacherDetail.getId(), IntegralType.REWARD_CENTER_OPEN_BOX, -50);
            integralHistory.setComment(newRewardService.fetchRewardIntegralComment(integralHistory.toIntegralType()));

            if (!userIntegralService.changeIntegral(integralHistory).isSuccess()) {
                throw new NoEnoughBalanceException();
            }
            // 记录结果
            RewardMoonLightBoxHistory boxHistory = new RewardMoonLightBoxHistory();
            if (Objects.nonNull(sku)) {
                boxHistory.setSkuName(sku.getSkuName());
                boxHistory.setSkuId(sku.getId());
            }
            boxHistory.setProductName(productDetail.getProductName());
            boxHistory.setProductId(productDetail.getId());
            boxHistory.setPrice(productDetail.getDiscountPrice());
            boxHistory.setUserId(teacherDetail.getId());
            boxHistory.setAwardId(SafeConverter.toInt(box.get("awardId")));
            boxHistory.setAwardName(box.get("awardName").toString());
            rewardMoonLightBoxHistoryPersistence.insert(boxHistory);
            // 发奖
            if (SafeConverter.toInt(box.get("awardId")) == 1) {
                String comment = "尊敬的" + teacherDetail.fetchRealname() + "老师，" +
                        "恭喜！您在奖品中心试手气中，抽中了“" + productDetail.getProductName() + "”，奖品将在下个月20日左右寄到您的手里（如遇寒暑假期间，则开学后配送）。";
                //系统消息
                teacherLoaderClient.sendTeacherMessage(teacherDetail.getId(), comment);

                // 直接产生一条不可修改的订单 状态为 待审核 但是价格是0 页面上要做成不可修改的
                RewardOrder order = new RewardOrder();
                if (Objects.nonNull(sku)) {
                    order.setSkuName(sku.getSkuName());
                    order.setSkuId(sku.getId());
                }
                order.setCode("");//订单号暂时没用， 先留着
                order.setProductId(productDetail.getId());
                order.setProductName(productDetail.getProductName());
                order.setSaleGroup(productDetail.getSaleGroup());
                order.setUnit(productDetail.getUnit());
                order.setTotalPrice(0.0);
                order.setBuyerId(teacherDetail.getId());
                order.setBuyerType(teacherDetail.getUserType());
                order.setBuyerName(teacherDetail.fetchRealname());
                order.setPrice(0.0);
                order.setQuantity(1);
                order.setStatus(RewardOrderStatus.SUBMIT.name());
                order.setDiscount(productDetail.getDiscount());
                order.setProductType(String.valueOf(newRewardLoader.getOneLevelCategoryType(productDetail.getOneLevelCategoryId())));
                order.setProductCategory(String.valueOf(productDetail.getOneLevelCategoryId()));
                order.setSource(RewardOrder.Source.moonlightbox);

                rewardOrderPersistence.insert(order);
                // 修改产品卖出量
                if (rewardProductDao.increaseSoldQuantity(productDetail.getId(), 1) == 0) {
                    logger.warn("Failed to increase reward product {} sold quantity, rollback", productDetail.getId());
                    throw new RuntimeException();
                }
                rewardProductVersion.increase();
            }
            int gold = 0;
            if (SafeConverter.toInt(box.get("awardId")) == 2) {
                gold = 50;
            }
            if (SafeConverter.toInt(box.get("awardId")) == 3) {
                gold = 10;
            }
            if (gold > 0) {
                IntegralHistory history = new IntegralHistory(teacherDetail.getId(), IntegralType.TEACHER_OPEN_REWARD_BOX, gold);
                history.setComment(newRewardService.fetchRewardIntegralComment(history.toIntegralType()));

                if (!userIntegralService.changeIntegral(history).isSuccess()) {
                    throw new RuntimeException();
                }
            }
            return MapMessage.successMessage().add("box", box);
        } catch (Exception ex) {
            String errorMessage;
            if (ex instanceof NoEnoughBalanceException) {
                errorMessage = "Failed to open moonlight box (userId={},productId={},skuId={},quantity={}): no enough balance";
            } else {
                errorMessage = "Failed to open moonlight box (userId={},productId={},skuId={},quantity={})";
            }
            logger.error(errorMessage, teacherDetail.getId(), productDetail.getId(), sku.getId(), 1);
            return MapMessage.errorMessage("开启失败，请重试");
        }
    }

    @Override
    public MapMessage createPresentRewardOrder(User user,
                                               RewardProductDetail productDetail,
                                               RewardSku sku,
                                               int quantity,
                                               RewardWishOrder wishOrder,
                                               RewardOrder.Source source) {

        if (user == null || productDetail == null || sku == null) {
            return MapMessage.errorMessage();
        }
        try {
            // 这里先修改库存 防止并发的情况
            if (rewardSkuDao.decreaseInventorySellable(sku.getId(), quantity) == 0) {
                logger.warn("Failed to descrease reward sku {} inventory, rollback", sku.getId());
                return MapMessage.errorMessage("奖品数量不足！");
            }

            final BigDecimal total = new BigDecimal(productDetail.getDiscountPrice()).multiply(new BigDecimal(quantity));
            int cost = total.setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
            IntegralHistory integralHistory = new IntegralHistory(user.getId(), newRewardService.fetchIntegralByExchangProduct(productDetail.getOneLevelCategoryId()), -cost);
            integralHistory.setComment(newRewardService.fetchRewardIntegralComment(integralHistory.toIntegralType()));

            if (!userIntegralService.changeIntegral(integralHistory).isSuccess()) {
                throw new NoEnoughBalanceException();
            }

            RewardOrder order = new RewardOrder();
            order.setSkuName(sku.getSkuName());
            order.setSkuId(sku.getId());
            order.setCode("");//订单号暂时没用， 先留着
            order.setProductId(productDetail.getId());
            order.setProductName(productDetail.getProductName());
            order.setSaleGroup(productDetail.getSaleGroup());
            order.setUnit(productDetail.getUnit());
            order.setTotalPrice(total.doubleValue());
            order.setBuyerId(user.getId());
            order.setBuyerName(user.fetchRealname());
            order.setBuyerType(user.getUserType());
            order.setPrice(productDetail.getDiscountPrice());
            order.setQuantity(quantity);
            order.setStatus(RewardOrderStatus.DELIVER.name());
            order.setDiscount(productDetail.getDiscount());
            order.setSource(source);
            order.setProductType(String.valueOf(newRewardLoader.getOneLevelCategoryType(productDetail.getOneLevelCategoryId())));
            order.setProductCategory(String.valueOf(productDetail.getOneLevelCategoryId()));

            // 设置班级ID
            Clazz clazz = deprecatedClazzLoaderClient.getRemoteReference().loadStudentClazz(user.getId());
            if (user.getUserType() == UserType.STUDENT.getType()
                    && clazz != null) {
                order.setClazzId(clazz.getId());
            }

            rewardOrderPersistence.insert(order);
            // 修改产品卖出量
            if (rewardProductDao.increaseSoldQuantity(productDetail.getId(), quantity) == 0) {
                logger.warn("Failed to increase reward product {} sold quantity, rollback", productDetail.getId());
                throw new RuntimeException();
            }
            rewardProductVersion.increase();
            Collection<String> keys = new LinkedHashSet<>();
            keys.add(CacheKeyGenerator.generateCacheKey(MemcachedKeyConstants.REWARD_USER_ORDER_COUNT, null, new Object[]{user.getId()}));
            if (wishOrder != null) {
                if (rewardWishOrderPersistence.achievedWishOrderById(wishOrder.getId()) > 0) {
                    keys.add(CacheKeyGenerator.generateCacheKey(MemcachedKeyConstants.REWARD_USER_WISH_ORDER, null, new Object[]{user.getId()}));
                }
            }
            RewardCache.getRewardCache().delete(keys);
            return MapMessage.successMessage();
        } catch (Exception ex) {
            String errorMessage;
            if (ex instanceof NoEnoughBalanceException) {
                errorMessage = "Failed to create present reward order (userId={},productId={},skuId={},quantity={}): no enough balance";
            } else {
                errorMessage = "Failed to create present reward order (userId={},productId={},skuId={},quantity={})";
            }
            logger.error(errorMessage, user.getId(), productDetail.getId(), sku.getId(), quantity);
            return MapMessage.errorMessage("兑换失败，请重试");
        }
    }

    @Override
    public MapMessage createActivityRecord(RewardActivityRecord record) {
        // 判断今天有没有参加活动的记录
        long count = rewardLoader.loadUserRecordsInDay(record.getUserId(), new Date())
                .stream()
                .filter(i -> Objects.equals(i.getActivityId(), record.getActivityId()))
                .count();

        if (count >= 10) {
            return MapMessage.errorMessage("每天最多捐赠10次哦~");
        }

        // 扣除学豆
        return $createActivityRecord(record);
    }

    @Override
    public MapMessage createPublicGoodActivityRecord(RewardActivityRecord record) {
        // 每人每天最多可捐赠60学豆，超过时提示"每天最多捐赠60学豆"
        List<RewardActivityRecord> rewardActivityRecords = rewardLoader.loadUserRecordsInDay(record.getUserId(), new Date());
        long dayPriceCount = rewardActivityRecords.stream().filter(r -> Objects.equals(r.getActivityId(), record.getActivityId()))
                .mapToLong(value -> value.getPrice() == null ? 0 : value.getPrice().longValue()).sum();

        int limit = 60;
        String unit = "学豆";
        User user = userLoaderClient.loadUser(record.getUserId());
        if(user.isTeacher()){
            unit = "园丁豆";
            limit = limit / 10;
        }

        // 测试环境方便qa就不校验了
        if (RuntimeMode.isProduction() && dayPriceCount + record.getPrice() > 60) {
            return MapMessage.errorMessage("每天最多捐赠" + limit + unit);
        }
        //FIXME:17公益捐赠记录是否显示在最近十条的捐赠记录中
        try {
            return $createActivityRecord(record);
        } catch (NoEnoughBalanceException e) {
            return MapMessage.errorMessage("学豆不足");
        }
    }

    private MapMessage $createActivityRecord(RewardActivityRecord record) {
        // 扣除学豆
        IntegralHistory integralHistory = new IntegralHistory(record.getUserId(), IntegralType.REWARD_PUBLIC_GOOD_INTEGRAL, -record.getPrice().intValue());
        integralHistory.setComment(newRewardService.fetchRewardIntegralComment(integralHistory.toIntegralType()));

        if (!userIntegralService.changeIntegral(integralHistory).isSuccess()) {
            throw new NoEnoughBalanceException();
        }

        rewardActivityRecordDao.insert(record);
        addHistoryRecord(record);
        // 更新活动的金额和参与人数进度
        if (rewardActivityDao.updateProgressData(record.getActivityId(), 1, record.getPrice().intValue()) == 0) {
            throw new RuntimeException();
        }

        // 如果更新后，已经完成筹款目标，则置上标志
        RewardActivity activity = rewardActivityDao.load(record.getActivityId());
        if (activity != null && activity.getRaisedMoney() >= activity.getTargetMoney()) {
            activity.setStatus(RewardActivity.Status.FINISHED.name());
            // 置上完成时间
            activity.setFinishTime(new Date());
            rewardActivityDao.upsert(activity);
        }

        return MapMessage.successMessage();
    }

    private void addHistoryRecord(RewardActivityRecord record) {
        String cacheKey = CacheKeyGenerator.generateCacheKey("RewardActivityHistory",
                new String[]{"activityId"}, new Object[]{record.getActivityId()});

        List<RewardActivityRecord> originRecords = RewardCache.getRewardCache().load(cacheKey);
        if (originRecords == null)
            originRecords = new ArrayList<>();

        originRecords.add(0, record);
        // 最多保留十条
        List<RewardActivityRecord> newRecords = new ArrayList<>();
        newRecords.addAll(originRecords.subList(0, Math.min(10, originRecords.size())));

        if (!Boolean.TRUE.equals(RewardCache.getRewardCache().set(cacheKey, 7 * 24 * 60 * 60, newRecords))) {
            logger.warn("Failed to add '{}' into cache, the value is: {}", cacheKey, newRecords);
        }
    }

    @Override
    public MapMessage updateActivity(RewardActivity activity) {
        try {
            rewardActivityDao.upsert(activity);
            return MapMessage.successMessage().add("id",activity.getId());
        } catch (Exception e) {
            return MapMessage.errorMessage("创建活动失败!");
        }
    }

    @Override
    public MapMessage updateActivityRecord(RewardActivityRecord record) {
        rewardActivityRecordDao.upsert(record);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage saveProductTargets(Long productId, Integer type, List<String> regionList, Boolean append) {
        if (productId == null || productId == 0L || type == null || type == 0 || CollectionUtils.isEmpty(regionList)) {
            return MapMessage.errorMessage("非法的参数！");
        }

        RewardProduct product = rewardProductDao.load(productId);
        if(product == null)
            return MapMessage.errorMessage("商品不存在!");

        if(!append){
            rewardProductTargetDao.clearProductTarget(productId,type);
        }

        List<RewardProductTarget> productTargets = CollectionUtils.toLinkedHashSet(regionList)
                .stream()
                .filter(StringUtils::isNotEmpty)
                .map(target -> {
                    RewardProductTarget productTarget = new RewardProductTarget();
                    productTarget.setProductId(productId);
                    productTarget.setTargetType(type);
                    productTarget.setTargetStr(target);

                    return productTarget;
                })
                .collect(Collectors.toList());

        rewardProductTargetDao.inserts(productTargets);

        return MapMessage.successMessage();
    }

    @Override
    public MapMessage clearProductTargets(Long productId, Integer type) {
        if (productId == null || productId == 0L || type == null || type == 0) {
            return MapMessage.errorMessage("非法的参数！");
        }
        RewardProduct product = rewardProductDao.load(productId);
        if(product == null)
            return MapMessage.errorMessage("商品不存在!");

        rewardProductTargetDao.clearProductTarget(productId,type);
        return MapMessage.successMessage();
    }

    /**
     * 取消失败的流量包订单，删除订单后，置上失败状态及原因
     * @param orderId
     * @param failedReason
     * @return
     */
    @Override
    public MapMessage cancelFlowPacketOrder(Long orderId, String failedReason) {

        RewardOrder order = rewardLoader.loadRewardOrders(Collections.singleton(orderId)).get(orderId);
        if(order == null)
            return MapMessage.errorMessage("订单不存在!");

        // 如果已经是失败状态，不能再次退还
        if(Objects.equals(order.getStatus(),RewardOrderStatus.FAILED.name())){
            return MapMessage.errorMessage("订单已处于失败状态，不能再次取消!");
        }

        MapMessage resultMsg = rewardManagement.updateRewardOrderStatus(orderId,failedReason,RewardOrderStatus.FAILED);
        if(!resultMsg.isSuccess()){
            return resultMsg;
        }

        int amount = order.getTotalPrice().intValue();
        if (order.getUnit().equals(RewardProductPriceUnit.园丁豆.name())) {
            amount = order.getTotalPrice().intValue() * 10;
        }
        IntegralType integralType = newRewardService.fetchIntegralByCancelProduct(NumberUtils.toLong(order.getProductCategory()));
        if (Objects.equals(integralType, integralType.UNKNOWN)) {// fixme 为了兼容老的ProductCategory，当前商品全发货之后可去掉（18.11.27）
            integralType = IntegralType.奖品相关;
        }
        IntegralHistory integralHistory = new IntegralHistory(order.getBuyerId(), integralType, amount);
        integralHistory.setComment(newRewardService.fetchRewardIntegralComment(integralHistory.toIntegralType()));

        resultMsg = userIntegralService.changeIntegral(integralHistory);
        if(!resultMsg.isSuccess()){
            return resultMsg;
        }

        return MapMessage.successMessage();
    }

    @Override
    public MapMessage createRewardOrderFree(Long userId, Long productId, int quantity) {
        try{
            StudentDetail stuDetail = studentLoaderClient.loadStudentDetail(userId);
            RewardProduct product = rewardLoader.loadRewardProductMap().get(productId);

            Validate.isTrue(product != null,"商品不存在!");
            Validate.isTrue(stuDetail != null,"用户不存在!");

            RewardSku sku = rewardLoader.loadProductSku(productId)
                    .stream()
                    .findFirst()
                    .orElse(null);
            Validate.isTrue(sku != null,"库存不存在!");

            RewardOrder order = new RewardOrder();
            order.setSkuName(sku.getSkuName());
            order.setSkuId(sku.getId());
            order.setCode("");//订单号暂时没用， 先留着
            order.setProductId(product.getId());
            order.setProductName(product.getProductName());
            order.setSaleGroup(product.getSaleGroup());
            order.setUnit(RewardProductPriceUnit.学豆.name());
            order.setBuyerId(stuDetail.getId());
            order.setBuyerName(stuDetail.fetchRealname());
            order.setBuyerType(stuDetail.getUserType());
            order.setPrice(product.getPriceOldS());
            order.setQuantity(quantity);
            order.setStatus(RewardOrderStatus.SUBMIT.name());
            order.setDiscount(1.0);
            order.setProductType(String.valueOf(newRewardLoader.getOneLevelCategoryType(product.getOneLevelCategoryId())));
            order.setProductCategory(String.valueOf(product.getOneLevelCategoryId()));
            order.setTotalPrice(10d);//抽奖默认10学豆
            order.setSource(RewardOrder.Source.gift);

            rewardOrderPersistence.insert(order);
            // 修改产品卖出量
            if (rewardProductDao.increaseSoldQuantity(product.getId(), 1) == 0) {
                logger.warn("Failed to increase reward product {} sold quantity, rollback", product.getId());
                throw new RuntimeException();
            }
            rewardProductVersion.increase();

            return MapMessage.successMessage();
        }catch (IllegalArgumentException e){
            return MapMessage.errorMessage(e.getMessage());
        }
        catch (Throwable t){
            logger.error("Failed to create free order!userId:{},productId:{}",userId,productId,t);
            // 抽奖发奖逻辑,记录日志方便后期恢复
            // logger.warn("createRewardOrderFreeFailed userId:{} productId:{} quantity:{}", userId, productId, quantity);
            return MapMessage.errorMessage(t.getMessage());
        }
    }

    private void recordBoxMonthCount(int price, Long userId) {
        String key = RewardMoonLightBoxHistory.getCacheKeyByPrice(price);
        int expirtation = (int) (MonthRange.current().getEndTime() / 1000);
        Long ret = CacheSystem.CBS.getCache("persistence").incr(key, 1, 1, expirtation);
        if (ret == null) {
            logger.error("Failed increase {} open moonlight box with delta {}", userId, 1);
        }
    }

    private Map<String, Object> getEmptyBox() {
        Map<String, Object> fourMap = new HashMap<>();
        fourMap.put("awardId", 4);
        fourMap.put("awardName", "空箱子");
        return fourMap;
    }

    private boolean boxMonthMax(int price) {
        boolean isMax = false;
        String cacheKey = RewardMoonLightBoxHistory.getCacheKeyByPrice(price);
        CacheObject<String> cacheObject = CacheSystem.CBS.getCache("persistence").get(cacheKey);
        if (cacheObject != null && StringUtils.isNotBlank(cacheObject.getValue())) {
            int count = SafeConverter.toInt(StringUtils.trim(cacheObject.getValue()));
            if (count >= getMoonLightBoxMaxMonthCount(price)) {
                isMax = true;
            }
        }
        return isMax;
    }

    private int getMoonLightBoxMaxMonthCount(int price) {
        int count = 0;
        if (price >= 1000 && price < 5000) {
            count = 10;
        } else if (price >= 5000 && price < 10000) {
            count = 5;
        } else if (price >= 10000 && price < 20000) {
            count = 2;
        } else if (price >= 20000 && price < 50000) {
            count = 1;
        } else if (price >= 50000) {
            count = 1;
        }
        return count;
    }

    private Map<String, Object> openBox(RewardProductDetail productDetail,TeacherDetail teacherDetail) {

        final BigDecimal total = new BigDecimal(productDetail.getDiscountPrice());
        int price = total.setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
        // 中奖品概率
        List<Map<String, Object>> boxList = new ArrayList<>();
        int oneRate = new BigDecimal(5).divide(new BigDecimal(3.125).multiply(new BigDecimal(price)), 6, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(1000000)).intValue();

        String unit = "学豆";
        int scale = 1;
        if(teacherDetail.isJuniorTeacher()) {
            unit = "学豆";
            scale = 10;
        }
        else if(teacherDetail.isPrimarySchool())
            unit = "园丁豆";

        Map<String, Object> oneMap = new HashMap<>();
        oneMap.put("awardId", 1);
        oneMap.put("awardName", "奖品箱子");
        oneMap.put("rate", oneRate);
        boxList.add(oneMap);

        Map<String, Object> twoMap = new HashMap<>();
        twoMap.put("awardId", 2);
        twoMap.put("awardName", (5 * scale) + unit);
        twoMap.put("rate", 100000);
        boxList.add(twoMap);

        Map<String, Object> threeMap = new HashMap<>();
        threeMap.put("awardId", 3);
        threeMap.put("awardName", (1 * scale) + unit);
        threeMap.put("rate", 800000);
        boxList.add(threeMap);

        Map<String, Object> fourMap = new HashMap<>();
        fourMap.put("awardId", 4);
        fourMap.put("awardName", "空箱子");
        fourMap.put("rate", 100000 - oneRate);
        boxList.add(fourMap);

        Integer randomResult = RandomUtils.nextInt(1000000);
        Integer lotteryStart = 0;
        for (Map<String, Object> box : boxList) {
            Integer lotteryEnd = lotteryStart + SafeConverter.toInt(box.get("rate"));
            if (randomResult >= lotteryStart && randomResult < lotteryEnd) {
                return box;
            }
            lotteryStart = lotteryEnd;
        }
        return null;
    }

    @Override
    public int tryShowTipFlag(User user) {
        try {
            return internalRewardTipService.tryShowTip(user);
        } catch (InterruptedException e) {
            logger.error("tryShowTipFlag", e);
        }
        return 0;
    }

    @Override
    public boolean isGraduateStopConvert(User user) {
        try {
            return internalRewardTipService.isGraduateStopConvert(user);
        } catch (InterruptedException e) {
            logger.error("tryShowTipFlag", e);
        }
        return false;
    }

    private RewardProductPriceUnit fetchOrderUnit(User user) {
        if (user != null && user.isTeacher()) {
            TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(user.getId());
            if (teacherDetail.isPrimarySchool() || teacherDetail.isInfantTeacher()) {
                return RewardProductPriceUnit.园丁豆;
            } else {
                return RewardProductPriceUnit.中学积分;
            }
        }

        return RewardProductPriceUnit.学豆;
    }
}
