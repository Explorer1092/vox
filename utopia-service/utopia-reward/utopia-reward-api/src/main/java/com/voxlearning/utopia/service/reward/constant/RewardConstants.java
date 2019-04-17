package com.voxlearning.utopia.service.reward.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Created by XiaoPeng.Yang on 14-7-15.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
abstract public class RewardConstants {
    public static final Double DISCOUNT_VIP = 0.9;                                                // vip 打折系数
    public static final Double DISCOUNT_ALL_SITE = 1.00;                                           // 全站打折系数
    public static final String TEACHER_DAY_LOTTERY_PERIOD_NO = "2014104";                          // 教师节当天老师抽奖开奖期号 2014104

    public static final String DEFAULT_PRODUCT_IMAGE_URL = "default.jpg";                                     // 列表页没有图片 默认显示图片URL

    public static final String STUSENT_REWARD_NAME = "学习用品中心";

    public static final String TEACHER_REWARD_NAME = "教学用品中心";
}
