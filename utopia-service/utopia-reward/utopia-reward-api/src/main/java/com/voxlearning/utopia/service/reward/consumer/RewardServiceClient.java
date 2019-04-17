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

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.calendar.MonthRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.spi.cache.AtomicCallback;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.utopia.api.constant.CouponProductionName;
import com.voxlearning.utopia.service.integral.api.mapper.UserIntegral;
import com.voxlearning.utopia.service.reward.api.RewardService;
import com.voxlearning.utopia.service.reward.api.mapper.newversion.teacher.entity.TeacherCouponEntity;
import com.voxlearning.utopia.service.reward.cache.RewardCache;
import com.voxlearning.utopia.service.reward.client.newversion.NewRewardLoaderClient;
import com.voxlearning.utopia.service.reward.constant.RewardOrderStatus;
import com.voxlearning.utopia.service.reward.constant.DuibaCoupon;
import com.voxlearning.utopia.service.reward.entity.*;
import com.voxlearning.utopia.service.reward.mapper.RewardProductDetail;
import com.voxlearning.utopia.service.user.api.entities.TeacherExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.ResearchStaffDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.*;

/**
 * Client implementation of remote service {@link RewardService}.
 *
 * @author Xiaohai Zhang
 * @since Dec 2, 2014
 */
public class RewardServiceClient implements RewardService {
    private static final Logger logger = LoggerFactory.getLogger(RewardServiceClient.class);

    @ImportService(interfaceClass = RewardService.class)
    private RewardService remoteReference;
    @Inject private TeacherLoaderClient teacherLoaderClient;

    @Inject private NewRewardLoaderClient newRewardLoaderClient;

    /**
     * 如果是学生帐户，则User必须是StudentDetail
     * 如果是老师帐户，则User必须是TeacherDetail
     * 如果是教研员帐户，则User必须是ResearchStaffDetail
     */
    public MapMessage createRewardOrder(final User user,
                                        final RewardProductDetail productDetail,
                                        final RewardSku sku,
                                        final int quantity,
                                        final RewardWishOrder wishOrder,
                                        RewardOrder.Source source) {
        return createRewardOrder(user, productDetail, sku, quantity, wishOrder, source, null);
    }

    protected Boolean isTeacherFreeZuoye(TeacherDetail teacher) {
        Boolean result = false;

        if (!teacher.isSchoolAmbassador() && !teacher.isJuniorTeacher()) {
            TeacherExtAttribute extAttribute = teacherLoaderClient.loadTeacherExtAttribute(teacher.getId());
            Integer teacherRal = extAttribute == null ? null : extAttribute.getRewardActiveLevel();
            if (teacherRal != null && teacherRal == 0) {
                result = true;
                Set<Long> allRelIds = teacherLoaderClient.loadRelTeacherIds(teacher.getId());
                Map<Long, TeacherExtAttribute> details = teacherLoaderClient.loadTeacherExtAttributes(allRelIds);
                for (TeacherExtAttribute detail : details.values()) {
                    if(detail.getRewardActiveLevel() != null && SafeConverter.toInt(detail.getRewardActiveLevel()) > 0) {
                        result = false;
                        break;
                    }
                }
            }
        }
        return result;
    }

    // fixme 这个方法在impl里有，但是写成一个接口也不合适，索性先搬过来,等重构吧。。。。。。
    public Integer getDiscountPrice(int quantity, RewardProductDetail productDetail, TeacherCouponEntity coupon) {
        Double price = coupon==null || coupon.getDiscount()==null ? productDetail.getDiscountPrice():productDetail.getDiscountPrice() * coupon.getDiscount();
        BigDecimal total = new BigDecimal(price).multiply(new BigDecimal(quantity));

        //托比装扮两件9折
        if (Objects.equals(quantity, 2) && newRewardLoaderClient.isTobyWear(productDetail.getOneLevelCategoryId())) {
            total = total.multiply(new BigDecimal(0.9));
        }
        return total.setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
    }

    @Override
    public MapMessage createRewardOrder(User user, RewardProductDetail productDetail, RewardSku sku, int quantity, RewardWishOrder wishOrder, RewardOrder.Source source, TeacherCouponEntity coupon) {
        if (user == null || productDetail == null || sku == null) {
            return MapMessage.errorMessage();
        }
        int totalPrice = getDiscountPrice(quantity, productDetail, coupon);
        switch (user.fetchUserType()) {
            case TEACHER: {
                if (!(user instanceof TeacherDetail)) {
                    throw new IllegalArgumentException("User must be TeacherDetail");
                }
                TeacherDetail teacher = (TeacherDetail) user;
                if (isTeacherFreeZuoye(teacher)) {
                    return MapMessage.errorMessage("由于您超过30天未布置作业，园丁豆已经暂时冻结，检查作业且有学生完成即可解冻。");
                }
                // 判断余额
                UserIntegral integral = teacher.getUserIntegral();
                if (integral == null || integral.getUsable() < totalPrice) {
                    String unit = teacher.isPrimarySchool() ? "园丁豆" : "学豆";
                    return MapMessage.errorMessage("你的" + unit + "数量不足，请检查一下价格和数量吧！");
                }

                // 记录轨迹
                recordAccessTrace(user.getId());
                break;
            }
            case STUDENT: {
                if (!(user instanceof StudentDetail)) {
                    throw new IllegalArgumentException("User must be StudentDetail");
                }
                StudentDetail student = (StudentDetail) user;

                // 判断余额
                UserIntegral integral = student.getUserIntegral();
                if (integral == null || integral.getUsable() < totalPrice) {
                    return MapMessage.errorMessage("你的学豆数量不足，请检查一下价格和数量吧！");
                }
                break;
            }
            case RESEARCH_STAFF: {
                if (!(user instanceof ResearchStaffDetail)) {
                    throw new IllegalArgumentException("User must be ResearchStaffDetail");
                }
                ResearchStaffDetail researchStaff = (ResearchStaffDetail) user;
                // 判断余额
                UserIntegral integral = researchStaff.getUserIntegral();
                if (integral == null || integral.getUsable() < totalPrice) {
                    return MapMessage.errorMessage("你的园丁豆数量不足，请检查一下价格和数量吧！");
                }
                break;
            }
            default: {
                return MapMessage.errorMessage("角色错误");
            }
        }

        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("RewardService:createRewardOrder")
                    .keys(user.getId(), productDetail.getId(), sku.getId())
                    .callback(() -> remoteReference.createRewardOrder(user, productDetail, sku, quantity, wishOrder, source, coupon))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            logger.error("Failed to create reward order (user={},product={},sku={},quantity={}): DUPLICATED OPERATION",
                    user.getId(), productDetail.getId(), sku.getId(), quantity);
            return MapMessage.errorMessage();
        } catch (Exception ex) {
            logger.error("Failed to create reward order (user={},product={},sku={},quantity={})",
                    user.getId(), productDetail.getId(), sku.getId(), quantity, ex);
            return MapMessage.errorMessage();
        }
    }

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
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("RewardService:updateRewardOrder")
                    .keys(user.getId(), order.getId())
                    .callback(() -> remoteReference.updateRewardOrder(user, order, product, sku, quantity))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            logger.error("Failed to update reward order quantity: DUPLICATED OPERATION");
            return MapMessage.errorMessage("修改失败");
        } catch (Exception ex) {
            logger.error("Failed to update reward order quantity", ex);
            return MapMessage.errorMessage("修改失败");
        }
    }

    public MapMessage deleteRewardOrder(final User user, final RewardOrder order) {
        if (user == null) {
            return MapMessage.errorMessage();
        }
        if (order == null) {
            return MapMessage.errorMessage("订单不存在");
        }
        if (!Objects.equals(user.getId(), order.getBuyerId())) {
            return MapMessage.errorMessage("订单与用户不一致");
        }
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("RewardService:deleteRewardOrder")
                    .keys(user.getId(), order.getId())
                    .callback(() -> remoteReference.deleteRewardOrder(user, order))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            logger.error("Failed to delete reward order (user={},order={}): DUPLICATED OPERATION", user.getId(), order.getId());
            return MapMessage.errorMessage("删除失败");
        } catch (Exception ex) {
            logger.error("Failed to delete reward order (user={},order={})", user.getId(), order.getId(), ex);
            return MapMessage.errorMessage("删除失败");
        }
    }

    @Override
    public MapMessage deleteRewardOrder(RewardOrder order) {
        return remoteReference.deleteRewardOrder(order);
    }

    @Override
    public MapMessage deleteRewardOrder(Long orderId) {
        return remoteReference.deleteRewardOrder(orderId);
    }

    public MapMessage createRewardWishOrder(final User user, final RewardProductDetail productDetail) {
        if (user == null || productDetail == null) {
            return MapMessage.errorMessage();
        }
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("RewardService:createRewardWishOrder")
                    .keys(user.getId())
                    .callback(() -> remoteReference.createRewardWishOrder(user, productDetail))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            logger.error("Failed to create reward wish order (user={},product={}): DUPLICATED OPERATION",
                    user.getId(), productDetail.getId());
            return MapMessage.errorMessage("添加愿望盒失败，请不要重复点击");
        } catch (Exception ex) {
            logger.error("Failed to create reward wish order (user={},product={})",
                    user.getId(), productDetail.getId(), ex);
            return MapMessage.errorMessage("添加愿望盒失败");
        }
    }

    public MapMessage deleteRewardWishOrder(final Long userId, final Long wishOrderId) {
        if (userId == null || wishOrderId == null) {
            return MapMessage.errorMessage();
        }
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("RewardService:deleteRewardWishOrder")
                    .keys(userId, wishOrderId)
                    .callback(() -> remoteReference.deleteRewardWishOrder(userId, wishOrderId))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            logger.error("Failed to delete reward wish order (userId={},wishOrderId={}): DUPLICATED OPERATION",
                    userId, wishOrderId);
            return MapMessage.errorMessage();
        } catch (Exception ex) {
            logger.error("Failed to delete reward wish order (userId={},wishOrderId={})",
                    userId, wishOrderId, ex);
            return MapMessage.errorMessage();
        }
    }

    @Override
    public MapMessage exchangedCoupon(User user, String mobile, CouponProductionName couponProductionName) {
        return remoteReference.exchangedCoupon(user, mobile, couponProductionName);
    }

    @Override
    public MapMessage exchangedCoupon(Long productId, User user, String mobile, TeacherCouponEntity coupon) {
        return remoteReference.exchangedCoupon(productId, user, mobile, coupon);
    }

    @Override
    public MapMessage exchangedDuibaCoupon(DuibaCoupon duibaCoupon, User user, RewardProductDetail rewardProductDetail) {
        if (duibaCoupon == null || user == null || rewardProductDetail == null) {
            return MapMessage.errorMessage().add("status", "fail").add("errorMessage", "参数为空");
        }
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("RewardService:exchangedDuibaCoupon")
                    .keys(user.getId(), duibaCoupon.getOrderNum())
                    .callback(() -> remoteReference.exchangedDuibaCoupon(duibaCoupon, user, rewardProductDetail))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            logger.error("Failed to exchangedDuibaCoupon (userId={},duibaCoupon={}): DUPLICATED OPERATION",
                    user.getId(), duibaCoupon);
            return MapMessage.errorMessage().add("status", "fail").add("errorMessage", "正在处理请不要多次点击");
        } catch (Exception ex) {
            logger.error("Failed to exchangedDuibaCoupon (userId={},duibaCoupon={}): DUPLICATED OPERATION",
                    user.getId(), duibaCoupon);
            return MapMessage.errorMessage().add("status", "fail").add("errorMessage", "兑换失败");
        }
    }

    @Override
    public MapMessage confirmDuibaCoupon(User user, Boolean success, Long id, String orderNum) {
        if (success == null || user == null || StringUtils.isBlank(orderNum) || id == null) {
            return MapMessage.errorMessage("参数为空");
        }
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("RewardService:confirmDuibaCoupon")
                    .keys(user.getId(), orderNum)
                    .callback(() -> remoteReference.confirmDuibaCoupon(user, success, id, orderNum))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            logger.error("Failed to confirmDuibaCoupon (userId={},orderNum={}): DUPLICATED OPERATION",
                    user.getId(), orderNum);
            return MapMessage.errorMessage("正在处理，请稍后");
        } catch (Exception ex) {
            logger.error("Failed to confirmDuibaCoupon (userId={},orderNum={}): DUPLICATED OPERATION",
                    user.getId(), orderNum);
            return MapMessage.errorMessage("确定失败");
        }
    }

    @Override
    public MapMessage couponRebate(Long userId, Long couponDetailId, CouponProductionName couponProductionName) {
        return remoteReference.couponRebate(userId, couponDetailId, couponProductionName);
    }

    public MapMessage openMoonLightBox(TeacherDetail teacherDetail, RewardProductDetail productDetail, RewardSku sku) {

        // 小学和中学都可以玩试手气，分开判断，单位不一样，分开处理
        int spentNum = 5;
        String unit = "学豆";
        if(teacherDetail.isPrimarySchool()){
            unit = "园丁豆";
        }else if(teacherDetail.isJuniorTeacher()){
            spentNum = 50;
        }

        Long skuId = 0l;
        if (Objects.nonNull(sku)) {
            skuId = sku.getId();
        }

        // 判断余额
        UserIntegral integral = teacherDetail.getUserIntegral();
        if (integral == null || integral.getUsable() < spentNum) {
            return MapMessage.errorMessage("对不起，"+ unit +"小于" + spentNum + "个无法试手气!");
        }

        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("RewardService:openMoonLightBox")
                    .keys(teacherDetail.getId(), productDetail.getId(), skuId)
                    .callback(() -> remoteReference.openMoonLightBox(teacherDetail, productDetail, sku))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            logger.error("Failed to open moon light box (user={},product={},sku={}): DUPLICATED OPERATION",
                    teacherDetail.getId(), productDetail.getId(), skuId);
            return MapMessage.errorMessage();
        } catch (Exception ex) {
            logger.error("Failed to open moon light box (user={},product={},sku={})",
                    teacherDetail.getId(), productDetail.getId(), skuId, ex);
            return MapMessage.errorMessage();
        }
    }

    @Override
    public MapMessage batchUpdateUserOrder(String[] userIds, String reason, RewardOrderStatus orderStatus) {
        return remoteReference.batchUpdateUserOrder(userIds, reason, orderStatus);
    }


    // 捐赠订单
    public MapMessage createPresentRewardOrder(final User user,
                                               final RewardProductDetail productDetail,
                                               final RewardSku sku,
                                               final int quantity,
                                               final RewardWishOrder wishOrder,
                                               RewardOrder.Source source) {
        if (user == null || productDetail == null || sku == null) {
            return MapMessage.errorMessage();
        }
        BigDecimal total = new BigDecimal(productDetail.getDiscountPrice()).multiply(new BigDecimal(quantity));
        int totalPrice = total.setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
        if (!(user instanceof StudentDetail)) {
            throw new IllegalArgumentException("User must be StudentDetail");
        }
        StudentDetail student = (StudentDetail) user;
        // 判断余额
        UserIntegral integral = student.getUserIntegral();
        if (integral == null || integral.getUsable() < totalPrice) {
            return MapMessage.errorMessage("你的学豆数量不足，请检查一下价格和数量吧！");
        }
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("RewardService:createRewardOrder")
                    .keys(user.getId(), productDetail.getId(), sku.getId())
                    .callback(() -> remoteReference.createPresentRewardOrder(user, productDetail, sku, quantity, wishOrder, source))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            logger.error("Failed to create reward order (user={},product={},sku={},quantity={}): DUPLICATED OPERATION",
                    user.getId(), productDetail.getId(), sku.getId(), quantity);
            return MapMessage.errorMessage();
        } catch (Exception ex) {
            logger.error("Failed to create reward order (user={},product={},sku={},quantity={})",
                    user.getId(), productDetail.getId(), sku.getId(), quantity, ex);
            return MapMessage.errorMessage();
        }
    }

    @Override
    public MapMessage createActivityRecord(RewardActivityRecord record) {
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("RewardService:createActivityRecord")
                    .keys(record.getActivityId(), record.getUserId())
                    .callback(new AtomicCallback<MapMessage>() {
                        @Override
                        public MapMessage execute() {
                            return remoteReference.createActivityRecord(record);
                        }
                    })
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            logger.error("Failed to create activity record (user={},activity={}): DUPLICATED OPERATION",
                    record.getUserId(), record.getActivityId());
            return MapMessage.errorMessage();
        } catch (Exception ex) {
            logger.error("Failed to create activity record (user={},activity={}): DUPLICATED OPERATION",
                    record.getUserId(), record.getActivityId());
            return MapMessage.errorMessage();
        }
    }

    @Override
    public MapMessage createPublicGoodActivityRecord(RewardActivityRecord record) {
        try {
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("RewardService:createActivityRecord")
                    .keys(record.getActivityId(), record.getUserId())
                    .callback(() -> remoteReference.createPublicGoodActivityRecord(record))
                    .build()
                    .execute();
        } catch (CannotAcquireLockException ex) {
            logger.error("Failed to create public good activity record (user={},activity={}): DUPLICATED OPERATION",
                    record.getUserId(), record.getActivityId());
            return MapMessage.errorMessage();
        } catch (Exception ex) {
            logger.error("Failed to create public good activity record (user={},activity={}): DUPLICATED OPERATION",
                    record.getUserId(), record.getActivityId());
            return MapMessage.errorMessage();
        }
    }

    @Override
    public MapMessage updateActivity(RewardActivity activity) {
        return remoteReference.updateActivity(activity);
    }

    @Override
    public MapMessage updateActivityRecord(RewardActivityRecord record) {
        return remoteReference.updateActivityRecord(record);
    }

    @Override
    public MapMessage saveProductTargets(Long productId, Integer type, List<String> regionList, Boolean append) {
        return remoteReference.saveProductTargets(productId,type,regionList,append);
    }

    @Override
    public MapMessage clearProductTargets(Long productId, Integer type) {
        return remoteReference.clearProductTargets(productId,type);
    }

    @Override
    public MapMessage cancelFlowPacketOrder(Long orderId, String failedReason) {
        return remoteReference.cancelFlowPacketOrder(orderId,failedReason);
    }

    @Override
    public MapMessage createRewardOrderFree(Long userId, Long productId, int quantity) {
        return remoteReference.createRewardOrderFree(userId,productId,quantity);
    }

    /**
     * 记录用户访当月是否访问过奖品中心
     * @param userId
     */
    public void recordAccessTrace(Long userId){
        // 存起来是给下个月看的，过期时间置成下个月月末
        MonthRange mr = MonthRange.current();
        long expireTime = mr.next().getEndTime();
        int ttl = (int)(expireTime - System.currentTimeMillis());

        String cacheKey = CacheKeyGenerator.generateCacheKey(
                "Reward:hadVisitedLastMonth",
                new String[]{"userId","month"},
                new Object[]{userId,mr.getMonth()});

        RewardCache.getPersistent().set(cacheKey, ttl, Boolean.TRUE);
    }

    @Override
    public int tryShowTipFlag(User user) {
        return remoteReference.tryShowTipFlag(user);
    }

    @Override
    public boolean isGraduateStopConvert(User user) {
        return remoteReference.isGraduateStopConvert(user);
    }
}
