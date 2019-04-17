package com.voxlearning.utopia.service.reward.impl.coupon;

import com.voxlearning.utopia.api.constant.CouponProductionName;
import com.voxlearning.utopia.service.reward.constant.IdentificationCouponName;
import com.voxlearning.utopia.service.reward.entity.RewardCouponDetail;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;

/**
 * Created by XiaoPeng.Yang on 14-9-16.
 */
@Named
@Slf4j
@NoArgsConstructor
@IdentificationCouponName(CouponProductionName.Starbooks英文原版绘本教学体验课)
public class Coupon_Starbooks extends CouponTemplate {
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
        return "申请“Starbooks免费原版英文书和外教体验课”";
    }

    @Override
    protected String getSmsMessage(RewardCouponDetail couponDetail) {
        return "您的孩子申请“Starbooks免费原版英文书和外教体验课”需要您的同意，请及时和孩子沟通哦！";
    }

    @Override
    protected String getSystemMessage(RewardCouponDetail couponDetail) {
        return "你已经申请“Starbooks免费原版英文绘本和外教体验课”，请及时和爸爸妈妈分享消息！家长同意体验，你还可以获得1000学豆奖励！体验号：" + couponDetail.getCouponNo();
    }
}