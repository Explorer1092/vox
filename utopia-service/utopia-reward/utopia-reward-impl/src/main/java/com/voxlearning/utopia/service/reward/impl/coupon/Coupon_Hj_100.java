package com.voxlearning.utopia.service.reward.impl.coupon;

import com.voxlearning.utopia.api.constant.CouponProductionName;
import com.voxlearning.utopia.service.reward.constant.IdentificationCouponName;
import com.voxlearning.utopia.service.reward.entity.RewardCouponDetail;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;

/**
 * Created by XiaoPeng.Yang on 14-7-30.
 */
@Named
@Slf4j
@NoArgsConstructor
@IdentificationCouponName(CouponProductionName.沪江100元优惠券)
public class Coupon_Hj_100 extends CouponTemplate {

    @Override
    protected String getIntegralComment() {
        return "兑换沪江优惠券";
    }

    @Override
    protected String getSmsMessage(RewardCouponDetail couponDetail) {
        return "您的孩子兑换了100元沪江课程抵用券" + couponDetail.getCouponNo() + "。请及时登录url.cn/01bK2z 注册课程";
    }

    @Override
    protected String getSystemMessage(RewardCouponDetail couponDetail) {
        return "同学你好，你兑换的沪江优惠券号码是:" + couponDetail.getCouponNo() +
                "，请及时登录<a href='http://class.hujiang.com/hjcard' target='_blank'>沪江网校</a> 注册课程！";
    }
}
