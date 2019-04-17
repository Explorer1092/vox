package com.voxlearning.utopia.agent.view.activity.palace;

import lombok.Data;

@Data
public class PalaceDataView {

    private Long id;
    private Integer idType;
    private String name;

    private Integer dayCouponCount;                  // 指定日期领取优惠券数量
    private Integer totalCouponCount;                // 累计领取优惠券数量
    private Integer totalOrderCount;                 // 累计订单数量
    private Integer totalAttendClassStuCount;        // 累计上课的学生数量
    private Integer totalMeetConditionStuCount;      // 上课学生中满足指定条件的学生数据（学习>=3天）

    private Double dayAvgCouponCount;
    private Double totalAvgCouponCount;
    private Double totalAvgOrderCount;
    private Double totalAvgAttendClassStuCount;
    private Double totalAvgMeetConditionStuCount;

}
