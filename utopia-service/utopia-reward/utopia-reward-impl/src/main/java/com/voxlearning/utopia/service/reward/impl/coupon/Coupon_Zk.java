package com.voxlearning.utopia.service.reward.impl.coupon;

import com.voxlearning.utopia.api.constant.CouponProductionName;
import com.voxlearning.utopia.service.reward.constant.IdentificationCouponName;
import com.voxlearning.utopia.service.reward.entity.RewardCouponDetail;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;

/**
 * Created by XiaoPeng.Yang on 14-9-17.
 */
@Named
@Slf4j
@NoArgsConstructor
@IdentificationCouponName(CouponProductionName.智康1对1免费试听得1000学豆)
public class Coupon_Zk extends CouponTemplate {
    @Override
    protected boolean sendSmsFlag() {
        return true;
    }

    @Override
    protected int getRebateAmount() {
        return 1000;
    }

    @Override
    protected String getIntegralComment() {
        return "预约“智康1对1免费试听课”";
    }

    @Override
    protected String getSmsMessage(RewardCouponDetail couponDetail) {
        return "您的孩子想预约体验“智康1对1免费试听课”需要您的同意，请及时和孩子沟通哦！";
    }

    @Override
    protected String getSystemMessage(RewardCouponDetail couponDetail) {
        return "你已经预约“智康1对1免费试听课”，请及时和爸爸妈妈分享消息！体验课程后，你还可以获得1000学豆奖励！体验编号：" + couponDetail.getCouponNo();
    }
}