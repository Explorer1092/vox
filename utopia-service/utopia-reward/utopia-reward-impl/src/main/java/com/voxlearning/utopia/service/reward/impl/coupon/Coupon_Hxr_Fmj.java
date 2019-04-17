package com.voxlearning.utopia.service.reward.impl.coupon;

import com.voxlearning.utopia.api.constant.CouponProductionName;
import com.voxlearning.utopia.service.reward.constant.IdentificationCouponName;
import com.voxlearning.utopia.service.reward.entity.RewardCouponDetail;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;

/**
 * 火星人小小发明家公开课
 * Created by haitian.gan on 2017/3/6.
 */
@Named
@Slf4j
@NoArgsConstructor
@IdentificationCouponName(CouponProductionName.火星人小小发明家公开课)
public class Coupon_Hxr_Fmj extends CouponTemplate{

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
        return "兑换火星人小小发明家公开课";
    }

    @Override
    protected String getSmsMessage(RewardCouponDetail couponDetail) {
        return "家长您好，您的孩子已成功兑换火星人俱乐部公开课免费券，兑换码为："+ couponDetail.getCouponNo() +"，请提前预约火星人进行试听，谢谢！";
    }

    @Override
    protected String getSystemMessage(RewardCouponDetail couponDetail) {
        return "您已成功兑换，请提前预约火星人进行试听，兑换码为:"+ couponDetail.getCouponNo() +"谢谢!";
    }

}
