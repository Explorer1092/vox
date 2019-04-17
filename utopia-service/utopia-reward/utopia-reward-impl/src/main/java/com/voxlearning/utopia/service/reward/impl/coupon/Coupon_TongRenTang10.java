package com.voxlearning.utopia.service.reward.impl.coupon;

import com.voxlearning.utopia.api.constant.CouponProductionName;
import com.voxlearning.utopia.service.reward.constant.IdentificationCouponName;
import com.voxlearning.utopia.service.reward.entity.RewardCouponDetail;

import javax.inject.Named;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 同仁堂蜂业旗舰店10元优惠券
 * Created by haitian.gan on 2017/3/6.
 */
@Named
@Slf4j
@NoArgsConstructor
@IdentificationCouponName(CouponProductionName.同仁堂蜂业旗舰店10元优惠券)
public class Coupon_TongRenTang10 extends CouponTemplate{

    @Override
    protected boolean sendSmsFlag() {
        return true;
    }

    @Override
    protected int getRebateAmount() {
        return 300;
    }

    @Override
    protected String getIntegralComment() {
        return "兑换同仁堂蜂业旗舰店10元优惠券";
    }

    @Override
    protected String getSmsMessage(RewardCouponDetail couponDetail) {
        return "您已成功兑换，请到同仁堂天猫直营旗舰店，10元无门槛优惠券。复制整条信息，打开「手机淘宝」￥MS1dZxi9ZS4￥(立即享受优惠)";
    }

    @Override
    protected String getSystemMessage(RewardCouponDetail couponDetail) {
        return "您已成功兑换，请到同仁堂天猫直营旗舰店，10元无门槛优惠券。复制整条信息，打开「手机淘宝」￥MS1dZxi9ZS4￥(立即享受优惠)";
    }

}
