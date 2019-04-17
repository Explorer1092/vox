package com.voxlearning.utopia.service.reward.impl.coupon;

import com.voxlearning.utopia.api.constant.CouponProductionName;
import com.voxlearning.utopia.service.reward.constant.IdentificationCouponName;
import com.voxlearning.utopia.service.reward.entity.RewardCouponDetail;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;

/**
 * Created by XiaoPeng.Yang on 14-9-15.
 */
@Named
@Slf4j
@NoArgsConstructor
@IdentificationCouponName(CouponProductionName.新东方高端少儿英语课免费体验得1000学豆)
public class Coupon_Maxen extends CouponTemplate {
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
        return " 预约“迈格森新东方高端少儿英语体验课”";
    }

    @Override
    protected String getSmsMessage(RewardCouponDetail couponDetail) {
        return "您的孩子想预约体验“迈格森新东方高端少儿英语体验课”需要您的同意，请及时和孩子沟通哦！";
    }

    @Override
    protected String getSystemMessage(RewardCouponDetail couponDetail) {
        return "你已经预约“迈格森新东方高端少儿英语体验课”，请及时和爸爸妈妈分享消息！体验后报名，你还可以获得1000学豆奖励！预约编号：" + couponDetail.getCouponNo();
    }
}