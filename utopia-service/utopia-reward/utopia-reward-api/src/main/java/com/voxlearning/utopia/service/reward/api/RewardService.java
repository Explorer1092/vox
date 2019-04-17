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

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.api.constant.CouponProductionName;
import com.voxlearning.utopia.service.reward.api.mapper.newversion.teacher.entity.TeacherCouponEntity;
import com.voxlearning.utopia.service.reward.constant.RewardOrderStatus;
import com.voxlearning.utopia.service.reward.constant.DuibaCoupon;
import com.voxlearning.utopia.service.reward.entity.*;
import com.voxlearning.utopia.service.reward.mapper.RewardProductDetail;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Reward service interface definition.
 *
 * @author Xiaopeng Yang
 * @author Xiaohai Zhang
 * @author haitian.gan
 * @since 2017-07-21
 */
@ServiceVersion(version = "2.3")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface RewardService extends IPingable {
    MapMessage createRewardOrder(User user, RewardProductDetail productDetail, RewardSku sku, int quantity, RewardWishOrder wishOrder, RewardOrder.Source source);

    MapMessage createRewardOrder(User user, RewardProductDetail productDetail, RewardSku sku, int quantity, RewardWishOrder wishOrder, RewardOrder.Source source, TeacherCouponEntity coupon);

    MapMessage updateRewardOrder(User user, RewardOrder order, RewardProduct product, RewardSku sku, int quantity);

    MapMessage deleteRewardOrder(User user, RewardOrder order);

    MapMessage deleteRewardOrder(RewardOrder order);

    MapMessage deleteRewardOrder(Long orderId);

    MapMessage createRewardWishOrder(User user, RewardProductDetail productDetail);

    MapMessage deleteRewardWishOrder(Long userId, Long wishOrderId);

    @Deprecated
    MapMessage exchangedCoupon(User user, String mobile, CouponProductionName couponProductionName);

    MapMessage exchangedCoupon(Long productId, User user, String mobile, TeacherCouponEntity coupon);

    MapMessage exchangedDuibaCoupon(DuibaCoupon duibaCoupon, User user, RewardProductDetail rewardProduct);

    MapMessage confirmDuibaCoupon(User user, Boolean success, Long id, String orderNum);

    MapMessage couponRebate(Long userId, Long couponDetailId, CouponProductionName couponProductionName);

    MapMessage openMoonLightBox(TeacherDetail teacherDetail, RewardProductDetail productDetail, RewardSku sku);

    MapMessage batchUpdateUserOrder(String[] userIds, String reason, RewardOrderStatus orderStatus);

    MapMessage createPresentRewardOrder(User user, RewardProductDetail productDetail, RewardSku sku, int quantity, RewardWishOrder wishOrder, RewardOrder.Source source);

    MapMessage createActivityRecord(RewardActivityRecord record);

    MapMessage createPublicGoodActivityRecord(RewardActivityRecord record);

    MapMessage updateActivity(RewardActivity activity);

    MapMessage updateActivityRecord(RewardActivityRecord record);

    MapMessage saveProductTargets(Long productId, Integer type, List<String> regionList, Boolean append);

    MapMessage clearProductTargets(Long productId, Integer type);

    MapMessage cancelFlowPacketOrder(Long orderId, String failedReason);

    /**
     * 供其它活动走奖品中心流程快捷下单使用(目前只针对学生)，不会扣学豆。
     * @param userId
     * @param productId
     * @param quantity
     * @return
     */
    MapMessage createRewardOrderFree(Long userId, Long productId, int quantity);

    /**
     * 尝试获取，是否需要在奖品中心显示的提示语
     * @param user
     * @return
     */
    int tryShowTipFlag(User user);

    boolean isGraduateStopConvert(User user);
}
