package com.voxlearning.utopia.service.reward.impl.coupon;

import com.voxlearning.utopia.api.constant.CouponProductionName;
import com.voxlearning.utopia.service.reward.constant.IdentificationCouponName;
import com.voxlearning.utopia.service.reward.entity.RewardCouponDetail;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;

/**
 * Created by XiaoPeng.Yang on 14-9-5.
 */
@Named
@Slf4j
@NoArgsConstructor
@IdentificationCouponName(CouponProductionName.唯品会一起作业专属优惠券)
public class Coupon_Vip extends CouponTemplate {
    @Override
    protected boolean sendSmsFlag() {
        return false;
    }

    @Override
    protected String getIntegralComment() {
        return "兑换唯品会（www.vip.vom）20元专属优惠券";
    }

    @Override
    protected String getSystemMessage(RewardCouponDetail couponDetail) {
        return "你已经兑换唯品会（www.vip.vom）20元专属优惠券，优惠券号：" + couponDetail.getCouponNo() + "，请及时使用哦！";
    }
}
