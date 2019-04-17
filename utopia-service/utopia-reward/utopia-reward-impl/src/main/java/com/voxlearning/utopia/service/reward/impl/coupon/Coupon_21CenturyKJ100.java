package com.voxlearning.utopia.service.reward.impl.coupon;

import com.voxlearning.utopia.api.constant.CouponProductionName;
import com.voxlearning.utopia.service.reward.constant.IdentificationCouponName;
import com.voxlearning.utopia.service.reward.entity.RewardCouponDetail;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;

/**
 * 21世纪教育网兑换码100学豆
 * Created by haitian.gan on 2017/3/6.
 */
@Named
@Slf4j
@NoArgsConstructor
@IdentificationCouponName(CouponProductionName.二十一世纪教育课件兑换码100学币)
public class Coupon_21CenturyKJ100 extends CouponTemplate{

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
        return "兑换21世纪教育课件兑换码";
    }

    @Override
    protected String getSmsMessage(RewardCouponDetail couponDetail) {
        return "您已成功兑换，请到21世纪教育网兑换相应课件，兑换码为:"+ couponDetail.getCouponNo() +"谢谢!";
    }

    @Override
    protected String getSystemMessage(RewardCouponDetail couponDetail) {
        return "您已成功兑换，请到21世纪教育网兑换相应课件，兑换码为:"+ couponDetail.getCouponNo() +"谢谢!";
    }

}
