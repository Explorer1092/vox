package com.voxlearning.utopia.service.reward.impl.coupon;

import com.voxlearning.utopia.api.constant.CouponProductionName;
import com.voxlearning.utopia.service.reward.constant.IdentificationCouponName;
import com.voxlearning.utopia.service.reward.entity.RewardCouponDetail;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;

/**
 * Created by XiaoPeng.Yang on 14-8-5.
 */
@Named
@Slf4j
@NoArgsConstructor
@IdentificationCouponName(CouponProductionName.贝乐学科英语报名券)
public class Coupon_Bl extends CouponTemplate {
    @Override
    protected boolean sendSmsFlag(){
        return true;
    }

    @Override
    protected int getRebateAmount() {
        return 1000;
    }

    @Override
    protected String getIntegralComment() {
        return "兑换贝乐学科英语报名券";
    }

    @Override
    protected String getSmsMessage(RewardCouponDetail couponDetail) {
        return "您的孩子想报名体验“贝乐学科英语免费外教课程”，领取了480元体验券：" + couponDetail.getCouponNo() + "，请及时使用";
    }

    @Override
    protected String getSystemMessage(RewardCouponDetail couponDetail) {
        return "你已经报名“贝乐学科英语免费体验课程”，报名编号：" + couponDetail.getCouponNo() +
                "，请及时和爸爸妈妈分享消息，参加体验！";
    }
}
