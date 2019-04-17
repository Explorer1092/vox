package com.voxlearning.utopia.service.reward.impl.coupon;

import com.voxlearning.utopia.api.constant.CouponProductionName;
import com.voxlearning.utopia.service.reward.constant.IdentificationCouponName;
import com.voxlearning.utopia.service.reward.entity.RewardCouponDetail;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;

/**
 * Created by XiaoPeng.Yang on 14-10-30.
 */
@Named
@Slf4j
@NoArgsConstructor
@IdentificationCouponName(CouponProductionName.大使专享Starbooks成人口语课体验)
public class Coupon_Starbooks_Dashi extends CouponTemplate {
    @Override
    protected boolean sendSmsFlag() {
        return false;
    }

    @Override
    protected String getIntegralComment() {
        return "兑换大使专享Starbooks成人口语课体验券";
    }

    @Override
    protected String getSystemMessage(RewardCouponDetail couponDetail) {
        return "你已经兑换大使专享Starbooks成人口语课体验券，优惠券号：" + couponDetail.getCouponNo() + "，请及时使用哦！";
    }
}
