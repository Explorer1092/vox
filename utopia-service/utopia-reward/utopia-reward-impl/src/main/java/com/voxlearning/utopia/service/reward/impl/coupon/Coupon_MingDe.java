/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.reward.impl.coupon;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.api.constant.CouponProductionName;
import com.voxlearning.utopia.service.reward.constant.IdentificationCouponName;
import com.voxlearning.utopia.service.reward.entity.RewardCouponDetail;
import com.voxlearning.utopia.service.reward.impl.service.AsyncRewardShortUrlServiceImpl;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by XiaoPeng.Yang on 14-8-8.
 */
@Named
@Slf4j
@NoArgsConstructor
@IdentificationCouponName(CouponProductionName.明德夏令营报名券)
public class Coupon_MingDe extends CouponTemplate {

    @Inject private AsyncRewardShortUrlServiceImpl asyncRewardShortUrlService;

    @Override
    protected int getRebateAmount() {
        return 1000;
    }

    @Override
    protected String getIntegralComment() {
        return "报名明德夏令营";
    }

    @Override
    protected String getSmsMessage(RewardCouponDetail couponDetail) {
        //fixme 此处接口 传入参数不能有test 只能用线上地址测试了 坑爹啊
        String mingdeUrl = "http://www.17zuoye.com/project/mingde/index.vpage" + "?userId=" + couponDetail.getUserId();
        String tinyUrl = asyncRewardShortUrlService.dwzTinyUrl(mingdeUrl).getUninterruptibly();
        if (StringUtils.isBlank(tinyUrl)) {
            log.error("明德夏令营报名券发短信生成短链接为空，用户ID为{}", couponDetail.getUserId());
            tinyUrl = mingdeUrl;
        }
        return "您的孩子想通过一起作业网立减400报名北京明德夏令营，报名编号：" + couponDetail.getCouponNo() + "，详情查看：" + tinyUrl;
    }

    @Override
    protected String getSystemMessage(RewardCouponDetail couponDetail) {
        return "你已经报名“明德夏令营”，报名编号：" + couponDetail.getCouponNo() +
                "，请及时和爸爸妈妈分享消息，参加体验！";
    }
}
