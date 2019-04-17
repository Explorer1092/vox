/*
 * VOX LEARNING TECHNOLOGY, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Vox Learning Technology, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Vox Learning Technology, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Vox Learning
 * Technology, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Vox Learning Technology, Inc.
 */

package com.voxlearning.utopia.service.reward.impl.coupon;

import com.voxlearning.utopia.api.constant.CouponProductionName;
import com.voxlearning.utopia.service.reward.constant.IdentificationCouponName;
import com.voxlearning.utopia.service.reward.entity.RewardCouponDetail;
import com.voxlearning.utopia.service.user.api.entities.User;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;

/**
 * Created by XiaoPeng.Yang on 14-12-29.
 */
@Named
@Slf4j
@NoArgsConstructor
@IdentificationCouponName(CouponProductionName.淄博少儿科技博物馆门票)
public class Coupon_zibo extends CouponTemplate {
    @Override
    protected boolean sendSmsFlag() {
        return true;
    }

    @Override
    protected String getIntegralComment() {
        return "兑换淄博少儿科技博物馆门票";
    }

    @Override
    protected String getSmsMessage(RewardCouponDetail couponDetail) {
        User user = studentLoaderClient.loadStudent(couponDetail.getUserId());
        return "恭喜" + user.fetchRealname() + "小朋友免费获得1张88元的淄博少儿科技馆门票（1大人带1小孩），请在两周内使用，地点及详情咨询：0533-6061099。";
    }

    @Override
    protected String getSystemMessage(RewardCouponDetail couponDetail) {
        return "你已免费获得1张88元的淄博少儿科技馆门票（1大人带1小孩），请在两周内使用，地点及详情咨询：0533-6061099。";
    }
}