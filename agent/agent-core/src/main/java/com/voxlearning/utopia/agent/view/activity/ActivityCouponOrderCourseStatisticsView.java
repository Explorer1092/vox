package com.voxlearning.utopia.agent.view.activity;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.agent.utils.MathUtils;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class ActivityCouponOrderCourseStatisticsView {

    private Long id;
    private Integer idType;
    private String name;

    private Integer dayCouponCount;                  // 指定日期领取优惠券数量
    private Integer totalCouponCount;                // 累计领取优惠券数量

    private Integer dayOrderCount;                  // 指定日期订单数量
    private Integer totalOrderCount;                // 累计订单数量

    private Integer dayOrderUserCount;                  // 指定日期首次在该活动下单的用户数
    private Integer totalOrderUserCount;                // 累计下单的用户数

    private Integer dayFirstAttendStuCount;       // 指定日期首次参加该活动课的学生数
    private Integer totalAttendStuCount;          // 累计参加该活动课的学生数

    private Integer dayMeetConditionStuCount;     // 指定日期上课并且满足市场指定条件的学生数
    private Integer totalMeetConditionStuCount;   // 累计上课并且满足市场指定条件的学生数

    public Map<String, Object> convertToAverageMap(double devided, int newScale){
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("id", this.id);
        dataMap.put("idType", this.idType);
        dataMap.put("name", this.name);
        dataMap.put("dayCouponCount", MathUtils.doubleDivide(SafeConverter.toDouble(this.dayCouponCount), devided, newScale));
        dataMap.put("totalCouponCount", MathUtils.doubleDivide(SafeConverter.toDouble(this.totalCouponCount), devided, newScale));
        dataMap.put("dayOrderCount", MathUtils.doubleDivide(SafeConverter.toDouble(this.dayOrderCount), devided, newScale));
        dataMap.put("totalOrderCount", MathUtils.doubleDivide(SafeConverter.toDouble(this.totalOrderCount), devided, newScale));

        dataMap.put("dayOrderUserCount", MathUtils.doubleDivide(SafeConverter.toDouble(this.dayOrderUserCount), devided, newScale));
        dataMap.put("totalOrderUserCount", MathUtils.doubleDivide(SafeConverter.toDouble(this.totalOrderUserCount), devided, newScale));

        dataMap.put("dayFirstAttendStuCount", MathUtils.doubleDivide(SafeConverter.toDouble(this.dayFirstAttendStuCount), devided, newScale));
        dataMap.put("totalAttendStuCount", MathUtils.doubleDivide(SafeConverter.toDouble(this.totalAttendStuCount), devided, newScale));
        dataMap.put("dayMeetConditionStuCount", MathUtils.doubleDivide(SafeConverter.toDouble(this.dayMeetConditionStuCount), devided, newScale));
        dataMap.put("totalMeetConditionStuCount", MathUtils.doubleDivide(SafeConverter.toDouble(this.totalMeetConditionStuCount), devided, newScale));
        return dataMap;
    }
}
