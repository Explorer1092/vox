package com.voxlearning.utopia.service.reward.impl.coupon;

import com.voxlearning.utopia.api.constant.CouponProductionName;
import com.voxlearning.utopia.service.reward.constant.IdentificationCouponName;
import com.voxlearning.utopia.service.reward.entity.RewardCouponDetail;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;

/**
 * Created by XiaoPeng.Yang on 14-9-10.
 */
@Named
@Slf4j
@NoArgsConstructor
@IdentificationCouponName(CouponProductionName.迪士尼英语免费试听课程)
public class Coupon_Disney extends CouponTemplate {
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
        return "兑换“迪士尼英语免费试听课程”";
    }

    @Override
    protected String getSmsMessage(RewardCouponDetail couponDetail) {
        return "您的孩子想报名体验“迪士尼英语免费试听课程”，需要您的同意，请及时和孩子沟通哦！体验号" + couponDetail.getCouponNo();
    }

    @Override
    protected String getSystemMessage(RewardCouponDetail couponDetail) {
        return "你已经报名体验“迪士尼英语免费试听课程”，请及时和爸爸妈妈分享消息！家长同意体验，你可获得1000学豆奖励！体验号" + couponDetail.getCouponNo();
    }
}