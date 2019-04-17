package com.voxlearning.utopia.agent.view.activity;

import lombok.Data;

@Data
public class ActivityCouponStatisticsView {
    private Long id;
    private Integer idType;
    private String name;

    private Integer dayCouponCount;                  // 指定日期领取优惠券数量
    private Integer totalCouponCount;                // 累计领取优惠券数量

}
