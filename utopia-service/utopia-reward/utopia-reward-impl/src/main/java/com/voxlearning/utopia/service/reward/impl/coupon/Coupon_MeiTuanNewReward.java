package com.voxlearning.utopia.service.reward.impl.coupon;

import com.voxlearning.utopia.api.constant.CouponProductionName;
import com.voxlearning.utopia.service.reward.constant.IdentificationCouponName;
import com.voxlearning.utopia.service.reward.entity.RewardCouponDetail;

import javax.inject.Named;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 美团外卖新用户优惠红包
 * Created by haitian.gan on 2017/7/3.
 */
@Named
@Slf4j
@NoArgsConstructor
@IdentificationCouponName(CouponProductionName.美团外卖新用户优惠红包)
public class Coupon_MeiTuanNewReward extends CouponTemplate{

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
        return "兑换美团外卖新用户优惠红包";
    }

    @Override
    protected String getSmsMessage(RewardCouponDetail couponDetail) {
        return "您已成功兑换，请到美团外卖APP兑换相应红包，复制粘贴兑换码领取红包，新客最高可领17元（仅限初次注册美团外卖用户），兑换码为:"+ couponDetail.getCouponNo() +"，谢谢!";
    }

    @Override
    protected String getSystemMessage(RewardCouponDetail couponDetail) {
        return "您已成功兑换，请到美团外卖APP兑换相应红包，复制粘贴兑换码领取红包，新客最高可领17元（仅限初次注册美团外卖用户），兑换码为:" + couponDetail.getCouponNo() +"，谢谢!";
    }

}
