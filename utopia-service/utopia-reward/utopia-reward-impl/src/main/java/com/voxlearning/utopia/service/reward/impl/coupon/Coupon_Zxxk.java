package com.voxlearning.utopia.service.reward.impl.coupon;

import com.voxlearning.utopia.api.constant.CouponProductionName;
import com.voxlearning.utopia.service.reward.constant.IdentificationCouponName;
import com.voxlearning.utopia.service.reward.entity.RewardCouponDetail;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;

/**
 * 学科网课件兑换
 * Created by haitian.gan on 2017/3/6.
 */
@Named
@Slf4j
@NoArgsConstructor
@IdentificationCouponName(CouponProductionName.学科网课件兑换码)
public class Coupon_Zxxk extends CouponTemplate{

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
        return "兑换学科网课件";
    }

    @Override
    protected String getSmsMessage(RewardCouponDetail couponDetail) {
        return "您已成功兑换，请到学科网兑换相应课件，兑换码为:"+ couponDetail.getCouponNo() +"谢谢!";
    }

    @Override
    protected String getSystemMessage(RewardCouponDetail couponDetail) {
        return "您已成功兑换，请到学科网兑换相应课件，兑换码为:"+ couponDetail.getCouponNo() +"谢谢!";
    }

}
