package com.voxlearning.utopia.service.reward.impl.coupon;

import com.voxlearning.utopia.api.constant.CouponProductionName;
import com.voxlearning.utopia.service.reward.constant.IdentificationCouponName;
import com.voxlearning.utopia.service.reward.entity.RewardCouponDetail;

import javax.inject.Named;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 粉红猪小妹PeppaPig第三季
 * Created by haitian.gan on 2017/7/6.
 */
@Named
@Slf4j
@NoArgsConstructor
@IdentificationCouponName(CouponProductionName.粉红猪小妹PeppaPig第三季)
public class Coupon_PeppaPig3 extends CouponTemplate{

    @Override
    protected boolean sendSmsFlag() {
        return false;
    }

    @Override
    protected int getRebateAmount() {
        return 300;
    }

    @Override
    protected String getIntegralComment() {
        return "粉红猪小妹PeppaPig第三季";
    }

    @Override
    protected String getSmsMessage(RewardCouponDetail couponDetail) {
        return "您已成功兑换，请到电脑端下载课件资源，具体下载链接请查看电脑端奖品中心兑换记录，谢谢!";
    }

    @Override
    protected String getSystemMessage(RewardCouponDetail couponDetail) {
        return "您已成功兑换，请到电脑端端下载课件资源，具体下载链接请查看电脑端奖品中心兑换记录，谢谢!";
    }

}
